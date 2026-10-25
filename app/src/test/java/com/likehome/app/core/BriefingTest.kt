package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ImportantDaysTest {
    private val exam = ImportantDay(1, LocalDate.of(2026, 11, 3), DayKind.EXAM, "Maths exam")
    private val bday = ImportantDay(2, LocalDate.of(1970, 2, 28), DayKind.BIRTHDAY, "Papa's birthday", yearly = true)
    private val leap = ImportantDay(3, LocalDate.of(2004, 2, 29), DayKind.BIRTHDAY, "Leap kid", yearly = true)

    @Test fun oneTimeDaysOnlyOnTheirDate() {
        assertTrue(exam.isOn(LocalDate.of(2026, 11, 3)))
        assertFalse(exam.isOn(LocalDate.of(2027, 11, 3)))
    }

    @Test fun yearlyDaysComeBackAndLeapBirthdaysMoveTo28th() {
        assertTrue(bday.isOn(LocalDate.of(2031, 2, 28)))
        assertTrue(leap.isOn(LocalDate.of(2027, 2, 28)))
        assertFalse(leap.isOn(LocalDate.of(2028, 2, 28)))
        assertTrue(leap.isOn(LocalDate.of(2028, 2, 29)))
    }

    @Test fun upcomingIsSortedAndSkipsThePast() {
        val from = LocalDate.of(2026, 11, 4)
        val list = ImportantDays.upcoming(listOf(exam, bday), from)
        assertEquals(listOf(2L), list.map { it.first.id })
        assertEquals(LocalDate.of(2027, 2, 28), list[0].second)
    }

    @Test fun roundTrip() {
        assertEquals(listOf(exam, bday), ImportantDays.decode(ImportantDays.encode(listOf(exam, bday))))
        assertTrue(ImportantDays.decode("junk").isEmpty())
    }
}

class BriefingTest {
    private val sample = """{"latitude":13.08,"longitude":80.27,"daily":{"time":["2026-11-03"],"temperature_2m_max":[31.4],"temperature_2m_min":[24.6],"precipitation_probability_max":[72],"weather_code":[61]}}"""

    @Test fun parsesOpenMeteo() {
        assertEquals(Weather(31, 25, 72, 61), Briefing.parseWeather(sample))
        assertNull(Briefing.parseWeather("{\"error\":true}"))
        assertNull(Briefing.parseWeather(null))
    }

    @Test fun adviceHasPriorities() {
        assertEquals(WeatherAdvice.RAIN, Briefing.advice(Weather(31, 25, 72, 61)))
        assertEquals(WeatherAdvice.STORM, Briefing.advice(Weather(30, 22, 90, 95)))
        assertEquals(WeatherAdvice.HOT, Briefing.advice(Weather(39, 28, 5, 0)))
        assertEquals(WeatherAdvice.COLD, Briefing.advice(Weather(3, -2, 10, 3)))
        assertEquals(WeatherAdvice.SNOW, Briefing.advice(Weather(-1, -6, 80, 73)))
        assertEquals(WeatherAdvice.LOVELY, Briefing.advice(Weather(24, 15, 10, 1)))
    }
}

class DayLinesTest {
    @Test fun everyLanguageAndKindNamesTheDay() {
        com.likehome.app.mom.Language.entries.forEach { lang ->
            DayKind.entries.forEach { kind ->
                val line = com.likehome.app.mom.DayLines.line(kind, "TITLE", lang)
                assertTrue("$lang $kind", line.contains("TITLE") && !line.contains("{"))
            }
        }
    }
}
