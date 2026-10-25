package com.likehome.app.family

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.FamilyAlarm
import com.likehome.app.core.FamilyEvent
import com.likehome.app.core.PairCode
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek

/**
 * Live family mode, on Firebase: a parent sets the child's alarms from their own phone,
 * sees whether they woke up, and can ring them with "WAKE UP!". The child links a parent
 * by showing a 6-digit code; either side can unlink. Everything here is off (and the
 * screens hidden) when the app is built without a google-services.json.
 *
 * Cloud layout (see firestore.rules and functions/index.js): `families/{id}` holds the
 * child, the parents and the child's name; under it `alarms`, `status/now` and `pokes`.
 */
object FamilyLive {

    enum class Role { NONE, CHILD, PARENT }

    /** This phone's place in a family, kept on the phone so screens show it instantly. */
    data class State(
        val role: Role = Role.NONE,
        val familyId: String = "",
        val childName: String = "",
        /** The pairing code on screen (child), until it's used or expires. */
        val code: String = "",
        val codeExpiresAt: Long = 0L,
    )

    /** The child's latest alarm event, for the parent's screen. */
    data class Status(val event: FamilyEvent, val level: Int, val at: Long)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
    @Volatile private var loaded = false

    fun available(context: Context): Boolean =
        runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("family", Context.MODE_PRIVATE)

    fun load(context: Context) {
        if (loaded) return
        val p = prefs(context)
        _state.value = State(
            role = Role.entries.firstOrNull { it.name == p.getString("role", null) } ?: Role.NONE,
            familyId = p.getString("familyId", null).orEmpty(),
            childName = p.getString("childName", null).orEmpty(),
            code = p.getString("code", null).orEmpty(),
            codeExpiresAt = p.getLong("codeExpiresAt", 0L),
        )
        loaded = true
    }

    private fun save(context: Context, state: State) {
        prefs(context).edit()
            .putString("role", state.role.name)
            .putString("familyId", state.familyId)
            .putString("childName", state.childName)
            .putString("code", state.code)
            .putLong("codeExpiresAt", state.codeExpiresAt)
            .apply()
        _state.value = state
    }

    private val db get() = FirebaseFirestore.getInstance()

    private suspend fun uid(): String {
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user?.uid ?: error("sign-in failed")
    }

    /** Saves this phone's push token so the family functions can reach it. */
    suspend fun registerToken(token: String? = null) {
        val me = uid()
        val t = token ?: FirebaseMessaging.getInstance().token.await()
        db.document("users/$me").set(mapOf("tokens" to FieldValue.arrayUnion(t)), SetOptions.merge()).await()
    }

    fun onNewToken(context: Context, token: String) {
        load(context)
        if (!available(context) || _state.value.role == Role.NONE) return
        scope.launch { runCatching { registerToken(token) } }
    }

    // --- The child: the person woken up ---------------------------------------------------

    /** Creates (or reuses) the child's family and a fresh pairing code to show the parent. */
    suspend fun startPairing(context: Context, childName: String): String {
        load(context)
        val me = uid()
        registerToken()
        var familyId = _state.value.familyId.takeIf { _state.value.role == Role.CHILD }
        if (familyId.isNullOrEmpty()) {
            val ref = db.collection("families").document()
            ref.set(
                mapOf("child" to me, "parents" to emptyList<String>(), "childName" to childName, "createdAt" to FieldValue.serverTimestamp()),
            ).await()
            familyId = ref.id
        } else {
            db.document("families/$familyId").update(mapOf<String, Any>("childName" to childName)).await()
        }
        db.document("users/$me").set(mapOf("familyId" to familyId, "role" to "child"), SetOptions.merge()).await()
        val code = PairCode.generate()
        val expires = System.currentTimeMillis() + PairCode.VALID_MS
        db.document("codes/$code").set(mapOf("familyId" to familyId, "createdBy" to me, "expiresAt" to expires)).await()
        save(context, State(Role.CHILD, familyId, childName, code, expires))
        return code
    }

    /** The parents' alarms, applied to this phone: added, changed, or removed. */
    suspend fun syncAlarms(context: Context) {
        load(context)
        val s = _state.value
        if (s.role != Role.CHILD || s.familyId.isEmpty()) return
        val remote = db.collection("families/${s.familyId}/alarms").get().await().documents
            .mapNotNull { doc -> doc.data?.let { FamilyAlarm.fromMap(doc.id, it) } }
        apply(context, remote)
    }

    private fun apply(context: Context, remote: List<FamilyAlarm>) {
        val repo = AlarmRepository.get(context)
        val local = repo.alarms.value.filter { it.fromFamily }.associateBy { it.familyId }
        remote.forEach { r ->
            val existing = local[r.remoteId]
            val alarm = (existing ?: Alarm(id = AlarmRepository.NEW_ID, hour = r.hour, minute = r.minute)).copy(
                hour = r.hour,
                minute = r.minute,
                days = r.days.map { DayOfWeek.of(it) }.toSet(),
                label = r.label,
                enabled = r.enabled,
                task = r.task,
                style = r.style,
                character = r.character,
                familyId = r.remoteId,
            )
            if (alarm != existing) AlarmScheduler.schedule(context, repo.upsert(alarm))
        }
        val keep = remote.map { it.remoteId }.toSet()
        local.values.filter { it.familyId !in keep }.forEach {
            AlarmScheduler.cancel(context, it.id)
            repo.delete(it.id)
        }
    }

    /** The child changed or removed a parent's alarm on their phone: tell the family. */
    fun childChanged(context: Context, alarm: Alarm, deleted: Boolean = false) {
        load(context)
        val s = _state.value
        if (!alarm.fromFamily || s.role != Role.CHILD || !available(context)) return
        scope.launch {
            runCatching {
                val ref = db.document("families/${s.familyId}/alarms/${alarm.familyId}")
                if (deleted) ref.delete().await()
                else ref.set(alarm.toFamily().toMap() + mapOf("updatedBy" to uid(), "updatedAt" to FieldValue.serverTimestamp())).await()
            }
        }
    }

    /** Ringing, snoozed, up, or slept through: the parents' phones hear about it. */
    fun report(context: Context, event: FamilyEvent, level: Int) {
        load(context)
        val s = _state.value
        if (s.role != Role.CHILD || s.familyId.isEmpty() || !available(context)) return
        scope.launch {
            runCatching {
                uid()
                db.document("families/${s.familyId}/status/now")
                    .set(mapOf("event" to event.name, "level" to level, "at" to System.currentTimeMillis())).await()
            }
        }
    }

    // --- The parent -----------------------------------------------------------------------

    /** Joins the child's family with their code. Returns the child's name. */
    suspend fun join(context: Context, code: String): String {
        load(context)
        uid()
        registerToken()
        val result = FirebaseFunctions.getInstance().getHttpsCallable("joinFamily").call(mapOf("code" to code)).await()
        val data = result.data as? Map<*, *> ?: error("no family")
        val familyId = data["familyId"] as? String ?: error("no family")
        val childName = data["childName"] as? String ?: ""
        save(context, State(Role.PARENT, familyId, childName))
        return childName
    }

    /** The child's alarms, live. */
    fun alarms(familyId: String): Flow<List<FamilyAlarm>> = callbackFlow {
        val registration = db.collection("families/$familyId/alarms").addSnapshotListener { snap, _ ->
            if (snap != null) {
                trySend(
                    snap.documents.mapNotNull { doc -> doc.data?.let { FamilyAlarm.fromMap(doc.id, it) } }
                        .sortedWith(compareBy({ it.hour }, { it.minute })),
                )
            }
        }
        awaitClose { registration.remove() }
    }

    /** The child's latest alarm event, live; null until their first ring after linking. */
    fun status(familyId: String): Flow<Status?> = callbackFlow {
        val registration = db.document("families/$familyId/status/now").addSnapshotListener { snap, _ ->
            val event = snap?.getString("event")?.let { name -> FamilyEvent.entries.firstOrNull { it.name == name } }
            trySend(event?.let { Status(it, (snap?.getLong("level") ?: 0L).toInt(), snap?.getLong("at") ?: 0L) })
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveAlarm(alarm: FamilyAlarm) {
        val s = _state.value
        val alarms = db.collection("families/${s.familyId}/alarms")
        val ref = if (alarm.remoteId.isEmpty()) alarms.document() else alarms.document(alarm.remoteId)
        ref.set(alarm.toMap() + mapOf("updatedBy" to uid(), "updatedAt" to FieldValue.serverTimestamp())).await()
    }

    suspend fun deleteAlarm(remoteId: String) {
        db.document("families/${_state.value.familyId}/alarms/$remoteId").delete().await()
    }

    /** "WAKE UP!": rings the child's phone at full drama. */
    suspend fun poke() {
        db.collection("families/${_state.value.familyId}/pokes")
            .add(mapOf("by" to uid(), "at" to FieldValue.serverTimestamp())).await()
    }

    // --- Both -----------------------------------------------------------------------------

    /**
     * Leaves the family. A child removes every parent and the alarms they set; a parent
     * just removes themselves. The phone forgets the family either way, even offline.
     */
    suspend fun unlink(context: Context) {
        load(context)
        val s = _state.value
        runCatching {
            val me = uid()
            when (s.role) {
                Role.CHILD -> db.document("families/${s.familyId}").update(mapOf<String, Any>("parents" to emptyList<String>())).await()
                Role.PARENT -> db.document("families/${s.familyId}").update(mapOf<String, Any>("parents" to FieldValue.arrayRemove(me))).await()
                Role.NONE -> Unit
            }
            db.document("users/$me").set(mapOf("familyId" to "", "role" to ""), SetOptions.merge()).await()
        }
        if (s.role == Role.CHILD) apply(context, emptyList())
        save(context, State())
    }
}

/** An app alarm as a family alarm, keeping its cloud id. */
fun Alarm.toFamily(): FamilyAlarm = FamilyAlarm(
    remoteId = familyId,
    hour = hour,
    minute = minute,
    days = days.map { it.value }.toSet(),
    label = label,
    enabled = enabled,
    task = task,
    style = style,
    character = character,
)

/** A family alarm as an app alarm, for showing and editing it on the parent's phone. */
fun FamilyAlarm.toAlarm(): Alarm = Alarm(
    id = 0,
    hour = hour,
    minute = minute,
    days = days.map { DayOfWeek.of(it) }.toSet(),
    label = label,
    enabled = enabled,
    task = task,
    style = style,
    character = character,
    familyId = remoteId,
)
