package com.likehome.app.core

import com.likehome.app.mom.MomLines
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

    /** [limit] value for an alarm that allows snoozing until Mom's last level. */
    const val NO_LIMIT = -1
    /** Choices in the alarm editor: no limit, 2, 1, or no snoozing at all. */
    val limitChoices = listOf(NO_LIMIT, 2, 1, 0)
    /**
     * How many snoozes this ring allows in all: the alarm's [limit] (or Mom's levels
     * without one; snoozing always stops at her last level), and none on an important day
     * when the user asked for that.
     */
    fun cap(limit: Int, importantToday: Boolean = false, strictOnImportantDays: Boolean = false): Int = when {
        importantToday && strictOnImportantDays -> 0
        limit < 0 -> MomLines.MAX_LEVEL
        else -> minOf(limit, MomLines.MAX_LEVEL)
    }

    /** Whether snoozing is still allowed after [snoozed] snoozes. */
    fun allowed(snoozed: Int, cap: Int): Boolean = snoozed < cap

    /** This is the last snooze allowed. */
    fun lastOne(snoozed: Int, cap: Int): Boolean = snoozed == cap - 1

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
