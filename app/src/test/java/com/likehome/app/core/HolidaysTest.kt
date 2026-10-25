package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class HolidaysTest {
    private val sample = """
        [{"date":"2026-01-26","localName":"गणतंत्र दिवस","name":"Republic Day","countryCode":"IN","fixed":true,"global":true,"counties":null,"launchYear":null,"types":["Public"]},
         {"date":"2026-03-04","localName":"Holi","name":"Holi","countryCode":"IN","fixed":false,"global":false,"counties":["IN-UP"],"launchYear":null,"types":["Public"]},
         {"date":"2026-08-15","localName":"","name":"Independence Day","countryCode":"IN","fixed":true,"global":true,"counties":null,"launchYear":null,"types":["Public"]}]
    """.trimIndent()

    @Test fun keepsOnlyNationwideHolidays() {
        val h = Holidays.parse(sample)
        assertEquals(listOf(LocalDate.of(2026, 1, 26), LocalDate.of(2026, 8, 15)), h.map { it.date })
        assertEquals("गणतंत्र दिवस", h[0].localName)
        assertEquals("Independence Day", h[1].localName)
    }

    @Test fun junkGivesNothing() {
        assertTrue(Holidays.parse(null).isEmpty())
        assertTrue(Holidays.parse("{\"status\":404}").isEmpty())
    }

    @Test fun skipsHolidaysOnlyWhenAskedAndVacationAlways() {
        val day = LocalDate.of(2026, 8, 15)
        assertTrue(Skips.skipped(day, setOf(day), skipHolidays = true, pausedUntil = null))
        assertFalse(Skips.skipped(day, setOf(day), skipHolidays = false, pausedUntil = null))
        assertTrue(Skips.skipped(day, emptySet(), skipHolidays = false, pausedUntil = day))
        assertFalse(Skips.skipped(day.plusDays(1), emptySet(), skipHolidays = false, pausedUntil = day))
    }
}

class NextRingTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val friday = ZonedDateTime.of(2026, 8, 14, 22, 0, 0, 0, zone)
    private val seven = LocalTime.of(7, 0)
    private val everyDay = DayOfWeek.entries.toSet()

    @Test fun skipsAHolidayToTheNextRingDay() {
        val holiday = LocalDate.of(2026, 8, 15)
        assertEquals(LocalDate.of(2026, 8, 16), NextRing.after(friday, seven, everyDay) { it == holiday }.toLocalDate())
    }

    @Test fun vacationSkipsUntilItEnds() {
        val until = LocalDate.of(2026, 8, 23)
        val weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        assertEquals(LocalDate.of(2026, 8, 24), NextRing.after(friday, seven, weekdays) { !it.isAfter(until) }.toLocalDate())
    }

    @Test fun oneTimeAlarmsAreNeverMoved() {
        assertEquals(LocalDate.of(2026, 8, 15), NextRing.after(friday, seven, emptySet()) { true }.toLocalDate())
    }

    @Test fun skippingEveryDayStillRingsEventually() {
        assertEquals(LocalDate.of(2026, 8, 15), NextRing.after(friday, seven, everyDay) { true }.toLocalDate())
    }
}
