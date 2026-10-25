package com.likehome.app.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.likehome.app.LikeHomeApp
import com.likehome.app.R
import com.likehome.app.core.Bedtime
import com.likehome.app.core.ReminderKind
import com.likehome.app.core.ReminderTimes
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.MainActivity
import com.likehome.app.ui.bedside.BedsideActivity
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.likehome.app.ui.common.nameRes

/**
 * Mom's nudges through the day: bedtime, water every two hours, breakfast. None needs to
 * be exact, so they use inexact alarms (no extra battery, no extra permission).
 */
object Reminders {

    private const val EXTRA_KIND = "kind"
    private val TimeFmt = DateTimeFormatter.ofPattern("h:mm a")

    fun scheduleAll(context: Context) = ReminderKind.entries.forEach { schedule(context, it) }

    /** Arms (or cancels) the next [kind] reminder from the current settings. */
    fun schedule(context: Context, kind: ReminderKind) {
        val am = context.getSystemService(AlarmManager::class.java)
        val operation = pendingIntent(context, kind)
        am.cancel(operation)
        val s = SettingsRepository.get(context).current
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val next = when (kind) {
            ReminderKind.BEDTIME -> if (s.bedtimeEnabled) Bedtime.next(now, LocalTime.of(s.bedtimeHour, s.bedtimeMinute)) else null
            ReminderKind.WATER -> if (s.waterEnabled) ReminderTimes.nextWater(now) else null
            ReminderKind.BREAKFAST -> if (s.breakfastEnabled) ReminderTimes.nextDaily(now, LocalTime.of(s.breakfastHour, s.breakfastMinute)) else null
        } ?: return
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), operation)
    }

    internal fun notify(context: Context, kind: ReminderKind) {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!granted) return

        val settings = SettingsRepository.get(context).current
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val seed = now.toInstant().toEpochMilli()
        val line = when (kind) {
            ReminderKind.BEDTIME -> {
                val nextAlarm = AlarmRepository.get(context).alarms.value
                    .filter { it.enabled }
                    .map { AlarmScheduler.nextRing(context, it, now) }
                    .minOrNull()
                MomLines.bedtimeLine(
                    seed = seed,
                    language = settings.language,
                    now = now.format(TimeFmt).lowercase(),
                    alarm = nextAlarm?.takeIf { Bedtime.mentionsAlarm(now, it) }?.format(TimeFmt)?.lowercase(),
                )
            }
            ReminderKind.WATER -> MomLines.waterLine(seed, settings.language)
            ReminderKind.BREAKFAST -> MomLines.breakfastLine(seed, settings.language)
        }
        // Tapping the bedtime nudge opens Sleep sounds; the others open the app.
        val target = Intent(context, MainActivity::class.java)
            .apply { if (kind == ReminderKind.BEDTIME) action = MainActivity.ACTION_SLEEP_SOUNDS }
        val open = PendingIntent.getActivity(
            context, kind.ordinal, target,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, LikeHomeApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(settings.character.nameRes))
            .setContentText(line)
            .setStyle(NotificationCompat.BigTextStyle().bigText(line))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .apply {
                if (kind == ReminderKind.BEDTIME) {
                    val bedside = PendingIntent.getActivity(
                        context, 60, Intent(context, BedsideActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    )
                    addAction(0, context.getString(R.string.bedside_going_to_sleep), bedside)
                }
            }
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_BASE + kind.ordinal, notification) }
    }

    private const val NOTIFICATION_BASE = 50
    private const val REQUEST_BASE = 200_000

    private fun pendingIntent(context: Context, kind: ReminderKind): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_BASE + kind.ordinal,
        Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind.name),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    internal fun kindOf(intent: Intent): ReminderKind? =
        intent.getStringExtra(EXTRA_KIND)?.let { name -> ReminderKind.entries.firstOrNull { it.name == name } }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = Reminders.kindOf(intent) ?: return
        Reminders.notify(context, kind)
        Reminders.schedule(context, kind)
    }
}
