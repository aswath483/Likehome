package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class BedtimeTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private fun at(day: Int, h: Int, m: Int) = ZonedDateTime.of(2026, 10, day, h, m, 0, 0, zone)

    @Test
    fun nextIsTonightOrTomorrow() {
        assertEquals(at(24, 23, 0), Bedtime.next(at(24, 21, 15), LocalTime.of(23, 0)))
        assertEquals(at(25, 23, 0), Bedtime.next(at(24, 23, 0), LocalTime.of(23, 0)))
        assertEquals(at(25, 0, 0), Bedtime.next(at(24, 22, 0), LocalTime.MIDNIGHT))
    }

    @Test
    fun onlyTomorrowMorningsAlarmIsMentioned() {
        val now = at(24, 23, 0)
        assertTrue(Bedtime.mentionsAlarm(now, at(25, 7, 0)))
        assertFalse(Bedtime.mentionsAlarm(now, at(26, 7, 0)))
        assertFalse(Bedtime.mentionsAlarm(now, null))
    }
}
