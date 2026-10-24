package com.likehome.app.core

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Clock text that follows the phone's 12/24-hour choice. */
object ClockFormat {
    private val h12 = DateTimeFormatter.ofPattern("h:mm")
    private val h24 = DateTimeFormatter.ofPattern("HH:mm")

    /** The digits, e.g. "7:05" or "07:05". */
    fun digits(time: LocalTime, is24Hour: Boolean): String = time.format(if (is24Hour) h24 else h12)

    /** "am"/"pm" for a 12-hour clock, null for 24-hour. */
    fun period(time: LocalTime, is24Hour: Boolean): String? =
        if (is24Hour) null else if (time.hour < 12) "am" else "pm"

    fun full(time: LocalTime, is24Hour: Boolean): String =
        period(time, is24Hour)?.let { "${digits(time, false)} $it" } ?: digits(time, true)

    /** "in 7 h 20 min", rounded up so an alarm a few seconds away never reads "in 0 min". */
    fun until(now: ZonedDateTime, target: ZonedDateTime): String {
        val seconds = Duration.between(now, target).seconds.coerceAtLeast(0)
        val minutes = (seconds + 59) / 60
        val h = minutes / 60
        val m = minutes % 60
        return when {
            minutes == 0L -> "now"
            h == 0L -> "in $m min"
            m == 0L -> "in $h h"
            else -> "in $h h $m min"
        }
    }

    /** "Today", "Tomorrow" or a weekday for the day an alarm rings. */
    fun dayWord(today: LocalDate, day: LocalDate): String = when (day) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> day.dayOfWeek.name.lowercase().replaceFirstChar(Char::uppercase)
    }
}
