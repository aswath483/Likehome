package com.likehome.app.core

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Gentle wake: for a few minutes before Mom calls, the screen brightens like a sunrise and
 * soft chimes rise from silence. The user can get up early; then the main ring stays quiet.
 */
object GentleWake {
    val choices = listOf(0, 5, 10, 15) // minutes before the alarm; 0 is off

    /** When the sunrise starts for a ring at [ringAt], or null if that's already past. */
    fun startAt(ringAt: Long, minutes: Int, now: Long): Long? {
        if (minutes <= 0) return null
        val start = ringAt - minutes * 60_000L
        return start.takeIf { it > now }
    }

    /** How far through the sunrise we are, 0..1. */
    fun progress(elapsedMs: Long, minutes: Int): Float =
        if (minutes <= 0) 1f else (elapsedMs.toFloat() / (minutes * 60_000L)).coerceIn(0f, 1f)

    /** Chime volume: starts nearly silent and eases up to half volume, never a jolt. */
    fun volume(progress: Float): Float = 0.03f + 0.47f * progress * progress

    /** Screen brightness, 0..1: dim at first, full daylight by the time Mom calls. */
    fun brightness(progress: Float): Float = 0.02f + 0.98f * progress

    /**
     * True when the main ring at [now] should stay quiet because the user already got up
     * during the sunrise for a ring planned at [plannedRingAt].
     */
    fun alreadyUp(plannedRingAt: Long, now: Long): Boolean =
        plannedRingAt > 0 && kotlin.math.abs(now - plannedRingAt) < 5 * 60_000L
}

/**
 * Soft wind chimes: pentatonic notes with a slow decay, a new one every second or two.
 * Made in code like the sleep sounds, so the app ships no audio files. Output is in -1..1.
 */
class ChimeGen(private val sampleRate: Int = 44_100, seed: Int = 1) {
    private val random = Random(seed)
    // C major pentatonic, two octaves up from middle C: bright but never shrill.
    private val notes = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 880.0, 1046.5)
    private val freq = DoubleArray(VOICES)
    private val age = LongArray(VOICES) { Long.MAX_VALUE / 2 }
    private var next = 0
    private var untilNext = 0

    fun fill(out: FloatArray) {
        for (i in out.indices) {
            if (untilNext-- <= 0) strike()
            var s = 0.0
            for (v in 0 until VOICES) {
                val t = age[v].toDouble() / sampleRate
                if (t < DECAY_S * 6) {
                    s += sin(2 * PI * freq[v] * t) * exp(-t / DECAY_S) * 0.22
                    age[v]++
                }
            }
            out[i] = s.coerceIn(-1.0, 1.0).toFloat()
        }
    }

    private fun strike() {
        freq[next] = notes[random.nextInt(notes.size)]
        age[next] = 0
        next = (next + 1) % VOICES
        untilNext = (sampleRate * (0.9 + random.nextDouble() * 1.4)).toInt()
    }

    private companion object {
        const val VOICES = 4
        const val DECAY_S = 1.2
    }
}
