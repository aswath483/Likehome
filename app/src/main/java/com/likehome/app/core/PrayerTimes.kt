package com.likehome.app.core

import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Fajr, sunrise and Maghrib for Ramadan mode, worked out on the phone with the standard
 * astronomical method (as in praytimes.org), so it needs no internet. Each country's
 * usual Fajr angle is used; Maghrib is sunset.
 */
object PrayerTimes {

    data class Day(val fajr: LocalTime, val sunrise: LocalTime, val maghrib: LocalTime)

    /** Fajr angle below the horizon, by the convention most used in each country. */
    fun fajrAngle(country: String): Double = when (country.uppercase()) {
        "SA", "AE", "QA", "KW", "BH", "OM", "YE" -> 18.5 // Umm al-Qura
        "ID", "MY", "BN", "SG" -> 20.0 // Kemenag / JAKIM / MUIS
        "EG", "SD", "LY", "SY", "LB", "IQ", "JO", "PS" -> 19.5 // Egyptian General Authority
        "US", "CA" -> 15.0 // ISNA
        "PK", "IN", "BD", "AF" -> 18.0 // Karachi
        "TR" -> 18.0 // Diyanet
        "MA", "DZ", "TN" -> 18.0
        else -> 18.0 // Muslim World League
    }

    /**
     * Times for [date] at [lat]/[lon] in a zone [utcOffsetHours] from UTC. Near the poles, where
     * the sun never gets low enough, Fajr falls back to 90 minutes before sunrise.
     */
    fun compute(date: LocalDate, lat: Double, lon: Double, utcOffsetHours: Double, fajrAngle: Double): Day? {
        val jd = julian(date) - lon / (15 * 24.0)
        fun hours(t: Double) = t + utcOffsetHours - lon / 15.0
        // Two passes: the second uses the first's times to refine the sun's position.
        var sunrise = angleTime(jd, 0.833, 6 / 24.0, lat, ccw = true)
        var sunset = angleTime(jd, 0.833, 18 / 24.0, lat, ccw = false)
        var fajr = angleTime(jd, fajrAngle, 5 / 24.0, lat, ccw = true)
        sunrise = angleTime(jd, 0.833, sunrise / 24.0, lat, ccw = true)
        sunset = angleTime(jd, 0.833, sunset / 24.0, lat, ccw = false)
        if (fajr.isFinite()) fajr = angleTime(jd, fajrAngle, fajr / 24.0, lat, ccw = true)
        if (!sunrise.isFinite() || !sunset.isFinite()) return null
        val fajrHours = if (fajr.isFinite()) hours(fajr) else hours(sunrise) - 1.5
        return Day(toTime(fajrHours), toTime(hours(sunrise)), toTime(hours(sunset)))
    }

    private fun julian(date: LocalDate): Double {
        var y = date.year
        var m = date.monthValue
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + date.dayOfMonth + b - 1524.5
    }

    private fun rad(d: Double) = Math.toRadians(d)
    private fun deg(r: Double) = Math.toDegrees(r)
    private fun fix(a: Double, b: Double): Double { val r = a - b * floor(a / b); return if (r < 0) r + b else r }

    /** Sun's declination and the equation of time, for Julian day [jd]. */
    private fun sun(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fix(357.529 + 0.98560028 * d, 360.0)
        val q = fix(280.459 + 0.98564736 * d, 360.0)
        val l = fix(q + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2 * g)), 360.0)
        val e = 23.439 - 0.00000036 * d
        val ra = fix(deg(atan2(cos(rad(e)) * sin(rad(l)), cos(rad(l)))) / 15.0, 24.0)
        val decl = deg(asin(sin(rad(e)) * sin(rad(l))))
        val eqt = q / 15.0 - ra
        return decl to eqt
    }

    /** Hours (local solar, before zone and longitude) when the sun is [angle] below the horizon. */
    private fun angleTime(jd: Double, angle: Double, guess: Double, lat: Double, ccw: Boolean): Double {
        val (decl, eqt) = sun(jd + guess)
        val noon = fix(12 - eqt, 24.0)
        val cosT = (-sin(rad(angle)) - sin(rad(decl)) * sin(rad(lat))) / (cos(rad(decl)) * cos(rad(lat)))
        if (abs(cosT) > 1) return Double.NaN
        val t = deg(acos(cosT)) / 15.0
        return noon + if (ccw) -t else t
    }

    private fun toTime(hours: Double): LocalTime {
        val minutes = Math.round(fix(hours, 24.0) * 60).toInt() % (24 * 60)
        return LocalTime.of(minutes / 60, minutes % 60)
    }
}

/** Whether a day falls in Ramadan, by the Umm al-Qura calendar built into Java. */
object RamadanCalendar {
    fun isRamadan(date: LocalDate): Boolean =
        runCatching { HijrahDate.from(date).get(ChronoField.MONTH_OF_YEAR) == 9 }.getOrDefault(false)

    /** Lead times before Fajr offered for the suhoor alarm, in minutes. */
    val leadChoices = listOf(30, 45, 60, 90)
}
