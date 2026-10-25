package com.likehome.app.ui.ring

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.likehome.app.Telemetry
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.Character
import com.likehome.app.core.Morning
import com.likehome.app.core.Mornings
import com.likehome.app.core.RingStyle
import com.likehome.app.core.Snooze
import com.likehome.app.core.WakeTask
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.theme.LikeHomeTheme
import java.time.LocalDate
import java.time.LocalDateTime
import androidx.compose.ui.res.stringResource
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.common.titleRes

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
        // Dark pages, light bar icons, whatever the phone's own theme. The comic page flips them.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

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
        val styleOverride = intent.getStringExtra(AlarmService.EXTRA_STYLE)
            ?.let { name -> RingStyle.entries.firstOrNull { it.name == name } }
        val characterOverride = intent.getStringExtra(AlarmService.EXTRA_CHARACTER)
            ?.let { name -> Character.entries.firstOrNull { it.name == name } }
        val streak = Mornings.streak(HistoryRepository.get(this).mornings.value)

        setContent {
            LikeHomeTheme {
                val card = report
                if (card != null) {
                    ReportCardScreen(card, onShare = { share(card) }, onDone = { finish() })
                } else {
                    RingScreen(
                        style = styleOverride ?: alarm?.style ?: settings.style,
                        p = RingProps(
                            level = level,
                            seed = seed,
                            label = label,
                            language = settings.language,
                            task = alarm?.task ?: WakeTask.MATH,
                            streak = streak,
                            snoozeMinutes = Snooze.minutes(settings.weekendMode, LocalDate.now()),
                            character = characterOverride ?: alarm?.character ?: settings.character,
                            name = stringResource((characterOverride ?: alarm?.character ?: settings.character).nameRes),
                            taskTitle = stringResource((alarm?.task ?: WakeTask.MATH).titleRes),
                            photoRef = alarm?.photoRef.orEmpty(),
                        ),
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
        Telemetry.event(this, "wake_up", "snoozes" to snoozes, "preview" to preview)
        return Reports.forMorning(resources, after, morning, newBadges = Mornings.newBadges(before, after))
    }

    /** Sharing opens other apps, so unlock the phone first when needed. */
    private fun share(card: ReportData) {
        Telemetry.event(this, "share_report", "snoozes" to card.snoozes)
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
