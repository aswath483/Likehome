package com.likehome.app.ui.ring

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Rage
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.common.CharacterFace
import com.likehome.app.ui.common.rememberIs24Hour
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.res.stringResource
import com.likehome.app.R
import com.likehome.app.ui.common.moodRes

private object Kitchen {
    val Cream = Color(0xFFFFF4E4)
    val Peach = Color(0xFFFFE3C4)
    val Wood = Color(0xFFC98B55)
    val WoodDark = Color(0xFF9E6A3E)
    val Ink = Color(0xFF3B2A18)
    val Brown = Color(0xFF7A6450)
    val Terracotta = Color(0xFFC2562F)
    val Sage = Color(0xFF8FB08C)

    val Tasks = TaskColors(
        card = Color.White, border = Color(0xFFE9D8C2), text = Ink, subtext = Brown,
        accent = Terracotta, onAccent = Color.White, track = Color(0xFFEADBC8), error = Color(0xFFB3261E),
    )
}

/** Warm kitchen: morning light, a wooden counter, chai steaming and a mood meter. */
@Composable
fun KitchenRingScreen(p: RingProps, onSnooze: () -> Unit, onAwake: () -> Unit) {
    var stage by remember(p.level, p.seed) { mutableStateOf(RingStage.Ringing) }
    val line = remember(p) { p.line }
    val clock by rememberClock()
    val is24 = rememberIs24Hour()

    RingPage(
        Brush.verticalGradient(listOf(Kitchen.Peach, Kitchen.Cream)),
        lightBackground = true,
        modifier = Modifier.drawBehind {
            // Wooden counter along the bottom.
            val top = size.height * 0.9f
            drawRect(Kitchen.Wood, Offset(0f, top), Size(size.width, size.height - top))
            drawRect(Kitchen.WoodDark, Offset(0f, top), Size(size.width, 6.dp.toPx()))
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    listOf(stringResource(R.string.greeting_morning), p.label).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = Kitchen.Brown,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(ClockFormat.digits(clock, is24), style = MaterialTheme.typography.displayMedium, color = Kitchen.Ink)
                    ClockFormat.period(clock, is24)?.let {
                        Spacer(Modifier.width(6.dp))
                        Text(it, style = MaterialTheme.typography.titleMedium, color = Kitchen.Brown, modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
            }
            Chai(Modifier.size(72.dp))
        }

        Row(verticalAlignment = Alignment.Top) {
            CharacterFace(p.character, p.level, Modifier.size(96.dp))
            Spacer(Modifier.width(12.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.weight(1f).padding(top = 18.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(p.name, style = MaterialTheme.typography.labelLarge, color = Kitchen.Terracotta)
                    Text(line, style = MaterialTheme.typography.bodyLarge, color = Kitchen.Ink)
                }
            }
        }

        Surface(
            color = Color.White.copy(alpha = 0.7f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFE9D8C2)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MoodGauge(Rage.percent(p.level) / 100f, Modifier.fillMaxWidth().height(96.dp))
                Text(stringResource(R.string.kitchen_mood, stringResource(moodRes(p.level))), style = MaterialTheme.typography.titleMedium, color = Kitchen.Ink)
            }
        }

        AnimatedContent(stage, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "kitchen") { s ->
            when (s) {
                RingStage.Ringing -> StyleActions(
                    p = p,
                    upText = stringResource(R.string.kitchen_up, p.taskTitle),
                    snoozeText = stringResource(R.string.kitchen_snooze, p.snoozeMinutes),
                    noSnoozeText = stringResource(R.string.kitchen_no_snooze),
                    colors = ActionColors(Kitchen.Terracotta, Color.White, Kitchen.Brown, Kitchen.Brown),
                    onUp = { stage = RingStage.Task },
                    onSnooze = onSnooze,
                )
                RingStage.Task -> WakeTaskCard(p, Kitchen.Tasks, onSolved = onAwake)
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

/** A semicircle from calm green to angry terracotta, with a needle that swings in. */
@Composable
private fun MoodGauge(value: Float, modifier: Modifier) {
    val needle by animateFloatAsState(value, tween(900), label = "gauge")
    Canvas(modifier) {
        val stroke = 14.dp.toPx()
        val r = minOf(size.width / 2, size.height) - stroke
        val c = Offset(size.width / 2, size.height - 4.dp.toPx())
        val topLeft = Offset(c.x - r, c.y - r)
        val arc = Size(2 * r, 2 * r)
        drawArc(
            Brush.horizontalGradient(listOf(Kitchen.Sage, Color(0xFFF2A65A), Kitchen.Terracotta)),
            180f, 180f, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round),
        )
        val angle = Math.toRadians(180.0 + 180.0 * needle)
        val tip = Offset(c.x + (cos(angle) * r * 0.85).toFloat(), c.y + (sin(angle) * r * 0.85).toFloat())
        drawLine(Kitchen.Ink, c, tip, 4.dp.toPx(), StrokeCap.Round)
        drawCircle(Kitchen.Ink, 7.dp.toPx(), c)
    }
}

/** A cup of chai with three wisps of steam rising forever. */
@Composable
private fun Chai(modifier: Modifier) {
    val t by rememberInfiniteTransition(label = "steam").animateFloat(0f, 1f, infiniteRepeatable(tween(2200)), label = "steam-rise")
    Box(modifier) {
        Canvas(Modifier.size(72.dp)) {
            val w = size.width
            val cupTop = w * 0.5f
            drawRoundRect(Color.White, Offset(w * 0.18f, cupTop), Size(w * 0.5f, w * 0.38f), androidx.compose.ui.geometry.CornerRadius(w * 0.08f))
            drawRect(Color(0xFFB07A4A), Offset(w * 0.22f, cupTop + w * 0.04f), Size(w * 0.42f, w * 0.08f))
            drawArc(Color.White, -90f, 180f, false, Offset(w * 0.6f, cupTop + w * 0.06f), Size(w * 0.18f, w * 0.2f), style = Stroke(w * 0.05f))
            repeat(3) { i ->
                val phase = (t + i / 3f) % 1f
                val x = w * (0.3f + i * 0.12f)
                val y = cupTop - phase * w * 0.45f
                val wisp = Path().apply {
                    moveTo(x, y + w * 0.12f)
                    quadraticTo(x + w * 0.06f, y + w * 0.06f, x, y)
                }
                drawPath(wisp, Kitchen.Brown.copy(alpha = (1f - phase) * 0.6f), style = Stroke(w * 0.03f, cap = StrokeCap.Round))
            }
        }
    }
}
