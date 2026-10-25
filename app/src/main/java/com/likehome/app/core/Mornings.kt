package com.likehome.app.core

import java.time.LocalDate
import java.time.LocalTime

/** One morning the user got up: how many snoozes it took and when they were up. */
data class Morning(val date: LocalDate, val snoozes: Int, val wokeAt: LocalTime)

/** Badges: up before 6:30 with no snooze; 7 mornings in a row; snoozed to the limit. */
enum class Badge { EARLY_BIRD, MOMS_FAVOURITE, SNOOZE_KING }

/** What Mom thinks of the user's last month. */
enum class Verdict { GOOD_CHILD, TRYING, SHARMA_JI }

/** Which report card one morning gets; the copy is in string resources. */
enum class ReportKind { PROUD_STREAK, PROUD, ONCE, TWICE, DISAPPOINTED }

data class Report(val kind: ReportKind, val snoozes: Int, val streak: Int)

object Mornings {

    private val EARLY = LocalTime.of(6, 30)
    private const val FAVOURITE_STREAK = 7

    /** The morning of each date: when several alarms rang, the worst one counts. */
    private fun byDate(history: List<Morning>): Map<LocalDate, Morning> =
        history.groupBy { it.date }.mapValues { (_, list) -> list.maxBy { it.snoozes } }

    /**
     * No-snooze streak: alarm mornings in a row without a snooze, newest first. Days with
     * no alarm (a free Sunday) neither count nor break it.
     */
    fun streak(history: List<Morning>): Int =
        byDate(history).values.sortedByDescending { it.date }.takeWhile { it.snoozes == 0 }.size

    /** The longest no-snooze run ever. */
    fun bestStreak(history: List<Morning>): Int {
        var best = 0
        var run = 0
        byDate(history).values.sortedBy { it.date }.forEach { m ->
            run = if (m.snoozes == 0) run + 1 else 0
            best = maxOf(best, run)
        }
        return best
    }

    /** The streak as it stood on [date], counting that morning. */
    fun streakOn(history: List<Morning>, date: LocalDate): Int = streak(history.filter { !it.date.isAfter(date) })

    fun averageSnoozes(history: List<Morning>): Double {
        val days = byDate(history).values
        return if (days.isEmpty()) 0.0 else days.sumOf { it.snoozes }.toDouble() / days.size
    }

    /** The morning recorded for each date (worst alarm of the day), newest first. */
    fun days(history: List<Morning>): List<Morning> = byDate(history).values.sortedByDescending { it.date }

    fun noSnoozeMornings(history: List<Morning>): Int = byDate(history).values.count { it.snoozes == 0 }

    fun badges(history: List<Morning>): Set<Badge> {
        val days = byDate(history).values.sortedBy { it.date }
        val out = mutableSetOf<Badge>()
        if (days.any { it.snoozes == 0 && it.wokeAt.isBefore(EARLY) }) out += Badge.EARLY_BIRD
        if (days.any { it.snoozes >= MAX_SNOOZES }) out += Badge.SNOOZE_KING
        var run = 0
        days.forEach { m ->
            run = if (m.snoozes == 0) run + 1 else 0
            if (run >= FAVOURITE_STREAK) out += Badge.MOMS_FAVOURITE
        }
        return out
    }

    /** Badges [after] has that [before] did not: shown as "new" on the report card. */
    fun newBadges(before: List<Morning>, after: List<Morning>): Set<Badge> = badges(after) - badges(before)

    /** Mom's verdict on the 30 days up to [today], or null with fewer than 3 mornings. */
    fun verdict(history: List<Morning>, today: LocalDate): Verdict? {
        val month = byDate(history).values.filter { !it.date.isAfter(today) && it.date.isAfter(today.minusDays(30)) }
        if (month.size < 3) return null
        val average = month.sumOf { it.snoozes }.toDouble() / month.size
        return when {
            average < 0.5 -> Verdict.GOOD_CHILD
            average < 1.5 -> Verdict.TRYING
            else -> Verdict.SHARMA_JI
        }
    }

    /** Snoozes for each of the 7 days ending [today], oldest first; null = no alarm that day. */
    fun week(history: List<Morning>, today: LocalDate): List<Int?> {
        val days = byDate(history)
        return (6 downTo 0).map { days[today.minusDays(it.toLong())]?.snoozes }
    }

    fun report(snoozes: Int, streak: Int): Report = Report(
        kind = when {
            snoozes == 0 && streak >= 2 -> ReportKind.PROUD_STREAK
            snoozes == 0 -> ReportKind.PROUD
            snoozes == 1 -> ReportKind.ONCE
            snoozes == 2 -> ReportKind.TWICE
            else -> ReportKind.DISAPPOINTED
        },
        snoozes = snoozes,
        streak = streak,
    )

    private const val MAX_SNOOZES = 3
}
