package com.likehome.app.core

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** One player's morning in a streak challenge, as stored in the cloud. */
data class ChallengeMorning(val uid: String, val date: LocalDate, val snoozes: Int)

/** A player's place in the challenge: [clean] mornings without a snooze count most. */
data class Standing(val uid: String, val name: String, val clean: Int, val mornings: Int, val streak: Int)

/**
 * Streak challenge with friends: up to [MAX_PLAYERS] people, 7, 14 or 30 days from the
 * same start. Whoever has the most no-snooze mornings when it ends wins; each player's
 * phone reports its own mornings, and everyone sees everyone's.
 */
object Challenge {
    val lengths = listOf(7, 14, 30)
    const val MAX_PLAYERS = 5
    const val NAME_MAX = 20

    fun lastDay(start: LocalDate, length: Int): LocalDate = start.plusDays(length - 1L)

    fun isOver(start: LocalDate, length: Int, today: LocalDate) = today.isAfter(lastDay(start, length))

    /** Days still to play, counting today. */
    fun daysLeft(start: LocalDate, length: Int, today: LocalDate): Int = when {
        today.isBefore(start) -> length
        isOver(start, length, today) -> 0
        else -> ChronoUnit.DAYS.between(today, lastDay(start, length)).toInt() + 1
    }

    fun counts(date: LocalDate, start: LocalDate, length: Int): Boolean =
        !date.isBefore(start) && !date.isAfter(lastDay(start, length))

    /** The cloud document id for [uid]'s morning on [date]: one per player per day. */
    fun morningId(uid: String, date: LocalDate) = "${uid}_$date"

    fun cleanName(input: String): String = input.trim().replace(Regex("\\s+"), " ").take(NAME_MAX)

    /**
     * Everyone's standing, best first: most no-snooze mornings, then the longer current
     * streak, then more mornings played. Mornings outside the challenge are ignored, and a
     * day with several alarms counts its worst one, as in the history.
     */
    fun standings(
        names: Map<String, String>,
        mornings: List<ChallengeMorning>,
        start: LocalDate,
        length: Int,
    ): List<Standing> {
        val inside = mornings.filter { it.uid in names && counts(it.date, start, length) }
        return names.map { (uid, name) ->
            val days = inside.filter { it.uid == uid }.groupBy { it.date }
                .mapValues { (_, list) -> list.maxOf { it.snoozes } }
            val streak = days.entries.sortedByDescending { it.key }.takeWhile { it.value == 0 }.size
            Standing(uid, name, clean = days.values.count { it == 0 }, mornings = days.size, streak = streak)
        }.sortedWith(compareByDescending<Standing> { it.clean }.thenByDescending { it.streak }.thenByDescending { it.mornings }.thenBy { it.name })
    }

    /** Everyone tied for first; empty before anyone has a clean morning. */
    fun leaders(standings: List<Standing>): Set<String> {
        val top = standings.firstOrNull()?.takeIf { it.clean > 0 } ?: return emptySet()
        return standings.filter { it.clean == top.clean && it.streak == top.streak && it.mornings == top.mornings }.map { it.uid }.toSet()
    }
}
