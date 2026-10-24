package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RingStyleTest {

    @Test
    fun smartMixTurnsComicAfterThreeSnoozes() {
        assertEquals(RingStyle.CHAT, RingStyle.SMART_MIX.resolve(0))
        assertEquals(RingStyle.CHAT, RingStyle.SMART_MIX.resolve(2))
        assertEquals(RingStyle.COMIC, RingStyle.SMART_MIX.resolve(3))
    }

    @Test
    fun fixedStylesNeverChange() {
        (0..3).forEach {
            assertEquals(RingStyle.CHAT, RingStyle.CHAT.resolve(it))
            assertEquals(RingStyle.COMIC, RingStyle.COMIC.resolve(it))
        }
    }

    @Test
    fun rageRisesWithEverySnooze() {
        val rage = (0..3).map(Rage::percent)
        assertEquals(rage.sorted(), rage)
        assertTrue(Rage.percent(99) <= 100)
    }
}
