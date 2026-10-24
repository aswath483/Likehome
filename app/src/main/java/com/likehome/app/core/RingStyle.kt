package com.likehome.app.core

/** How Mom appears on the ringing screen. Lines and alarm logic are the same for all. */
enum class RingStyle(val short: String, val title: String, val detail: String) {
    CHAT("Chat", "Mom texts you", "Her messages pile up like a real chat"),
    COMIC("Comic", "Comic drama", "Bold comic panels and a rage meter"),
    SMART_MIX("Mix", "Smart mix", "Starts as chat, turns comic after 3 snoozes");

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

/** Comic-style rage meter and sound effects, one per drama level. */
object Rage {
    private val percents = listOf(15, 45, 70, 95)
    private val sounds = listOf("BEEP!", "HMPH!", "WHAT?!", "SPLASH!")

    fun percent(level: Int): Int = percents[level.coerceIn(0, percents.lastIndex)]
    fun sound(level: Int): String = sounds[level.coerceIn(0, sounds.lastIndex)]
}
