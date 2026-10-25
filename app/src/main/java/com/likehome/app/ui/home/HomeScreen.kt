package com.likehome.app.ui.home

import com.likehome.app.family.FamilyLive

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.ClockFormat
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.common.MaxContentWidth
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.Tag
import com.likehome.app.ui.common.pressScale
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.withHaptic
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import androidx.compose.ui.res.stringResource
import com.likehome.app.R
import com.likehome.app.ui.common.repeatText
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.common.titleRes
import com.likehome.app.ui.common.shortRes
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.common.text

@Composable
fun HomeScreen(
    needsSetup: Boolean,
    onFixSetup: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStyles: () -> Unit,
    openNewAlarm: Boolean = false,
    onNewAlarmOpened: () -> Unit = {},
) {
    val context = LocalContext.current
    val repo = remember { AlarmRepository.get(context) }
    val alarms by repo.alarms.collectAsState()
    val settings by remember { SettingsRepository.get(context) }.settings.collectAsState()
    val mornings by remember { HistoryRepository.get(context) }.mornings.collectAsState()
    var editing by remember { mutableStateOf<Alarm?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val is24 = rememberIs24Hour()

    // Re-render the countdown every 20 s, so "in 1 min" never goes stale.
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(20_000); tick++ } }
    val now = remember(tick) { ZonedDateTime.now(ZoneId.systemDefault()) }

    val next = remember(alarms, tick) { alarms.filter { it.enabled }.minByOrNull { AlarmScheduler.nextRing(context, it, now) } }

    fun newAlarm() {
        editing = Alarm(
            id = AlarmRepository.NEW_ID, hour = 7, minute = 0, days = Alarm.WEEKDAYS,
            style = settings.style, character = settings.character,
        )
    }

    fun delete(alarm: Alarm) {
        AlarmScheduler.cancel(context, alarm.id)
        repo.delete(alarm.id)
        FamilyLive.childChanged(context, alarm, deleted = true)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = context.getString(R.string.alarm_deleted),
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                val restored = repo.upsert(alarm)
                if (restored.enabled) AlarmScheduler.schedule(context, restored)
                FamilyLive.childChanged(context, restored)
            }
        }
    }

    LaunchedEffect(openNewAlarm) {
        if (openNewAlarm) {
            newAlarm()
            onNewAlarmOpened()
        }
    }

    val bars = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical).asPaddingValues()

    Box(Modifier.fillMaxSize().background(LH.Espresso)) {
        Sky(now.toLocalTime(), Modifier.fillMaxWidth().height(460.dp))

        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = MaxContentWidth)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                top = bars.calculateTopPadding(),
                bottom = bars.calculateBottomPadding() + 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Header(now.toLocalTime(), next, now, is24, onOpenStyles, onOpenHistory, onOpenSettings) }
            if (needsSetup) item { SetupBanner(onFixSetup) }
            item {
                NextAlarmHero(
                    next = next,
                    now = now,
                    is24 = is24,
                    onEdit = { next?.let { editing = it } },
                    onCreate = ::newAlarm,
                )
            }
            item {
                Box(Modifier.clip(RoundedCornerShape(28.dp))) {
                    StreakCard(mornings, now.toLocalDate(), onClick = onOpenHistory)
                }
            }
            if (alarms.isNotEmpty()) {
                item {
                    Row(Modifier.padding(top = 14.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel(stringResource(R.string.your_alarms), Modifier.weight(1f))
                        Text(stringResource(R.string.swipe_to_delete), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
                    }
                }
            }
            items(alarms, key = { it.id }) { alarm ->
                SwipeToDelete(onDelete = { delete(alarm) }) {
                    AlarmRow(
                        alarm = alarm,
                        now = now,
                        is24 = is24,
                        onClick = { editing = alarm },
                        onToggle = { on ->
                            val updated = repo.upsert(alarm.copy(enabled = on))
                            if (on) AlarmScheduler.schedule(context, updated)
                            else AlarmScheduler.cancel(context, updated.id)
                            FamilyLive.childChanged(context, updated)
                        },
                    )
                }
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = LH.Crema,
                    contentColor = LH.Cream,
                    actionColor = LH.Saffron,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(horizontal = 20.dp).widthIn(max = MaxContentWidth),
                )
            }
            Spacer(Modifier.height(10.dp))
            NewAlarmButton(onClick = ::newAlarm)
        }
    }

    editing?.let { alarm ->
        AlarmEditorSheet(
            initial = alarm,
            onDismiss = { editing = null },
            onSave = { edited ->
                val saved = repo.upsert(edited.copy(enabled = true))
                AlarmScheduler.schedule(context, saved)
                FamilyLive.childChanged(context, saved)
                editing = null
            },
            onDelete = if (alarm.id == AlarmRepository.NEW_ID) null else {
                {
                    editing = null
                    delete(alarm)
                }
            },
        )
    }
}

/**
 * The sky behind the header: warm dawn in the morning, a soft afternoon glow, plum at
 * dusk and a quiet moon at night. Fades into the page so content sits on plain ground.
 */
@Composable
private fun Sky(time: LocalTime, modifier: Modifier) {
    val hour = time.hour + time.minute / 60f
    val (tint, glow, glowX) = when {
        hour in 5f..11f -> Triple(LH.Dawn, LH.Saffron, 0.78f)
        hour in 11f..17f -> Triple(Color(0xFF2A1F14), Color(0xFFF2C38A), 0.5f)
        hour in 17f..21f -> Triple(Color(0xFF2A1630), Color(0xFFE8786A), 0.22f)
        else -> Triple(LH.Night, Color(0xFFCFC7E8), 0.8f)
    }
    val night = hour < 5f || hour >= 21f
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(tint, LH.Espresso)))
        val center = Offset(size.width * glowX, size.height * 0.16f)
        drawCircle(
            Brush.radialGradient(listOf(glow.copy(alpha = 0.32f), glow.copy(alpha = 0f)), center = center, radius = size.width * 0.55f),
            radius = size.width * 0.55f,
            center = center,
        )
        drawCircle(glow.copy(alpha = if (night) 0.85f else 0.55f), radius = if (night) 14.dp.toPx() else 22.dp.toPx(), center = center)
        if (night) {
            // A few stars, placed the same way every night.
            val stars = listOf(0.12f to 0.08f, 0.3f to 0.2f, 0.55f to 0.06f, 0.66f to 0.3f, 0.9f to 0.14f, 0.42f to 0.36f)
            stars.forEach { (x, y) -> drawCircle(Color.White.copy(alpha = 0.5f), 1.5.dp.toPx(), Offset(size.width * x, size.height * y)) }
        }
    }
}

@Composable
private fun Header(
    time: LocalTime,
    next: Alarm?,
    now: ZonedDateTime,
    is24: Boolean,
    onOpenStyles: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val greeting = when (time.hour) {
        in 5..11 -> stringResource(R.string.greeting_morning)
        in 12..16 -> stringResource(R.string.greeting_afternoon)
        in 17..21 -> stringResource(R.string.greeting_evening)
        else -> stringResource(R.string.greeting_night)
    }
    Column(Modifier.padding(top = 8.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.home_brand), style = MaterialTheme.typography.labelMedium, color = LH.Saffron, modifier = Modifier.weight(1f).padding(start = 4.dp))
            IconButton(onClick = withHaptic(onOpenStyles)) { Icon(Icons.Outlined.Palette, contentDescription = stringResource(R.string.styles_styles), tint = LH.Oat) }
            IconButton(onClick = withHaptic(onOpenHistory)) { Icon(Icons.Outlined.CalendarMonth, contentDescription = stringResource(R.string.history_title), tint = LH.Oat) }
            IconButton(onClick = withHaptic(onOpenSettings)) { Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings_title), tint = LH.Oat) }
        }
        Spacer(Modifier.height(10.dp))
        Text(greeting, style = MaterialTheme.typography.headlineLarge, color = LH.Cream, modifier = Modifier.padding(horizontal = 4.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            when {
                next == null -> stringResource(R.string.no_alarm_worried)
                else -> {
                    val at = AlarmScheduler.nextRing(LocalContext.current, next, now)
                    stringResource(R.string.mom_will_call, ClockFormat.dayOf(now.toLocalDate(), at.toLocalDate()).text(resources()).lowercase(), ClockFormat.full(at.toLocalTime(), is24))
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            color = LH.Oat,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun SetupBanner(onFix: () -> Unit) {
    Surface(
        onClick = withHaptic(onFix),
        color = LH.MyBubble,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, LH.SaffronDeep),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = LH.Saffron)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.reach_title), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(stringResource(R.string.reach_detail), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }
    }
}

/** The next wake-up, big; or an inviting empty state when there's none. */
@Composable
private fun NextAlarmHero(next: Alarm?, now: ZonedDateTime, is24: Boolean, onEdit: () -> Unit, onCreate: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = withHaptic { if (next != null) onEdit() else onCreate() },
        interactionSource = source,
        color = LH.Roast.copy(alpha = 0.92f),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = Modifier.fillMaxWidth().animateContentSize().pressScale(source, 0.985f),
    ) {
        if (next == null) {
            Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MoodFace(1, Modifier.size(88.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.headlineSmall, color = LH.Cream)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.empty_detail), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                Spacer(Modifier.height(18.dp))
                PrimaryButton(stringResource(R.string.first_alarm), onClick = onCreate)
            }
        } else {
            val context = LocalContext.current
            val trigger = AlarmScheduler.nextRing(context, next, now)
            // The calendar moved it earlier than usual for a meeting.
            val moved = trigger.isBefore(next.nextTrigger(now))
            Column(Modifier.padding(horizontal = 26.dp, vertical = 24.dp)) {
                SectionLabel(stringResource(R.string.next_wake_up))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(ClockFormat.digits(trigger.toLocalTime(), is24), style = MaterialTheme.typography.displayLarge, color = LH.Cream, maxLines = 1)
                    ClockFormat.period(trigger.toLocalTime(), is24)?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(it, style = MaterialTheme.typography.headlineSmall, color = LH.Oat, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(LH.Saffron))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.dot_pair, ClockFormat.dayOf(now.toLocalDate(), trigger.toLocalDate()).text(resources()), ClockFormat.until(now, trigger).text(resources())),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LH.Oat,
                    )
                }
                Spacer(Modifier.height(14.dp))
                if (moved) {
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.calendar_moved), style = MaterialTheme.typography.bodyMedium, color = LH.Saffron)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(stringResource(next.character.nameRes), LH.Oat)
                    Tag(stringResource(next.style.titleRes), LH.Oat)
                    Tag(stringResource(next.task.titleRes), LH.Oat)
                }
            }
        }
    }
}

/** Swipe an alarm left to delete it; a red tray with a bin shows underneath. */
@Composable
private fun SwipeToDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else false
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val reveal by animateFloatAsState(if (state.targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0.5f, label = "bin")
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(LH.Danger.copy(alpha = 0.22f)).padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = LH.Danger, modifier = Modifier.alpha(reveal))
            }
        },
    ) { content() }
}

@Composable
private fun AlarmRow(alarm: Alarm, now: ZonedDateTime, is24: Boolean, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val fade = if (alarm.enabled) 1f else 0.45f
    val source = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val res = resources()
    val repeat = if (alarm.isRepeating) alarm.repeatText(res)
    else stringResource(R.string.once_on, ClockFormat.dayOf(now.toLocalDate(), alarm.nextTrigger(now).toLocalDate()).text(res).lowercase())
    Surface(
        onClick = withHaptic(onClick),
        interactionSource = source,
        color = LH.Roast,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().pressScale(source, 0.985f),
    ) {
        Row(
            Modifier.padding(horizontal = 22.dp, vertical = 16.dp).heightIn(min = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).alpha(fade)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(ClockFormat.digits(alarm.time, is24), style = MaterialTheme.typography.displayMedium, color = LH.Cream, maxLines = 1)
                    ClockFormat.period(alarm.time, is24)?.let {
                        Spacer(Modifier.width(6.dp))
                        Text(it, style = MaterialTheme.typography.titleMedium, color = LH.Oat, modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
                if (alarm.fromFamily) {
                    Text(stringResource(R.string.family_set_by), style = MaterialTheme.typography.labelMedium, color = LH.Saffron)
                }
                if (alarm.label.isNotBlank()) {
                    Text(alarm.label, style = MaterialTheme.typography.titleSmall, color = LH.Cream, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    stringResource(R.string.alarm_row_summary, repeat, stringResource(alarm.character.nameRes), stringResource(alarm.style.shortRes)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LH.Oat,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = alarm.enabled,
                onCheckedChange = { on ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle(on)
                },
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
private fun NewAlarmButton(onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = withHaptic(onClick),
        interactionSource = source,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = LH.Saffron, contentColor = LH.OnSaffron),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
        modifier = Modifier.heightIn(min = 60.dp).pressScale(source),
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.new_alarm), style = MaterialTheme.typography.labelLarge)
    }
}
