package com.likehome.app.ui.setup

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.BatteryGuide
import com.likehome.app.core.BrandGuide

/** What the phone currently lets Mom do. Re-read whenever the app comes back to front. */
data class SetupStatus(
    val notifications: Boolean = true,
    val fullScreen: Boolean = true,
    val exactAlarms: Boolean = true,
    val batteryUnrestricted: Boolean = true,
) {
    /** Missing any of these can stop the alarm from ringing or showing. */
    val needsAttention: Boolean get() = !notifications || !fullScreen || !exactAlarms
}

object SetupChecks {

    fun read(context: Context): SetupStatus = SetupStatus(
        notifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED,
        fullScreen = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent(),
        exactAlarms = AlarmScheduler.canRingExactly(context),
        batteryUnrestricted = context.getSystemService(PowerManager::class.java)
            .isIgnoringBatteryOptimizations(context.packageName),
    )

    val guide: BrandGuide get() = BatteryGuide.forManufacturer(Build.MANUFACTURER, Build.BRAND)

    fun openNotificationSettings(context: Context) = open(
        context,
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
    )

    fun openFullScreenSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            open(context, Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri(context)))
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            open(context, Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri(context)))
        }
    }

    /** The app's own settings page, where most phones keep the battery choice. */
    fun openAppSettings(context: Context) =
        open(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(context)))

    /** Tries the maker's autostart screen; falls back to the app's settings page. */
    fun openAutostart(context: Context, guide: BrandGuide) {
        val opened = guide.autostartScreens.any { (pkg, cls) ->
            open(context, Intent().setComponent(ComponentName(pkg, cls)))
        }
        if (!opened) openAppSettings(context)
    }

    private fun packageUri(context: Context) = Uri.parse("package:${context.packageName}")

    private fun open(context: Context, intent: Intent): Boolean = runCatching {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.isSuccess
}
