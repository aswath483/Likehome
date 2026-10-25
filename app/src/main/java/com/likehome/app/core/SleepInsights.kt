package com.likehome.app.core

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** What Mom says about the user's sleep; the copy is in string resources. */
enum class SleepTip { SHORT_NIGHTS, IRREGULAR, BAD_WEEKDAY, IMPROVING, GREAT, NEED_MORE }

/**
 * Sleep insights from the last [WINDOW_DAYS] days: when the user goes to bed (opening the
 * bedside clock, starting sleep sounds, "Going to sleep" on the bedtime reminder) and when
 * they got up (the wake-up history).
 */
data class SleepSummary(
    val mornings: Int,
    val averageWake: LocalTime?,
    /** How much the wake-up time wanders, in minutes (standard deviation). */
    val wakeSpread: Int?,
    /** Average hours of sleep on nights with a bedtime; null without any. */
    val averageSleepMinutes: Int?,
    val nightsWithBedtime: Int,
    /** The weekday with the most snoozes on average, if one clearly stands out. */
    val worstDay: DayOfWeek?,
    /** Average snoozes this week minus the week before; negative is better. */
    val snoozeTrend: Double?,
    val tip: SleepTip,
)

object SleepInsights {
    const val WINDOW_DAYS = 28
    private const val MIN_MORNINGS = 5
    private const val SHORT_NIGHT_MINUTES = 7 * 60
    private const val IRREGULAR_MINUTES = 45

    /**
     * The night that ends on [date]'s morning: a bedtime after noon the day before up to the
     * wake-up time. Bedtimes in [bedtimes] are local date-times.
     */
    fun sleepMinutes(bedtimes: List<LocalDateTime>, morning: Morning): Int? {
        val wake = morning.date.atTime(morning.wokeAt)
        val from = morning.date.minusDays(1).atTime(LocalTime.NOON)
        val bed = bedtimes.filter { it.isAfter(from) && it.isBefore(wake) }.maxOrNull() ?: return null
        val minutes = Duration.between(bed, wake).toMinutes().toInt()
        // Under an hour is a nap or a reopened clock, not a night; over 14 hours is a lost record.
        return minutes.takeIf { it in 60..14 * 60 }
    }

    fun summary(history: List<Morning>, bedtimes: List<LocalDateTime>, today: LocalDate): SleepSummary {
        val days = Mornings.days(history).filter { !it.date.isAfter(today) && it.date.isAfter(today.minusDays(WINDOW_DAYS.toLong())) }
        if (days.size < MIN_MORNINGS) {
            return SleepSummary(days.size, null, null, null, 0, null, null, SleepTip.NEED_MORE)
        }
        val wakeMinutes = days.map { it.wokeAt.toSecondOfDay() / 60.0 }
        val mean = wakeMinutes.average()
        val spread = sqrt(wakeMinutes.sumOf { (it - mean) * (it - mean) } / wakeMinutes.size).roundToInt()
        val sleeps = days.mapNotNull { sleepMinutes(bedtimes, it) }
        val averageSleep = sleeps.takeIf { it.size >= 3 }?.average()?.roundToInt()

        val byWeekday = days.groupBy { it.date.dayOfWeek }.filterValues { it.size >= 2 }
            .mapValues { (_, list) -> list.sumOf { it.snoozes }.toDouble() / list.size }
        val overall = days.sumOf { it.snoozes }.toDouble() / days.size
        val worst = byWeekday.maxByOrNull { it.value }?.takeIf { it.value >= 1.0 && it.value >= overall + 0.75 }?.key

        fun avgSnoozes(fromDaysAgo: Long, toDaysAgo: Long) = days
            .filter { it.date.isAfter(today.minusDays(fromDaysAgo)) && !it.date.isAfter(today.minusDays(toDaysAgo)) }
            .takeIf { it.size >= 3 }?.let { list -> list.sumOf { it.snoozes }.toDouble() / list.size }
        val thisWeek = avgSnoozes(7, 0)
        val lastWeek = avgSnoozes(14, 7)
        val trend = if (thisWeek != null && lastWeek != null) thisWeek - lastWeek else null

        val tip = when {
            averageSleep != null && averageSleep < SHORT_NIGHT_MINUTES -> SleepTip.SHORT_NIGHTS
            spread > IRREGULAR_MINUTES -> SleepTip.IRREGULAR
            worst != null -> SleepTip.BAD_WEEKDAY
            trend != null && trend <= -0.5 -> SleepTip.IMPROVING
            overall < 0.5 -> SleepTip.GREAT
            trend != null && trend < 0 -> SleepTip.IMPROVING
            else -> SleepTip.IRREGULAR.takeIf { spread > IRREGULAR_MINUTES / 2 } ?: SleepTip.GREAT
        }
        return SleepSummary(
            mornings = days.size,
            averageWake = LocalTime.ofSecondOfDay((mean * 60).roundToInt().toLong().coerceIn(0, 86_399)),
            wakeSpread = spread,
            averageSleepMinutes = averageSleep,
            nightsWithBedtime = sleeps.size,
            worstDay = worst,
            snoozeTrend = trend,
            tip = tip,
        )
    }

    /** Bedtimes are kept as one ISO date-time per line; only the last [WINDOW_DAYS] days. */
    fun encode(bedtimes: List<LocalDateTime>): String = bedtimes.joinToString("\n")

    fun decode(text: String?): List<LocalDateTime> =
        text.orEmpty().lines().mapNotNull { runCatching { LocalDateTime.parse(it.trim()) }.getOrNull() }

    /** Adds [at], dropping records older than the window and repeats within half an hour. */
    fun add(bedtimes: List<LocalDateTime>, at: LocalDateTime): List<LocalDateTime> {
        val kept = bedtimes.filter {
            it.isAfter(at.minusDays(WINDOW_DAYS + 1L)) && Duration.between(it, at).abs().toMinutes() >= 30
        }
        return (kept + at).sorted()
    }
}

/** The bedside clock's slow drift, so the same pixels aren't lit all night. */
object BedsideClock {
    private const val STEPS = 8
    private const val REACH = 0.06f

    /**
     * Where the clock sits this minute, as fractions of the screen (x, y), within ±[REACH].
     * It walks a small loop, one step a minute, so it never jumps far.
     */
    fun offset(now: LocalDateTime): Pair<Float, Float> {
        val step = (now.hour * 60 + now.minute) % STEPS
        val angle = 2 * Math.PI * step / STEPS
        return Pair((kotlin.math.cos(angle) * REACH).toFloat(), (kotlin.math.sin(angle) * REACH).toFloat())
    }
}
