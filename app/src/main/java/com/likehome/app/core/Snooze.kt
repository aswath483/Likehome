package com.likehome.app.core

import java.time.DayOfWeek
import java.time.LocalDate

object Snooze {
    const val MINUTES = 5
    const val WEEKEND_MINUTES = 10

    /** Weekend mode: Mom lets you sleep a little longer on Saturday and Sunday. */
    fun minutes(weekendMode: Boolean, date: LocalDate): Int {
        val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        return if (weekendMode && weekend) WEEKEND_MINUTES else MINUTES
    }
}
