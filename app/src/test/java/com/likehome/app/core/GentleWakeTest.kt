package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GentleWakeTest {
    @Test fun startsBeforeTheRing() {
        assertEquals(1_000_000L - 600_000L, GentleWake.startAt(1_000_000L, 10, now = 0L))
    }

    @Test fun offOrPastMeansNoSunrise() {
        assertNull(GentleWake.startAt(1_000_000L, 0, now = 0L))
        assertNull(GentleWake.startAt(1_000_000L, 10, now = 500_000L))
    }

    @Test fun volumeAndBrightnessRiseSmoothly() {
        assertEquals(0.03f, GentleWake.volume(0f), 0.001f)
        assertEquals(0.5f, GentleWake.volume(1f), 0.001f)
        assertTrue(GentleWake.volume(0.5f) < 0.3f)
        assertEquals(1f, GentleWake.brightness(GentleWake.progress(20 * 60_000L, 10)), 0.001f)
        assertEquals(0.5f, GentleWake.progress(5 * 60_000L, 10), 0.001f)
    }

    @Test fun mainRingStaysQuietOnlyNearItsTime() {
        assertTrue(GentleWake.alreadyUp(plannedRingAt = 1_000_000L, now = 1_000_500L))
        assertFalse(GentleWake.alreadyUp(plannedRingAt = 1_000_000L, now = 1_000_000L + 86_400_000L))
        assertFalse(GentleWake.alreadyUp(plannedRingAt = 0L, now = 10L))
    }

    @Test fun chimesStayInRange() {
        val out = FloatArray(44_100 * 3)
        ChimeGen(seed = 7).fill(out)
        assertTrue(out.all { it in -1f..1f })
        assertTrue(out.any { kotlin.math.abs(it) > 0.05f })
    }
}
