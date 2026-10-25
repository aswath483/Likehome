package com.likehome.app.data

import android.content.Context
import com.likehome.app.core.Character
import com.likehome.app.core.RingStyle
import com.likehome.app.core.WakeTask
import com.likehome.app.widget.NextAlarmWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek

/**
 * Stores alarms in SharedPreferences as JSON. Small, synchronous and safe to call
 * from broadcast receivers (boot, alarm fire) as well as the UI.
 */
class AlarmRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("alarms", Context.MODE_PRIVATE)

    private val _alarms = MutableStateFlow(load())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    fun get(id: Int): Alarm? = _alarms.value.firstOrNull { it.id == id }

    fun upsert(alarm: Alarm): Alarm {
        val saved = if (alarm.id == NEW_ID) alarm.copy(id = nextId()) else alarm
        val list = _alarms.value.filterNot { it.id == saved.id } + saved
        persist(list.sortedWith(compareBy({ it.hour }, { it.minute })))
        return saved
    }

    fun delete(id: Int) = persist(_alarms.value.filterNot { it.id == id })

    private fun nextId(): Int {
        val next = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, next + 1).apply()
        return next
    }

    private fun persist(list: List<Alarm>) {
        val json = JSONArray()
        list.forEach { a ->
            json.put(
                JSONObject()
                    .put("id", a.id)
                    .put("hour", a.hour)
                    .put("minute", a.minute)
                    .put("label", a.label)
                    .put("enabled", a.enabled)
                    .put("days", JSONArray(a.days.map { it.value }))
                    .put("task", a.task.name)
                    .put("style", a.style.name)
                    .put("character", a.character.name)
                    .put("photo", a.photoRef)
            )
        }
        prefs.edit().putString(KEY_ALARMS, json.toString()).apply()
        _alarms.value = list
        NextAlarmWidget.refresh(appContext)
    }

    private fun load(): List<Alarm> {
        val raw = prefs.getString(KEY_ALARMS, null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            (0 until json.length()).map { i ->
                val o = json.getJSONObject(i)
                val days = o.optJSONArray("days")
                Alarm(
                    id = o.getInt("id"),
                    hour = o.getInt("hour"),
                    minute = o.getInt("minute"),
                    label = o.optString("label"),
                    enabled = o.optBoolean("enabled", true),
                    days = if (days == null) emptySet() else
                        (0 until days.length()).map { DayOfWeek.of(days.getInt(it)) }.toSet(),
                    task = enumOrDefault(o.optString("task"), WakeTask.MATH),
                    style = enumOrDefault(o.optString("style"), RingStyle.CHAT),
                    character = enumOrDefault(o.optString("character"), Character.MOM),
                    photoRef = o.optString("photo"),
                )
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        const val NEW_ID = -1
        private const val KEY_ALARMS = "alarms_json"
        private const val KEY_NEXT_ID = "next_id"

        @Volatile private var instance: AlarmRepository? = null

        fun get(context: Context): AlarmRepository =
            instance ?: synchronized(this) {
                instance ?: AlarmRepository(context).also { instance = it }
            }
    }
}
