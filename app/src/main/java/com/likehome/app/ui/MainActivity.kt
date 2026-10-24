package com.likehome.app.ui

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.review.ReviewManagerFactory
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.Mornings
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.about.AboutScreen
import com.likehome.app.ui.history.HistoryScreen
import com.likehome.app.ui.home.HomeScreen
import com.likehome.app.ui.nav.NavHost
import com.likehome.app.ui.nav.Route
import com.likehome.app.ui.nav.rememberNavigator
import com.likehome.app.ui.onboarding.OnboardingResult
import com.likehome.app.ui.onboarding.OnboardingScreen
import com.likehome.app.ui.premium.PremiumScreen
import com.likehome.app.ui.settings.SettingsScreen
import com.likehome.app.ui.setup.SetupChecks
import com.likehome.app.ui.setup.SetupScreen
import com.likehome.app.ui.setup.SetupStatus
import com.likehome.app.ui.styles.StylesScreen
import com.likehome.app.ui.theme.LikeHomeTheme

class MainActivity : ComponentActivity() {

    private var setup by mutableStateOf(SetupStatus())

    /** The system prompt shows at most twice; after that, notifications are fixed in Settings. */
    private var askedForNotifications = false

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { refreshSetup() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always dark, so system bar icons are always light, whatever the phone's theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        refreshSetup()
        val start = if (SettingsRepository.get(this).current.onboarded) Route.Home else Route.Onboarding

        setContent {
            LikeHomeTheme {
                val nav = rememberNavigator(start)
                NavHost(nav) { route ->
                    when (route) {
                        Route.Onboarding -> OnboardingScreen(
                            status = setup,
                            onRequestNotifications = ::requestNotifications,
                            onFinish = { result ->
                                finishOnboarding(result)
                                nav.reset(Route.Home)
                            },
                        )
                        Route.Home -> HomeScreen(
                            needsSetup = setup.needsAttention,
                            onFixSetup = { nav.go(Route.Setup) },
                            onOpenSettings = { nav.go(Route.Settings) },
                            onOpenHistory = { nav.go(Route.History) },
                            onOpenStyles = { nav.go(Route.Styles) },
                        )
                        Route.Settings -> SettingsScreen(
                            setup = setup,
                            onBack = { nav.back() },
                            onOpenSetup = { nav.go(Route.Setup) },
                            onOpenStyles = { nav.go(Route.Styles) },
                            onOpenHistory = { nav.go(Route.History) },
                            onOpenPremium = { nav.go(Route.Premium) },
                            onOpenAbout = { nav.go(Route.About) },
                        )
                        Route.Setup -> SetupScreen(
                            status = setup,
                            onRequestNotifications = ::requestNotifications,
                            onBack = { nav.back() },
                        )
                        Route.History -> HistoryScreen(onBack = { nav.back() })
                        Route.Styles -> StylesScreen(onBack = { nav.back() }, onOpenPremium = { nav.go(Route.Premium) })
                        Route.Premium -> PremiumScreen(onBack = { nav.back() })
                        Route.About -> AboutScreen(
                            onBack = { nav.back() },
                            onReplayIntro = { nav.reset(Route.Onboarding) },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSetup()
        // If Mom is ringing but her screen got hidden (no notification permission, or the
        // user pressed Home), opening the app takes them straight back to her.
        if (AlarmService.isRinging) AlarmService.reopenRingScreen(this)
        else maybeAskForReview()
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

    private companion object {
        const val REVIEW_AFTER = 5
    }
}
