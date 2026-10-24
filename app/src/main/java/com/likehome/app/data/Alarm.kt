package com.likehome.app.data

import com.likehome.app.core.RingStyle
import com.likehome.app.core.WakeTask
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * One alarm set by the user.
 *
 * @param days repeat days; empty means a one-time alarm.
 * @param task what the user must do to stop it.
 * @param style how Mom appears when it rings.
 */
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val days: Set<DayOfWeek> = emptySet(),
    val enabled: Boolean = true,
    val task: WakeTask = WakeTask.MATH,
    val style: RingStyle = RingStyle.CHAT,
) {
    val time: LocalTime get() = LocalTime.of(hour, minute)

    val isRepeating: Boolean get() = days.isNotEmpty()

    /** The next moment this alarm should ring, strictly after [now]. */
    fun nextTrigger(now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): ZonedDateTime {
        var candidate = now.with(time).withSecond(0).withNano(0)
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
        if (days.isEmpty()) return candidate
        repeat(7) {
            if (candidate.dayOfWeek in days) return candidate
            candidate = candidate.plusDays(1)
        }
        return candidate
    }

    fun repeatSummary(): String = when {
        days.isEmpty() -> "Once"
        days.size == 7 -> "Every day"
        days == WEEKDAYS -> "Weekdays"
        days == WEEKEND -> "Weekends"
        else -> DayOfWeek.entries.filter { it in days }
            .joinToString(" ") { it.name.take(2).lowercase().replaceFirstChar(Char::uppercase) }
    }

    companion object {
        val WEEKDAYS = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
}

/** Human-friendly "in 7 h 20 min" text. */
fun untilText(target: ZonedDateTime, now: LocalDateTime = LocalDateTime.now()): String {
    val minutes = java.time.Duration.between(now, target.toLocalDateTime()).toMinutes().coerceAtLeast(0)
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0L -> "in $m min"
        m == 0L -> "in $h h"
        else -> "in $h h $m min"
    }
}
