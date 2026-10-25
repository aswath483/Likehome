package com.likehome.app.family

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.likehome.app.core.Challenge
import com.likehome.app.core.ChallengeMorning
import com.likehome.app.core.Morning
import com.likehome.app.core.PairCode
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
import java.time.LocalDate

/**
 * Streak challenge with friends, on the same Firebase project as live family mode (and
 * hidden with it when the app has no google-services.json). One challenge per phone.
 *
 * Cloud layout (see firestore.rules and functions/index.js): `challenges/{id}` holds the
 * members, their names, the start date and the length; `mornings/{uid}_{date}` under it.
 */
object ChallengeLive {

    data class State(
        val challengeId: String = "",
        val start: LocalDate? = null,
        val length: Int = 7,
        val myName: String = "",
        /** The invite code on screen, until it expires. */
        val code: String = "",
        val codeExpiresAt: Long = 0L,
    ) {
        val active: Boolean get() = challengeId.isNotEmpty() && start != null
    }

    /** The challenge as everyone sees it, live. */
    data class Board(val names: Map<String, String>, val mornings: List<ChallengeMorning>)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
    @Volatile private var loaded = false

    private val db get() = FirebaseFirestore.getInstance()

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("challenge", Context.MODE_PRIVATE)

    fun load(context: Context) {
        if (loaded) return
        val p = prefs(context)
        _state.value = State(
            challengeId = p.getString("id", null).orEmpty(),
            start = p.getString("start", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            length = p.getInt("length", 7),
            myName = p.getString("name", null).orEmpty(),
            code = p.getString("code", null).orEmpty(),
            codeExpiresAt = p.getLong("codeExpiresAt", 0L),
        )
        loaded = true
    }

    private fun save(context: Context, s: State) {
        prefs(context).edit()
            .putString("id", s.challengeId)
            .putString("start", s.start?.toString().orEmpty())
            .putInt("length", s.length)
            .putString("name", s.myName)
            .putString("code", s.code)
            .putLong("codeExpiresAt", s.codeExpiresAt)
            .apply()
        _state.value = s
    }

    private suspend fun uid(): String {
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user?.uid ?: error("sign-in failed")
    }

    /** The signed-in player, so the screen can mark "you"; null before the first sign-in. */
    fun me(): String? = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()

    /** Starts a challenge from today and makes an invite code. */
    suspend fun start(context: Context, name: String, length: Int): String {
        load(context)
        val me = uid()
        FamilyLive.registerToken()
        val clean = Challenge.cleanName(name)
        val today = LocalDate.now()
        val ref = db.collection("challenges").document()
        ref.set(
            mapOf(
                "members" to listOf(me),
                "names" to mapOf(me to clean),
                "start" to today.toString(),
                "length" to length,
                "createdAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
        save(context, State(ref.id, today, length, clean))
        return invite(context)
    }

    /** A fresh invite code for the current challenge, valid for 10 minutes. */
    suspend fun invite(context: Context): String {
        load(context)
        val s = _state.value
        val code = PairCode.generate()
        val expires = System.currentTimeMillis() + PairCode.VALID_MS
        db.document("challengeCodes/$code").set(mapOf("challengeId" to s.challengeId, "createdBy" to uid(), "expiresAt" to expires)).await()
        save(context, s.copy(code = code, codeExpiresAt = expires))
        return code
    }

    /** Joins a friend's challenge with their code. */
    suspend fun join(context: Context, code: String, name: String) {
        load(context)
        uid()
        FamilyLive.registerToken()
        val clean = Challenge.cleanName(name)
        val result = FirebaseFunctions.getInstance().getHttpsCallable("joinChallenge")
            .call(mapOf("code" to code, "name" to clean)).await()
        val data = result.getData() as? Map<*, *> ?: error("no challenge")
        val id = data["challengeId"] as? String ?: error("no challenge")
        val start = (data["start"] as? String)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: error("no challenge")
        val length = (data["length"] as? Number)?.toInt() ?: 7
        save(context, State(id, start, length, clean))
    }

    /** This phone got up: post the morning so friends see it (and get a push). */
    fun report(context: Context, morning: Morning) {
        if (!FamilyLive.available(context)) return
        load(context)
        val s = _state.value
        val start = s.start ?: return
        if (!s.active || !Challenge.counts(morning.date, start, s.length)) return
        scope.launch {
            runCatching {
                val me = uid()
                val ref = db.document("challenges/${s.challengeId}/mornings/${Challenge.morningId(me, morning.date)}")
                // Several alarms on one day: the worst one counts, as in the history.
                val earlier = ref.get().await().getLong("snoozes")?.toInt()
                if (earlier != null && earlier >= morning.snoozes) return@launch
                ref.set(
                    mapOf("uid" to me, "date" to morning.date.toString(), "snoozes" to morning.snoozes, "at" to FieldValue.serverTimestamp()),
                ).await()
            }
        }
    }

    /** Everyone's names and mornings, live. */
    fun board(challengeId: String): Flow<Board> = callbackFlow {
        var names = emptyMap<String, String>()
        var mornings = emptyList<ChallengeMorning>()
        val top = db.document("challenges/$challengeId").addSnapshotListener { snap, _ ->
            val members = (snap?.get("members") as? List<*>).orEmpty().filterIsInstance<String>()
            val all = (snap?.get("names") as? Map<*, *>).orEmpty()
            names = members.associateWith { (all[it] as? String).orEmpty() }
            trySend(Board(names, mornings))
        }
        val days = db.collection("challenges/$challengeId/mornings").addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            mornings = snap.documents.mapNotNull { d ->
                val uid = d.getString("uid") ?: return@mapNotNull null
                val date = d.getString("date")?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@mapNotNull null
                ChallengeMorning(uid, date, (d.getLong("snoozes") ?: 0L).toInt())
            }
            trySend(Board(names, mornings))
        }
        awaitClose {
            top.remove()
            days.remove()
        }
    }

    /** Leaves the challenge; the phone forgets it even when offline. */
    suspend fun leave(context: Context) {
        load(context)
        val s = _state.value
        runCatching {
            val me = uid()
            db.document("challenges/${s.challengeId}")
                .update(mapOf<String, Any>("members" to FieldValue.arrayRemove(me), "names.$me" to FieldValue.delete())).await()
        }
        save(context, State(myName = s.myName))
    }
}
