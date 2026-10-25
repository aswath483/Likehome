package com.likehome.app.core

import org.json.JSONArray
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/** A public holiday, from the date.nager.at feed. */
data class Holiday(val date: LocalDate, val name: String, val localName: String)

object Holidays {

    /**
     * Parses date.nager.at's `/api/v3/PublicHolidays/{year}/{country}` answer, keeping only
     * nationwide holidays (regional ones would skip alarms for people elsewhere).
     */
    fun parse(json: String?): List<Holiday> = runCatching {
        val array = JSONArray(json ?: return emptyList())
        (0 until array.length()).mapNotNull { i ->
            val o = array.getJSONObject(i)
            val global = o.optBoolean("global", true)
            val date = runCatching { LocalDate.parse(o.getString("date")) }.getOrNull()
            if (!global || date == null) null
            else Holiday(date, o.optString("name"), o.optString("localName").ifBlank { o.optString("name") })
        }.distinctBy { it.date }
    }.getOrDefault(emptyList())

    fun url(year: Int, country: String) = "https://date.nager.at/api/v3/PublicHolidays/$year/${country.uppercase()}"
}

/** When a repeating alarm skips a day: a public holiday it was told to skip, or vacation. */
object Skips {
    fun skipped(date: LocalDate, holidays: Set<LocalDate>, skipHolidays: Boolean, pausedUntil: LocalDate?): Boolean =
        (pausedUntil != null && !date.isAfter(pausedUntil)) || (skipHolidays && date in holidays)
}

/** The next ring of an alarm at [time] on [days] (empty = once), strictly after [now]. */
object NextRing {
    /** A year ahead is enough for any vacation; past that the alarm rings anyway. */
    private const val SEARCH_DAYS = 400

    fun after(
        now: ZonedDateTime,
        time: LocalTime,
        days: Set<DayOfWeek>,
        skip: (LocalDate) -> Boolean = { false },
    ): ZonedDateTime {
        var candidate = now.with(time).withSecond(0).withNano(0)
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
        // A one-time alarm was set for that day on purpose: holidays and vacation don't move it.
        if (days.isEmpty()) return candidate
        val first = candidate
        repeat(SEARCH_DAYS) {
            if (candidate.dayOfWeek in days && !skip(candidate.toLocalDate())) return candidate
            candidate = candidate.plusDays(1)
        }
        return generateSequence(first) { it.plusDays(1) }.first { it.dayOfWeek in days }
    }
}
