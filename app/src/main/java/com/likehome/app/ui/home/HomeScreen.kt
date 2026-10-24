package com.likehome.app.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.data.untilText
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TimeFmt = DateTimeFormatter.ofPattern("h:mm")
private val AmPmFmt = DateTimeFormatter.ofPattern("a")

@Composable
fun HomeScreen(needsSetup: Boolean, onFixSetup: () -> Unit, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AlarmRepository.get(context) }
    val alarms by repo.alarms.collectAsState()
    val settings by remember { SettingsRepository.get(context) }.settings.collectAsState()
    val mornings by remember { HistoryRepository.get(context) }.mornings.collectAsState()
    var editing by remember { mutableStateOf<Alarm?>(null) }

    // Re-render the countdown every 30 s.
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); tick++ } }

    val next = remember(alarms, tick) {
        alarms.filter { it.enabled }.minByOrNull { it.nextTrigger() }
    }

    Box(Modifier.fillMaxSize().background(LH.Espresso)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Header(onOpenSettings) }
            if (needsSetup) item { SetupBanner(onFixSetup) }
            item { NextAlarmCard(next, tick) }
            item { StreakCard(mornings, remember(tick) { LocalDate.now() }) }
            if (alarms.isNotEmpty()) {
                item {
                    Text(
                        "YOUR ALARMS",
                        style = MaterialTheme.typography.labelMedium,
                        color = LH.Muted,
                        modifier = Modifier.padding(top = 18.dp, bottom = 2.dp),
                    )
                }
            }
            items(alarms, key = { it.id }) { alarm ->
                AlarmRow(
                    alarm = alarm,
                    onClick = { editing = alarm },
                    onToggle = { on ->
                        val updated = repo.upsert(alarm.copy(enabled = on))
                        if (on) AlarmScheduler.schedule(context, updated)
                        else AlarmScheduler.cancel(context, updated.id)
                    },
                )
            }
        }

        NewAlarmButton(
            onClick = {
                editing = Alarm(
                    id = AlarmRepository.NEW_ID, hour = 7, minute = 0,
                    days = Alarm.WEEKDAYS, style = settings.style,
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    editing?.let { alarm ->
        AlarmEditorSheet(
            initial = alarm,
            onDismiss = { editing = null },
            onSave = { edited ->
                val saved = repo.upsert(edited.copy(enabled = true))
                AlarmScheduler.schedule(context, saved)
                editing = null
            },
            onDelete = if (alarm.id == AlarmRepository.NEW_ID) null else {
                {
                    AlarmScheduler.cancel(context, alarm.id)
                    repo.delete(alarm.id)
                    editing = null
                }
            },
        )
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Sleep soon, beta"
    }
    Column(Modifier.statusBarsPadding().padding(top = 16.dp, bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "LIKE HOME",
                style = MaterialTheme.typography.labelMedium,
                color = LH.Saffron,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = LH.Oat)
            }
        }
        Text(greeting, style = MaterialTheme.typography.headlineLarge, color = LH.Cream)
        Spacer(Modifier.height(4.dp))
        Text(
            "Wake up like Mom is still calling you.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
    }
}

@Composable
private fun SetupBanner(onFix: () -> Unit) {
    Surface(
        color = LH.MyBubble,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onFix),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = LH.Saffron)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Let Mom reach you", style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(
                    "Allow alarm notifications so she can wake you, even when your phone is locked.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LH.Oat,
                )
            }
        }
    }
}

@Composable
private fun NextAlarmCard(next: Alarm?, tick: Int) {
    Surface(
        color = LH.Roast,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
            Text("NEXT WAKE-UP", style = MaterialTheme.typography.labelMedium, color = LH.Muted)
            Spacer(Modifier.height(12.dp))
            if (next == null) {
                Text("No alarm set", style = MaterialTheme.typography.headlineSmall, color = LH.Cream)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Set one and Mom will take care of the rest.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LH.Oat,
                )
            } else {
                val trigger = remember(next, tick) { next.nextTrigger() }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(next.time.format(TimeFmt), style = MaterialTheme.typography.displayLarge, color = LH.Cream)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        next.time.format(AmPmFmt).lowercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LH.Oat,
                        modifier = Modifier.padding(bottom = 14.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(LH.Saffron))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${untilText(trigger)} · ${next.repeatSummary()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LH.Oat,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmRow(alarm: Alarm, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val fade = if (alarm.enabled) 1f else 0.45f
    Surface(
        color = LH.Roast,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        alarm.time.format(TimeFmt),
                        style = MaterialTheme.typography.displayMedium,
                        color = LH.Cream.copy(alpha = fade),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        alarm.time.format(AmPmFmt).lowercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = LH.Oat.copy(alpha = fade),
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                Text(
                    listOf(alarm.label.ifBlank { null }, alarm.repeatSummary(), alarm.task.short).filterNotNull().joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LH.Oat.copy(alpha = fade),
                )
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LH.OnSaffron,
                    checkedTrackColor = LH.Saffron,
                    uncheckedThumbColor = LH.Muted,
                    uncheckedTrackColor = LH.Crema,
                    uncheckedBorderColor = LH.Line,
                ),
            )
        }
    }
}

@Composable
private fun NewAlarmButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = LH.Saffron, contentColor = LH.OnSaffron),
        contentPadding = PaddingValues(horizontal = 28.dp),
        modifier = modifier.navigationBarsPadding().padding(bottom = 24.dp).height(60.dp),
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Text("New alarm", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
