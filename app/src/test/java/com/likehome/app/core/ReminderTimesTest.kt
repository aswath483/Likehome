package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderTimesTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private fun at(day: Int, h: Int, m: Int) = ZonedDateTime.of(2026, 10, day, h, m, 0, 0, zone)

    @Test
    fun waterEveryTwoHoursThenTomorrowMorning() {
        assertEquals(at(24, 9, 0), ReminderTimes.nextWater(at(24, 6, 0)))
        assertEquals(at(24, 13, 0), ReminderTimes.nextWater(at(24, 11, 0)))
        assertEquals(at(25, 9, 0), ReminderTimes.nextWater(at(24, 21, 30)))
    }

    @Test
    fun breakfastIsDaily() {
        assertEquals(at(25, 8, 30), ReminderTimes.nextDaily(at(24, 9, 0), LocalTime.of(8, 30)))
    }
}
