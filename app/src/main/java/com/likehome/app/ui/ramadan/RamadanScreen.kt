package com.likehome.app.ui.ramadan

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.alarm.PhoneLocation
import com.likehome.app.alarm.Ramadan
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.RamadanCalendar
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.SwitchRow
import com.likehome.app.ui.common.dateFormat
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.theme.LH

/** Ramadan mode: the suhoor alarm before Fajr and the iftar nudge at Maghrib. */
@Composable
fun RamadanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context) }
    val settings by repo.settings.collectAsState()
    val is24 = rememberIs24Hour()
    var locating by remember { mutableStateOf(false) }
    var noLocation by remember { mutableStateOf(false) }

    fun apply() = Ramadan.schedule(context)
    fun locate() {
        locating = true
        PhoneLocation.get(context) { found ->
            locating = false
            noLocation = found == null
            if (found != null) {
                repo.update { it.copy(latitude = found.first, longitude = found.second) }
                apply()
            }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locate() else noLocation = true
    }
    fun askForLocation() {
        if (PhoneLocation.hasPermission(context)) locate() else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    PageScaffold(title = stringResource(R.string.ramadan_title), onBack = onBack) {
        Text(stringResource(R.string.ramadan_intro), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        SwitchRow(
            title = stringResource(R.string.ramadan_title),
            detail = stringResource(R.string.ramadan_switch_detail),
            checked = settings.ramadanEnabled,
            onChange = { on ->
                repo.update { it.copy(ramadanEnabled = on) }
                if (on && settings.latitude == null) askForLocation()
                apply()
            },
        )

        if (settings.ramadanEnabled) {
            val next = remember(settings) { Ramadan.next(context) }
            Card {
                when {
                    settings.latitude == null -> {
                        Text(stringResource(R.string.ramadan_need_location), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                        SecondaryButton(
                            stringResource(if (locating) R.string.ramadan_locating else R.string.ramadan_use_location),
                            onClick = { if (!locating) askForLocation() },
                        )
                        if (noLocation) Text(stringResource(R.string.ramadan_no_location), style = MaterialTheme.typography.bodySmall, color = LH.Danger)
                    }
                    next == null -> Text(stringResource(R.string.ramadan_none_soon), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                    else -> {
                        SectionLabel(stringResource(R.string.ramadan_next))
                        Text(
                            stringResource(
                                R.string.dot_pair,
                                next.suhoor.toLocalDate().format(resources().dateFormat("EEEdMMM")),
                                ClockFormat.full(next.suhoor.toLocalTime(), is24),
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            color = LH.Cream,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        Text(
                            stringResource(R.string.ramadan_times, ClockFormat.full(next.fajr, is24), ClockFormat.full(next.maghrib, is24)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LH.Oat,
                        )
                    }
                }
            }

            SectionLabel(stringResource(R.string.ramadan_lead), Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RamadanCalendar.leadChoices.forEach { m ->
                    ChoicePill(
                        text = stringResource(R.string.tile_minutes, m),
                        selected = m == settings.ramadanLead,
                        onClick = { repo.update { it.copy(ramadanLead = m) }; apply() },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            SwitchRow(
                title = stringResource(R.string.iftar_reminder),
                detail = stringResource(R.string.iftar_reminder_detail),
                checked = settings.iftarReminder,
                onChange = { on -> repo.update { it.copy(iftarReminder = on) }; apply() },
            )
            SwitchRow(
                title = stringResource(R.string.ramadan_auto),
                detail = stringResource(R.string.ramadan_auto_detail),
                checked = settings.ramadanAuto,
                onChange = { on -> repo.update { it.copy(ramadanAuto = on) }; apply() },
            )
            if (settings.latitude != null) {
                SecondaryButton(stringResource(R.string.ramadan_update_location), onClick = { askForLocation() })
            }
        }
    }
}
