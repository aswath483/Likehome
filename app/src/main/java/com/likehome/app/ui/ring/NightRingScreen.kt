package com.likehome.app.ui.ring

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.core.ClockFormat
import com.likehome.app.ui.common.CharacterFace
import com.likehome.app.ui.common.rememberIs24Hour
import kotlin.random.Random

private object Night {
    val Deep = Color(0xFF070914)
    val Indigo = Color(0xFF121733)
    val Moon = Color(0xFFE9E4CF)
    val Text = Color(0xFFDCE3FF)
    val Soft = Color(0xFF9AA3C7)
    val Lavender = Color(0xFF8EA2FF)

    val Tasks = TaskColors(
        card = Color(0xFF10142A), border = Color(0xFF252B4D), text = Color(0xFFE3E8FF), subtext = Soft,
        accent = Lavender, onAccent = Color(0xFF0A0D1F), track = Color(0xFF252B4D), error = Color(0xFFFF9AA2),
    )
}

/** Calm night: a quiet sky, a breathing circle, and the gentlest wake-up there is. */
@Composable
fun NightRingScreen(p: RingProps, onSnooze: () -> Unit, onAwake: () -> Unit) {
    var stage by remember(p.level, p.seed) { mutableStateOf(RingStage.Ringing) }
    val line = remember(p) { p.line }
    val clock by rememberClock()
    val is24 = rememberIs24Hour()
    val sky = rememberInfiniteTransition(label = "sky")
    val twinkle by sky.animateFloat(0.3f, 1f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "twinkle")
    val breathe by sky.animateFloat(0.92f, 1.06f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")
    val stars = remember { List(40) { Random(it * 31).let { r -> Triple(r.nextFloat(), r.nextFloat() * 0.6f, r.nextFloat()) } } }

    RingPage(
        Brush.verticalGradient(listOf(Night.Deep, Night.Indigo)),
        lightBackground = false,
        modifier = Modifier.drawBehind {
            stars.forEach { (x, y, phase) ->
                val a = (0.25f + 0.75f * ((twinkle + phase) % 1f)).coerceIn(0f, 1f)
                drawCircle(Color.White.copy(alpha = a * 0.7f), 1.3.dp.toPx(), Offset(x * size.width, y * size.height))
            }
            // A crescent moon: a full disc with a sky-coloured disc bitten out of it.
            val moon = Offset(size.width * 0.82f, size.height * 0.1f)
            drawCircle(Night.Moon, 18.dp.toPx(), moon)
            drawCircle(Night.Deep, 15.dp.toPx(), moon + Offset(8.dp.toPx(), -5.dp.toPx()))
        },
    ) {
        Text(
            ClockFormat.full(clock, is24),
            style = MaterialTheme.typography.displayMedium,
            color = Night.Text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
        )
        if (p.label.isNotBlank()) {
            Text(p.label, style = MaterialTheme.typography.bodyMedium, color = Night.Soft, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(200.dp).scale(breathe)) {
                drawCircle(Brush.radialGradient(listOf(Night.Lavender.copy(alpha = 0.28f), Color.Transparent)))
            }
            CharacterFace(p.character, p.level, Modifier.size(92.dp))
        }

        Text(
            line,
            style = MaterialTheme.typography.titleLarge,
            color = Night.Text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Breathe in. Breathe out. ${p.name} is keeping it quiet for you.",
            style = MaterialTheme.typography.bodyMedium,
            color = Night.Soft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NightStat("${p.streak}", "calm mornings")
            NightStat("${p.level}", if (p.level == 1) "snooze" else "snoozes")
        }

        AnimatedContent(stage, transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) }, label = "night") { s ->
            when (s) {
                RingStage.Ringing -> StyleActions(
                    p = p,
                    upText = "Good morning · ${p.task.title}",
                    snoozeText = "${p.snoozeMinutes} more quiet minutes",
                    noSnoozeText = "The night is over. Time to rise, gently.",
                    colors = ActionColors(Night.Lavender, Color(0xFF0A0D1F), Night.Soft, Night.Soft),
                    onUp = { stage = RingStage.Task },
                    onSnooze = onSnooze,
                )
                RingStage.Task -> WakeTaskCard(p.task, Night.Tasks, onSolved = onAwake)
            }
        }
    }
}

@Composable
private fun NightStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Night.Text)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Night.Soft)
    }
}
