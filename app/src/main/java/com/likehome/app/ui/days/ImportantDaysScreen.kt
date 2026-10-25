package com.likehome.app.ui.days

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.core.DayKind
import com.likehome.app.core.ImportantDay
import com.likehome.app.core.ImportantDays
import com.likehome.app.data.ImportantDayStore
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.dateFormat
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.theme.LH
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Exams, interviews, birthdays: Mom brings them up that morning. */
@Composable
fun ImportantDaysScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ImportantDayStore.get(context) }
    val days by store.days.collectAsState()
    var editing by remember { mutableStateOf<ImportantDay?>(null) }
    val today = LocalDate.now()

    PageScaffold(title = stringResource(R.string.days_title), onBack = onBack) {
        Text(stringResource(R.string.days_intro), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        val upcoming = ImportantDays.upcoming(days, today)
        if (upcoming.isEmpty()) {
            Text(stringResource(R.string.days_empty), style = MaterialTheme.typography.bodyMedium, color = LH.Muted)
        }
        upcoming.forEach { (day, on) ->
            Card(Modifier.clickable { editing = day }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(day.kind.icon, contentDescription = null, tint = LH.Saffron)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(day.title, style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                        val date = on.format(resources().dateFormat("EEEdMMMyyyy"))
                        Text(
                            if (day.yearly) stringResource(R.string.days_yearly_on, date) else date,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LH.Oat,
                        )
                    }
                }
            }
        }
        PrimaryButton(stringResource(R.string.days_add), onClick = { editing = ImportantDay(0, today.plusDays(1), DayKind.EXAM, "") })
    }

    editing?.let { day ->
        DayEditor(
            initial = day,
            onSave = { store.save(it); editing = null },
            onDelete = if (day.id != 0L) ({ store.delete(day.id); editing = null }) else null,
            onDismiss = { editing = null },
        )
    }
}

private val DayKind.icon: ImageVector get() = when (this) {
    DayKind.EXAM -> Icons.Outlined.School
    DayKind.INTERVIEW -> Icons.Outlined.Work
    DayKind.BIRTHDAY -> Icons.Outlined.Cake
    DayKind.TRIP -> Icons.Outlined.Flight
    DayKind.OTHER -> Icons.Outlined.Event
}

private val DayKind.labelRes: Int get() = when (this) {
    DayKind.EXAM -> R.string.day_exam
    DayKind.INTERVIEW -> R.string.day_interview
    DayKind.BIRTHDAY -> R.string.day_birthday
    DayKind.TRIP -> R.string.day_trip
    DayKind.OTHER -> R.string.day_other
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayEditor(initial: ImportantDay, onSave: (ImportantDay) -> Unit, onDelete: (() -> Unit)?, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(initial.title) }
    var kind by remember { mutableStateOf(initial.kind) }
    var date by remember { mutableStateOf(initial.date) }
    var yearly by remember { mutableStateOf(initial.yearly) }
    var picking by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial.id == 0L) R.string.days_add else R.string.days_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    DayKind.entries.take(3).forEach { k ->
                        ChoicePill(stringResource(k.labelRes), selected = k == kind, onClick = {
                            kind = k
                            if (k == DayKind.BIRTHDAY) yearly = true
                        }, modifier = Modifier.weight(1f))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    DayKind.entries.drop(3).forEach { k ->
                        ChoicePill(stringResource(k.labelRes), selected = k == kind, onClick = { kind = k }, modifier = Modifier.weight(1f))
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(40) },
                    placeholder = { Text(stringResource(R.string.days_title_hint)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LH.Saffron, unfocusedBorderColor = LH.Line, cursorColor = LH.Saffron,
                        focusedTextColor = LH.Cream, unfocusedTextColor = LH.Cream,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = { picking = true }) {
                    Text(date.format(resources().dateFormat("EEEdMMMyyyy")), color = LH.Saffron)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.days_every_year), style = MaterialTheme.typography.bodyMedium, color = LH.Cream, modifier = Modifier.weight(1f))
                    Switch(checked = yearly, onCheckedChange = { yearly = it }, colors = SwitchDefaults.colors(checkedTrackColor = LH.Saffron))
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(stringResource(R.string.delete), color = LH.Danger) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(ImportantDay(initial.id, date, kind, title.trim(), yearly)) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.save), color = if (title.isNotBlank()) LH.Saffron else LH.Muted)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        containerColor = LH.Roast,
        titleContentColor = LH.Cream,
        textContentColor = LH.Oat,
    )

    if (picking) {
        val utc = ZoneOffset.UTC
        val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(utc).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(utc).toLocalDate() }
                    picking = false
                }) { Text(stringResource(R.string.done)) }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = state) }
    }
}
