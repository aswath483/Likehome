package com.likehome.app.core

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** What kind of day it is, for Mom's line and the icon. */
enum class DayKind { EXAM, INTERVIEW, BIRTHDAY, TRIP, OTHER }

/**
 * A day the user told Mom about: an exam, an interview, someone's birthday. [yearly] days
 * (birthdays, anniversaries) come back every year on the same date.
 */
data class ImportantDay(
    val id: Long,
    val date: LocalDate,
    val kind: DayKind,
    val title: String,
    val yearly: Boolean = false,
) {
    fun isOn(day: LocalDate): Boolean = if (yearly) {
        // A 29 February birthday is celebrated on 28 February in other years.
        val leapFallback = date.monthValue == 2 && date.dayOfMonth == 29 && !day.isLeapYear &&
            day.monthValue == 2 && day.dayOfMonth == 28
        (day.monthValue == date.monthValue && day.dayOfMonth == date.dayOfMonth) || leapFallback
    } else {
        day == date
    }

    /** The next date this day falls on, from [from]; null for a one-time day already past. */
    fun nextFrom(from: LocalDate): LocalDate? {
        if (!yearly) return date.takeIf { !it.isBefore(from) }
        var candidate = date.withYear(from.year)
        if (candidate.isBefore(from)) candidate = date.withYear(from.year + 1)
        return candidate
    }
}

object ImportantDays {
    fun today(days: List<ImportantDay>, day: LocalDate): ImportantDay? =
        days.filter { it.isOn(day) }.minByOrNull { it.kind.ordinal }

    /** Upcoming days in date order; past one-time days are left out. */
    fun upcoming(days: List<ImportantDay>, from: LocalDate): List<Pair<ImportantDay, LocalDate>> =
        days.mapNotNull { d -> d.nextFrom(from)?.let { d to it } }.sortedBy { it.second }

    fun encode(days: List<ImportantDay>): String = JSONArray(days.map {
        JSONObject().put("id", it.id).put("date", it.date.toString()).put("kind", it.kind.name)
            .put("title", it.title).put("yearly", it.yearly)
    }).toString()

    fun decode(json: String?): List<ImportantDay> = runCatching {
        val a = JSONArray(json ?: return emptyList())
        (0 until a.length()).mapNotNull { i ->
            val o = a.getJSONObject(i)
            val date = runCatching { LocalDate.parse(o.getString("date")) }.getOrNull() ?: return@mapNotNull null
            ImportantDay(
                id = o.optLong("id"),
                date = date,
                kind = DayKind.entries.firstOrNull { it.name == o.optString("kind") } ?: DayKind.OTHER,
                title = o.optString("title").take(40),
                yearly = o.optBoolean("yearly", false),
            )
        }
    }.getOrDefault(emptyList())
}

/** The day's weather for Mom's briefing, from Open-Meteo's daily forecast. */
data class Weather(val maxC: Int, val minC: Int, val rainChance: Int, val code: Int)

/** What Mom says about the weather. */
enum class WeatherAdvice { STORM, SNOW, RAIN, HOT, COLD, LOVELY }

object Briefing {

    fun weatherUrl(lat: Double, lon: Double) =
        "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
            "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code&timezone=auto&forecast_days=1"

    fun parseWeather(json: String?): Weather? = runCatching {
        val daily = JSONObject(json ?: return null).getJSONObject("daily")
        fun first(key: String) = daily.getJSONArray(key).let { if (it.isNull(0)) null else it.getDouble(0) }
        Weather(
            maxC = Math.round(first("temperature_2m_max") ?: return null).toInt(),
            minC = Math.round(first("temperature_2m_min") ?: return null).toInt(),
            rainChance = Math.round(first("precipitation_probability_max") ?: 0.0).toInt(),
            code = (first("weather_code") ?: 0.0).toInt(),
        )
    }.getOrNull()

    /** The most important thing to say about [w] (WMO weather codes). */
    fun advice(w: Weather): WeatherAdvice = when {
        w.code in 95..99 -> WeatherAdvice.STORM
        w.code in 71..77 || w.code in 85..86 -> WeatherAdvice.SNOW
        w.rainChance >= 50 || w.code in 51..67 || w.code in 80..82 -> WeatherAdvice.RAIN
        w.maxC >= 33 -> WeatherAdvice.HOT
        w.maxC <= 8 -> WeatherAdvice.COLD
        else -> WeatherAdvice.LOVELY
    }
}
