// Live family mode: the server side. Firestore holds the family; these functions join a
// parent with a code and push changes to phones, so they arrive even with the app closed.
// All pushes are data messages; each phone writes the notification in its own language.

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentWritten, onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { codeProblem, tokensOf, deadTokens } = require("./logic");

initializeApp();
const db = getFirestore();

/** A parent enters the child's 6-digit code; this adds them to the child's family. */
exports.joinFamily = onCall(async (request) => {
  const uid = request.auth && request.auth.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in first.");
  const code = String((request.data && request.data.code) || "").replace(/\D/g, "");
  if (code.length !== 6) throw new HttpsError("invalid-argument", "A code has 6 digits.");

  return db.runTransaction(async (tx) => {
    const codeRef = db.doc(`codes/${code}`);
    const codeSnap = await tx.get(codeRef);
    const data = codeSnap.exists ? codeSnap.data() : null;
    const problem = codeProblem(data, Date.now(), uid);
    if (problem) throw new HttpsError(problem, "That code doesn't work.");

    const familyRef = db.doc(`families/${data.familyId}`);
    const family = await tx.get(familyRef);
    if (!family.exists) throw new HttpsError("not-found", "That family no longer exists.");

    tx.update(familyRef, { parents: FieldValue.arrayUnion(uid) });
    tx.set(db.doc(`users/${uid}`), { familyId: data.familyId, role: "parent" }, { merge: true });
    tx.delete(codeRef);
    return { familyId: data.familyId, childName: family.get("childName") || "" };
  });
});

async function send(uids, data) {
  if (uids.length === 0) return;
  const refs = uids.map((u) => db.doc(`users/${u}`));
  const docs = await db.getAll(...refs);
  const tokens = tokensOf(docs.map((d) => (d.exists ? d.data() : null)));
  if (tokens.length === 0) return;
  const result = await getMessaging().sendEachForMulticast({
    tokens,
    data,
    android: { priority: "high", ttl: 10 * 60 * 1000 },
  });
  const dead = deadTokens(tokens, result.responses);
  if (dead.length > 0) {
    await Promise.all(docs.filter((d) => d.exists).map((d) => d.ref.update({ tokens: FieldValue.arrayRemove(...dead) })));
  }
}

async function familyOf(fid) {
  const snap = await db.doc(`families/${fid}`).get();
  return snap.exists ? snap.data() : null;
}

/** A parent added, changed or removed an alarm: the child's phone syncs its alarms. */
exports.onAlarmChange = onDocumentWritten("families/{fid}/alarms/{aid}", async (event) => {
  const family = await familyOf(event.params.fid);
  if (!family) return;
  const after = event.data.after.exists ? event.data.after.data() : null;
  // The child's own change (switching one off) doesn't need to be sent back to them.
  if (after && after.updatedBy === family.child) return;
  await send([family.child], { type: "alarms", familyId: event.params.fid });
});

/** A parent pressed "WAKE UP!": ring the child's phone now. */
exports.onPoke = onDocumentCreated("families/{fid}/pokes/{pid}", async (event) => {
  const family = await familyOf(event.params.fid);
  if (!family) return;
  await send([family.child], { type: "poke", familyId: event.params.fid });
});

/** The child's alarm rang, was snoozed or answered: tell the parents. */
exports.onStatus = onDocumentWritten("families/{fid}/status/{doc}", async (event) => {
  if (!event.data.after.exists) return;
  const family = await familyOf(event.params.fid);
  if (!family) return;
  const s = event.data.after.data();
  await send(family.parents || [], {
    type: "status",
    event: String(s.event || ""),
    level: String(s.level || 0),
    childName: String(family.childName || ""),
  });
});
