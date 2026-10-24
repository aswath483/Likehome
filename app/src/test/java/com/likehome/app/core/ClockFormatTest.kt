package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ClockFormatTest {
    private val zone = ZoneId.of("Asia/Kolkata")

    @Test
    fun followsTwelveAndTwentyFourHourClocks() {
        val t = LocalTime.of(19, 5)
        assertEquals("7:05", ClockFormat.digits(t, false))
        assertEquals("pm", ClockFormat.period(t, false))
        assertEquals("19:05", ClockFormat.digits(t, true))
        assertNull(ClockFormat.period(t, true))
        assertEquals("12:30 am", ClockFormat.full(LocalTime.of(0, 30), false))
        assertEquals("00:30", ClockFormat.full(LocalTime.of(0, 30), true))
    }

    @Test
    fun untilRoundsUp() {
        val now = ZonedDateTime.of(2026, 10, 24, 22, 0, 30, 0, zone)
        assertEquals("in 1 min", ClockFormat.until(now, now.plusSeconds(20)))
        assertEquals("in 9 h", ClockFormat.until(now, now.plusHours(9)))
        assertEquals("in 8 h 30 min", ClockFormat.until(now, now.plusMinutes(510)))
        assertEquals("now", ClockFormat.until(now, now.minusMinutes(1)))
    }

    @Test
    fun dayWords() {
        val d = LocalDate.of(2026, 10, 24)
        assertEquals("Today", ClockFormat.dayWord(d, d))
        assertEquals("Tomorrow", ClockFormat.dayWord(d, d.plusDays(1)))
        assertEquals("Monday", ClockFormat.dayWord(d, d.plusDays(2)))
    }
}
