package com.likehome.app.core

import org.junit.Assert.assertEquals
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
}
