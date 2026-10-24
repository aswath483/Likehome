package com.likehome.app.ui.ring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.WakeTask
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.theme.LikeHomeTheme

/** Full-screen ringing screen, shown over the lock screen. */
class RingActivity : ComponentActivity() {

    /** Set once the user proves they're awake; the screen then shows how the morning went. */
    private var awake by mutableStateOf(false)

    // The service auto-stops after 10 minutes; close the screen with it.
    private val finishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!awake) finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge()

        // Back must not silence Mom.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })

        ContextCompat.registerReceiver(
            this, finishedReceiver,
            IntentFilter(AlarmService.ACTION_RING_FINISHED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        render(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        render(intent)
    }

    private fun render(intent: Intent) {
        awake = false
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val level = intent.getIntExtra(AlarmScheduler.EXTRA_LEVEL, 0)
        val seed = intent.getLongExtra(AlarmScheduler.EXTRA_SEED, System.currentTimeMillis())
        val alarm = AlarmRepository.get(this).get(alarmId)
        val label = alarm?.label.orEmpty()
        val settings = SettingsRepository.get(this).current

        setContent {
            LikeHomeTheme {
                if (awake) {
                    AwakeScreen(level = level, onDone = { finish() })
                } else {
                    RingScreen(
                        style = alarm?.style ?: settings.style,
                        level = level,
                        seed = seed,
                        label = label,
                        language = settings.language,
                        task = alarm?.task ?: WakeTask.MATH,
                        onSnooze = {
                            AlarmService.snooze(this, alarmId, level, seed)
                            finish()
                        },
                        onAwake = {
                            awake = true
                            AlarmService.dismiss(this)
                        },
                    )
                }
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        unregisterReceiver(finishedReceiver)
        super.onDestroy()
    }
}
