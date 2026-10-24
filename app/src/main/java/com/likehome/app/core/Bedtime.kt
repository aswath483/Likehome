package com.likehome.app.core

import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

object Bedtime {
    /** Times offered in Settings, 10 pm to midnight. */
    val choices: List<LocalTime> = listOf(
        LocalTime.of(22, 0), LocalTime.of(22, 30), LocalTime.of(23, 0), LocalTime.of(23, 30), LocalTime.MIDNIGHT,
    )

    /** The next reminder at [time], strictly after [now]. */
    fun next(now: ZonedDateTime, time: LocalTime): ZonedDateTime {
        val today = now.with(time).withSecond(0).withNano(0)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    /** Mention the alarm only when it is the coming morning's, not one days away. */
    fun mentionsAlarm(now: ZonedDateTime, alarmAt: ZonedDateTime?): Boolean =
        alarmAt != null && alarmAt.isAfter(now) && Duration.between(now, alarmAt) <= Duration.ofHours(14)
}
