package com.likehome.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes

class LikeHomeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createAlarmChannel()
        createReminderChannel()
        createSleepChannel()
        Telemetry.start(this)
    }

    private fun createReminderChannel() {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            getString(R.string.channel_reminder_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = getString(R.string.channel_reminder_desc) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Quiet and low: it only carries the sleep sounds' Stop button. */
    private fun createSleepChannel() {
        val channel = NotificationChannel(
            SLEEP_CHANNEL_ID,
            getString(R.string.channel_sleep_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createAlarmChannel() {
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            getString(R.string.channel_alarm_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_alarm_desc)
            // Sound is played by AlarmService so it can escalate; keep the channel silent.
            setSound(null, null as AudioAttributes?)
            enableVibration(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val ALARM_CHANNEL_ID = "alarm_ringing"
        const val REMINDER_CHANNEL_ID = "reminders"
        const val SLEEP_CHANNEL_ID = "sleep_sounds"
    }
}
