package com.likehome.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.likehome.app.core.CalendarWake
import com.likehome.app.core.GentleWake
import com.likehome.app.core.Skips
import com.likehome.app.core.Snooze
import com.likehome.app.data.HolidayStore
import com.likehome.app.data.SettingsRepository
import java.time.LocalDate
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import com.likehome.app.ui.MainActivity
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

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
    /** Request code offset for the sunrise before an alarm (gentle wake). */
    private const val SUNRISE_OFFSET = 300_000

    /**
     * Arms the alarm's next ring. A snooze already pending is left alone: this also runs
     * on clock and time-zone changes, which must not silently drop a snoozed Mom.
     */
    fun schedule(context: Context, alarm: Alarm) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(firePendingIntent(context, alarm.id, alarm.id, 0, 0))
        cancelSunrise(context, alarm.id)
        if (!alarm.enabled) {
            SunriseService.stopFor(context, alarm.id)
            planned(context).edit().remove("${alarm.id}").apply()
            return
        }
        val triggerAt = ringTime(context, alarm, ZonedDateTime.now(ZoneId.systemDefault())).toInstant().toEpochMilli()
        planned(context).edit().putLong("${alarm.id}", triggerAt).apply()
        setClock(context, triggerAt, firePendingIntent(context, alarm.id, alarm.id, level = 0, seed = triggerAt))
        // A sunrise already running for a ring that moved (calendar, vacation, edit) ends.
        SunriseService.stopFor(context, alarm.id, triggerAt)
        armSunrise(context, alarm.id, triggerAt)
        if (alarm.calendarLead > 0) CalendarEvents.armNightlyCheck(context)
    }

    /** The usual time, or earlier for an early meeting when the alarm follows the calendar. */
    private fun ringTime(context: Context, alarm: Alarm, now: ZonedDateTime): ZonedDateTime {
        val regular = alarm.nextTrigger(now, skipper(context, alarm))
        if (alarm.calendarLead <= 0) return regular
        val (from, to) = CalendarWake.searchWindow(regular, alarm.calendarLead)
        return CalendarWake.ringAt(regular, CalendarEvents.firstStart(context, from, to), alarm.calendarLead, now)
    }

    /**
     * When the alarm will actually ring, as armed: the calendar can move it earlier than
     * [Alarm.nextTrigger]. Falls back to the usual time when nothing valid is armed.
     */
    fun nextRing(context: Context, alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): ZonedDateTime {
        val regular = alarm.nextTrigger(now, skipper(context, alarm))
        val at = planned(context).getLong("${alarm.id}", 0L)
        val regularMs = regular.toInstant().toEpochMilli()
        return if (alarm.enabled && at > now.toInstant().toEpochMilli() && at <= regularMs) {
            Instant.ofEpochMilli(at).atZone(now.zone)
        } else {
            regular
        }
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
        cancelSunrise(context, alarmId)
        SunriseService.stopFor(context, alarmId)
        planned(context).edit().remove("$alarmId").apply()
    }

    /**
     * Gentle wake: a sunrise a few minutes before the ring. Not an alarm clock entry, so the
     * status bar keeps showing Mom's real time; without exact alarms there's simply no sunrise.
     */
    private fun armSunrise(context: Context, alarmId: Int, ringAt: Long) {
        // A 20-minute nap with a 15-minute sunrise would be woken 5 minutes in.
        if (Nap.isNap(context, alarmId)) return
        val start = GentleWake.startAt(ringAt, SunriseService.minutes(context), System.currentTimeMillis()) ?: return
        if (!canRingExactly(context)) return
        runCatching {
            context.getSystemService(AlarmManager::class.java)
                .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start, sunrisePendingIntent(context, alarmId, ringAt))
        }
    }

    private fun cancelSunrise(context: Context, alarmId: Int) =
        context.getSystemService(AlarmManager::class.java).cancel(sunrisePendingIntent(context, alarmId, 0L))

    private fun sunrisePendingIntent(context: Context, alarmId: Int, ringAt: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context, SUNRISE_OFFSET + alarmId,
            Intent(context, SunriseReceiver::class.java)
                .putExtra(EXTRA_ALARM_ID, alarmId)
                .putExtra(SunriseService.EXTRA_RING_AT, ringAt),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    /** Arms a ring that isn't a user alarm (the suhoor alarm), at [triggerAt]. */
    fun scheduleSpecial(context: Context, id: Int, triggerAt: Long) =
        setClock(context, triggerAt, firePendingIntent(context, id, id, level = 0, seed = triggerAt))

    fun cancelSpecial(context: Context, id: Int) =
        context.getSystemService(AlarmManager::class.java).cancel(firePendingIntent(context, id, id, 0, 0))

    /** Re-arm every enabled alarm and pending snooze, e.g. after reboot or a clock change. */
    fun rescheduleAll(context: Context) {
        AlarmRepository.get(context).alarms.value.forEach { schedule(context, it) }
        CalendarEvents.armNightlyCheck(context)
        Ramadan.schedule(context)
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

    /** The days [alarm] sits out: holidays (if it skips them) and vacation mode. */
    fun skipper(context: Context, alarm: Alarm): (LocalDate) -> Boolean {
        val pausedUntil = SettingsRepository.get(context).current.pausedUntil
        if (pausedUntil == null && !alarm.skipHolidays) return { false }
        val holidays = if (alarm.skipHolidays) HolidayStore.dates(context) else emptySet()
        return { day -> Skips.skipped(day, holidays, alarm.skipHolidays, pausedUntil) }
    }

    /** Re-reads the calendar for alarms that follow it, e.g. when the app opens. */
    fun refreshCalendarAlarms(context: Context) {
        AlarmRepository.get(context).alarms.value.filter { it.enabled && it.calendarLead > 0 }.forEach { schedule(context, it) }
        CalendarEvents.armNightlyCheck(context)
    }

    /** The armed ring time of each alarm, by id, so screens show what will really happen. */
    private fun planned(context: Context) =
        context.applicationContext.getSharedPreferences("planned", Context.MODE_PRIVATE)

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
