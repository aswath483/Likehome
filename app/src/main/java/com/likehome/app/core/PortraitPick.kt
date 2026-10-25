package com.likehome.app.core

/** Where a character's picture comes from. */
enum class PortraitSource { USER, BUNDLED, DRAWN }

/**
 * The picture to show for a drama level, and whether it really shows that mood.
 * When it doesn't (one photo used for every mood), screens add a mood ring instead.
 */
data class Portrait(val source: PortraitSource, val level: Int?, val exactMood: Boolean)

object PortraitPick {
    /**
     * Best first: the user's photo for this mood, the user's other photos (their
     * sweet one first), bundled art for this mood, other bundled art, then the drawn face.
     * [user] and [bundled] are the drama levels that have a picture.
     */
    fun pick(level: Int, user: Set<Int>, bundled: Set<Int>): Portrait {
        fun from(levels: Set<Int>, source: PortraitSource): Portrait? = when {
            level in levels -> Portrait(source, level, exactMood = true)
            0 in levels -> Portrait(source, 0, exactMood = false)
            levels.isNotEmpty() -> Portrait(source, levels.min(), exactMood = false)
            else -> null
        }
        return from(user, PortraitSource.USER)
            ?: from(bundled, PortraitSource.BUNDLED)
            ?: Portrait(PortraitSource.DRAWN, null, exactMood = true)
    }
}
