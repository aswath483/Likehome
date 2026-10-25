package com.likehome.app.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.likehome.app.LikeHomeApp
import com.likehome.app.R
import com.likehome.app.core.Character
import com.likehome.app.core.ChimeGen
import com.likehome.app.core.GentleWake
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.sunrise.SunriseActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Fires a few minutes before an alarm and starts the sunrise. */
class SunriseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val ringAt = intent.getLongExtra(SunriseService.EXTRA_RING_AT, 0L)
        val alarm = AlarmRepository.get(context).get(alarmId)
        // The alarm was turned off or moved since the sunrise was armed, or Mom is already calling.
        if (alarm == null || !alarm.enabled || ringAt <= System.currentTimeMillis() || AlarmService.isRinging) return
        ContextCompat.startForegroundService(context, SunriseService.startIntent(context, alarmId, ringAt))
    }
}

/**
 * The sunrise before Mom calls: soft chimes rising from silence and a screen that brightens
 * ([SunriseActivity]). Getting up now starts Mom's ring early, task and all; the planned
 * ring then stays quiet. Mom's ring replaces the sunrise when its time comes.
 */
class SunriseService : Service() {

    data class State(val alarmId: Int, val ringAt: Long, val startedAt: Long)

    @Volatile private var running = false
    private var thread: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UP -> {
                val s = _state.value
                stopSunrise()
                if (s != null) {
                    earlyUp(this).edit().putLong("${s.alarmId}", s.ringAt).apply()
                    ContextCompat.startForegroundService(this, AlarmService.startIntent(this, s.alarmId, level = 0, seed = s.ringAt))
                }
                return START_NOT_STICKY
            }
            ACTION_STOP -> {
                stopSunrise()
                return START_NOT_STICKY
            }
        }
        val alarmId = intent?.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1) ?: -1
        val ringAt = intent?.getLongExtra(EXTRA_RING_AT, 0L) ?: 0L
        val state = State(alarmId, ringAt, System.currentTimeMillis())
        _state.value = state
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, notification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0,
        )
        SleepSoundService.stop(this)
        val minutes = ((ringAt - state.startedAt) / 60_000L).toInt().coerceAtLeast(1)
        running = false
        thread?.join(500)
        running = true
        thread = Thread({ play(minutes, endAt = ringAt + END_GRACE_MS) }, "sunrise").apply { start() }
        runCatching { startActivity(screen().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        return START_NOT_STICKY
    }

    private fun play(minutes: Int, endAt: Long) {
        val rate = 44_100
        val minBuffer = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(rate)
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(maxOf(minBuffer, rate))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull() ?: return
        val gen = ChimeGen(rate, seed = SystemClock.elapsedRealtime().toInt())
        val buffer = FloatArray(rate / 10)
        val started = SystemClock.elapsedRealtime()
        track.play()
        try {
            // Mom's ring normally ends the sunrise; if it never comes (alarm moved or
            // switched off), the sunrise ends on its own shortly after.
            while (running && System.currentTimeMillis() < endAt) {
                track.setVolume(GentleWake.volume(GentleWake.progress(SystemClock.elapsedRealtime() - started, minutes)))
                gen.fill(buffer)
                track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            }
        } finally {
            runCatching { track.stop() }
            track.release()
        }
        if (running) Handler(Looper.getMainLooper()).post { stopSunrise() }
    }

    private fun caller(alarmId: Int): Character =
        AlarmRepository.get(this).get(alarmId)?.character ?: SettingsRepository.get(this).current.character

    private fun screen() = Intent(this, SunriseActivity::class.java)

    private fun notification(): Notification {
        val open = PendingIntent.getActivity(this, 40, screen(), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val up = PendingIntent.getService(
            this, 41, Intent(this, SunriseService::class.java).setAction(ACTION_UP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, LikeHomeApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.sunrise_title))
            .setContentText(getString(R.string.sunrise_detail, getString(caller(_state.value?.alarmId ?: -1).nameRes)))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSilent(true)
            .setFullScreenIntent(open, true)
            .setContentIntent(open)
            .addAction(0, getString(R.string.sunrise_up), up)
            .build()
    }

    private fun stopSunrise() {
        running = false
        thread?.join(500)
        thread = null
        _state.value = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        _state.value = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_RING_AT = "ring_at"
        private const val ACTION_UP = "com.likehome.app.action.SUNRISE_UP"
        private const val ACTION_STOP = "com.likehome.app.action.SUNRISE_STOP"
        private const val NOTIFICATION_ID = 46
        /** How long after the planned ring a sunrise gives up waiting for Mom. */
        private const val END_GRACE_MS = 2 * 60_000L

        private val _state = MutableStateFlow<State?>(null)
        /** The sunrise in progress, or null. */
        val state: StateFlow<State?> = _state.asStateFlow()

        fun startIntent(context: Context, alarmId: Int, ringAt: Long) =
            Intent(context, SunriseService::class.java)
                .putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                .putExtra(EXTRA_RING_AT, ringAt)

        /** The user is up before Mom: ring now (the task still has to be done). */
        fun up(context: Context) =
            context.startService(Intent(context, SunriseService::class.java).setAction(ACTION_UP))

        /** Ends the sunrise without ringing, e.g. because Mom's ring is starting. */
        fun stop(context: Context) {
            if (_state.value == null) return
            // Never let stopping the sunrise break scheduling; it also ends on its own.
            runCatching { context.startService(Intent(context, SunriseService::class.java).setAction(ACTION_STOP)) }
        }

        /**
         * Ends [alarmId]'s sunrise unless it leads to a ring at [ringAt]: the alarm was
         * switched off, deleted or moved. Null [ringAt] ends it in any case.
         */
        fun stopFor(context: Context, alarmId: Int, ringAt: Long? = null) {
            val s = _state.value ?: return
            if (s.alarmId == alarmId && s.ringAt != ringAt) stop(context)
        }

        /**
         * True (once) when the ring for [alarmId] arriving now was already answered during
         * the sunrise, so it should stay quiet.
         */
        fun consumeEarlyUp(context: Context, alarmId: Int): Boolean {
            val prefs = earlyUp(context)
            val planned = prefs.getLong("$alarmId", 0L)
            if (planned == 0L) return false
            prefs.edit().remove("$alarmId").apply()
            return GentleWake.alreadyUp(planned, System.currentTimeMillis())
        }

        private fun earlyUp(context: Context) =
            context.applicationContext.getSharedPreferences("early_up", Context.MODE_PRIVATE)

        /** Minutes of sunrise before each alarm; 0 is off. */
        fun minutes(context: Context) = SettingsRepository.get(context).current.gentleMinutes
    }
}
