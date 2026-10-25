package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PortraitPickTest {

    @Test
    fun userPhotoForThisMoodWins() {
        assertEquals(Portrait(PortraitSource.USER, 2, true), PortraitPick.pick(2, user = setOf(0, 2), bundled = setOf(0, 1, 2, 3)))
    }

    @Test
    fun oneUserPhotoServesEveryMoodWithARing() {
        assertEquals(Portrait(PortraitSource.USER, 0, false), PortraitPick.pick(3, user = setOf(0), bundled = setOf(3)))
        assertEquals(Portrait(PortraitSource.USER, 1, false), PortraitPick.pick(3, user = setOf(2, 1), bundled = emptySet()))
    }

    @Test
    fun bundledArtThenDrawnFace() {
        assertEquals(Portrait(PortraitSource.BUNDLED, 1, true), PortraitPick.pick(1, emptySet(), setOf(0, 1)))
        assertEquals(Portrait(PortraitSource.BUNDLED, 0, false), PortraitPick.pick(3, emptySet(), setOf(0)))
        assertEquals(Portrait(PortraitSource.DRAWN, null, true), PortraitPick.pick(0, emptySet(), emptySet()))
    }
}
