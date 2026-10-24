package com.likehome.app.alarm

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.likehome.app.LikeHomeApp
import com.likehome.app.R
import com.likehome.app.data.AlarmRepository
import com.likehome.app.core.Morning
import com.likehome.app.core.RingStyle
import com.likehome.app.core.Snooze
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.MainActivity
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.ring.RingActivity
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

/**
 * Rings the alarm: plays sound with a gentle volume ramp, vibrates, and shows the
 * full-screen ringing UI. Lives until the user wakes up, snoozes, or 10 minutes pass.
 */
class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var tone: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var voice: MomVoice? = null
    private var audioFocus: AudioFocusRequest? = null
    /** The user's alarm volume before Mom raised it to the floor; restored afterwards. */
    private var savedAlarmVolume: Int? = null
    /** Ringing during a phone call: quiet, no voice, so the call isn't drowned out. */
    private var inCall = false
    /** True while Mom is talking; the ringtone drops so she can be heard. */
    private var ducked = false
    private val handler = Handler(Looper.getMainLooper())
    private var volume = 0.15f

    private var alarmId = -1
    private var level = 0
    private var seed = 0L
    /** Style forced by a test ring from the styles gallery; null = the alarm's own. */
    private var styleOverride: String? = null

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
                        seed = sd,
                        minutes = Snooze.minutes(SettingsRepository.get(this).current.weekendMode, LocalDate.now()),
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
        styleOverride = intent?.getStringExtra(EXTRA_STYLE)

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
        )

        acquireWakeLock()
        prepareAudio()
        startSound()
        startVibration()
        if (!inCall) startVoice()
        // Works when the app is in front; otherwise the full-screen intent shows the screen.
        activeRing = ringIntent()
        runCatching { startActivity(ringIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

        handler.postDelayed({ ringOut() }, AUTO_STOP_MS)
        return START_NOT_STICKY
    }

    /**
     * Ten minutes and nobody answered. Mom tries again a level angrier; at full drama she
     * gives up, the morning counts as missed, and a notification says so.
     */
    private fun ringOut() {
        if (alarmId != PREVIEW_ID) {
            if (level < MomLines.MAX_LEVEL) {
                AlarmScheduler.scheduleSnooze(
                    this, alarmId, level + 1, seed,
                    Snooze.minutes(SettingsRepository.get(this).current.weekendMode, LocalDate.now()),
                )
            } else {
                HistoryRepository.get(this).record(
                    Morning(LocalDate.now(), MomLines.MAX_LEVEL, LocalTime.now().withSecond(0).withNano(0))
                )
                notifyMissed()
            }
        }
        stopRinging()
    }

    private fun notifyMissed() {
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!allowed) return
        val open = PendingIntent.getActivity(
            this, 2, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val text = "You slept through every call. She's not angry, just disappointed."
        val notification = NotificationCompat.Builder(this, LikeHomeApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Mom gave up")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(this).notify(MISSED_NOTIFICATION_ID, notification) }
    }

    /** Takes audio focus (music pauses), and lifts a near-silent alarm volume to a floor. */
    private fun prepareAudio() {
        val am = getSystemService(AudioManager::class.java)
        inCall = am.mode == AudioManager.MODE_IN_CALL || am.mode == AudioManager.MODE_IN_COMMUNICATION
        if (!inCall) {
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val current = am.getStreamVolume(AudioManager.STREAM_ALARM)
            val floor = (max * MIN_ALARM_VOLUME).roundToInt()
            if (current < floor) {
                // Can throw in some Do Not Disturb modes; then the user's volume stays.
                runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, floor, 0) }
                    .onSuccess { savedAlarmVolume = current }
            }
        }
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        runCatching { am.requestAudioFocus(request) }
        audioFocus = request
    }

    private fun releaseAudio() {
        val am = getSystemService(AudioManager::class.java)
        audioFocus?.let { runCatching { am.abandonAudioFocusRequest(it) } }
        audioFocus = null
        savedAlarmVolume?.let { runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, it, 0) } }
        savedAlarmVolume = null
    }

    private fun ringIntent(): Intent = Intent(this, RingActivity::class.java).apply {
        putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        putExtra(AlarmScheduler.EXTRA_LEVEL, level)
        putExtra(AlarmScheduler.EXTRA_SEED, seed)
        styleOverride?.let { putExtra(EXTRA_STYLE, it) }
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
        // During a call she stays quiet so the call can be heard.
        volume = if (inCall) IN_CALL_VOLUME else (0.15f + level * 0.25f).coerceAtMost(1f)
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
        if (!inCall) rampVolume()
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
                if (player == null) return
                volume = (volume + 0.05f).coerceAtMost(1f)
                applyVolume()
                if (volume < 1f) handler.postDelayed(this, 3_000)
            }
        }, 3_000)
    }

    private fun applyVolume() {
        val v = if (ducked) volume * DUCK else volume
        runCatching { player?.setVolume(v, v) }
    }

    /** Mom says her newest line now, then again every so often while ringing. */
    private fun startVoice() {
        val settings = SettingsRepository.get(this).current
        if (!settings.momSpeaks) return
        val line = MomLines.lineFor(level, seed, settings.language)
        voice = MomVoice(this) { speaking ->
            ducked = speaking
            applyVolume()
        }
        handler.post(object : Runnable {
            override fun run() {
                voice?.say(line, level) ?: return
                handler.postDelayed(this, VOICE_REPEAT_MS)
            }
        })
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
        voice?.shutdown()
        voice = null
        ducked = false
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        releaseAudio()
    }

    private fun stopRinging() {
        activeRing = null
        releaseOutputs()
        sendBroadcast(Intent(ACTION_RING_FINISHED).setPackage(packageName))
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        activeRing = null
        releaseOutputs()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 42
        private const val AUTO_STOP_MS = 10 * 60_000L
        private const val VOICE_REPEAT_MS = 40_000L
        private const val MISSED_NOTIFICATION_ID = 44
        /** Share of the maximum alarm volume Mom never rings below. */
        private const val MIN_ALARM_VOLUME = 0.5f
        private const val IN_CALL_VOLUME = 0.08f
        private const val DUCK = 0.2f

        /** The ringing screen's intent while Mom is ringing, else null. */
        @Volatile private var activeRing: Intent? = null

        val isRinging: Boolean get() = activeRing != null

        /** Brings the ringing screen back, e.g. when the user opens the app mid-ring. */
        fun reopenRingScreen(context: Context) {
            val intent = activeRing ?: return
            runCatching { context.startActivity(Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }

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

        /** Extra naming a [com.likehome.app.core.RingStyle] to use instead of the alarm's. */
        const val EXTRA_STYLE = "style"

        /** Ring right now as a test, with a fresh random line, optionally in [style]. */
        fun preview(context: Context, style: RingStyle? = null) = ContextCompat.startForegroundService(
            context,
            startIntent(context, PREVIEW_ID, level = 0, seed = System.currentTimeMillis())
                .apply { style?.let { putExtra(EXTRA_STYLE, it.name) } },
        )

        fun dismiss(context: Context) =
            context.startService(Intent(context, AlarmService::class.java).setAction(ACTION_DISMISS))
    }
}
