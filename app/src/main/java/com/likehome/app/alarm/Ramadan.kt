package com.likehome.app.alarm

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
import com.likehome.app.core.PrayerTimes
import com.likehome.app.core.RamadanCalendar
import com.likehome.app.data.HolidayStore
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.RamadanLines
import com.likehome.app.ui.MainActivity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Ramadan mode: Mom wakes the user for suhoor a chosen time before Fajr, and nudges them
 * at Maghrib for iftar. Prayer times are worked out on the phone ([PrayerTimes]) from the
 * location the user shared once. The suhoor ring is a normal Mom ring (snooze and task
 * included) under its own id, re-armed each day; it doesn't count as a morning.
 */
object Ramadan {

    const val SUHOOR_ID = -10
    private const val IFTAR_REQUEST = 310_000
    /** How far ahead to look for the next Ramadan day. */
    private const val LOOKAHEAD_DAYS = 400L

    data class Next(val suhoor: ZonedDateTime, val fajr: LocalTime, val maghrib: LocalTime)

    /** Today's (or the next Ramadan day's) times, or null when off or without a location. */
    fun next(context: Context, now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): Next? {
        val s = SettingsRepository.get(context).current
        val lat = s.latitude ?: return null
        val lon = s.longitude ?: return null
        val angle = PrayerTimes.fajrAngle(HolidayStore.country(context))
        var day = now.toLocalDate()
        repeat(LOOKAHEAD_DAYS.toInt()) {
            if (!s.ramadanAuto || RamadanCalendar.isRamadan(day)) {
                val offset = now.zone.rules.getOffset(day.atStartOfDay()).totalSeconds / 3600.0
                val times = PrayerTimes.compute(day, lat, lon, offset, angle)
                if (times != null) {
                    val suhoor = day.atTime(times.fajr).atZone(now.zone).minusMinutes(s.ramadanLead.toLong())
                    if (suhoor.isAfter(now)) return Next(suhoor, times.fajr, times.maghrib)
                }
            }
            day = day.plusDays(1)
        }
        return null
    }

    /** Arms the next suhoor ring and today's iftar nudge (or cancels both when off). */
    fun schedule(context: Context) {
        val s = SettingsRepository.get(context).current
        AlarmScheduler.cancelSpecial(context, SUHOOR_ID)
        val am = context.getSystemService(AlarmManager::class.java)
        val iftar = PendingIntent.getBroadcast(
            context, IFTAR_REQUEST, Intent(context, IftarReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        am.cancel(iftar)
        if (!s.ramadanEnabled) return
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val next = next(context, now) ?: return
        AlarmScheduler.scheduleSpecial(context, SUHOOR_ID, next.suhoor.toInstant().toEpochMilli())
        if (s.iftarReminder) iftarTime(context, now)?.let {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it.toInstant().toEpochMilli(), iftar)
        }
    }

    /** Maghrib today if it's still ahead and today is a fasting day, else on the next one. */
    private fun iftarTime(context: Context, now: ZonedDateTime): ZonedDateTime? {
        val s = SettingsRepository.get(context).current
        val lat = s.latitude ?: return null
        val lon = s.longitude ?: return null
        val angle = PrayerTimes.fajrAngle(HolidayStore.country(context))
        var day: LocalDate = now.toLocalDate()
        repeat(LOOKAHEAD_DAYS.toInt()) {
            if (!s.ramadanAuto || RamadanCalendar.isRamadan(day)) {
                val offset = now.zone.rules.getOffset(day.atStartOfDay()).totalSeconds / 3600.0
                val maghrib = PrayerTimes.compute(day, lat, lon, offset, angle)?.maghrib
                val at = maghrib?.let { day.atTime(it).atZone(now.zone) }
                if (at != null && at.isAfter(now)) return at
            }
            day = day.plusDays(1)
        }
        return null
    }
}

/** Maghrib: Mom says it's time to break the fast, then the next day's nudge is armed. */
class IftarReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (allowed) {
            val s = SettingsRepository.get(context).current
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val line = RamadanLines.iftarLine(System.currentTimeMillis(), s.language)
            val notification = NotificationCompat.Builder(context, LikeHomeApp.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.iftar_title))
                .setContentText(line)
                .setStyle(NotificationCompat.BigTextStyle().bigText(line))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            runCatching { NotificationManagerCompat.from(context).notify(70, notification) }
        }
        Ramadan.schedule(context)
    }
}
