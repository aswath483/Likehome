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
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.BeachAccess
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.ui.common.dateFormat
import com.likehome.app.ui.common.resources
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.runtime.LaunchedEffect
import com.likehome.app.family.FamilyLive
import com.likehome.app.ui.family.FamilyNumberDialog
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Restore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.likehome.app.data.Backup
import androidx.compose.material.icons.outlined.Bedtime
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
    onOpenSleep: () -> Unit,
    onOpenFamily: () -> Unit,
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
        var pickingVacation by remember { mutableStateOf(false) }
        val paused = settings.pausedUntil
        NavRow(
            Icons.Outlined.BeachAccess,
            stringResource(R.string.vacation_mode),
            if (paused == null) stringResource(R.string.vacation_off)
            else stringResource(R.string.vacation_until, paused.format(resources().dateFormat("EEEdMMM"))),
            onClick = { pickingVacation = true },
        )
        if (paused != null) {
            TextButton(onClick = {
                repo.update { it.copy(pausedUntil = null) }
                AlarmScheduler.rescheduleAll(context)
            }) { Text(stringResource(R.string.vacation_resume), color = LH.Saffron) }
        }
        if (pickingVacation) {
            VacationPicker(
                initial = paused,
                onPick = { day ->
                    pickingVacation = false
                    repo.update { it.copy(pausedUntil = day) }
                    AlarmScheduler.rescheduleAll(context)
                },
                onDismiss = { pickingVacation = false },
            )
        }
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
        NavRow(Icons.Outlined.Bedtime, stringResource(R.string.sleep_title), stringResource(R.string.sleep_settings_detail), onClick = onOpenSleep)
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

        SectionLabel(stringResource(R.string.family), Modifier.padding(top = 12.dp))
        if (remember { FamilyLive.available(context) }) {
            LaunchedEffect(Unit) { FamilyLive.load(context) }
            val family by FamilyLive.state.collectAsState()
            NavRow(
                Icons.Outlined.Groups,
                stringResource(R.string.family_live_title),
                when (family.role) {
                    FamilyLive.Role.NONE -> stringResource(R.string.family_live_detail_none)
                    FamilyLive.Role.CHILD -> stringResource(R.string.family_live_detail_child)
                    FamilyLive.Role.PARENT -> stringResource(R.string.family_live_detail_parent, family.childName.ifBlank { stringResource(R.string.family_child_default) })
                },
                onClick = onOpenFamily,
            )
        }
        var editingNumber by remember { mutableStateOf(false) }
        NavRow(
            Icons.Outlined.FamilyRestroom,
            stringResource(R.string.family_number_title),
            settings.familyNumber.ifBlank { stringResource(R.string.family_number_unset) },
            onClick = { editingNumber = true },
        )
        Text(stringResource(R.string.family_share_voice_hint), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
        if (editingNumber) {
            FamilyNumberDialog(
                current = settings.familyNumber,
                onSave = { n -> repo.update { it.copy(familyNumber = n) }; editingNumber = false },
                onDismiss = { editingNumber = false },
            )
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

        SectionLabel(stringResource(R.string.your_data), Modifier.padding(top = 12.dp))
        val saveBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            if (uri != null) {
                val ok = Backup.export(context, uri)
                Toast.makeText(context, if (ok) R.string.backup_saved else R.string.backup_failed, Toast.LENGTH_LONG).show()
            }
        }
        var restoreFrom by remember { mutableStateOf<Uri?>(null) }
        val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> restoreFrom = uri }
        NavRow(Icons.Outlined.Backup, stringResource(R.string.backup_save), stringResource(R.string.backup_save_detail), onClick = {
            runCatching { saveBackup.launch(Backup.fileName()) }
        })
        NavRow(Icons.Outlined.Restore, stringResource(R.string.backup_restore), stringResource(R.string.backup_restore_detail), onClick = {
            runCatching { pickBackup.launch(arrayOf("application/zip", "application/octet-stream")) }
        })
        restoreFrom?.let { uri ->
            AlertDialog(
                onDismissRequest = { restoreFrom = null },
                title = { Text(stringResource(R.string.backup_confirm_title)) },
                text = { Text(stringResource(R.string.backup_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        restoreFrom = null
                        if (Backup.restore(context, uri)) {
                            Toast.makeText(context, R.string.backup_restored, Toast.LENGTH_LONG).show()
                            Backup.restartApp(context)
                        } else {
                            Toast.makeText(context, R.string.backup_not_valid, Toast.LENGTH_LONG).show()
                        }
                    }) { Text(stringResource(R.string.backup_restore_confirm), color = LH.Danger) }
                },
                dismissButton = { TextButton(onClick = { restoreFrom = null }) { Text(stringResource(R.string.cancel)) } },
                containerColor = LH.Roast,
                titleContentColor = LH.Cream,
                textContentColor = LH.Oat,
            )
        }

        SectionLabel(stringResource(R.string.app_name), Modifier.padding(top = 12.dp))
        NavRow(Icons.Outlined.EmojiEmotions, stringResource(R.string.whatsapp_stickers), stringResource(R.string.whatsapp_stickers_detail), onClick = { StickerPack.addToWhatsApp(context) })
        NavRow(Icons.Outlined.Info, stringResource(R.string.about), stringResource(R.string.about_detail), onClick = onOpenAbout)
    }
}

/** Picks the last day of a vacation: repeating alarms stay quiet until the day after. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VacationPicker(initial: LocalDate?, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val today = LocalDate.now()
    val utc = ZoneOffset.UTC
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initial ?: today.plusDays(6)).atStartOfDay(utc).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                !Instant.ofEpochMilli(utcTimeMillis).atZone(utc).toLocalDate().isBefore(today)
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(utc).toLocalDate()) } },
                enabled = state.selectedDateMillis != null,
            ) { Text(stringResource(R.string.vacation_pause)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) {
        DatePicker(state = state, title = { Text(stringResource(R.string.vacation_pick), modifier = Modifier.padding(16.dp)) })
    }
}
