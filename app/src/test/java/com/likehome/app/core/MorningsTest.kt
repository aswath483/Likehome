package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class MorningsTest {

    private val today = LocalDate.of(2026, 10, 24)
    private val seven = LocalTime.of(7, 0)

    private fun m(daysAgo: Long, snoozes: Int, at: LocalTime = seven) = Morning(today.minusDays(daysAgo), snoozes, at)

    @Test
    fun streakCountsNoSnoozeMorningsNewestFirst() {
        val h = listOf(m(5, 2), m(4, 0), m(3, 0), m(1, 0), m(0, 0))
        assertEquals(4, Mornings.streak(h))   // the gap on day 2 (no alarm) doesn't break it
    }

    @Test
    fun aSnoozeTodayBreaksTheStreak() {
        assertEquals(0, Mornings.streak(listOf(m(2, 0), m(1, 0), m(0, 1))))
        assertEquals(0, Mornings.streak(emptyList()))
    }

    @Test
    fun worstAlarmOfTheDayCounts() {
        val h = listOf(m(1, 0), m(0, 0, LocalTime.of(6, 0)), m(0, 2, LocalTime.of(8, 0)))
        assertEquals(0, Mornings.streak(h))
        assertEquals(listOf(null, null, null, null, null, 0, 2), Mornings.week(h, today))
    }

    @Test
    fun badges() {
        assertEquals(setOf(Badge.EARLY_BIRD), Mornings.badges(listOf(m(0, 0, LocalTime.of(6, 10)))))
        assertEquals(setOf(Badge.SNOOZE_KING), Mornings.badges(listOf(m(0, 3))))
        val week = (6L downTo 0L).map { m(it, 0) }
        assertTrue(Badge.MOMS_FAVOURITE in Mornings.badges(week))
        assertTrue(Badge.MOMS_FAVOURITE !in Mornings.badges(week.drop(1)))
    }

    @Test
    fun newBadgesAreOnlyTheJustEarnedOnes() {
        val before = (6L downTo 1L).map { m(it, 0) }
        assertEquals(setOf(Badge.MOMS_FAVOURITE), Mornings.newBadges(before, before + m(0, 0)))
        assertEquals(emptySet<Badge>(), Mornings.newBadges(before, before + m(0, 1)))
    }

    @Test
    fun verdictNeedsThreeMorningsAndFollowsTheAverage() {
        assertNull(Mornings.verdict(listOf(m(1, 0), m(0, 0)), today))
        assertEquals(Verdict.GOOD_CHILD, Mornings.verdict(listOf(m(2, 0), m(1, 0), m(0, 1)), today))
        assertEquals(Verdict.TRYING, Mornings.verdict(listOf(m(2, 1), m(1, 1), m(0, 1)), today))
        assertEquals(Verdict.SHARMA_JI, Mornings.verdict(listOf(m(2, 3), m(1, 2), m(0, 3)), today))
        // Older than 30 days is forgotten.
        assertNull(Mornings.verdict(listOf(m(40, 3), m(35, 3), m(0, 0)), today))
    }

    @Test
    fun reportMentionsTheStreak() {
        assertEquals(ReportKind.PROUD_STREAK, Mornings.report(0, 5).kind)
        assertEquals(5, Mornings.report(0, 5).streak)
        assertEquals(ReportKind.PROUD, Mornings.report(0, 1).kind)
        assertEquals(ReportKind.ONCE, Mornings.report(1, 0).kind)
        assertEquals(ReportKind.DISAPPOINTED, Mornings.report(3, 0).kind)
    }
}

class MorningsStatsTest {
    private val today = LocalDate.of(2026, 10, 24)
    private fun m(daysAgo: Long, snoozes: Int) = Morning(today.minusDays(daysAgo), snoozes, LocalTime.of(7, 0))

    @Test
    fun bestStreakAndStreakOnADay() {
        val h = listOf(m(6, 0), m(5, 0), m(4, 0), m(3, 2), m(2, 0), m(1, 0), m(0, 1))
        assertEquals(3, Mornings.bestStreak(h))
        assertEquals(2, Mornings.streakOn(h, today.minusDays(1)))
        assertEquals(0, Mornings.streakOn(h, today))
        assertEquals(3.0 / 7, Mornings.averageSnoozes(h), 1e-9)
        assertEquals(today, Mornings.days(h).first().date)
    }
}
