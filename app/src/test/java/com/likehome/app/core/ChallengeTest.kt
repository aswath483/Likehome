package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ChallengeTest {
    private val start = LocalDate.of(2026, 10, 20)
    private val names = mapOf("a" to "Asha", "b" to "Ben")

    private fun m(uid: String, day: Long, snoozes: Int) = ChallengeMorning(uid, start.plusDays(day), snoozes)

    @Test fun daysLeftCountsToday() {
        assertEquals(7, Challenge.daysLeft(start, 7, start))
        assertEquals(1, Challenge.daysLeft(start, 7, start.plusDays(6)))
        assertEquals(0, Challenge.daysLeft(start, 7, start.plusDays(7)))
        assertEquals(7, Challenge.daysLeft(start, 7, start.minusDays(2)))
        assertTrue(Challenge.isOver(start, 7, start.plusDays(7)))
        assertFalse(Challenge.isOver(start, 7, start.plusDays(6)))
    }

    @Test fun mostCleanMorningsLeads() {
        val s = Challenge.standings(names, listOf(m("a", 0, 0), m("a", 1, 2), m("b", 0, 0), m("b", 1, 0)), start, 7)
        assertEquals(listOf("b", "a"), s.map { it.uid })
        assertEquals(2, s[0].clean)
        assertEquals(2, s[0].streak)
        assertEquals(0, s[1].streak)
        assertEquals(setOf("b"), Challenge.leaders(s))
    }

    @Test fun outsideMorningsAndStrangersAreIgnored() {
        val s = Challenge.standings(names, listOf(m("a", -1, 0), m("a", 7, 0), m("z", 0, 0)), start, 7)
        assertTrue(s.all { it.mornings == 0 })
        assertEquals(setOf("a", "b"), s.map { it.uid }.toSet())
        assertEquals(emptySet<String>(), Challenge.leaders(s))
    }

    @Test fun worstAlarmOfTheDayCounts() {
        val s = Challenge.standings(mapOf("a" to "Asha"), listOf(m("a", 0, 0), m("a", 0, 1)), start, 7)
        assertEquals(0, s.single().clean)
        assertEquals(1, s.single().mornings)
    }

    @Test fun tiesShareTheLead() {
        val s = Challenge.standings(names, listOf(m("a", 0, 0), m("b", 0, 0)), start, 7)
        assertEquals(setOf("a", "b"), Challenge.leaders(s))
    }

    @Test fun namesAreTidied() {
        assertEquals("Priya S", Challenge.cleanName("  Priya   S "))
        assertEquals(20, Challenge.cleanName("x".repeat(40)).length)
        assertEquals("a_2026-10-20", Challenge.morningId("a", start))
    }
}
