package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CropMathTest {

    @Test
    fun noZoomTakesTheCentredSquare() {
        // A 4000×3000 landscape photo in a 300 px viewport: the middle 3000×3000.
        assertEquals(Triple(500, 0, 3000), CropMath.sourceSquare(4000, 3000, 300f, 1f, 0f, 0f))
    }

    @Test
    fun zoomAndPanMoveTheSquare() {
        // Zoom 2×: a 1500 px square. Dragging the photo right (+x) shows more of its left side.
        val (left, top, size) = CropMath.sourceSquare(4000, 3000, 300f, 2f, 100f, 0f)
        assertEquals(1500, size)
        assertEquals(750, top)
        assertEquals(2000 - 500 - 750, left) // 100 px on screen is 500 photo px at scale 0.2
    }

    @Test
    fun offsetIsClampedSoThePhotoAlwaysCovers() {
        assertEquals(0f to 0f, CropMath.clampOffset(3000, 3000, 300f, 1f, 80f, -80f))
        val (x, _) = CropMath.clampOffset(4000, 3000, 300f, 1f, 999f, 0f)
        assertEquals(50f, x, 0.01f)
    }

    @Test
    fun zoomIsLimited() {
        assertEquals(CropMath.scale(1000, 1000, 100f, CropMath.MAX_ZOOM), CropMath.scale(1000, 1000, 100f, 50f), 0f)
        assertEquals(CropMath.scale(1000, 1000, 100f, 1f), CropMath.scale(1000, 1000, 100f, 0.2f), 0f)
    }
}
