package com.likehome.app.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.likehome.app.LikeHomeApp
import com.likehome.app.R
import com.likehome.app.core.Bedtime
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.MainActivity
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** A nightly "Beta, sleep now" nudge. It doesn't need to be exact, so it uses an inexact alarm. */
object BedtimeReminder {

    private const val REQUEST_CODE = 200_000
    private const val NOTIFICATION_ID = 43
    private val TimeFmt = DateTimeFormatter.ofPattern("h:mm a")

    /** Arms (or cancels) tonight's reminder from the current settings. */
    fun schedule(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val operation = pendingIntent(context)
        am.cancel(operation)
        val s = SettingsRepository.get(context).current
        if (!s.bedtimeEnabled) return
        val next = Bedtime.next(ZonedDateTime.now(ZoneId.systemDefault()), LocalTime.of(s.bedtimeHour, s.bedtimeMinute))
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), operation)
    }

    internal fun notify(context: Context) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33
        if (!granted) return

        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val nextAlarm = AlarmRepository.get(context).alarms.value
            .filter { it.enabled }
            .map { it.nextTrigger(now) }
            .minOrNull()
        val line = MomLines.bedtimeLine(
            seed = now.toInstant().toEpochMilli(),
            language = SettingsRepository.get(context).current.language,
            now = now.format(TimeFmt).lowercase(),
            alarm = nextAlarm?.takeIf { Bedtime.mentionsAlarm(now, it) }?.format(TimeFmt)?.lowercase(),
        )
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, LikeHomeApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Mummy")
            .setContentText(line)
            .setStyle(NotificationCompat.BigTextStyle().bigText(line))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification) }
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE,
        Intent(context, BedtimeReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}

class BedtimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        BedtimeReminder.notify(context)
        BedtimeReminder.schedule(context)
    }
}
