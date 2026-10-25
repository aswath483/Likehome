package com.likehome.app.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.likehome.app.LikeHomeApp
import com.likehome.app.R
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.NoiseGen
import com.likehome.app.core.SleepSound
import com.likehome.app.core.SleepTimer
import com.likehome.app.ui.MainActivity
import com.likehome.app.ui.common.labelRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId

/**
 * Plays a [SleepSound] made in code, with an optional sleep timer that fades out over
 * the last half minute. Runs as a media foreground service so it keeps playing with the
 * screen off; an alarm ringing stops it.
 */
class SleepSoundService : Service() {

    /** What's playing: the sound and, with a timer, when it ends (epoch ms). */
    data class Playing(val sound: SleepSound, val endsAt: Long?)

    @Volatile private var running = false
    private var thread: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopPlaying()
            return START_NOT_STICKY
        }
        val sound = intent?.getStringExtra(EXTRA_SOUND)?.let { name -> SleepSound.entries.firstOrNull { it.name == name } }
            ?: SleepSound.RAIN
        val minutes = intent?.getIntExtra(EXTRA_MINUTES, 30) ?: 30
        val endsAt = if (minutes > 0) System.currentTimeMillis() + minutes * 60_000L else null
        val notification = notification(sound, endsAt)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startPlaying(sound, minutes)
        _playing.value = Playing(sound, endsAt)
        return START_NOT_STICKY
    }

    private fun startPlaying(sound: SleepSound, minutes: Int) {
        running = false
        thread?.join(500)
        running = true
        thread = Thread({ play(sound, minutes) }, "sleep-sound").apply { start() }
    }

    /** Streams the sound in small buffers; volume follows the timer's fade, then it stops. */
    private fun play(sound: SleepSound, minutes: Int) {
        val rate = 44_100
        val minBuffer = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
        }.getOrNull() ?: return stopSelfFromThread()
        val gen = NoiseGen(sound, rate, seed = SystemClock.elapsedRealtime().toInt())
        val buffer = FloatArray(rate / 10)
        val started = SystemClock.elapsedRealtime()
        track.play()
        try {
            while (running) {
                val gain = SleepTimer.volume(SystemClock.elapsedRealtime() - started, minutes)
                if (gain <= 0f) break
                // A short fade-in so the sound doesn't start with a jolt.
                val fadeIn = ((SystemClock.elapsedRealtime() - started) / 3_000f).coerceIn(0f, 1f)
                track.setVolume(gain * fadeIn)
                gen.fill(buffer)
                track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            }
        } finally {
            runCatching { track.stop() }
            track.release()
        }
        if (running) stopSelfFromThread()
    }

    private fun stopSelfFromThread() {
        running = false
        _playing.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopPlaying() {
        running = false
        thread?.join(500)
        thread = null
        _playing.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        _playing.value = null
        super.onDestroy()
    }

    private fun notification(sound: SleepSound, endsAt: Long?): Notification {
        val open = PendingIntent.getActivity(
            this, 30, Intent(this, MainActivity::class.java).setAction(MainActivity.ACTION_SLEEP_SOUNDS),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stop = PendingIntent.getService(
            this, 31, Intent(this, SleepSoundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val text = endsAt?.let {
            val time = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
            getString(R.string.sleep_until, ClockFormat.full(time, DateFormat.is24HourFormat(this)))
        } ?: getString(R.string.sleep_no_timer)
        return NotificationCompat.Builder(this, LikeHomeApp.SLEEP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.sleep_playing, getString(sound.labelRes)))
            .setContentText(text)
            .setContentIntent(open)
            .addAction(0, getString(R.string.stop), stop)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 7
        private const val ACTION_STOP = "com.likehome.app.SLEEP_STOP"
        private const val EXTRA_SOUND = "sound"
        private const val EXTRA_MINUTES = "minutes"

        private val _playing = MutableStateFlow<Playing?>(null)
        /** What's playing right now, for the Sleep sounds screen; null when silent. */
        val playing: StateFlow<Playing?> = _playing.asStateFlow()

        fun start(context: Context, sound: SleepSound, minutes: Int) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SleepSoundService::class.java)
                    .putExtra(EXTRA_SOUND, sound.name)
                    .putExtra(EXTRA_MINUTES, minutes),
            )
        }

        fun stop(context: Context) {
            if (_playing.value == null) return
            context.startService(Intent(context, SleepSoundService::class.java).setAction(ACTION_STOP))
        }
    }
}
