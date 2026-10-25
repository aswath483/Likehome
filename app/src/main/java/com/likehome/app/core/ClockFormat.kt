package com.likehome.app.core

import java.time.DayOfWeek
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

    /** Hours and minutes to [target], rounded up so an alarm seconds away never reads "0 min". */
    fun until(now: ZonedDateTime, target: ZonedDateTime): Span {
        val seconds = Duration.between(now, target).seconds.coerceAtLeast(0)
        val minutes = (seconds + 59) / 60
        return Span((minutes / 60).toInt(), (minutes % 60).toInt())
    }

    /** Whether [day] is today, tomorrow, or which weekday (for "Rings tomorrow", "Monday"). */
    fun dayOf(today: LocalDate, day: LocalDate): Day = when (day) {
        today -> Day.Today
        today.plusDays(1) -> Day.Tomorrow
        else -> Day.Weekday(day.dayOfWeek)
    }

    data class Span(val hours: Int, val minutes: Int) {
        val isNow: Boolean get() = hours == 0 && minutes == 0
    }

    sealed interface Day {
        data object Today : Day
        data object Tomorrow : Day
        data class Weekday(val day: DayOfWeek) : Day
    }
}
