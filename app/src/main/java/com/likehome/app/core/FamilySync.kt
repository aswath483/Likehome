package com.likehome.app.core

import kotlin.random.Random

/** Live family mode's shared rules: pairing codes and the alarm shape stored in the cloud. */
object PairCode {
    const val LENGTH = 6
    /** A code works for 10 minutes; long enough to read it out over the phone. */
    const val VALID_MS = 10 * 60_000L

    fun generate(random: Random = Random.Default): String =
        (1..LENGTH).joinToString("") { random.nextInt(10).toString() }

    /** What the parent typed, as a code: digits only. Null unless it has exactly [LENGTH]. */
    fun normalise(input: String): String? = input.filter(Char::isDigit).takeIf { it.length == LENGTH }
}

/** What happened to the child's alarm, as the parent's phone shows it. */
enum class FamilyEvent { RINGING, SNOOZED, AWAKE, GAVE_UP }

/**
 * An alarm the parent set, as stored in the family's cloud document. Fields mirror the
 * app's own alarm; names of enums are stored so an older app can skip ones it doesn't know.
 */
data class FamilyAlarm(
    val remoteId: String,
    val hour: Int,
    val minute: Int,
    val days: Set<Int> = emptySet(),
    val label: String = "",
    val enabled: Boolean = true,
    val task: WakeTask = WakeTask.MATH,
    val style: RingStyle = RingStyle.CHAT,
    val character: Character = Character.MOM,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "hour" to hour,
        "minute" to minute,
        "days" to days.sorted(),
        "label" to label,
        "enabled" to enabled,
        "task" to task.name,
        "style" to style.name,
        "character" to character.name,
    )

    companion object {
        /** Null when the document is missing a time or has one out of range. */
        fun fromMap(remoteId: String, map: Map<String, Any?>): FamilyAlarm? {
            val hour = (map["hour"] as? Number)?.toInt() ?: return null
            val minute = (map["minute"] as? Number)?.toInt() ?: return null
            if (hour !in 0..23 || minute !in 0..59) return null
            return FamilyAlarm(
                remoteId = remoteId,
                hour = hour,
                minute = minute,
                days = (map["days"] as? List<*>).orEmpty().mapNotNull { (it as? Number)?.toInt() }.filter { it in 1..7 }.toSet(),
                label = (map["label"] as? String).orEmpty().take(40),
                enabled = map["enabled"] as? Boolean ?: true,
                task = WakeTask.entries.firstOrNull { it.name == map["task"] } ?: WakeTask.MATH,
                style = RingStyle.entries.firstOrNull { it.name == map["style"] } ?: RingStyle.CHAT,
                character = Character.entries.firstOrNull { it.name == map["character"] } ?: Character.MOM,
            )
        }
    }
}
