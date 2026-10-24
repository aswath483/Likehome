package com.likehome.app.core

import java.time.DayOfWeek
import java.time.LocalDate

object Snooze {
    const val MINUTES = 5
    const val WEEKEND_MINUTES = 10

    /** Weekend mode: Mom lets you sleep a little longer on Saturday and Sunday. */
    fun minutes(weekendMode: Boolean, date: LocalDate): Int {
        val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        return if (weekendMode && weekend) WEEKEND_MINUTES else MINUTES
    }

    /** A snooze missed by up to this long (phone was off) still rings, straight away. */
    const val LATE_GRACE_MS = 30 * 60_000L
    const val LATE_DELAY_MS = 10_000L

    /**
     * When to ring a snooze saved for [at] after a reboot at [now]: on time if still ahead,
     * a few seconds from now if it was missed recently, or null if it's too old to matter.
     */
    fun restoreAt(at: Long, now: Long): Long? = when {
        at > now -> at
        now - at <= LATE_GRACE_MS -> now + LATE_DELAY_MS
        else -> null
    }
}
