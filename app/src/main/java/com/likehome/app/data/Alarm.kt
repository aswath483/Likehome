package com.likehome.app.data

import com.likehome.app.core.Character
import com.likehome.app.core.NextRing
import com.likehome.app.core.RingStyle
import com.likehome.app.core.Snooze
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
 * @param barcode the product code the barcode check asks for, or blank.
 * @param sound the ringtone's URI; blank for the phone's alarm sound, [SILENT] for voice only.
 * @param calendarLead minutes to ring before the day's first calendar event when that is
 *   earlier than the usual time; 0 is off (see [com.likehome.app.core.CalendarWake]).
 * @param familyId the cloud id when a parent set this alarm in live family mode, else blank.
 * @param snoozeLimit how many snoozes this alarm allows; [Snooze.NO_LIMIT] for Mom's usual three.
 * @param skipHolidays a repeating alarm stays quiet on the country's public holidays.
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
    val barcode: String = "",
    val sound: String = "",
    val calendarLead: Int = 0,
    val familyId: String = "",
    val skipHolidays: Boolean = false,
    val snoozeLimit: Int = Snooze.NO_LIMIT,
) {
    val fromFamily: Boolean get() = familyId.isNotEmpty()

    val time: LocalTime get() = LocalTime.of(hour, minute)

    val isRepeating: Boolean get() = days.isNotEmpty()

    /**
     * The next moment this alarm should ring, strictly after [now]. [skip] says which days a
     * repeating alarm sits out (public holidays, vacation); see [com.likehome.app.core.Skips].
     */
    fun nextTrigger(
        now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
        skip: (java.time.LocalDate) -> Boolean = { false },
    ): ZonedDateTime = NextRing.after(now, time, days, skip)

    companion object {
        /** [sound] for no ringtone: only vibration and Mom's voice. */
        const val SILENT = "silent"

        val WEEKDAYS = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
}
