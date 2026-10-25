package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

class CalendarWakeTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private fun at(day: Int, h: Int, m: Int = 0) = ZonedDateTime.of(2026, 11, day, h, m, 0, 0, zone)
    private val now = at(2, 22)
    private val regular = at(3, 8)

    @Test fun ringsEarlierForAnEarlyMeeting() {
        assertEquals(at(3, 7, 0), CalendarWake.ringAt(regular, at(3, 8, 0), 60, now))
        assertEquals(at(3, 6, 30), CalendarWake.ringAt(regular, at(3, 7, 15), 45, now))
    }

    @Test fun neverLater() {
        assertEquals(regular, CalendarWake.ringAt(regular, at(3, 11), 60, now))
    }

    @Test fun offOrNoEventKeepsTheUsualTime() {
        assertEquals(regular, CalendarWake.ringAt(regular, at(3, 8), 0, now))
        assertEquals(regular, CalendarWake.ringAt(regular, null, 60, now))
        assertEquals(regular, CalendarWake.ringAt(regular, at(4, 6), 60, now))
    }

    @Test fun neverBeforeFourOrInThePast() {
        assertEquals(at(3, 4, 0), CalendarWake.ringAt(regular, at(3, 5, 0), 90, now))
        val late = at(3, 6, 50)
        assertEquals(regular, CalendarWake.ringAt(regular, at(3, 7, 30), 60, late))
    }
}

class NoiseTest {
    private fun buffer(sound: SleepSound) = FloatArray(44_100 * 2).also { NoiseGen(sound).fill(it) }

    @Test fun everySoundStaysInRangeAndIsNotSilent() {
        SleepSound.entries.forEach { s ->
            val b = buffer(s)
            assertTrue(s.name, b.all { it in -1f..1f })
            assertTrue(s.name, b.map { abs(it) }.average() > 0.005)
        }
    }

    @Test fun brownIsSmootherThanWhite() {
        fun roughness(b: FloatArray) = (1 until b.size).map { abs(b[it] - b[it - 1]) }.average() / b.map { abs(it) }.average()
        assertTrue(roughness(buffer(SleepSound.BROWN)) < roughness(buffer(SleepSound.WHITE)) / 5)
    }

    @Test fun timerFadesOutAtTheEnd() {
        assertEquals(1f, SleepTimer.volume(0, 30))
        assertEquals(0.5f, SleepTimer.volume(30 * 60_000L - 15_000, 30), 0.01f)
        assertEquals(0f, SleepTimer.volume(31 * 60_000L, 30))
        assertEquals(1f, SleepTimer.volume(10 * 3_600_000L, 0))
    }
}
