package com.likehome.app.ui.ring

import android.app.KeyguardManager
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
import com.likehome.app.core.Morning
import com.likehome.app.core.Mornings
import com.likehome.app.core.WakeTask
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.theme.LikeHomeTheme
import java.time.LocalDateTime

/** Full-screen ringing screen, shown over the lock screen. */
class RingActivity : ComponentActivity() {

    /** Set once the user proves they're awake; the screen then shows the report card. */
    private var report by mutableStateOf<ReportData?>(null)

    // The service auto-stops after 10 minutes; close the screen with it.
    private val finishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (report == null) finish()
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
        report = null
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val level = intent.getIntExtra(AlarmScheduler.EXTRA_LEVEL, 0)
        val seed = intent.getLongExtra(AlarmScheduler.EXTRA_SEED, System.currentTimeMillis())
        val alarm = AlarmRepository.get(this).get(alarmId)
        val label = alarm?.label.orEmpty()
        val settings = SettingsRepository.get(this).current
        val streak = Mornings.streak(HistoryRepository.get(this).mornings.value)

        setContent {
            LikeHomeTheme {
                val card = report
                if (card != null) {
                    ReportCardScreen(card, onShare = { share(card) }, onDone = { finish() })
                } else {
                    RingScreen(
                        style = alarm?.style ?: settings.style,
                        level = level,
                        seed = seed,
                        label = label,
                        language = settings.language,
                        task = alarm?.task ?: WakeTask.MATH,
                        streak = streak,
                        onSnooze = {
                            AlarmService.snooze(this, alarmId, level, seed)
                            finish()
                        },
                        onAwake = {
                            AlarmService.dismiss(this)
                            report = recordMorning(snoozes = level, preview = alarmId == AlarmService.PREVIEW_ID)
                        },
                    )
                }
            }
        }
    }

    /** Saves this morning (not for test rings) and builds its report card. */
    private fun recordMorning(snoozes: Int, preview: Boolean): ReportData {
        val history = HistoryRepository.get(this)
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val before = history.mornings.value
        val morning = Morning(today, snoozes, now.toLocalTime().withSecond(0).withNano(0))
        val after = before + morning
        if (!preview) history.record(morning)
        val streak = Mornings.streak(after)
        val copy = Mornings.report(snoozes, streak)
        return ReportData(
            date = today,
            wokeAt = morning.wokeAt,
            snoozes = snoozes,
            streak = streak,
            title = copy.title,
            body = copy.body,
            week = Mornings.week(after, today),
            newBadges = Mornings.newBadges(before, after),
            verdict = Mornings.verdict(after, today)?.title,
        )
    }

    /** Sharing opens other apps, so unlock the phone first when needed. */
    private fun share(card: ReportData) {
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (keyguard.isKeyguardLocked) {
            keyguard.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() = ReportShare.share(this@RingActivity, card)
            })
        } else {
            ReportShare.share(this, card)
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
