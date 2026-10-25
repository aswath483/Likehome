package com.likehome.app.core

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Sleep sounds, made from noise in code so the app ships no audio files. */
enum class SleepSound { RAIN, WAVES, FAN, BROWN, WHITE }

/**
 * Fills audio buffers with one [SleepSound], sample by sample, keeping filter state between
 * buffers so playback has no clicks. Output is in -1..1.
 */
class NoiseGen(private val sound: SleepSound, private val sampleRate: Int = 44_100, seed: Int = 1) {
    private val random = Random(seed)
    private var brown = 0.0
    private var low = 0.0
    private val pink = DoubleArray(3)
    private var t = 0L
    private var dropLeft = 0
    private var dropLevel = 0.0

    fun fill(out: FloatArray) {
        for (i in out.indices) out[i] = next().coerceIn(-1.0, 1.0).toFloat()
    }

    private fun white() = random.nextDouble() * 2 - 1

    private fun next(): Double {
        t++
        val w = white()
        return when (sound) {
            SleepSound.WHITE -> w * 0.35
            SleepSound.BROWN -> brownStep(w) * 3.0
            SleepSound.FAN -> {
                low += (w - low) * 0.08
                low * 1.6 + sin(2 * PI * 110 * t / sampleRate) * 0.02
            }
            SleepSound.WAVES -> {
                // Brown noise swelling and falling like waves, about every 9 seconds.
                val swell = 0.25 + 0.75 * (0.5 + 0.5 * sin(2 * PI * t / (sampleRate * 9.0)))
                brownStep(w) * 3.2 * swell
            }
            SleepSound.RAIN -> pinkStep(w) * 0.5 + drop()
        }
    }

    private fun brownStep(w: Double): Double {
        brown = (brown + w * 0.02) * 0.998
        return brown
    }

    /** Paul Kellet's economy pink filter: softer than white, like steady rain. */
    private fun pinkStep(w: Double): Double {
        pink[0] = 0.99765 * pink[0] + w * 0.0990460
        pink[1] = 0.96300 * pink[1] + w * 0.2965164
        pink[2] = 0.57000 * pink[2] + w * 1.0526913
        return (pink[0] + pink[1] + pink[2] + w * 0.1848) * 0.25
    }

    /** Now and then a single heavier drop. */
    private fun drop(): Double {
        if (dropLeft == 0 && random.nextInt(sampleRate / 6) == 0) {
            dropLeft = sampleRate / 200
            dropLevel = 0.15 + random.nextDouble() * 0.2
        }
        if (dropLeft == 0) return 0.0
        dropLeft--
        return white() * dropLevel * dropLeft / (sampleRate / 200.0)
    }
}

/** The sleep timer: how long sounds play, and a gentle fade over the last half minute. */
object SleepTimer {
    val choices = listOf(15, 30, 60, 0) // minutes; 0 plays until stopped
    const val FADE_MS = 30_000L

    /** Volume 0..1 at [elapsedMs] for a timer of [minutes] (0 = no timer). */
    fun volume(elapsedMs: Long, minutes: Int): Float {
        if (minutes <= 0) return 1f
        val total = minutes * 60_000L
        val left = total - elapsedMs
        return when {
            left <= 0 -> 0f
            left >= FADE_MS -> 1f
            else -> left.toFloat() / FADE_MS
        }
    }
}
