package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        assertEquals(ClockFormat.Span(0, 1), ClockFormat.until(now, now.plusSeconds(20)))
        assertEquals(ClockFormat.Span(9, 0), ClockFormat.until(now, now.plusHours(9)))
        assertEquals(ClockFormat.Span(8, 30), ClockFormat.until(now, now.plusMinutes(510)))
        assertTrue(ClockFormat.until(now, now.minusMinutes(1)).isNow)
    }

    @Test
    fun dayWords() {
        val d = LocalDate.of(2026, 10, 24)
        assertEquals(ClockFormat.Day.Today, ClockFormat.dayOf(d, d))
        assertEquals(ClockFormat.Day.Tomorrow, ClockFormat.dayOf(d, d.plusDays(1)))
        assertEquals(ClockFormat.Day.Weekday(d.plusDays(2).dayOfWeek), ClockFormat.dayOf(d, d.plusDays(2)))
    }
}
