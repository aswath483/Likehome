package com.likehome.app.data

import android.content.Context
import android.telephony.TelephonyManager
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.Holiday
import com.likehome.app.core.Holidays
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.Executors

/**
 * The phone's country's public holidays, for alarms set to skip them. Fetched from
 * date.nager.at (free, no account) for this year and next, and kept on the phone so
 * alarms keep skipping holidays offline. Refreshed at most once a month.
 */
object HolidayStore {

    private const val REFRESH_MS = 30L * 24 * 60 * 60 * 1000
    private val io = Executors.newSingleThreadExecutor()
    @Volatile private var cache: Pair<String, List<Holiday>>? = null

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("holidays", Context.MODE_PRIVATE)

    /** Where the phone is: the mobile network's country, else the SIM's, else the locale's. */
    fun country(context: Context): String {
        val tm = context.getSystemService(TelephonyManager::class.java)
        return listOf(tm?.networkCountryIso, tm?.simCountryIso, Locale.getDefault().country)
            .firstOrNull { !it.isNullOrBlank() && it.length == 2 }.orEmpty().uppercase()
    }

    fun holidays(context: Context): List<Holiday> {
        val country = country(context)
        cache?.let { (c, list) -> if (c == country) return list }
        val p = prefs(context)
        val year = LocalDate.now().year
        val list = (year..year + 1).flatMap { Holidays.parse(p.getString("$country-$it", null)) }
        cache = country to list
        return list
    }

    fun dates(context: Context): Set<LocalDate> = holidays(context).map { it.date }.toSet()

    /** The next holiday from today, for "Skips Independence Day, 15 Aug". */
    fun next(context: Context, from: LocalDate = LocalDate.now()): Holiday? =
        holidays(context).filter { !it.date.isBefore(from) }.minByOrNull { it.date }

    /** Fetches missing or month-old years in the background, then re-arms alarms. */
    fun refresh(context: Context) {
        val app = context.applicationContext
        io.execute {
            val country = country(app)
            if (country.isEmpty()) return@execute
            val p = prefs(app)
            val year = LocalDate.now().year
            var changed = false
            for (y in year..year + 1) {
                val key = "$country-$y"
                val fresh = System.currentTimeMillis() - p.getLong("$key-at", 0L) < REFRESH_MS
                if (p.contains(key) && fresh) continue
                val body = fetch(Holidays.url(y, country)) ?: continue
                // An unknown country answers with an error object; store it as "none".
                p.edit().putString(key, body).putLong("$key-at", System.currentTimeMillis()).apply()
                changed = true
            }
            if (changed) {
                cache = null
                AlarmScheduler.rescheduleAll(app)
            }
        }
    }

    private fun fetch(url: String): String? = runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        try {
            when (connection.responseCode) {
                200 -> connection.inputStream.bufferedReader().use { it.readText() }
                204, 404 -> "[]"
                else -> null
            }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}
