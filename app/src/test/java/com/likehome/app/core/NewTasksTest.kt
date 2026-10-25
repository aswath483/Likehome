package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NewTasksTest {
    @Test fun memorySequenceHasNoRepeatsInARow() {
        repeat(50) { seed ->
            val seq = MemoryRound.random(random = Random(seed)).sequence
            assertEquals(5, seq.size)
            assertTrue(seq.all { it in 0 until MemoryRound.TILES })
            assertTrue(seq.zipWithNext().none { (a, b) -> a == b })
        }
    }

    @Test fun memoryRightOrderFinishes() {
        val round = MemoryRound(listOf(1, 4, 7))
        assertEquals(MemoryRound.Result.GOOD, round.tap(1))
        assertEquals(MemoryRound.Result.GOOD, round.tap(4))
        assertEquals(MemoryRound.Result.DONE, round.tap(7))
    }

    @Test fun memoryWrongTapStartsOver() {
        val round = MemoryRound(listOf(1, 4, 7))
        round.tap(1)
        assertEquals(MemoryRound.Result.WRONG, round.tap(5))
        assertEquals(0, round.position)
        assertEquals(MemoryRound.Result.GOOD, round.tap(1))
    }

    @Test fun lightMustStayOn() {
        val check = LightCheck(lux = 100f, holdMs = 3000)
        assertEquals(0f, check.feed(500f, 0), 0.001f)
        assertEquals(0.5f, check.feed(500f, 1500), 0.001f)
        assertEquals(0f, check.feed(10f, 2000), 0.001f)
        check.feed(500f, 3000)
        assertEquals(1f, check.feed(500f, 6000), 0.001f)
    }

    @Test fun barcodeMatchesOnlyTheSavedOne() {
        assertTrue(BarcodeCheck.matches(" 8901030865278", "8901030865278"))
        assertFalse(BarcodeCheck.matches("123", "8901030865278"))
        assertFalse(BarcodeCheck.matches("anything", ""))
        assertFalse(BarcodeCheck.matches(null, "8901030865278"))
    }
}
