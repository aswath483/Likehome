package com.likehome.app.mom

import org.json.JSONArray
import java.time.LocalDate

/**
 * Festival lines delivered through Remote Config, so a new festival (or next year's
 * Diwali date) needs no app update. JSON: a list of
 * `{"date":"2027-10-29","english":"…","hinglish":"…","hindi":"…"}`.
 */
object RemoteFestivals {

    fun parse(json: String?): Map<LocalDate, Map<Language, String>> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val list = JSONArray(json)
            (0 until list.length()).mapNotNull { i ->
                val o = list.getJSONObject(i)
                val date = runCatching { LocalDate.parse(o.getString("date")) }.getOrNull() ?: return@mapNotNull null
                val lines = Language.entries.mapNotNull { lang ->
                    o.optString(lang.name.lowercase()).takeIf { it.isNotBlank() }?.let { lang to it }
                }.toMap()
                if (lines.isEmpty()) null else date to lines
            }.toMap()
        }.getOrDefault(emptyMap())
    }
}
