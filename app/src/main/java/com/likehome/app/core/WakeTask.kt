package com.likehome.app.core

import kotlin.math.sqrt
import kotlin.random.Random

/** What the user must do to stop the alarm; [target] is how many sums, steps or shakes. */
enum class WakeTask(val target: Int) {
    MATH(3),
    STEPS(30),
    SHAKE(20),
    PHOTO(1),
    VOICE(1),
    PROMISE(1),
    MEMORY(5),
    BARCODE(1),
    LIGHT(3),
}

private const val GRAVITY = 9.81f

/**
 * Counts shakes from accelerometer readings (m/s²). A shake is a jolt above
 * [thresholdG]; jolts closer together than [minGapMs] count once.
 */
class ShakeCounter(
    private val thresholdG: Float = 2.2f,
    private val minGapMs: Long = 280,
) {
    var count = 0
        private set
    private var lastShakeAt = Long.MIN_VALUE / 2

    /** Feeds one reading; returns true when it counted as a new shake. */
    fun feed(x: Float, y: Float, z: Float, timeMs: Long): Boolean {
        val g = sqrt(x * x + y * y + z * z) / GRAVITY
        if (g < thresholdG || timeMs - lastShakeAt < minGapMs) return false
        lastShakeAt = timeMs
        count++
        return true
    }
}

/**
 * Step counting from the accelerometer, for phones without a step sensor or when the
 * activity permission is off. A step is a peak above [highMs2] followed by a dip below
 * [lowMs2], at least [minGapMs] after the previous step.
 */
class AccelStepCounter(
    private val highMs2: Float = 11.8f,
    private val lowMs2: Float = 8.8f,
    private val minGapMs: Long = 260,
) {
    var count = 0
        private set
    private var armed = false
    private var lastStepAt = Long.MIN_VALUE / 2

    /** Feeds one reading; returns true when it completed a new step. */
    fun feed(x: Float, y: Float, z: Float, timeMs: Long): Boolean {
        val m = sqrt(x * x + y * y + z * z)
        if (m > highMs2) armed = true
        if (armed && m < lowMs2) {
            armed = false
            if (timeMs - lastStepAt >= minGapMs) {
                lastStepAt = timeMs
                count++
                return true
            }
        }
        return false
    }
}

/** A sum to solve: "a + b". */
data class Sum(val a: Int, val b: Int) {
    val answer: Int get() = a + b
    override fun toString() = "$a + $b"
}

object MathPuzzle {
    /** Two-digit sums that need a moment's thought but no paper. */
    fun sums(count: Int = WakeTask.MATH.target, random: Random = Random.Default): List<Sum> =
        List(count) { Sum(random.nextInt(12, 60), random.nextInt(7, 40)) }
}

/**
 * Mom's memory game: tiles on a 3×3 grid light up one by one and the user taps them back
 * in the same order. No tile lights twice in a row, so every flash is easy to see.
 */
class MemoryRound(val sequence: List<Int>) {
    enum class Result { GOOD, WRONG, DONE }

    var position = 0
        private set

    fun tap(tile: Int): Result {
        if (tile != sequence[position]) {
            position = 0
            return Result.WRONG
        }
        position++
        return if (position == sequence.size) Result.DONE else Result.GOOD
    }

    companion object {
        const val TILES = 9

        fun random(length: Int = WakeTask.MEMORY.target, random: Random = Random.Default): MemoryRound {
            val tiles = mutableListOf<Int>()
            while (tiles.size < length) {
                val next = random.nextInt(TILES)
                if (next != tiles.lastOrNull()) tiles += next
            }
            return MemoryRound(tiles)
        }
    }
}

/**
 * Lights on: the ambient light sensor must read at least [lux] for [holdMs] in a row, so a
 * quick flash of the phone's screen at a lamp doesn't count but switching the room light on does.
 */
class LightCheck(
    private val lux: Float = 120f,
    private val holdMs: Long = WakeTask.LIGHT.target * 1000L,
) {
    private var brightSince = -1L

    /** 0..1 progress towards done; resets when the room goes dark again. */
    fun feed(reading: Float, timeMs: Long): Float {
        if (reading < lux) {
            brightSince = -1L
            return 0f
        }
        if (brightSince < 0) brightSince = timeMs
        return ((timeMs - brightSince).toFloat() / holdMs).coerceAtMost(1f)
    }
}

/** The barcode check: scan the same product (toothpaste, coffee jar) chosen when the alarm was set. */
object BarcodeCheck {
    fun matches(scanned: String?, saved: String): Boolean =
        saved.isNotBlank() && scanned?.trim() == saved.trim()
}
