package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SnoozeTest {
    private val friday = LocalDate.of(2026, 10, 23)
    private val saturday = LocalDate.of(2026, 10, 24)
    private val sunday = LocalDate.of(2026, 10, 25)

    @Test
    fun weekendModeLengthensOnlyWeekendSnoozes() {
        assertEquals(5, Snooze.minutes(weekendMode = true, date = friday))
        assertEquals(10, Snooze.minutes(weekendMode = true, date = saturday))
        assertEquals(10, Snooze.minutes(weekendMode = true, date = sunday))
        assertEquals(5, Snooze.minutes(weekendMode = false, date = sunday))
    }

    @Test
    fun snoozeRestoreAfterReboot() {
        val now = 1_000_000_000L
        assertEquals(now + 60_000, Snooze.restoreAt(now + 60_000, now))
        assertEquals(now + Snooze.LATE_DELAY_MS, Snooze.restoreAt(now - 5 * 60_000, now))
        assertEquals(null, Snooze.restoreAt(now - 2 * 60 * 60_000, now))
    }

    @Test fun limitCapsSnoozes() {
        assertEquals(3, Snooze.cap(Snooze.NO_LIMIT))
        assertEquals(1, Snooze.cap(1))
        assertEquals(3, Snooze.cap(9))
        assertEquals(0, Snooze.cap(0))
        assertEquals(0, Snooze.cap(Snooze.NO_LIMIT, importantToday = true, strictOnImportantDays = true))
        assertEquals(2, Snooze.cap(2, importantToday = true, strictOnImportantDays = false))
        assertEquals(2, Snooze.cap(2, importantToday = false, strictOnImportantDays = true))
    }

    @Test fun allowedUntilTheCap() {
        assertTrue(Snooze.allowed(0, 1))
        assertTrue(Snooze.lastOne(0, 1))
        assertFalse(Snooze.allowed(1, 1))
        assertFalse(Snooze.allowed(0, 0))
        assertTrue(Snooze.allowed(2, 3))
        assertFalse(Snooze.lastOne(1, 3))
    }
}
