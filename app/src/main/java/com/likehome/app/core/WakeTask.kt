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
