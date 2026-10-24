package com.likehome.app.mom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class MomLinesTest {

    private val tuesday = LocalDate.of(2026, 10, 27)

    @Test
    fun hasAtLeastFortyLinesPerLanguage() {
        Language.entries.forEach { assertTrue(it.name, MomLines.lineCount(it) >= 40) }
    }

    @Test
    fun dailyAlarmSeedsPickDifferentLines() {
        // Seeds are trigger times on whole minutes; a week of 7:00 alarms must not repeat one line.
        val seeds = (0L..6L).map {
            LocalDateTime.of(2026, 11, 2, 7, 0).plusDays(it).toInstant(ZoneOffset.UTC).toEpochMilli()
        }
        val lines = seeds.map { MomLines.lineFor(1, it, Language.ENGLISH, tuesday) }.toSet()
        assertTrue("only ${lines.size} distinct lines", lines.size >= 4)
    }

    @Test
    fun conversationHasOneLinePerLevelAndIsStableAcrossSnoozes() {
        val seed = 1_793_000_000_000L
        val atTwo = MomLines.conversation(2, seed, Language.HINGLISH, tuesday)
        val atThree = MomLines.conversation(3, seed, Language.HINGLISH, tuesday)
        assertEquals(3, atTwo.size)
        assertEquals(atTwo, atThree.take(3))
    }

    @Test
    fun levelsOutsideRangeAreClamped() {
        assertEquals(MomLines.lineFor(3, 5000L, date = tuesday), MomLines.lineFor(9, 5000L, date = tuesday))
        assertEquals(MomLines.lineFor(0, 5000L, date = tuesday), MomLines.lineFor(-1, 5000L, date = tuesday))
    }

    @Test
    fun newYearAndDiwaliAlwaysGetTheirLine() {
        (0L..20L).forEach { s ->
            assertTrue(MomLines.lineFor(0, s * 60_000, date = LocalDate.of(2027, 1, 1)).contains("New Year"))
            assertTrue(MomLines.lineFor(0, s * 60_000, date = LocalDate.of(2026, 11, 8)).contains("Diwali"))
        }
    }

    @Test
    fun bedtimeLineSkipsAlarmMentionWithoutAlarm() {
        (0L..40L).forEach { s ->
            val line = MomLines.bedtimeLine(s * 60_000, Language.ENGLISH, "11:00 pm", alarm = null)
            assertFalse(line, line.contains("{"))
            assertFalse(line, line.contains("alarm is at"))
        }
        val withAlarm = (0L..40L).map { MomLines.bedtimeLine(it * 60_000, Language.ENGLISH, "11:00 pm", "7:00 am") }
        assertTrue(withAlarm.any { it.contains("7:00 am") })
    }

    @Test
    fun mixIsNeverNegative() {
        listOf(Long.MIN_VALUE, -1L, 0L, Long.MAX_VALUE, 1_793_000_000_000L).forEach {
            assertTrue(MomLines.mix(it, 3) >= 0)
        }
    }
}
