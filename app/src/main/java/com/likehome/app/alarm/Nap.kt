package com.likehome.app.alarm

import android.content.Context
import com.likehome.app.core.WakeTask
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.SettingsRepository
import java.time.ZoneId
import java.time.ZonedDateTime

/** A one-tap power nap: an alarm 20 minutes from now, from the tile or a shortcut. */
object Nap {
    const val MINUTES = 20
    private const val LABEL = "Power nap"

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("nap", Context.MODE_PRIVATE)

    /**
     * Whether [alarmId] is the nap alarm. True while it rings too (it's switched off by
     * then), so a nap gets no sunrise and doesn't count as a morning.
     */
    fun isNap(context: Context, alarmId: Int): Boolean =
        alarmId >= 0 && prefs(context).getInt("id", -1) == alarmId

    /** The nap alarm, while one is set and still to ring. */
    fun current(context: Context): Alarm? {
        val id = prefs(context).getInt("id", -1).takeIf { it >= 0 } ?: return null
        return AlarmRepository.get(context).get(id)?.takeIf { it.enabled }
    }

    /** Sets a nap, or cancels the one already set. Returns true when a nap is now set. */
    fun toggle(context: Context): Boolean {
        val repo = AlarmRepository.get(context)
        val existing = prefs(context).getInt("id", -1).takeIf { it >= 0 }?.let { repo.get(it) }
        if (existing != null) {
            AlarmScheduler.cancel(context, existing.id)
            repo.delete(existing.id)
            prefs(context).edit().remove("id").apply()
            if (existing.enabled) return false
        }
        val at = ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(MINUTES.toLong())
        val settings = SettingsRepository.get(context).current
        val nap = repo.upsert(
            Alarm(
                id = AlarmRepository.NEW_ID,
                hour = at.hour,
                minute = at.minute,
                label = LABEL,
                task = WakeTask.SHAKE,
                style = settings.style,
                character = settings.character,
            )
        )
        // Saved first, so the scheduler knows it's a nap (no sunrise).
        prefs(context).edit().putInt("id", nap.id).apply()
        AlarmScheduler.schedule(context, nap)
        return true
    }
}
