package com.likehome.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.likehome.app.alarm.AlarmService
import com.likehome.app.alarm.BedtimeReminder
import com.likehome.app.core.Bedtime
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Snooze
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.Language
import com.likehome.app.ui.common.ChoiceCard
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.common.NavRow
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.SwitchRow
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.setup.SetupStatus
import com.likehome.app.ui.theme.LH

/** The hub for every choice, and the way to every other screen. */
@Composable
fun SettingsScreen(
    setup: SetupStatus,
    onBack: () -> Unit,
    onOpenSetup: () -> Unit,
    onOpenStyles: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenVoice: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context) }
    val settings by repo.settings.collectAsState()
    val is24 = rememberIs24Hour()

    PageScaffold(title = "Settings", onBack = onBack) {
        SectionLabel("Mom speaks", Modifier.padding(top = 8.dp))
        Language.entries.forEach { lang ->
            ChoiceCard(
                title = lang.label,
                detail = "\"${lang.sample}\"",
                selected = settings.language == lang,
                onClick = { repo.update { it.copy(language = lang) } },
            )
        }

        SectionLabel("Look and feel", Modifier.padding(top = 12.dp))
        NavRow(Icons.Outlined.Palette, "Styles & characters", "${settings.character.displayName} · ${settings.style.title}", onClick = onOpenStyles)
        NavRow(Icons.Outlined.CalendarMonth, "Your mornings", "Streaks, badges and report cards", onClick = onOpenHistory)

        SectionLabel("Mom's habits", Modifier.padding(top = 12.dp))
        SwitchRow(
            title = "Mom speaks her lines",
            detail = "Reads them aloud with your phone's voice while the alarm rings",
            checked = settings.momSpeaks,
            onChange = { on -> repo.update { it.copy(momSpeaks = on) } },
        )
        NavRow(Icons.Outlined.Mic, "Record a voice", "Real Mom, real drama: your own clips for each level", onClick = onOpenVoice)
        SwitchRow(
            title = "Weekend mode",
            detail = "Snoozes last ${Snooze.WEEKEND_MINUTES} minutes on Saturday and Sunday",
            checked = settings.weekendMode,
            onChange = { on -> repo.update { it.copy(weekendMode = on) } },
        )
        SwitchRow(
            title = "Bedtime reminder",
            detail = "\"Beta, sleep now.\" A nudge every night",
            checked = settings.bedtimeEnabled,
            onChange = { on ->
                repo.update { it.copy(bedtimeEnabled = on) }
                BedtimeReminder.schedule(context)
            },
        )
        if (settings.bedtimeEnabled) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Bedtime.choices.forEach { t ->
                    ChoicePill(
                        text = ClockFormat.digits(t, is24),
                        selected = t.hour == settings.bedtimeHour && t.minute == settings.bedtimeMinute,
                        onClick = {
                            repo.update { it.copy(bedtimeHour = t.hour, bedtimeMinute = t.minute) }
                            BedtimeReminder.schedule(context)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (!is24) Text("At night, 10 pm to midnight", style = MaterialTheme.typography.bodySmall, color = LH.Muted)
        }

        SectionLabel("Reliability", Modifier.padding(top = 12.dp))
        NavRow(
            Icons.Outlined.BatteryChargingFull,
            "Permissions & battery",
            if (setup.needsAttention) "Something could stop Mom. Tap to fix." else "All set. Nothing can silence Mom.",
            onClick = onOpenSetup,
        )

        SectionLabel("Try it", Modifier.padding(top = 12.dp))
        Text(
            "Ring a test alarm now. Snoozing a test just stops it, and it doesn't count in your streak.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        PrimaryButton("Preview Mom now", onClick = { AlarmService.preview(context) })

        SectionLabel("Like Home", Modifier.padding(top = 12.dp))
        NavRow(Icons.Outlined.Info, "About", "Privacy, fonts and version", onClick = onOpenAbout)
    }
}
