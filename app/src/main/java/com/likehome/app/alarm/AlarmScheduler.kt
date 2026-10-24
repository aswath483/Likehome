package com.likehome.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import com.likehome.app.ui.MainActivity

/**
 * Schedules alarms with [AlarmManager.setAlarmClock], the most reliable exact alarm:
 * it fires in Doze and shows the alarm icon in the status bar.
 */
object AlarmScheduler {

    const val EXTRA_ALARM_ID = "alarm_id"
    const val EXTRA_LEVEL = "level"
    const val EXTRA_SEED = "seed"

    /** Request code offset for snoozes so they never collide with the main alarm. */
    private const val SNOOZE_OFFSET = 100_000

    fun schedule(context: Context, alarm: Alarm) {
        cancel(context, alarm.id)
        if (!alarm.enabled) return
        val triggerAt = alarm.nextTrigger().toInstant().toEpochMilli()
        setClock(context, triggerAt, firePendingIntent(context, alarm.id, alarm.id, level = 0, seed = triggerAt))
    }

    fun scheduleSnooze(context: Context, alarmId: Int, level: Int, seed: Long, minutes: Int = 5) {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        setClock(
            context,
            triggerAt,
            firePendingIntent(context, SNOOZE_OFFSET + alarmId, alarmId, level, seed)
        )
    }

    fun cancel(context: Context, alarmId: Int) {
        val am = context.getSystemService(AlarmManager::class.java)
        am.cancel(firePendingIntent(context, alarmId, alarmId, 0, 0))
        am.cancel(firePendingIntent(context, SNOOZE_OFFSET + alarmId, alarmId, 0, 0))
    }

    /** Re-arm every enabled alarm, e.g. after reboot or a time-zone change. */
    fun rescheduleAll(context: Context) {
        AlarmRepository.get(context).alarms.value.forEach { schedule(context, it) }
    }

    /** False on Android 12+ when the user has turned off "Alarms & reminders" for us. */
    fun canRingExactly(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    private fun setClock(context: Context, triggerAt: Long, operation: PendingIntent) {
        val am = context.getSystemService(AlarmManager::class.java)
        val showIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        try {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, showIntent), operation)
        } catch (e: SecurityException) {
            // Exact alarms were revoked: ring a little late rather than not at all.
            // The setup screen asks the user to turn the permission back on.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    private fun firePendingIntent(
        context: Context,
        requestCode: Int,
        alarmId: Int,
        level: Int,
        seed: Long,
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_LEVEL, level)
            putExtra(EXTRA_SEED, seed)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
