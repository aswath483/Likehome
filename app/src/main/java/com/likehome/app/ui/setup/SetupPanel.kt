package com.likehome.app.ui.setup

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.theme.LH

/**
 * Everything Mom needs from the phone, each with its fix. [onRequestNotifications]
 * shows the system permission prompt (it needs the activity).
 */
@Composable
fun SetupPanel(status: SetupStatus, onRequestNotifications: () -> Unit) {
    val context = LocalContext.current
    val guide = SetupChecks.guide

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SetupRow(
            title = "Alarm notifications",
            detail = "So Mom can ring and show on your screen.",
            ok = status.notifications,
            onFix = onRequestNotifications,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            SetupRow(
                title = "Show over the lock screen",
                detail = "So Mom's screen opens even when the phone is locked.",
                ok = status.fullScreen,
                onFix = { SetupChecks.openFullScreenSettings(context) },
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SetupRow(
                title = "Alarms & reminders",
                detail = "So Mom rings at the exact minute, not a few minutes late.",
                ok = status.exactAlarms,
                onFix = { SetupChecks.openExactAlarmSettings(context) },
            )
        }
        SetupRow(
            title = "Battery: unrestricted",
            detail = "Battery savers can stop alarms. Set Like Home to Unrestricted.",
            ok = status.batteryUnrestricted,
            onFix = { SetupChecks.openAppSettings(context) },
        )

        SectionLabel("On ${guide.brand}", Modifier.padding(top = 12.dp))
        Surface(
            color = LH.Roast,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LH.Line),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                guide.steps.forEachIndexed { i, step ->
                    Row {
                        Text("${i + 1}.", style = MaterialTheme.typography.bodyMedium, color = LH.Saffron, modifier = Modifier.width(22.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                    }
                }
                if (guide.autostartScreens.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    FixButton("Open autostart settings") { SetupChecks.openAutostart(context, guide) }
                }
            }
        }
    }
}

@Composable
private fun SetupRow(title: String, detail: String, ok: Boolean, onFix: () -> Unit) {
    Surface(
        color = if (ok) LH.Roast else LH.MyBubble,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (ok) LH.Line else LH.SaffronDeep),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                contentDescription = if (ok) "Done" else "Needs attention",
                tint = if (ok) LH.Muted else LH.Saffron,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
            if (!ok) {
                Spacer(Modifier.width(8.dp))
                FixButton("Allow", onFix)
            }
        }
    }
}

@Composable
private fun FixButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, LH.Saffron),
    ) {
        Text(text, color = LH.Saffron, style = MaterialTheme.typography.labelLarge)
    }
}

/** Full screen version, reached from Settings and the home banner. */
@Composable
fun SetupScreen(status: SetupStatus, onRequestNotifications: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(LH.Espresso)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = LH.Cream)
            }
            Text("Permissions & battery", style = MaterialTheme.typography.headlineSmall, color = LH.Cream)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Make sure nothing on your phone can silence Mom.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        Spacer(Modifier.height(16.dp))
        SetupPanel(status, onRequestNotifications)
        Spacer(Modifier.height(24.dp))
    }
}
