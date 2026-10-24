package com.likehome.app.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.likehome.app.LikeHomeApp
import com.likehome.app.R
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.ring.RingActivity

/**
 * Rings the alarm: plays sound with a gentle volume ramp, vibrates, and shows the
 * full-screen ringing UI. Lives until the user wakes up, snoozes, or 10 minutes pass.
 */
class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var tone: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var volume = 0.15f

    private var alarmId = -1
    private var level = 0
    private var seed = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SNOOZE -> {
                // Read state from the intent so a snooze works even if the service restarted.
                val id = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId) ?: alarmId
                val lvl = intent?.getIntExtra(AlarmScheduler.EXTRA_LEVEL, level) ?: level
                val sd = intent?.getLongExtra(AlarmScheduler.EXTRA_SEED, seed) ?: seed
                // A test ring from Settings just stops; it never comes back.
                if (id != PREVIEW_ID) {
                    AlarmScheduler.scheduleSnooze(
                        this, id,
                        level = (lvl + 1).coerceAtMost(MomLines.MAX_LEVEL),
                        seed = sd
                    )
                }
                stopRinging()
                return START_NOT_STICKY
            }
            ACTION_DISMISS -> {
                stopRinging()
                return START_NOT_STICKY
            }
        }

        // A new ring replaces any ring in progress.
        releaseOutputs()
        alarmId = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        level = intent?.getIntExtra(AlarmScheduler.EXTRA_LEVEL, 0) ?: 0
        seed = intent?.getLongExtra(AlarmScheduler.EXTRA_SEED, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
        )

        acquireWakeLock()
        startSound()
        startVibration()
        // Works when the app is in front; otherwise the full-screen intent shows the screen.
        runCatching { startActivity(ringIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

        handler.postDelayed({ stopRinging() }, AUTO_STOP_MS)
        return START_NOT_STICKY
    }

    private fun ringIntent(): Intent = Intent(this, RingActivity::class.java).apply {
        putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        putExtra(AlarmScheduler.EXTRA_LEVEL, level)
        putExtra(AlarmScheduler.EXTRA_SEED, seed)
    }

    private fun buildNotification(): Notification {
        val fullScreen = PendingIntent.getActivity(
            this, 1, ringIntent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val label = AlarmRepository.get(this).get(alarmId)?.label?.takeIf { it.isNotBlank() }
        return NotificationCompat.Builder(this, LikeHomeApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(label ?: "Mummy is calling")
            .setContentText(MomLines.lineFor(level, seed, SettingsRepository.get(this).current.language))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(fullScreen, true)
            .setContentIntent(fullScreen)
            .build()
    }

    private fun startSound() {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        // Mom gets louder with each snooze: higher levels start at a higher volume.
        volume = (0.15f + level * 0.25f).coerceAtMost(1f)
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        }.getOrNull()
        if (player == null) fallbackTone()
        rampVolume()
    }

    /** Backup if the ringtone can't be played: a system tone at alarm volume. */
    private fun fallbackTone() {
        tone = runCatching {
            ToneGenerator(AudioManager.STREAM_ALARM, 100).apply {
                startTone(ToneGenerator.TONE_CDMA_HIGH_L, AUTO_STOP_MS.toInt())
            }
        }.getOrNull()
    }

    private fun rampVolume() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val p = player ?: return
                volume = (volume + 0.05f).coerceAtMost(1f)
                runCatching { p.setVolume(volume, volume) }
                if (volume < 1f) handler.postDelayed(this, 3_000)
            }
        }, 3_000)
    }

    private fun startVibration() {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = v
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 400, 600, 1200), 0))
    }

    private fun acquireWakeLock() {
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LikeHome:ring")
            .apply { acquire(AUTO_STOP_MS + 5_000) }
    }

    private fun releaseOutputs() {
        handler.removeCallbacksAndMessages(null)
        player?.run { runCatching { stop() }; release() }
        player = null
        tone?.release()
        tone = null
        vibrator?.cancel()
        vibrator = null
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    private fun stopRinging() {
        releaseOutputs()
        sendBroadcast(Intent(ACTION_RING_FINISHED).setPackage(packageName))
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        releaseOutputs()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 42
        private const val AUTO_STOP_MS = 10 * 60_000L

        /** Alarm id used for a test ring started from Settings. */
        const val PREVIEW_ID = -2

        const val ACTION_SNOOZE = "com.likehome.app.action.SNOOZE"
        const val ACTION_DISMISS = "com.likehome.app.action.DISMISS"
        const val ACTION_RING_FINISHED = "com.likehome.app.action.RING_FINISHED"

        fun startIntent(context: Context, alarmId: Int, level: Int, seed: Long) =
            Intent(context, AlarmService::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmScheduler.EXTRA_LEVEL, level)
                putExtra(AlarmScheduler.EXTRA_SEED, seed)
            }

        fun snooze(context: Context, alarmId: Int, level: Int, seed: Long) =
            context.startService(startIntent(context, alarmId, level, seed).setAction(ACTION_SNOOZE))

        /** Ring right now as a test, with a fresh random line. */
        fun preview(context: Context) = ContextCompat.startForegroundService(
            context, startIntent(context, PREVIEW_ID, level = 0, seed = System.currentTimeMillis())
        )

        fun dismiss(context: Context) =
            context.startService(Intent(context, AlarmService::class.java).setAction(ACTION_DISMISS))
    }
}
