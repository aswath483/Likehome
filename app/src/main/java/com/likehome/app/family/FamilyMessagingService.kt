package com.likehome.app.family

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.likehome.app.LikeHomeApp
import com.likehome.app.R
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.FamilyEvent
import com.likehome.app.ui.MainActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pushes from the family functions (all data messages, so each phone writes in its own
 * language): "alarms" re-syncs the parents' alarms, "poke" rings Mom now, and "status"
 * tells a parent what the child's alarm did, and "challenge" says a friend in the
 * streak challenge got up.
 */
class FamilyMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) = FamilyLive.onNewToken(this, token)

    override fun onMessageReceived(message: RemoteMessage) {
        FamilyLive.load(this)
        when (message.data["type"]) {
            // Runs on FCM's worker thread, which allows a short blocking sync.
            "alarms" -> runBlocking { withTimeoutOrNull(SYNC_TIMEOUT_MS) { runCatching { FamilyLive.syncAlarms(this@FamilyMessagingService) } } }
            "poke" -> if (FamilyLive.state.value.role == FamilyLive.Role.CHILD) AlarmService.wakeUpCall(this)
            "status" -> showStatus(message.data)
            "challenge" -> showChallenge(message.data)
        }
    }

    /** A friend in the streak challenge got up. */
    private fun showChallenge(data: Map<String, String>) {
        val name = data["name"].orEmpty().ifBlank { getString(R.string.challenge_friend_default) }
        val snoozes = data["snoozes"]?.toIntOrNull() ?: 0
        val text = if (snoozes == 0) getString(R.string.challenge_push_clean, name)
            else resources.getQuantityString(R.plurals.challenge_push_snoozed, snoozes, name, snoozes)
        val open = PendingIntent.getActivity(
            this, 51, Intent(this, MainActivity::class.java).setAction(MainActivity.ACTION_CHALLENGE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        notify(CHALLENGE_NOTIFICATION_ID, getString(R.string.challenge_title), text, open)
    }

    private fun notify(id: Int, title: String, text: String, open: PendingIntent) {
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!allowed) return
        val notification = NotificationCompat.Builder(this, LikeHomeApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(this).notify(id, notification) }
    }

    private fun showStatus(data: Map<String, String>) {
        val name = data["childName"].orEmpty().ifBlank { getString(R.string.family_child_default) }
        val level = data["level"]?.toIntOrNull() ?: 0
        val text = when (FamilyEvent.entries.firstOrNull { it.name == data["event"] } ?: return) {
            FamilyEvent.RINGING -> getString(R.string.family_status_ringing, name)
            FamilyEvent.SNOOZED -> resources.getQuantityString(R.plurals.family_status_snoozed, level, name, level)
            FamilyEvent.AWAKE -> if (level == 0) getString(R.string.family_status_awake_proud, name)
                else resources.getQuantityString(R.plurals.family_status_awake, level, name, level)
            FamilyEvent.GAVE_UP -> getString(R.string.family_status_gave_up, name)
        }
        val open = PendingIntent.getActivity(
            this, 50, Intent(this, MainActivity::class.java).setAction(MainActivity.ACTION_FAMILY),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        notify(STATUS_NOTIFICATION_ID, getString(R.string.family_live_title), text, open)
    }

    private companion object {
        const val SYNC_TIMEOUT_MS = 15_000L
        const val STATUS_NOTIFICATION_ID = 60
        const val CHALLENGE_NOTIFICATION_ID = 61
    }
}
