package com.likehome.app.core

/**
 * Square cropping for profile photos. The photo is drawn centred in a viewport, scaled to
 * cover a [viewport]-sized square, times the user's [zoom], and moved by an offset.
 */
object CropMath {
    const val MAX_ZOOM = 5f

    /** Scale from photo pixels to screen pixels. */
    fun scale(imageW: Int, imageH: Int, viewport: Float, zoom: Float): Float =
        viewport / minOf(imageW, imageH) * zoom.coerceIn(1f, MAX_ZOOM)

    /** Keeps the photo covering the square: the offset can't reveal empty space. */
    fun clampOffset(imageW: Int, imageH: Int, viewport: Float, zoom: Float, x: Float, y: Float): Pair<Float, Float> {
        val s = scale(imageW, imageH, viewport, zoom)
        val maxX = ((imageW * s - viewport) / 2).coerceAtLeast(0f)
        val maxY = ((imageH * s - viewport) / 2).coerceAtLeast(0f)
        // "+ 0f" turns -0.0 into 0.0.
        return (x.coerceIn(-maxX, maxX) + 0f) to (y.coerceIn(-maxY, maxY) + 0f)
    }

    /** The square of the photo, in photo pixels (left, top, size), that the viewport shows. */
    fun sourceSquare(imageW: Int, imageH: Int, viewport: Float, zoom: Float, x: Float, y: Float): Triple<Int, Int, Int> {
        val s = scale(imageW, imageH, viewport, zoom)
        val size = (viewport / s).toInt().coerceIn(1, minOf(imageW, imageH))
        val left = (imageW / 2f - x / s - size / 2f).toInt().coerceIn(0, imageW - size)
        val top = (imageH / 2f - y / s - size / 2f).toInt().coerceIn(0, imageH - size)
        return Triple(left, top, size)
    }
}
