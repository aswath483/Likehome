package com.likehome.app.data

import com.likehome.app.core.Character
import com.likehome.app.core.RingStyle
import com.likehome.app.core.WakeTask
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * One alarm set by the user.
 *
 * @param days repeat days; empty means a one-time alarm.
 * @param task what the user must do to stop it.
 * @param style how the ringing screen looks.
 * @param character who wakes the user.
 * @param photoRef file name of the reference photo for the photo check, or blank.
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
    val character: Character = Character.MOM,
    val photoRef: String = "",
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

    companion object {
        val WEEKDAYS = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
}
