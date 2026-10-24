package com.likehome.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.likehome.app.core.Snooze
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

    /**
     * Arms the alarm's next ring. A snooze already pending is left alone: this also runs
     * on clock and time-zone changes, which must not silently drop a snoozed Mom.
     */
    fun schedule(context: Context, alarm: Alarm) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(firePendingIntent(context, alarm.id, alarm.id, 0, 0))
        if (!alarm.enabled) return
        val triggerAt = alarm.nextTrigger().toInstant().toEpochMilli()
        setClock(context, triggerAt, firePendingIntent(context, alarm.id, alarm.id, level = 0, seed = triggerAt))
    }

    fun scheduleSnooze(context: Context, alarmId: Int, level: Int, seed: Long, minutes: Int = Snooze.MINUTES) {
        armSnooze(context, alarmId, level, seed, System.currentTimeMillis() + minutes * 60_000L)
    }

    private fun armSnooze(context: Context, alarmId: Int, level: Int, seed: Long, triggerAt: Long) {
        // Saved so it can be re-armed after a reboot, which clears every alarm.
        snoozes(context).edit().putString("$alarmId", "$triggerAt,$level,$seed").apply()
        setClock(context, triggerAt, firePendingIntent(context, SNOOZE_OFFSET + alarmId, alarmId, level, seed))
    }

    /** The snooze has rung (or was answered); forget it. */
    fun clearSnooze(context: Context, alarmId: Int) {
        snoozes(context).edit().remove("$alarmId").apply()
    }

    /** Turns an alarm off completely: its next ring and any pending snooze. */
    fun cancel(context: Context, alarmId: Int) {
        val am = context.getSystemService(AlarmManager::class.java)
        am.cancel(firePendingIntent(context, alarmId, alarmId, 0, 0))
        am.cancel(firePendingIntent(context, SNOOZE_OFFSET + alarmId, alarmId, 0, 0))
        clearSnooze(context, alarmId)
    }

    /** Re-arm every enabled alarm and pending snooze, e.g. after reboot or a clock change. */
    fun rescheduleAll(context: Context) {
        AlarmRepository.get(context).alarms.value.forEach { schedule(context, it) }
        val now = System.currentTimeMillis()
        snoozes(context).all.forEach { (key, value) ->
            val id = key.toIntOrNull()
            val parts = (value as? String)?.split(',')
            val at = parts?.getOrNull(0)?.toLongOrNull()
            val level = parts?.getOrNull(1)?.toIntOrNull()
            val seed = parts?.getOrNull(2)?.toLongOrNull()
            val ringAt = if (at != null) Snooze.restoreAt(at, now) else null
            if (id == null || level == null || seed == null || ringAt == null) {
                snoozes(context).edit().remove(key).apply()
            } else {
                armSnooze(context, id, level, seed, ringAt)
            }
        }
    }

    private fun snoozes(context: Context) =
        context.applicationContext.getSharedPreferences("snoozes", Context.MODE_PRIVATE)

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
