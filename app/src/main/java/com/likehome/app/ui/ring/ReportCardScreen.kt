package com.likehome.app.ui.ring

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.stickers.StickerPack
import com.likehome.app.ui.common.MaxContentWidth
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.WeekDots
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.theme.LH
import com.likehome.app.core.ClockFormat
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** The morning report card, full screen: shown after the wake-up task. */
@Composable
fun ReportCardScreen(report: ReportData, onShare: () -> Unit, onDone: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(LH.Espresso)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (report.snoozes == 0) Petals(Modifier.fillMaxSize())
        Column(
            Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ReportCardContent(report, onShare, onDone, doneLabel = "Done")
        }
    }
}

/** The card itself; also shown in a sheet from History. Elements rise in one after another. */
@Composable
fun ColumnScope.ReportCardContent(report: ReportData, onShare: () -> Unit, onDone: () -> Unit, doneLabel: String) {
    val is24 = rememberIs24Hour()
    var shown by remember(report) { mutableStateOf(false) }
    LaunchedEffect(report) { shown = true }

    Rise(shown, 0) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            SectionLabel("Morning report")
            Text(
                report.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                style = MaterialTheme.typography.bodyMedium,
                color = LH.Oat,
            )
            Spacer(Modifier.height(22.dp))
            MoodFace(report.snoozes.coerceAtMost(3), Modifier.size(120.dp))
        }
    }
    Spacer(Modifier.height(20.dp))
    Rise(shown, 1) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(report.title, style = MaterialTheme.typography.headlineLarge, color = LH.Cream, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(report.body, style = MaterialTheme.typography.bodyLarge, color = LH.Oat, textAlign = TextAlign.Center)
        }
    }

    Spacer(Modifier.height(24.dp))
    Rise(shown, 2) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat(countUp(report.snoozes, shown).toString(), if (report.snoozes == 1) "snooze" else "snoozes", Modifier.weight(1f))
            Stat(ClockFormat.digits(report.wokeAt, is24), ClockFormat.period(report.wokeAt, is24)?.let { "up at · $it" } ?: "up at", Modifier.weight(1f))
            Stat(countUp(report.streak, shown).toString(), "day streak", Modifier.weight(1f))
        }
    }

    if (report.newBadges.isNotEmpty()) {
        Spacer(Modifier.height(6.dp))
        report.newBadges.forEach { badge ->
            Rise(shown, 3) {
                Surface(color = LH.MyBubble, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        Text("New badge: ${badge.title}", style = MaterialTheme.typography.titleMedium, color = LH.Saffron)
                        Text(badge.detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Rise(shown, 4) {
        Column(Modifier.fillMaxWidth()) {
            SectionLabel("This week")
            Spacer(Modifier.height(10.dp))
            WeekDots(report.week, report.date, Modifier.fillMaxWidth())
            report.verdict?.let {
                Spacer(Modifier.height(18.dp))
                Text(
                    "Mom's verdict this month: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LH.Oat,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    Spacer(Modifier.height(28.dp))
    Rise(shown, 5) {
        Column(Modifier.fillMaxWidth()) {
            PrimaryButton("Share with friends", onClick = onShare)
            Spacer(Modifier.height(10.dp))
            val context = LocalContext.current
            SecondaryButton("Get Mom's WhatsApp stickers", onClick = { StickerPack.addToWhatsApp(context) })
            Spacer(Modifier.height(10.dp))
            SecondaryButton(doneLabel, onClick = onDone)
        }
    }
}

/** Fades and lifts [content] in, [index] steps after the first. */
@Composable
private fun Rise(visible: Boolean, index: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) progress.animateTo(1f, tween(480, delayMillis = index * 90, easing = FastOutSlowInEasing))
    }
    Box(
        Modifier.fillMaxWidth().graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 24.dp.toPx()
        }
    ) { content() }
}

@Composable
private fun countUp(target: Int, started: Boolean): Int {
    val value by animateIntAsState(if (started) target else 0, tween(700, delayMillis = 250), label = "count")
    return value
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        color = LH.Roast,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = LH.Cream, maxLines = 1)
            Text(label, style = MaterialTheme.typography.bodySmall, color = LH.Oat, maxLines = 1)
        }
    }
}

/** A soft burst of saffron petals for a no-snooze morning; plays once. */
@Composable
private fun Petals(modifier: Modifier) {
    val petals = remember {
        val r = Random(7)
        List(26) { Triple(r.nextFloat() * 360f, 0.35f + r.nextFloat() * 0.65f, 3f + r.nextFloat() * 5f) }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(1800, easing = LinearEasing)) }
    Canvas(modifier) {
        val p = t.value
        if (p >= 1f) return@Canvas
        val origin = Offset(size.width / 2, size.height * 0.22f)
        petals.forEach { (angle, speed, radius) ->
            val a = Math.toRadians(angle.toDouble())
            val d = p * speed * size.minDimension * 0.6f
            val pos = Offset(
                origin.x + (cos(a) * d).toFloat(),
                origin.y + (sin(a) * d).toFloat() + p * p * size.height * 0.25f,
            )
            drawCircle(LH.Saffron.copy(alpha = (1f - p) * 0.8f), radius.dp.toPx(), pos)
        }
    }
}
