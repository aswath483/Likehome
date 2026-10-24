package com.likehome.app.core

import java.time.LocalDate
import java.time.LocalTime

/** One morning the user got up: how many snoozes it took and when they were up. */
data class Morning(val date: LocalDate, val snoozes: Int, val wokeAt: LocalTime)

enum class Badge(val title: String, val detail: String) {
    EARLY_BIRD("Early Bird", "Up before 6:30 with no snooze"),
    MOMS_FAVOURITE("Mom's Favourite", "7 alarm mornings in a row, no snooze"),
    SNOOZE_KING("Snooze King", "Snoozed until Mom took the snooze away"),
}

/** What Mom thinks of the user's last month. */
enum class Verdict(val title: String, val detail: String) {
    GOOD_CHILD("Good child", "Mom is proud. She told the whole family."),
    TRYING("Trying, at least", "Mom sees the effort. She wants more."),
    SHARMA_JI("Sharma ji's son is better", "Mom has stopped comparing. Almost."),
}

/** Report card copy for one morning, by snooze count. */
data class Report(val title: String, val body: String)

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

    fun report(snoozes: Int, streak: Int): Report = when {
        snoozes == 0 && streak >= 2 -> Report("Good child!", "No snoozes, $streak days in a row. Mom is telling everyone.")
        snoozes == 0 -> Report("Good child!", "No snoozes. Mom is proud. Breakfast is waiting.")
        snoozes == 1 -> Report("Okay, okay.", "Snoozed once. Mom is mostly happy.")
        snoozes == 2 -> Report("Finally.", "Snoozed twice. Mom will mention this at dinner.")
        else -> Report("Mom is disappointed.", "Snoozed $snoozes times. Sharma ji's son would never.")
    }

    private const val MAX_SNOOZES = 3
}
