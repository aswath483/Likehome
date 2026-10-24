package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WakeTaskTest {

    @Test
    fun shakeCountsStrongJoltsOnly() {
        val c = ShakeCounter()
        assertFalse(c.feed(0f, 0f, 9.81f, 0))      // phone resting
        assertFalse(c.feed(5f, 5f, 12f, 100))      // a nudge (~1.5 g)
        assertTrue(c.feed(25f, 5f, 10f, 400))      // a shake (~2.8 g)
        assertEquals(1, c.count)
    }

    @Test
    fun shakeJoltsTooCloseTogetherCountOnce() {
        val c = ShakeCounter()
        assertTrue(c.feed(30f, 0f, 0f, 1000))
        assertFalse(c.feed(30f, 0f, 0f, 1100))
        assertTrue(c.feed(30f, 0f, 0f, 1300))
        assertEquals(2, c.count)
    }

    @Test
    fun accelStepsNeedAPeakThenADip() {
        val c = AccelStepCounter()
        var t = 0L
        // 10 walking strides: up to ~13 m/s², down to ~7 m/s², every 500 ms.
        repeat(10) {
            c.feed(0f, 0f, 13f, t)
            c.feed(0f, 0f, 9.8f, t + 120)
            c.feed(0f, 0f, 7f, t + 250)
            t += 500
        }
        assertEquals(10, c.count)
    }

    @Test
    fun accelStepsIgnoreAPhoneLyingStill() {
        val c = AccelStepCounter()
        repeat(200) { c.feed(0.1f, 0.2f, 9.8f, it * 20L) }
        assertEquals(0, c.count)
    }

    @Test
    fun mathSumsAreTwoDigitAndCorrect() {
        val sums = MathPuzzle.sums(50, Random(7))
        assertEquals(50, sums.size)
        sums.forEach {
            assertTrue(it.a in 12..59 && it.b in 7..39)
            assertEquals(it.a + it.b, it.answer)
        }
    }
}
