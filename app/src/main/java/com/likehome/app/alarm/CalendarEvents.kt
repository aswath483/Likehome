package com.likehome.app.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.likehome.app.data.AlarmRepository
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** Reads the phone's calendars (only to find the day's first event) for calendar-aware alarms. */
object CalendarEvents {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    /**
     * Start of the first timed event between [from] and [to] in a visible calendar, skipping
     * all-day events and invitations the user declined. Null without permission or events.
     */
    fun firstStart(context: Context, from: ZonedDateTime, to: ZonedDateTime): ZonedDateTime? {
        if (!hasPermission(context)) return null
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, from.toInstant().toEpochMilli())
            ContentUris.appendId(it, to.toInstant().toEpochMilli())
        }.build()
        val projection = arrayOf(CalendarContract.Instances.BEGIN)
        val selection = "${CalendarContract.Instances.ALL_DAY} = 0 AND ${CalendarContract.Instances.VISIBLE} = 1 AND " +
            "${CalendarContract.Instances.SELF_ATTENDEE_STATUS} != ${CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED} AND " +
            "${CalendarContract.Instances.BEGIN} >= ?"
        return runCatching {
            context.contentResolver.query(
                uri, projection, selection, arrayOf(from.toInstant().toEpochMilli().toString()),
                "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { c ->
                if (c.moveToFirst()) Instant.ofEpochMilli(c.getLong(0)).atZone(from.zone) else null
            }
        }.getOrNull()
    }

    /** The first timed event between [from] and [to]: its start and title, for the briefing. */
    fun firstEvent(context: Context, from: ZonedDateTime, to: ZonedDateTime): Pair<ZonedDateTime, String>? {
        if (!hasPermission(context)) return null
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, from.toInstant().toEpochMilli())
            ContentUris.appendId(it, to.toInstant().toEpochMilli())
        }.build()
        val selection = "${CalendarContract.Instances.ALL_DAY} = 0 AND ${CalendarContract.Instances.VISIBLE} = 1 AND " +
            "${CalendarContract.Instances.SELF_ATTENDEE_STATUS} != ${CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED} AND " +
            "${CalendarContract.Instances.BEGIN} >= ?"
        return runCatching {
            context.contentResolver.query(
                uri, arrayOf(CalendarContract.Instances.BEGIN, CalendarContract.Instances.TITLE), selection,
                arrayOf(from.toInstant().toEpochMilli().toString()), "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { c ->
                if (c.moveToFirst()) Instant.ofEpochMilli(c.getLong(0)).atZone(from.zone) to c.getString(1).orEmpty() else null
            }
        }.getOrNull()
    }

    private const val REFRESH_REQUEST = 300_000

    /**
     * Meetings get added in the evening, after the alarm was armed. When any alarm follows
     * the calendar, a nightly check at 3:30 re-arms them (inexact is fine: the earliest
     * calendar ring is 4:00).
     */
    fun armNightlyCheck(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getBroadcast(
            context, REFRESH_REQUEST, Intent(context, CalendarCheckReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val anyFollowsCalendar = AlarmRepository.get(context).alarms.value.any { it.enabled && it.calendarLead > 0 }
        if (!anyFollowsCalendar) {
            am.cancel(pending)
            return
        }
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var at = now.with(LocalTime.of(3, 30)).withSecond(0).withNano(0)
        if (!at.isAfter(now)) at = at.plusDays(1)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toInstant().toEpochMilli(), pending)
    }
}

/** The nightly calendar check: re-arms every alarm, which re-reads the calendar. */
class CalendarCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.rescheduleAll(context)
    }
}
