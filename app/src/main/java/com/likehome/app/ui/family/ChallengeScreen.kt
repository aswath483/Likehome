package com.likehome.app.ui.family

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.likehome.app.core.Challenge
import com.likehome.app.core.PairCode
import com.likehome.app.core.Standing
import com.likehome.app.family.ChallengeLive
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.dateFormat
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Streak challenge: start one for 7, 14 or 30 days and invite up to four friends with a
 * code, then watch who gets up without snoozing. Shown only when Firebase is set up.
 */
@Composable
fun ChallengeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { ChallengeLive.load(context) }
    val state by ChallengeLive.state.collectAsState()
    var confirmLeave by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PageScaffold(title = stringResource(R.string.challenge_title), onBack = onBack) {
        Text(stringResource(R.string.challenge_intro), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        if (state.active) {
            Board(state)
            SecondaryButton(stringResource(R.string.challenge_leave), onClick = { confirmLeave = true })
        } else {
            StartOrJoin(state.myName)
        }
    }
    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            text = { Text(stringResource(R.string.challenge_leave_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    scope.launch { ChallengeLive.leave(context) }
                }) { Text(stringResource(R.string.challenge_leave), color = LH.Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text(stringResource(R.string.cancel)) } },
            containerColor = LH.Roast,
            textContentColor = LH.Oat,
        )
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, hint: String, digits: Boolean = false) {
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
private fun StartOrJoin(savedName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(savedName) }
    var length by rememberSaveable { mutableIntStateOf(Challenge.lengths.first()) }
    var code by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    val named = Challenge.cleanName(name).isNotEmpty()

    Field(name, { name = it.take(Challenge.NAME_MAX) }, stringResource(R.string.challenge_your_name))
    Card {
        Text(stringResource(R.string.challenge_start), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
        Text(stringResource(R.string.challenge_start_detail), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Challenge.lengths.forEach { d ->
                ChoicePill(pluralStringResource(R.plurals.challenge_days, d, d), selected = d == length, onClick = { length = d }, modifier = Modifier.weight(1f))
            }
        }
        PrimaryButton(stringResource(R.string.challenge_start_button), enabled = !busy && named, onClick = {
            busy = true
            error = null
            scope.launch {
                runCatching { ChallengeLive.start(context, name, length) }.onFailure { error = R.string.family_error }
                busy = false
            }
        })
    }
    Card {
        Text(stringResource(R.string.challenge_join), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
        Text(stringResource(R.string.challenge_join_detail), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        Field(code, { code = it.filter(Char::isDigit).take(PairCode.LENGTH) }, stringResource(R.string.family_code_hint), digits = true)
        PrimaryButton(stringResource(R.string.challenge_join_button), enabled = !busy && named && PairCode.normalise(code) != null, onClick = {
            busy = true
            error = null
            scope.launch {
                runCatching { ChallengeLive.join(context, code, name) }.onFailure { e ->
                    error = when ((e as? FirebaseFunctionsException)?.code) {
                        FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> R.string.challenge_full
                        FirebaseFunctionsException.Code.NOT_FOUND,
                        FirebaseFunctionsException.Code.DEADLINE_EXCEEDED,
                        FirebaseFunctionsException.Code.FAILED_PRECONDITION,
                        FirebaseFunctionsException.Code.INVALID_ARGUMENT -> R.string.family_code_invalid
                        else -> R.string.family_error
                    }
                }
                busy = false
            }
        })
    }
    error?.let { Text(stringResource(it), style = MaterialTheme.typography.bodyMedium, color = LH.Danger) }
}

@Composable
private fun Board(state: ChallengeLive.State) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val start = state.start ?: return
    val board by remember(state.challengeId) { ChallengeLive.board(state.challengeId) }.collectAsState(initial = null)
    val today = remember { LocalDate.now() }
    val left = Challenge.daysLeft(start, state.length, today)
    val over = Challenge.isOver(start, state.length, today)
    val me = remember { ChallengeLive.me() }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.codeExpiresAt) {
        while (now < state.codeExpiresAt) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    val standings = board?.let { Challenge.standings(it.names, it.mornings, start, state.length) }.orEmpty()
    val leaders = Challenge.leaders(standings)
    Card {
        SectionLabel(
            if (over) stringResource(R.string.challenge_over)
            else pluralStringResource(R.plurals.challenge_days_left, left, left),
        )
        Text(
            stringResource(
                R.string.challenge_dates,
                start.format(resources().dateFormat("dMMM")),
                Challenge.lastDay(start, state.length).format(resources().dateFormat("dMMM")),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        if (over && leaders.isNotEmpty()) {
            val winners = standings.filter { it.uid in leaders }.joinToString(", ") { it.name.ifBlank { "?" } }
            Text(
                if (me in leaders) stringResource(R.string.challenge_you_won) else stringResource(R.string.challenge_winner, winners),
                style = MaterialTheme.typography.titleLarge,
                color = LH.Saffron,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        standings.forEachIndexed { i, s -> StandingRow(i + 1, s, isMe = s.uid == me, leads = s.uid in leaders) }
        if (board == null) Text(stringResource(R.string.challenge_loading), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
    }

    if (!over && standings.size < Challenge.MAX_PLAYERS) {
        Card {
            if (state.code.isNotEmpty() && now < state.codeExpiresAt) {
                SectionLabel(stringResource(R.string.challenge_code_title))
                Text(
                    state.code.chunked(3).joinToString(" "),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp),
                    color = LH.Saffron,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                Text(stringResource(R.string.challenge_code_body), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                val message = stringResource(R.string.challenge_share_text, state.code)
                TextButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, message)
                    runCatching { context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }) { Text(stringResource(R.string.challenge_share), color = LH.Saffron) }
            } else {
                Text(stringResource(R.string.challenge_invite_detail), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                PrimaryButton(stringResource(R.string.challenge_invite), onClick = {
                    scope.launch { runCatching { ChallengeLive.invite(context) } }
                })
            }
        }
    }
}

@Composable
private fun StandingRow(place: Int, s: Standing, isMe: Boolean, leads: Boolean) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$place", style = MaterialTheme.typography.titleLarge, color = if (leads) LH.Saffron else LH.Muted, modifier = Modifier.width(32.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (isMe) stringResource(R.string.challenge_you, s.name) else s.name.ifBlank { "?" },
                style = MaterialTheme.typography.titleMedium,
                color = LH.Cream,
            )
            Text(
                pluralStringResource(R.plurals.challenge_streak, s.streak, s.streak),
                style = MaterialTheme.typography.bodySmall,
                color = LH.Oat,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${s.clean}", style = MaterialTheme.typography.headlineSmall, color = if (leads) LH.Saffron else LH.Cream)
            Text(pluralStringResource(R.plurals.challenge_clean, s.clean), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
        }
    }
}
