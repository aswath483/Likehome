// Pure helpers for the family functions, kept apart so they can be tested without Firebase.

const CODE_VALID_MS = 10 * 60 * 1000;

/** Why a pairing code can't be used, or null when it can. */
function codeProblem(code, now, callerUid) {
  if (!code) return "not-found";
  if (now > code.expiresAt) return "deadline-exceeded";
  if (code.createdBy === callerUid) return "failed-precondition"; // you can't parent yourself
  return null;
}

/** Every push token of the given user documents, without duplicates or blanks. */
function tokensOf(userDocs) {
  const all = userDocs.flatMap((u) => (u && Array.isArray(u.tokens) ? u.tokens : []));
  return [...new Set(all.filter((t) => typeof t === "string" && t.length > 0))];
}

/** Tokens FCM said are dead, so they can be removed from the user documents. */
function deadTokens(tokens, responses) {
  const dead = [];
  responses.forEach((r, i) => {
    const code = r && r.error && r.error.code;
    if (code === "messaging/registration-token-not-registered" || code === "messaging/invalid-registration-token") {
      dead.push(tokens[i]);
    }
  });
  return dead;
}

const MAX_PLAYERS = 5;

/** Why [uid] can't join this streak challenge, or null when they can (or already have). */
function challengeProblem(challenge, uid) {
  if (!challenge) return "not-found";
  const members = Array.isArray(challenge.members) ? challenge.members : [];
  if (members.includes(uid)) return null;
  if (members.length >= MAX_PLAYERS) return "resource-exhausted";
  return null;
}

/** A player's display name: trimmed, single spaces, at most 20 characters. */
function cleanName(name) {
  return String(name || "").trim().replace(/\s+/g, " ").slice(0, 20);
}

module.exports = { CODE_VALID_MS, MAX_PLAYERS, codeProblem, tokensOf, deadTokens, challengeProblem, cleanName };
