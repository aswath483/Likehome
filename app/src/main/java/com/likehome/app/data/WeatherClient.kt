package com.likehome.app.data

import android.content.Context
import com.likehome.app.core.Briefing
import com.likehome.app.core.Weather
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/** Today's weather from Open-Meteo (free, no account), kept for the day once fetched. */
object WeatherClient {

    /** Blocking: call off the main thread. Null offline or without a saved location. */
    fun today(context: Context): Weather? {
        val s = SettingsRepository.get(context).current
        val lat = s.latitude ?: return null
        val lon = s.longitude ?: return null
        val prefs = context.applicationContext.getSharedPreferences("weather", Context.MODE_PRIVATE)
        val key = "${LocalDate.now()}:$lat,$lon"
        if (prefs.getString("key", null) == key) Briefing.parseWeather(prefs.getString("json", null))?.let { return it }
        val json = runCatching {
            val c = URL(Briefing.weatherUrl(lat, lon)).openConnection() as HttpURLConnection
            c.connectTimeout = 8_000
            c.readTimeout = 8_000
            try { if (c.responseCode == 200) c.inputStream.bufferedReader().use { it.readText() } else null } finally { c.disconnect() }
        }.getOrNull() ?: return null
        prefs.edit().putString("key", key).putString("json", json).apply()
        return Briefing.parseWeather(json)
    }
}
