package com.likehome.app.core

import com.likehome.app.mom.Language
import com.likehome.app.mom.LinePacks

/**
 * Shared text clean-up: lower case, letters and digits only, single spaces. Vowel signs
 * and other combining marks stay, or Hindi "जाग" would fall apart into "ज ग".
 */
internal fun normalise(text: String): String =
    text.lowercase()
        .map { if (it.isLetterOrDigit() || it == ' ' || it.isMark()) it else ' ' }
        .joinToString("")
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ")

/** The typed promise: a sentence to copy exactly (case and punctuation don't matter). */
object Promise {
    private val english = listOf(
        "I promise I will not snooze tomorrow",
        "I am awake and I am getting out of bed",
        "Sharma ji's son is not better than me",
        "I will drink water and eat my breakfast",
        "Today I wake up on the first call",
    )
    private val hindi = listOf(
        "मैं वादा करता हूँ कल स्नूज़ नहीं करूँगा",
        "मैं जाग गया हूँ और बिस्तर से उठ रहा हूँ",
        "आज मैं पहली आवाज़ पर उठूँगा",
    )

    fun sentence(seed: Long, language: Language): String {
        val pool = LinePacks.forLanguage(language)?.promises ?: if (language == Language.HINDI) hindi else english
        return pool[Math.floorMod(seed / 60_000, pool.size.toLong()).toInt()]
    }

    fun matches(typed: String, target: String): Boolean = normalise(typed) == normalise(target)
}

/** The spoken check: did the recogniser hear the user say they're awake? */
object VoiceCheck {
    /** What the user is asked to say. */
    fun phrase(name: String, language: Language): String {
        LinePacks.forLanguage(language)?.let { return it.awakePhrase.format(name) }
        return if (language == Language.HINDI) "मैं जाग गया, $name!" else "I'm awake, $name!"
    }

    /** Every language's "awake" words: the phone's recogniser may answer in any of them. */
    private val awakeWords: Set<String> by lazy {
        setOf("awake", "wake", "woke", "up", "jag", "jaag", "जाग", "जागा", "जागी", "uth", "utha", "uthi", "उठ") +
            LinePacks.all.flatMap { pack -> pack.awakeWords.map(::normalise) }
    }

    /**
     * Speech recognisers mishear names and contractions, so this accepts any of the
     * recogniser's guesses that contains an "awake" word and at least one more word.
     */
    fun matches(guesses: List<String>): Boolean = guesses.any { guess ->
        val words = normalise(guess).split(' ').filter { it.isNotBlank() }
        words.size >= 2 && words.any { it in awakeWords }
    }
}

private fun Char.isMark(): Boolean =
    category == CharCategory.NON_SPACING_MARK || category == CharCategory.COMBINING_SPACING_MARK

/**
 * The photo check compares a morning photo with the reference the user took when setting
 * the alarm, using a difference hash (shape) and a colour histogram (colours). Both come
 * from tiny downscaled copies, so lighting and small angle changes still match.
 */
object ImageMatch {
    const val HASH_W = 9
    const val HASH_H = 8
    const val THRESHOLD = 0.66f

    /** 64-bit difference hash from a 9×8 grey image (0..255), row by row. */
    fun dHash(grey: IntArray): Long {
        require(grey.size == HASH_W * HASH_H)
        var hash = 0L
        var bit = 0
        for (y in 0 until HASH_H) for (x in 0 until HASH_W - 1) {
            if (grey[y * HASH_W + x] > grey[y * HASH_W + x + 1]) hash = hash or (1L shl bit)
            bit++
        }
        return hash
    }

    fun hamming(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

    /** 4×4×4 RGB histogram of ARGB pixels, summing to 1. */
    fun histogram(argb: IntArray): FloatArray {
        val bins = FloatArray(64)
        argb.forEach { c ->
            val r = (c shr 16 and 0xFF) / 64
            val g = (c shr 8 and 0xFF) / 64
            val b = (c and 0xFF) / 64
            bins[r * 16 + g * 4 + b] += 1f
        }
        val total = argb.size.coerceAtLeast(1).toFloat()
        for (i in bins.indices) bins[i] /= total
        return bins
    }

    fun intersection(a: FloatArray, b: FloatArray): Float = a.indices.sumOf { minOf(a[it], b[it]).toDouble() }.toFloat()

    /** 0 (nothing alike) to 1 (same picture). */
    fun similarity(hashA: Long, histA: FloatArray, hashB: Long, histB: FloatArray): Float =
        0.6f * (1f - hamming(hashA, hashB) / 64f) + 0.4f * intersection(histA, histB)
}
