package com.likehome.app.ui.family

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.functions.FirebaseFunctionsException
import com.likehome.app.R
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.FamilyAlarm
import com.likehome.app.core.FamilyEvent
import com.likehome.app.core.PairCode
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import com.likehome.app.family.FamilyLive
import com.likehome.app.family.toAlarm
import com.likehome.app.family.toFamily
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.repeatText
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.home.AlarmEditorSheet
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/**
 * Live family mode: link a parent with a code, then the parent sets the child's alarms,
 * sees how their morning went, and can ring them. Shown only when Firebase is set up.
 */
@Composable
fun FamilyLiveScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { FamilyLive.load(context) }
    val state by FamilyLive.state.collectAsState()
    var confirmUnlink by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PageScaffold(title = stringResource(R.string.family_live_title), onBack = onBack) {
        Text(stringResource(R.string.family_live_intro), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        when (state.role) {
            FamilyLive.Role.NONE -> ChooseRole()
            FamilyLive.Role.CHILD -> ChildPanel(state)
            FamilyLive.Role.PARENT -> ParentPanel(state)
        }
        if (state.role != FamilyLive.Role.NONE) {
            SecondaryButton(stringResource(R.string.family_unlink), onClick = { confirmUnlink = true })
        }
    }
    if (confirmUnlink) {
        AlertDialog(
            onDismissRequest = { confirmUnlink = false },
            text = { Text(stringResource(R.string.family_unlink_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmUnlink = false
                    scope.launch { FamilyLive.unlink(context) }
                }) { Text(stringResource(R.string.family_unlink), color = LH.Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmUnlink = false }) { Text(stringResource(R.string.cancel)) } },
            containerColor = LH.Roast,
            textContentColor = LH.Oat,
        )
    }
}

@Composable
private fun FamilyField(value: String, onChange: (String) -> Unit, hint: String, digits: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(hint) },
        singleLine = true,
        keyboardOptions = if (digits) KeyboardOptions(keyboardType = KeyboardType.NumberPassword) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LH.Saffron,
            unfocusedBorderColor = LH.Line,
            cursorColor = LH.Saffron,
            focusedTextColor = LH.Cream,
            unfocusedTextColor = LH.Cream,
            focusedPlaceholderColor = LH.Muted,
            unfocusedPlaceholderColor = LH.Muted,
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    )
}

@Composable
private fun ChooseRole() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }

    Card {
        Text(stringResource(R.string.family_im_child), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
        Text(stringResource(R.string.family_im_child_detail), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        FamilyField(name, { name = it.take(30) }, stringResource(R.string.family_your_name))
        PrimaryButton(stringResource(R.string.family_show_code), enabled = !busy, onClick = {
            busy = true
            error = null
            scope.launch {
                runCatching { FamilyLive.startPairing(context, name.trim()) }.onFailure { error = R.string.family_error }
                busy = false
            }
        })
    }
    Card {
        Text(stringResource(R.string.family_im_parent), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
        Text(stringResource(R.string.family_im_parent_detail), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        FamilyField(code, { code = it.filter(Char::isDigit).take(PairCode.LENGTH) }, stringResource(R.string.family_code_hint), digits = true)
        PrimaryButton(stringResource(R.string.family_link), enabled = !busy && PairCode.normalise(code) != null, onClick = {
            busy = true
            error = null
            scope.launch {
                runCatching { FamilyLive.join(context, code) }.onFailure { e ->
                    val bad = (e as? FirebaseFunctionsException)?.code in setOf(
                        FirebaseFunctionsException.Code.NOT_FOUND,
                        FirebaseFunctionsException.Code.DEADLINE_EXCEEDED,
                        FirebaseFunctionsException.Code.FAILED_PRECONDITION,
                        FirebaseFunctionsException.Code.INVALID_ARGUMENT,
                    )
                    error = if (bad) R.string.family_code_invalid else R.string.family_error
                }
                busy = false
            }
        })
    }
    error?.let { Text(stringResource(it), style = MaterialTheme.typography.bodyMedium, color = LH.Danger) }
}

@Composable
private fun ChildPanel(state: FamilyLive.State) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.codeExpiresAt) {
        while (now < state.codeExpiresAt) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    LaunchedEffect(Unit) { runCatching { FamilyLive.syncAlarms(context) } }
    if (state.code.isNotEmpty() && now < state.codeExpiresAt) {
        Card {
            SectionLabel(stringResource(R.string.family_code_title))
            Text(
                state.code.chunked(3).joinToString(" "),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp),
                color = LH.Saffron,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Text(stringResource(R.string.family_code_body), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        }
    } else {
        Card {
            Text(stringResource(R.string.family_child_linked), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            val fromFamily = AlarmRepository.get(context).alarms.collectAsState().value.count { it.fromFamily }
            if (fromFamily > 0) {
                Text(
                    pluralStringResource(R.plurals.family_alarm_count, fromFamily, fromFamily),
                    style = MaterialTheme.typography.titleMedium,
                    color = LH.Cream,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        SecondaryButton(stringResource(R.string.family_new_code), onClick = {
            scope.launch { runCatching { FamilyLive.startPairing(context, state.childName) } }
        })
    }
}

@Composable
private fun ParentPanel(state: FamilyLive.State) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val name = state.childName.ifBlank { stringResource(R.string.family_child_default) }
    val alarms by remember(state.familyId) { FamilyLive.alarms(state.familyId) }.collectAsState(initial = emptyList())
    val status by remember(state.familyId) { FamilyLive.status(state.familyId) }.collectAsState(initial = null)
    var editing by remember { mutableStateOf<Alarm?>(null) }
    val is24 = rememberIs24Hour()

    Card {
        SectionLabel(stringResource(R.string.family_parent_status, name))
        val s = status
        Text(
            if (s == null) stringResource(R.string.family_status_none) else statusText(name, s.event, s.level),
            style = MaterialTheme.typography.titleMedium,
            color = LH.Cream,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (s != null && s.at > 0) {
            val time = Instant.ofEpochMilli(s.at).atZone(ZoneId.systemDefault()).toLocalTime()
            Text(ClockFormat.full(time, is24), style = MaterialTheme.typography.bodyMedium, color = LH.Muted)
        }
    }
    PrimaryButton(stringResource(R.string.family_wake_up), onClick = {
        scope.launch {
            val ok = runCatching { FamilyLive.poke() }.isSuccess
            Toast.makeText(
                context,
                if (ok) context.getString(R.string.family_wake_up_sent, name) else context.getString(R.string.family_error),
                Toast.LENGTH_SHORT,
            ).show()
        }
    })

    SectionLabel(stringResource(R.string.family_alarms_for, name), Modifier.padding(top = 12.dp))
    if (alarms.isEmpty()) {
        Text(stringResource(R.string.family_no_alarms), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
    }
    alarms.forEach { a -> RemoteAlarmRow(a, is24, onClick = { editing = a.toAlarm() }, onToggle = { on ->
        scope.launch { runCatching { FamilyLive.saveAlarm(a.copy(enabled = on)) } }
    }) }
    SecondaryButton(stringResource(R.string.family_add_alarm), onClick = { editing = Alarm(id = AlarmRepository.NEW_ID, hour = 7, minute = 0, days = Alarm.WEEKDAYS) })

    editing?.let { alarm ->
        AlarmEditorSheet(
            initial = alarm,
            onDismiss = { editing = null },
            onSave = { edited ->
                editing = null
                scope.launch { runCatching { FamilyLive.saveAlarm(edited.copy(enabled = true).toFamily()) } }
            },
            onDelete = if (alarm.fromFamily) ({
                editing = null
                scope.launch { runCatching { FamilyLive.deleteAlarm(alarm.familyId) } }
            }) else null,
        )
    }
}

@Composable
private fun RemoteAlarmRow(alarm: FamilyAlarm, is24: Boolean, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val local = alarm.toAlarm()
    Card(Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ClockFormat.full(local.time, is24), style = MaterialTheme.typography.headlineSmall, color = LH.Cream)
                val repeat = local.repeatText(resources())
                Text(
                    listOf(repeat, alarm.label).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LH.Oat,
                )
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = LH.Saffron),
            )
        }
    }
}

@Composable
private fun statusText(name: String, event: FamilyEvent, level: Int): String = when (event) {
    FamilyEvent.RINGING -> stringResource(R.string.family_status_ringing, name)
    FamilyEvent.SNOOZED -> pluralStringResource(R.plurals.family_status_snoozed, level, name, level)
    FamilyEvent.AWAKE -> if (level == 0) stringResource(R.string.family_status_awake_proud, name)
        else pluralStringResource(R.plurals.family_status_awake, level, name, level)
    FamilyEvent.GAVE_UP -> stringResource(R.string.family_status_gave_up, name)
}
