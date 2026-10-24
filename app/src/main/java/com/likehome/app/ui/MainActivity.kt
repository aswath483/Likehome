package com.likehome.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.review.ReviewManagerFactory
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.Mornings
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.home.HomeScreen
import com.likehome.app.ui.onboarding.OnboardingResult
import com.likehome.app.ui.onboarding.OnboardingScreen
import com.likehome.app.ui.settings.SettingsScreen
import com.likehome.app.ui.setup.SetupChecks
import com.likehome.app.ui.setup.SetupScreen
import com.likehome.app.ui.setup.SetupStatus
import com.likehome.app.ui.theme.LikeHomeTheme

class MainActivity : ComponentActivity() {

    private var setup by mutableStateOf(SetupStatus())
    private var screen by mutableStateOf(Screen.Home)

    /** The system prompt shows at most twice; after that, notifications are fixed in Settings. */
    private var askedForNotifications = false

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { refreshSetup() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshSetup()
        if (!SettingsRepository.get(this).current.onboarded) screen = Screen.Onboarding

        setContent {
            LikeHomeTheme {
                when (screen) {
                    Screen.Onboarding -> OnboardingScreen(
                        status = setup,
                        onRequestNotifications = ::requestNotifications,
                        onFinish = ::finishOnboarding,
                    )
                    Screen.Home -> HomeScreen(
                        needsSetup = setup.needsAttention,
                        onFixSetup = { screen = Screen.Setup },
                        onOpenSettings = { screen = Screen.Settings },
                    )
                    Screen.Settings -> {
                        BackHandler { screen = Screen.Home }
                        SettingsScreen(
                            onBack = { screen = Screen.Home },
                            onOpenSetup = { screen = Screen.Setup },
                        )
                    }
                    Screen.Setup -> {
                        BackHandler { screen = Screen.Home }
                        SetupScreen(
                            status = setup,
                            onRequestNotifications = ::requestNotifications,
                            onBack = { screen = Screen.Home },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSetup()
        maybeAskForReview()
    }

    private fun refreshSetup() {
        setup = SetupChecks.read(this)
    }

    private fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !askedForNotifications) {
            askedForNotifications = true
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            SetupChecks.openNotificationSettings(this)
        }
    }

    private fun finishOnboarding(result: OnboardingResult) {
        SettingsRepository.get(this).update {
            it.copy(onboarded = true, style = result.style, language = result.language)
        }
        result.firstAlarm?.let { alarm ->
            AlarmScheduler.schedule(this, AlarmRepository.get(this).upsert(alarm))
        }
        screen = Screen.Home
    }

    /** Ask for a Play review once, after 5 mornings without a snooze. */
    private fun maybeAskForReview() {
        val settings = SettingsRepository.get(this)
        if (settings.current.reviewAsked) return
        if (Mornings.noSnoozeMornings(HistoryRepository.get(this).mornings.value) < REVIEW_AFTER) return
        settings.update { it.copy(reviewAsked = true) }
        val manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful && !isFinishing) manager.launchReviewFlow(this, request.result)
        }
    }

    private enum class Screen { Onboarding, Home, Settings, Setup }

    private companion object {
        const val REVIEW_AFTER = 5
    }
}
