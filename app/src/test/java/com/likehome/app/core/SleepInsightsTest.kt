package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SleepInsightsTest {
    private val today = LocalDate.of(2026, 10, 25) // a Sunday

    private fun morning(daysAgo: Long, h: Int, m: Int, snoozes: Int = 0) =
        Morning(today.minusDays(daysAgo), snoozes, LocalTime.of(h, m))

    @Test fun nightIsBedtimeToWake() {
        val m = morning(0, 7, 0)
        val bed = listOf(today.minusDays(1).atTime(23, 30), today.minusDays(1).atTime(22, 0))
        assertEquals(7 * 60 + 30, SleepInsights.sleepMinutes(bed, m))
    }

    @Test fun napsAndOldRecordsDontCount() {
        val m = morning(0, 7, 0)
        assertNull(SleepInsights.sleepMinutes(listOf(today.atTime(6, 30)), m))
        assertNull(SleepInsights.sleepMinutes(listOf(today.minusDays(2).atTime(23, 0)), m))
    }

    @Test fun fewMorningsAskForMore() {
        val s = SleepInsights.summary(listOf(morning(1, 7, 0), morning(2, 7, 0)), emptyList(), today)
        assertEquals(SleepTip.NEED_MORE, s.tip)
    }

    @Test fun shortNightsComeFirst() {
        val history = (1L..7L).map { morning(it, 6, 30) }
        // In bed at 1 am, up at 6:30: five and a half hours.
        val beds = (1L..7L).map { today.minusDays(it).atTime(1, 0) }
        val s = SleepInsights.summary(history, beds, today)
        assertEquals(330, s.averageSleepMinutes)
        assertEquals(SleepTip.SHORT_NIGHTS, s.tip)
        assertEquals(LocalTime.of(6, 30), s.averageWake)
        assertEquals(0, s.wakeSpread)
    }

    @Test fun wanderingWakeTimeIsIrregular() {
        val history = (1L..8L).map { if (it % 2 == 0L) morning(it, 6, 0) else morning(it, 8, 0) }
        val s = SleepInsights.summary(history, emptyList(), today)
        assertEquals(60, s.wakeSpread)
        assertEquals(SleepTip.IRREGULAR, s.tip)
    }

    @Test fun findsTheBadWeekday() {
        // Every Monday takes 3 snoozes; other days none.
        val history = (1L..21L).map { d ->
            val date = today.minusDays(d)
            morning(d, 7, 0, snoozes = if (date.dayOfWeek == DayOfWeek.MONDAY) 3 else 0)
        }
        val s = SleepInsights.summary(history, emptyList(), today)
        assertEquals(DayOfWeek.MONDAY, s.worstDay)
        assertEquals(SleepTip.BAD_WEEKDAY, s.tip)
    }

    @Test fun improvingWhenSnoozesDrop() {
        val history = (1L..6L).map { morning(it, 7, 0, 0) } + (8L..13L).map { morning(it, 7, 0, 2) }
        val s = SleepInsights.summary(history, emptyList(), today)
        assertTrue(s.snoozeTrend!! < 0)
        assertEquals(SleepTip.IMPROVING, s.tip)
    }

    @Test fun bedtimesRoundTripAndDedupe() {
        val a = LocalDateTime.of(2026, 10, 24, 23, 0)
        val list = SleepInsights.add(SleepInsights.add(emptyList(), a), a.plusMinutes(10))
        assertEquals(listOf(a.plusMinutes(10)), list)
        assertEquals(list, SleepInsights.decode(SleepInsights.encode(list)))
        val old = SleepInsights.add(listOf(a.minusDays(40)), a)
        assertEquals(listOf(a), old)
    }
}

class BedsideClockTest {
    @Test fun driftStaysSmallAndMoves() {
        val base = LocalDateTime.of(2026, 10, 25, 1, 0)
        val spots = (0L until 8L).map { BedsideClock.offset(base.plusMinutes(it)) }
        assertTrue(spots.all { kotlin.math.abs(it.first) <= 0.06f && kotlin.math.abs(it.second) <= 0.06f })
        assertEquals(8, spots.toSet().size)
        assertEquals(spots[0], BedsideClock.offset(base.plusMinutes(8)))
    }
}
