package com.likehome.app.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import com.likehome.app.data.HolidayStore
import com.likehome.app.alarm.AlarmService
import com.likehome.app.alarm.Nap
import com.likehome.app.core.Mornings
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.about.AboutScreen
import com.likehome.app.ui.faces.FacesScreen
import com.likehome.app.ui.history.HistoryScreen
import com.likehome.app.ui.home.HomeScreen
import com.likehome.app.ui.nav.NavHost
import com.likehome.app.ui.nav.Route
import com.likehome.app.ui.nav.rememberNavigator
import com.likehome.app.ui.onboarding.OnboardingResult
import com.likehome.app.ui.onboarding.OnboardingScreen
import com.likehome.app.ui.settings.SettingsScreen
import com.likehome.app.ui.setup.SetupChecks
import com.likehome.app.ui.setup.SetupScreen
import com.likehome.app.ui.setup.SetupStatus
import com.likehome.app.ui.styles.StylesScreen
import com.likehome.app.ui.theme.LikeHomeTheme
import com.likehome.app.ui.voice.RecordVoiceScreen
import com.likehome.app.ui.sleep.SleepScreen
import com.likehome.app.ui.family.VoiceImportScreen
import com.likehome.app.ui.family.ChallengeScreen
import com.likehome.app.ui.family.FamilyLiveScreen
import com.likehome.app.ui.ramadan.RamadanScreen
import com.likehome.app.ui.days.ImportantDaysScreen
import com.likehome.app.ui.family.incomingVoiceFile
import androidx.compose.runtime.LaunchedEffect
import com.likehome.app.R

class MainActivity : ComponentActivity() {

    private var setup by mutableStateOf(SetupStatus())
    /** Set by the "New alarm" shortcut; Home opens the editor and clears it. */
    private var openNewAlarm by mutableStateOf(false)
    /** Set when the sleep sounds notification is tapped; the next frame opens that screen. */
    private var openSleep by mutableStateOf(false)
    /** A voice note shared into the app (e.g. Mom's, from WhatsApp), waiting to be placed. */
    private var incomingVoice by mutableStateOf(false)
    /** Set when a family status notification is tapped. */
    private var openFamily by mutableStateOf(false)
    private var openChallenge by mutableStateOf(false)
    private var openHistory by mutableStateOf(false)

    /** The system prompt shows at most twice; after that, notifications are fixed in Settings. */
    private var askedForNotifications = false

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { refreshSetup() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Re-arm everything: meetings may have moved, and after a restore (from a backup
        // file or Google backup on a new phone) nothing is armed yet.
        AlarmScheduler.rescheduleAll(this)
        HolidayStore.refresh(this)
        // The app is always dark, so system bar icons are always light, whatever the phone's theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        refreshSetup()
        handleShortcut(intent)
        val start = if (SettingsRepository.get(this).current.onboarded) Route.Home else Route.Onboarding

        setContent {
            LikeHomeTheme {
                val nav = rememberNavigator(start)
                LaunchedEffect(incomingVoice) {
                    if (incomingVoice) {
                        nav.go(Route.VoiceImport)
                        incomingVoice = false
                    }
                }
                LaunchedEffect(openHistory) {
                    if (openHistory) {
                        nav.go(Route.History)
                        openHistory = false
                    }
                }
                LaunchedEffect(openChallenge) {
                    if (openChallenge) {
                        nav.go(Route.Challenge)
                        openChallenge = false
                    }
                }
                LaunchedEffect(openFamily) {
                    if (openFamily) {
                        nav.go(Route.FamilyLive)
                        openFamily = false
                    }
                }
                LaunchedEffect(openSleep) {
                    if (openSleep) {
                        nav.go(Route.Sleep)
                        openSleep = false
                    }
                }
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
                            openNewAlarm = openNewAlarm,
                            onNewAlarmOpened = { openNewAlarm = false },
                        )
                        Route.Settings -> SettingsScreen(
                            setup = setup,
                            onBack = { nav.back() },
                            onOpenSetup = { nav.go(Route.Setup) },
                            onOpenStyles = { nav.go(Route.Styles) },
                            onOpenHistory = { nav.go(Route.History) },
                            onOpenAbout = { nav.go(Route.About) },
                            onOpenVoice = { nav.go(Route.Voice) },
                            onOpenFaces = { nav.go(Route.Faces) },
                            onOpenSleep = { nav.go(Route.Sleep) },
                            onOpenFamily = { nav.go(Route.FamilyLive) },
                            onOpenRamadan = { nav.go(Route.Ramadan) },
                            onOpenDays = { nav.go(Route.ImportantDays) },
                            onOpenChallenge = { nav.go(Route.Challenge) },
                        )
                        Route.Sleep -> SleepScreen(onBack = { nav.back() })
                        Route.FamilyLive -> FamilyLiveScreen(onBack = { nav.back() })
                        Route.Ramadan -> RamadanScreen(onBack = { nav.back() })
                        Route.ImportantDays -> ImportantDaysScreen(onBack = { nav.back() })
                        Route.Challenge -> ChallengeScreen(onBack = { nav.back() })
                        Route.VoiceImport -> VoiceImportScreen(incomingVoiceFile(this@MainActivity), onDone = { nav.back() })
                        Route.Setup -> SetupScreen(
                            status = setup,
                            onRequestNotifications = ::requestNotifications,
                            onBack = { nav.back() },
                        )
                        Route.History -> HistoryScreen(onBack = { nav.back() })
                        Route.Styles -> StylesScreen(onBack = { nav.back() }, onOpenFaces = { nav.go(Route.Faces) })
                        Route.Faces -> FacesScreen(onBack = { nav.back() })
                        Route.Voice -> RecordVoiceScreen(onBack = { nav.back() })
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcut(intent)
    }

    /** Copies a shared voice note in now: the permission to read it ends with this intent. */
    private fun receiveVoice(intent: Intent) {
        @Suppress("DEPRECATION")
        val uri = intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM) ?: return
        val copied = runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                incomingVoiceFile(this).outputStream().use { out -> input.copyTo(out) }
            }
        }.getOrNull() != null
        if (copied) incomingVoice = true
        else Toast.makeText(this, R.string.photo_failed, Toast.LENGTH_LONG).show()
    }

    /** App shortcuts (long-press the icon): power nap, new alarm, test ring. */
    private fun handleShortcut(intent: Intent?) {
        if (!SettingsRepository.get(this).current.onboarded) return
        when (intent?.action) {
            ACTION_NAP -> {
                val set = Nap.toggle(this)
                Toast.makeText(
                    this,
                    if (set) getString(R.string.nap_set, Nap.MINUTES) else getString(R.string.nap_cancelled),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            ACTION_NEW_ALARM -> openNewAlarm = true
            ACTION_PREVIEW -> AlarmService.preview(this)
            ACTION_SLEEP_SOUNDS -> openSleep = true
            ACTION_FAMILY -> openFamily = true
            ACTION_CHALLENGE -> openChallenge = true
            ACTION_HISTORY -> openHistory = true
            Intent.ACTION_SEND -> if (intent.type?.startsWith("audio/") == true) receiveVoice(intent)
        }
        // Handled once; a rotation must not repeat it.
        intent?.action = null
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
            it.copy(onboarded = true, character = result.character, style = result.style, language = result.language)
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

    companion object {
        const val REVIEW_AFTER = 5
        const val ACTION_NAP = "com.likehome.app.action.NAP"
        const val ACTION_NEW_ALARM = "com.likehome.app.action.NEW_ALARM"
        const val ACTION_PREVIEW = "com.likehome.app.action.PREVIEW"
        const val ACTION_SLEEP_SOUNDS = "com.likehome.app.action.SLEEP_SOUNDS"
        const val ACTION_FAMILY = "com.likehome.app.action.FAMILY"
        const val ACTION_CHALLENGE = "com.likehome.app.action.CHALLENGE"
        const val ACTION_HISTORY = "com.likehome.app.action.HISTORY"
    }
}
