package com.likehome.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
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
import com.likehome.app.alarm.Reminders
import com.likehome.app.core.ReminderKind
import com.likehome.app.core.ReminderTimes
import com.likehome.app.core.Bedtime
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Snooze
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.Language
import com.likehome.app.stickers.StickerPack
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
import androidx.compose.ui.res.stringResource
import com.likehome.app.R
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.common.titleRes

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
    onOpenFaces: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context) }
    val settings by repo.settings.collectAsState()
    val is24 = rememberIs24Hour()

    PageScaffold(title = stringResource(R.string.settings_title), onBack = onBack) {
        SectionLabel(stringResource(R.string.mom_speaks_section), Modifier.padding(top = 8.dp))
        Language.entries.forEach { lang ->
            ChoiceCard(
                title = lang.label,
                detail = stringResource(R.string.quote, lang.sample),
                selected = settings.language == lang,
                onClick = { repo.update { it.copy(language = lang) } },
            )
        }

        SectionLabel(stringResource(R.string.look_and_feel), Modifier.padding(top = 12.dp))
        NavRow(Icons.Outlined.Palette, stringResource(R.string.styles_title), stringResource(R.string.dot_pair, stringResource(settings.character.nameRes), stringResource(settings.style.titleRes)), onClick = onOpenStyles)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NavRow(Icons.Outlined.Language, stringResource(R.string.app_language), stringResource(R.string.app_language_detail), onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                    )
                }
            })
        }
        NavRow(Icons.Outlined.CalendarMonth, stringResource(R.string.history_title), stringResource(R.string.your_mornings_detail), onClick = onOpenHistory)

        SectionLabel(stringResource(R.string.moms_habits), Modifier.padding(top = 12.dp))
        SwitchRow(
            title = stringResource(R.string.speaks_lines),
            detail = stringResource(R.string.speaks_lines_detail),
            checked = settings.momSpeaks,
            onChange = { on -> repo.update { it.copy(momSpeaks = on) } },
        )
        NavRow(Icons.Outlined.Face, stringResource(R.string.use_own_photos), stringResource(R.string.use_own_photos_settings_detail), onClick = onOpenFaces)
        NavRow(Icons.Outlined.Mic, stringResource(R.string.record_voice), stringResource(R.string.record_voice_detail), onClick = onOpenVoice)
        SwitchRow(
            title = stringResource(R.string.weekend_mode),
            detail = stringResource(R.string.weekend_mode_detail, Snooze.WEEKEND_MINUTES),
            checked = settings.weekendMode,
            onChange = { on -> repo.update { it.copy(weekendMode = on) } },
        )
        SwitchRow(
            title = stringResource(R.string.bedtime_reminder),
            detail = stringResource(R.string.bedtime_reminder_detail),
            checked = settings.bedtimeEnabled,
            onChange = { on ->
                repo.update { it.copy(bedtimeEnabled = on) }
                Reminders.schedule(context, ReminderKind.BEDTIME)
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
                            Reminders.schedule(context, ReminderKind.BEDTIME)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (!is24) Text(stringResource(R.string.bedtime_hint), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
        }

        SwitchRow(
            title = stringResource(R.string.water_reminders),
            detail = stringResource(R.string.water_reminders_detail),
            checked = settings.waterEnabled,
            onChange = { on ->
                repo.update { it.copy(waterEnabled = on) }
                Reminders.schedule(context, ReminderKind.WATER)
            },
        )
        SwitchRow(
            title = stringResource(R.string.breakfast_reminder),
            detail = stringResource(R.string.breakfast_reminder_detail),
            checked = settings.breakfastEnabled,
            onChange = { on ->
                repo.update { it.copy(breakfastEnabled = on) }
                Reminders.schedule(context, ReminderKind.BREAKFAST)
            },
        )
        if (settings.breakfastEnabled) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ReminderTimes.breakfastChoices.forEach { t ->
                    ChoicePill(
                        text = ClockFormat.digits(t, is24),
                        selected = t.hour == settings.breakfastHour && t.minute == settings.breakfastMinute,
                        onClick = {
                            repo.update { it.copy(breakfastHour = t.hour, breakfastMinute = t.minute) }
                            Reminders.schedule(context, ReminderKind.BREAKFAST)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        SectionLabel(stringResource(R.string.reliability), Modifier.padding(top = 12.dp))
        NavRow(
            Icons.Outlined.BatteryChargingFull,
            stringResource(R.string.setup_title),
            stringResource(if (setup.needsAttention) R.string.reliability_bad else R.string.reliability_good),
            onClick = onOpenSetup,
        )

        SectionLabel(stringResource(R.string.try_it), Modifier.padding(top = 12.dp))
        Text(
            stringResource(R.string.try_it_detail),
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        PrimaryButton(stringResource(R.string.preview_mom), onClick = { AlarmService.preview(context) })

        SectionLabel(stringResource(R.string.app_name), Modifier.padding(top = 12.dp))
        NavRow(Icons.Outlined.EmojiEmotions, stringResource(R.string.whatsapp_stickers), stringResource(R.string.whatsapp_stickers_detail), onClick = { StickerPack.addToWhatsApp(context) })
        NavRow(Icons.Outlined.Info, stringResource(R.string.about), stringResource(R.string.about_detail), onClick = onOpenAbout)
    }
}
