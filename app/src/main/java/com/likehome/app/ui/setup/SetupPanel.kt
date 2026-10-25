package com.likehome.app.ui.setup

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.theme.LH
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import com.likehome.app.R
import com.likehome.app.ui.common.brandRes
import com.likehome.app.ui.common.stepsRes

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
            title = stringResource(R.string.setup_notifications),
            detail = stringResource(R.string.setup_notifications_detail),
            ok = status.notifications,
            onFix = onRequestNotifications,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            SetupRow(
                title = stringResource(R.string.setup_lock),
                detail = stringResource(R.string.setup_lock_detail),
                ok = status.fullScreen,
                onFix = { SetupChecks.openFullScreenSettings(context) },
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SetupRow(
                title = stringResource(R.string.setup_exact),
                detail = stringResource(R.string.setup_exact_detail),
                ok = status.exactAlarms,
                onFix = { SetupChecks.openExactAlarmSettings(context) },
            )
        }
        SetupRow(
            title = stringResource(R.string.setup_battery),
            detail = stringResource(R.string.setup_battery_detail),
            ok = status.batteryUnrestricted,
            onFix = { SetupChecks.openAppSettings(context) },
        )

        SectionLabel(stringResource(R.string.setup_on_brand, stringResource(guide.brandRes)), Modifier.padding(top = 12.dp))
        Surface(
            color = LH.Roast,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LH.Line),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stringArrayResource(guide.stepsRes).forEachIndexed { i, step ->
                    Row {
                        Text(stringResource(R.string.step_number, i + 1), style = MaterialTheme.typography.bodyMedium, color = LH.Saffron, modifier = Modifier.width(22.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                    }
                }
                if (guide.autostartScreens.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    FixButton(stringResource(R.string.setup_autostart)) { SetupChecks.openAutostart(context, guide) }
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
                contentDescription = stringResource(if (ok) R.string.done else R.string.needs_attention),
                tint = if (ok) LH.Muted else LH.Saffron,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
            if (!ok) {
                Spacer(Modifier.width(8.dp))
                FixButton(stringResource(R.string.allow), onFix)
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
    PageScaffold(title = stringResource(R.string.setup_title), onBack = onBack) {
        Text(
            stringResource(if (status.needsAttention) R.string.setup_needs else R.string.setup_ok),
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        Spacer(Modifier.height(4.dp))
        SetupPanel(status, onRequestNotifications)
    }
}
