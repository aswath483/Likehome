package com.likehome.app.core

import java.time.LocalTime
import java.time.ZonedDateTime

/** Mom's daytime nudges, besides the alarm itself. */
enum class ReminderKind(val title: String) {
    BEDTIME("Bedtime"),
    WATER("Water"),
    BREAKFAST("Breakfast"),
}

object ReminderTimes {
    /** Water every two hours through the waking day. */
    val waterHours = listOf(9, 11, 13, 15, 17, 19, 21)

    /** Breakfast times offered in Settings. */
    val breakfastChoices: List<LocalTime> = listOf(LocalTime.of(7, 30), LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30))

    fun nextWater(now: ZonedDateTime): ZonedDateTime {
        val today = waterHours.map { now.with(LocalTime.of(it, 0)).withSecond(0).withNano(0) }.firstOrNull { it.isAfter(now) }
        return today ?: now.plusDays(1).with(LocalTime.of(waterHours.first(), 0)).withSecond(0).withNano(0)
    }

    /** The next time a daily reminder at [time] is due, strictly after [now]. */
    fun nextDaily(now: ZonedDateTime, time: LocalTime): ZonedDateTime = Bedtime.next(now, time)
}
