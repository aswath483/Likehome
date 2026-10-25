package com.likehome.app.data

import android.content.Context
import com.likehome.app.core.Morning
import com.likehome.app.widget.NextAlarmWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/** Every morning the user got up, for streaks, badges and the report card. */
class HistoryRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("history", Context.MODE_PRIVATE)

    private val _mornings = MutableStateFlow(load())
    val mornings: StateFlow<List<Morning>> = _mornings.asStateFlow()

    fun record(morning: Morning) {
        val list = (_mornings.value + morning).takeLast(MAX_KEPT)
        val json = JSONArray()
        list.forEach {
            json.put(
                JSONObject()
                    .put("date", it.date.toString())
                    .put("snoozes", it.snoozes)
                    .put("woke", it.wokeAt.toString())
            )
        }
        prefs.edit().putString(KEY, json.toString()).apply()
        _mornings.value = list
        NextAlarmWidget.refreshAll(appContext)
    }

    private fun load(): List<Morning> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            (0 until json.length()).map { i ->
                val o = json.getJSONObject(i)
                Morning(
                    date = LocalDate.parse(o.getString("date")),
                    snoozes = o.getInt("snoozes"),
                    wokeAt = LocalTime.parse(o.getString("woke")),
                )
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val KEY = "mornings_json"
        /** About four months: enough for the monthly verdict and badges. */
        private const val MAX_KEPT = 120

        @Volatile private var instance: HistoryRepository? = null

        fun get(context: Context): HistoryRepository =
            instance ?: synchronized(this) {
                instance ?: HistoryRepository(context).also { instance = it }
            }
    }
}
