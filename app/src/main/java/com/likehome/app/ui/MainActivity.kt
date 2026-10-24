package com.likehome.app.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.likehome.app.ui.home.HomeScreen
import com.likehome.app.ui.settings.SettingsScreen
import com.likehome.app.ui.theme.LikeHomeTheme

class MainActivity : ComponentActivity() {

    private var notificationsAllowed by mutableStateOf(true)
    private var fullScreenAllowed by mutableStateOf(true)
    private var screen by mutableStateOf(Screen.Home)

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { refreshPermissions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshPermissions()
        if (!notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            LikeHomeTheme {
                when (screen) {
                    Screen.Home -> HomeScreen(
                        needsSetup = !notificationsAllowed || !fullScreenAllowed,
                        onFixSetup = ::openSetup,
                        onOpenSettings = { screen = Screen.Settings },
                    )
                    Screen.Settings -> {
                        BackHandler { screen = Screen.Home }
                        SettingsScreen(onBack = { screen = Screen.Home })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
    }

    private fun refreshPermissions() {
        notificationsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        fullScreenAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
    }

    private enum class Screen { Home, Settings }

    private fun openSetup() {
        val intent = when {
            !notificationsAllowed -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName"))
            else -> return
        }
        runCatching { startActivity(intent) }
    }
}
