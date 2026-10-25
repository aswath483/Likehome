package com.likehome.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

class PrayerTimesTest {
    private val equinox = LocalDate.of(2026, 3, 20)

    private fun near(expected: String, actual: LocalTime, minutes: Long = 6) {
        val diff = abs(Duration.between(LocalTime.parse(expected), actual).toMinutes())
        assertTrue("expected ~$expected, got $actual", diff <= minutes)
    }

    @Test fun meccaAtTheEquinox() {
        val d = PrayerTimes.compute(equinox, 21.4225, 39.8262, 3.0, PrayerTimes.fajrAngle("SA"))!!
        near("06:28", d.sunrise)
        near("18:34", d.maghrib)
        near("05:13", d.fajr)
    }

    @Test fun jakartaAtTheEquinox() {
        val d = PrayerTimes.compute(equinox, -6.2088, 106.8456, 7.0, PrayerTimes.fajrAngle("ID"))!!
        near("06:01", d.sunrise)
        near("18:07", d.maghrib)
        near("04:44", d.fajr)
    }

    @Test fun orderIsAlwaysFajrSunriseMaghrib() {
        listOf(Triple(51.5, -0.12, 0.0), Triple(28.6, 77.2, 5.5), Triple(40.7, -74.0, -5.0), Triple(-33.9, 151.2, 10.0)).forEach { (lat, lon, tz) ->
            (0L until 365L step 30).forEach { i ->
                val d = PrayerTimes.compute(LocalDate.of(2026, 1, 1).plusDays(i), lat, lon, tz, 18.0)
                assertNotNull(d)
                assertTrue("$lat ${d!!}", d.fajr.isBefore(d.sunrise) && d.sunrise.isBefore(d.maghrib))
            }
        }
    }

    @Test fun farNorthSummerFallsBackForFajr() {
        val d = PrayerTimes.compute(LocalDate.of(2026, 6, 21), 59.3, 18.1, 2.0, 18.0)!!
        assertTrue(Duration.between(d.fajr, d.sunrise).toMinutes() in 85..95)
    }

    @Test fun ramadanFollowsTheHijriCalendar() {
        assertTrue(RamadanCalendar.isRamadan(LocalDate.of(2026, 3, 1)))
        assertFalse(RamadanCalendar.isRamadan(LocalDate.of(2026, 6, 1)))
    }
}

class RamadanLinesTest {
    @Test fun everyLanguageHasSuhoorAndIftarLines() {
        com.likehome.app.mom.Language.entries.forEach { lang ->
            assertTrue(com.likehome.app.mom.RamadanLines.suhoorLine(1L, lang).isNotBlank())
            assertTrue(com.likehome.app.mom.RamadanLines.iftarLine(1L, lang).isNotBlank())
        }
    }
}
