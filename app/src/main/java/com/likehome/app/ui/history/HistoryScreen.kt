package com.likehome.app.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.core.Badge
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Morning
import com.likehome.app.core.Mornings
import com.likehome.app.data.HistoryRepository
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.withHaptic
import com.likehome.app.ui.ring.ReportCardContent
import com.likehome.app.ui.ring.ReportShare
import com.likehome.app.ui.ring.Reports
import com.likehome.app.ui.theme.LH
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.likehome.app.R
import com.likehome.app.ui.common.detailRes
import com.likehome.app.ui.common.titleRes
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.common.dateFormat
import com.likehome.app.ui.common.weekdayInitials

/** Every morning so far: stats, a month calendar, all badges and past report cards. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val history by remember { HistoryRepository.get(context) }.mornings.collectAsState()
    val today = remember { LocalDate.now() }
    val days = remember(history) { Mornings.days(history) }
    var month by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    var opened by remember { mutableStateOf<Morning?>(null) }
    val is24 = rememberIs24Hour()

    PageScaffold(title = stringResource(R.string.history_title), onBack = onBack) {
        if (days.isEmpty()) {
            EmptyHistory()
        } else {

        val noSnooze = Mornings.noSnoozeMornings(history)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("${Mornings.streak(history)}", stringResource(R.string.stat_current_streak), Modifier.weight(1f))
            Stat("${Mornings.bestStreak(history)}", stringResource(R.string.stat_best_streak), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat(stringResource(R.string.percent, (noSnooze * 100f / days.size).roundToInt()), stringResource(R.string.stat_no_snooze), Modifier.weight(1f))
            Stat(String.format("%.1f", Mornings.averageSnoozes(history)), stringResource(R.string.stat_average), Modifier.weight(1f))
        }

        Mornings.verdict(history, today)?.let { v ->
            Card {
                SectionLabel(stringResource(R.string.verdict_this_month))
                Spacer(Modifier.height(6.dp))
                Text(stringResource(v.titleRes), style = MaterialTheme.typography.headlineSmall, color = LH.Saffron)
                Text(stringResource(v.detailRes), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }

        val shownMonth = YearMonth.parse(month)
        val firstMonth = YearMonth.from(days.last().date)
        SectionLabel(stringResource(R.string.calendar), Modifier.padding(top = 8.dp))
        Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { month = shownMonth.minusMonths(1).toString() }, enabled = shownMonth > firstMonth) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_month), tint = if (shownMonth > firstMonth) LH.Cream else LH.Line)
                }
                Text(
                    shownMonth.format(resources().dateFormat("MMMMyyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    color = LH.Cream,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                val canNext = shownMonth < YearMonth.from(today)
                IconButton(onClick = { month = shownMonth.plusMonths(1).toString() }, enabled = canNext) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month), tint = if (canNext) LH.Cream else LH.Line)
                }
            }
            Spacer(Modifier.height(8.dp))
            MonthGrid(shownMonth, days.associateBy { it.date }, today, onOpen = { opened = it })
        }

        SectionLabel(stringResource(R.string.badges), Modifier.padding(top = 8.dp))
        val earned = Mornings.badges(history)
        Badge.entries.forEach { BadgeRow(it, it in earned) }

        SectionLabel(stringResource(R.string.recent_mornings), Modifier.padding(top = 8.dp))
        days.take(30).forEach { m ->
            MorningRow(m, is24, onClick = { opened = m })
        }
        }
    }

    opened?.let { m ->
        ModalBottomSheet(
            onDismissRequest = { opened = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = LH.Espresso,
            contentColor = LH.Cream,
        ) {
            val res = resources()
            val report = remember(m, history, res) { Reports.forMorning(res, history, m) }
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ReportCardContent(report, onShare = { ReportShare.share(context, report) }, onDone = { opened = null }, doneLabel = stringResource(R.string.close))
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        MoodFace(0, Modifier.size(110.dp))
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.history_empty_title), style = MaterialTheme.typography.headlineSmall, color = LH.Cream)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.history_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(color = LH.Roast, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, LH.Line), modifier = modifier) {
        Column(Modifier.padding(18.dp)) {
            Text(value, style = MaterialTheme.typography.displaySmall, color = LH.Cream, maxLines = 1)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        }
    }
}

/** A Monday-first month; each alarm day is a dot you can tap for its report. */
@Composable
private fun MonthGrid(month: YearMonth, byDate: Map<LocalDate, Morning>, today: LocalDate, onOpen: (Morning) -> Unit) {
    val first = month.atDay(1)
    val lead = first.dayOfWeek.value - DayOfWeek.MONDAY.value
    val cells = lead + month.lengthOfMonth()
    val rows = (cells + 6) / 7
    Row(Modifier.fillMaxWidth()) {
        resources().weekdayInitials().forEach {
            Text(it, style = MaterialTheme.typography.labelMedium, color = LH.Muted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(6.dp))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rows) { r ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { c ->
                    val dayNumber = r * 7 + c - lead + 1
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (dayNumber in 1..month.lengthOfMonth()) {
                            val date = month.atDay(dayNumber)
                            DayCell(date, byDate[date], isToday = date == today, onOpen = onOpen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, morning: Morning?, isToday: Boolean, onOpen: (Morning) -> Unit) {
    val said = when {
        morning == null -> stringResource(R.string.week_no_alarm, "${date.dayOfMonth}")
        morning.snoozes == 0 -> stringResource(R.string.week_no_snooze, "${date.dayOfMonth}")
        else -> stringResource(R.string.week_snoozed, "${date.dayOfMonth}", morning.snoozes)
    }
    val base = Modifier.size(36.dp).clip(CircleShape).semantics { contentDescription = said }
    val styled = when {
        morning == null -> base
        morning.snoozes == 0 -> base.background(LH.Saffron)
        else -> base.border(2.dp, LH.Saffron, CircleShape)
    }
    Surface(
        onClick = withHaptic { morning?.let(onOpen) },
        enabled = morning != null,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = styled.then(if (isToday && morning == null) Modifier.border(1.dp, LH.Muted, CircleShape) else Modifier),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    morning?.snoozes == 0 -> LH.OnSaffron
                    morning != null -> LH.Cream
                    else -> LH.Muted
                },
            )
        }
    }
}

@Composable
private fun BadgeRow(badge: Badge, earned: Boolean) {
    Surface(
        color = if (earned) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (earned) LH.SaffronDeep else LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(if (earned) LH.Saffron else LH.Crema),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(badge.titleRes).take(1), style = MaterialTheme.typography.titleMedium, color = if (earned) LH.OnSaffron else LH.Muted)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(badge.titleRes), style = MaterialTheme.typography.titleMedium, color = if (earned) LH.Cream else LH.Oat)
                Text(if (earned) stringResource(R.string.badge_earned, stringResource(badge.detailRes)) else stringResource(badge.detailRes), style = MaterialTheme.typography.bodyMedium, color = LH.Muted)
            }
        }
    }
}

@Composable
private fun MorningRow(m: Morning, is24: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = withHaptic(onClick),
        color = LH.Roast,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            MoodFace(m.snoozes.coerceAtMost(3), Modifier.size(40.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(m.date.format(resources().dateFormat("EEEdMMM")), style = MaterialTheme.typography.titleSmall, color = LH.Cream)
                Text(
                    stringResource(
                        R.string.up_at,
                        ClockFormat.full(m.wokeAt, is24),
                        if (m.snoozes == 0) stringResource(R.string.no_snooze) else pluralStringResource(R.plurals.snoozes, m.snoozes, m.snoozes),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LH.Oat,
                )
            }
        }
    }
}
