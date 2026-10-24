package com.likehome.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.likehome.app.data.AlarmRepository

/** Receives the exact alarm and hands off to [AlarmService], which rings. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val level = intent.getIntExtra(AlarmScheduler.EXTRA_LEVEL, 0)
        val seed = intent.getLongExtra(AlarmScheduler.EXTRA_SEED, System.currentTimeMillis())

        // Arm the next occurrence of a repeating alarm right away; a one-time alarm
        // switches itself off once it has rung (only on the first ring, not snoozes).
        if (level > 0) AlarmScheduler.clearSnooze(context, alarmId)
        val repo = AlarmRepository.get(context)
        repo.get(alarmId)?.let { alarm ->
            if (level == 0) {
                if (alarm.isRepeating) AlarmScheduler.schedule(context, alarm)
                else repo.upsert(alarm.copy(enabled = false))
            }
        }

        ContextCompat.startForegroundService(
            context,
            AlarmService.startIntent(context, alarmId, level, seed)
        )
    }

    companion object {
        const val ACTION_FIRE = "com.likehome.app.action.FIRE"
    }
}
