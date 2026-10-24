package com.likehome.app.mom

/**
 * Mom's lines by drama level. Level 0 is the first ring; each snooze moves up one level.
 * Kept in code for v1; later these come from voice packs.
 */
object MomLines {

    const val MAX_LEVEL = 3

    private val lines: List<List<String>> = listOf(
        // 0 – Sweet
        listOf(
            "Good morning beta. Breakfast is ready.",
            "Uth jao beta, the sun is already up.",
            "Rise and shine! Chai is getting ready.",
            "Morning, my sleepyhead. Time to get up.",
        ),
        // 1 – Annoyed
        listOf(
            "Five minutes became ten. Your breakfast is getting cold.",
            "I heard \"five more minutes\" yesterday also.",
            "Beta, I'm not calling again. Up!",
            "Your alarm is ringing. I can hear it from the kitchen.",
        ),
        // 2 – Comparing
        listOf(
            "Sharma ji's son already went for a run.",
            "Your cousin is at office already. And you?",
            "When I was your age, I woke up before the birds.",
            "The neighbours' kids are already at school.",
        ),
        // 3 – Full drama
        listOf(
            "That's it. I'm coming up with a bucket of water!",
            "I am counting to three. One…",
            "Fine, sleep. I'll tell your father.",
            "Last warning. After this, no more breakfast for anyone!",
        ),
    )

    fun lineFor(level: Int, seed: Long = System.currentTimeMillis()): String {
        val pool = lines[level.coerceIn(0, MAX_LEVEL)]
        return pool[((seed / 1000) % pool.size).toInt()]
    }

    /** The conversation so far: one line per level reached, oldest first. */
    fun conversation(level: Int, seed: Long): List<String> =
        (0..level.coerceIn(0, MAX_LEVEL)).map { lineFor(it, seed + it * 1000L) }

    fun moodLabel(level: Int): String = when (level.coerceIn(0, MAX_LEVEL)) {
        0 -> "Sweet"
        1 -> "Annoyed"
        2 -> "Comparing you"
        else -> "Full drama"
    }
}
