package com.likehome.app.core

import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * "Earlier if I have an early meeting": an alarm with a lead time rings [leadMinutes]
 * before the day's first calendar event when that is earlier than its usual time. It never
 * moves later, never into the past, and never before [EARLIEST] (a 6 am flight should not
 * ring at 1 am; the user sets a separate alarm for that).
 */
object CalendarWake {

    val EARLIEST: LocalTime = LocalTime.of(4, 0)

    /** The lead times offered, in minutes; 0 is off. */
    val leadChoices = listOf(0, 45, 60, 90)

    fun ringAt(
        regular: ZonedDateTime,
        firstEvent: ZonedDateTime?,
        leadMinutes: Int,
        now: ZonedDateTime,
    ): ZonedDateTime {
        if (leadMinutes <= 0 || firstEvent == null) return regular
        if (firstEvent.toLocalDate() != regular.toLocalDate()) return regular
        val early = firstEvent.minusMinutes(leadMinutes.toLong()).withSecond(0).withNano(0)
        return when {
            !early.isBefore(regular) -> regular
            !early.isAfter(now) -> regular
            early.toLocalTime().isBefore(EARLIEST) -> regular.with(EARLIEST).takeIf { it.isAfter(now) && it.isBefore(regular) } ?: regular
            else -> early
        }
    }

    /** The window to search for that first event: from [EARLIEST] to the usual alarm plus the lead. */
    fun searchWindow(regular: ZonedDateTime, leadMinutes: Int): Pair<ZonedDateTime, ZonedDateTime> =
        regular.with(EARLIEST) to regular.plusMinutes(leadMinutes.toLong())
}
