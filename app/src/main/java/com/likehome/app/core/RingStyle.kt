package com.likehome.app.core

/**
 * How the ringing screen looks. Lines and alarm logic are the same for all. Names and
 * descriptions are string resources (see ui/common/Labels.kt).
 */
enum class RingStyle {
    CHAT, COMIC, NEWS, RETRO, FILMY, KITCHEN, NIGHT, FRIDGE, SMART_MIX;

    /** The style to draw at this drama level. */
    fun resolve(level: Int): RingStyle = when {
        this != SMART_MIX -> this
        level >= SMART_MIX_SWITCH_LEVEL -> COMIC
        else -> CHAT
    }

    companion object {
        const val SMART_MIX_SWITCH_LEVEL = 3
    }
}

/** Comic-style rage meter, one reading per drama level (sound effects are string resources). */
object Rage {
    private val percents = listOf(15, 45, 70, 95)

    fun percent(level: Int): Int = percents[level.coerceIn(0, percents.lastIndex)]
}
