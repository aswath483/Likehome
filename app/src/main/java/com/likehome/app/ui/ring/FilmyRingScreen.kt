package com.likehome.app.ui.ring

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likehome.app.ui.common.CharacterFace
import kotlin.random.Random

private object Filmy {
    val Maroon = Color(0xFF3A0712)
    val Night = Color(0xFF12020A)
    val Velvet = Color(0xFF7A0E22)
    val VelvetDark = Color(0xFF3F0612)
    val Gold = Color(0xFFE2B04A)
    val Cream = Color(0xFFF7E7D0)
    val Rose = Color(0xFFD9B9A0)

    /** The film's title gets more tragic with every snooze. */
    val titles = listOf("Ek Pyaari Subah", "Kabhi Snooze Kabhi Gham", "Dilwale Sote Rahenge", "Maa Ka Intezaar")

    val Tasks = TaskColors(
        card = Color(0xFF2A0A12), border = Gold.copy(alpha = 0.5f), text = Cream, subtext = Rose,
        accent = Gold, onAccent = Color(0xFF2A0A12), track = Color(0xFF4A1A24), error = Color(0xFFFF8A80),
    )
}

/** Filmy drama: velvet curtains, a spotlight, and a dialogue fit for the climax. */
@Composable
fun FilmyRingScreen(p: RingProps, onSnooze: () -> Unit, onAwake: () -> Unit) {
    var stage by remember(p.level, p.seed) { mutableStateOf(RingStage.Ringing) }
    val line = remember(p) { p.line }
    val violin by rememberInfiniteTransition(label = "violin").animateFloat(
        0.45f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "violin-glow",
    )
    val grain = remember { List(120) { Random(it).let { r -> Offset(r.nextFloat(), r.nextFloat()) } } }

    RingPage(
        Brush.verticalGradient(listOf(Filmy.Maroon, Filmy.Night)),
        lightBackground = false,
        horizontalPadding = 40.dp,
        modifier = Modifier.drawBehind {
            spotlight()
            curtains()
            grain.forEach { drawCircle(Color.White.copy(alpha = 0.05f), 1.2f, Offset(it.x * size.width, it.y * size.height)) }
        },
    ) {
        Text(
            "EPISODE ${p.level + 1}",
            style = MaterialTheme.typography.labelMedium,
            color = Filmy.Gold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            Filmy.titles[p.level.coerceIn(0, 3)],
            style = MaterialTheme.typography.headlineMedium,
            color = Filmy.Gold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CharacterFace(p.character, p.level, Modifier.size(132.dp))
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "“$line”",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp, lineHeight = 34.sp, fontStyle = FontStyle.Italic),
                color = Filmy.Cream,
                textAlign = TextAlign.Center,
            )
            Text(
                "— ${p.name}",
                style = MaterialTheme.typography.titleMedium,
                color = Filmy.Gold,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                "♪ a sad violin plays ♪",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = Filmy.Rose,
                modifier = Modifier.padding(top = 14.dp).alpha(violin),
            )
        }
        AnimatedContent(stage, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) }, label = "filmy") { s ->
            when (s) {
                RingStage.Ringing -> StyleActions(
                    p = p,
                    upText = "THE END · ${p.taskCall}",
                    snoozeText = "Interval (${p.snoozeMinutes} min)",
                    noSnoozeText = "No interval. This is the climax.",
                    colors = ActionColors(Filmy.Gold, Filmy.Maroon, Filmy.Rose, Filmy.Rose),
                    onUp = { stage = RingStage.Task },
                    onSnooze = onSnooze,
                )
                RingStage.Task -> WakeTaskCard(p.task, Filmy.Tasks, onSolved = onAwake)
            }
        }
    }
}

private fun DrawScope.spotlight() {
    drawCircle(
        Brush.radialGradient(
            listOf(Filmy.Gold.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(size.width / 2, size.height * 0.28f),
            radius = size.width * 0.7f,
        ),
        radius = size.width * 0.7f,
        center = Offset(size.width / 2, size.height * 0.28f),
    )
}

/** Velvet curtains on both edges, with folds. */
private fun DrawScope.curtains() {
    val w = size.width * 0.07f
    listOf(0f, size.width - w).forEach { x ->
        drawRect(Brush.horizontalGradient(listOf(Filmy.Velvet, Filmy.VelvetDark, Filmy.Velvet), startX = x, endX = x + w), Offset(x, 0f), Size(w, size.height))
    }
    drawRect(Brush.verticalGradient(listOf(Filmy.Velvet, Filmy.VelvetDark)), Offset.Zero, Size(size.width, size.height * 0.03f))
}
