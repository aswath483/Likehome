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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Rage
import com.likehome.app.ui.common.CharacterFace
import com.likehome.app.ui.common.rememberIs24Hour

private object News {
    val Navy = Color(0xFF0B1B3A)
    val NavyDeep = Color(0xFF060F22)
    val Studio = Color(0xFF13305E)
    val Red = Color(0xFFD62828)
    val Gold = Color(0xFFF2C14E)
    val White = Color(0xFFFFFFFF)
    val Mist = Color(0xFFB8C4DD)

    val Tasks = TaskColors(
        card = Color(0xFF12244A), border = Color(0xFF2B3F6B), text = White, subtext = Mist,
        accent = Red, onAccent = White, track = Color(0xFF2B3F6B), error = Color(0xFFFF8A80),
    )
}

/** Breaking news: the family's anchor reports live on your snoozing. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsRingScreen(p: RingProps, onSnooze: () -> Unit, onAwake: () -> Unit) {
    var stage by remember(p.level, p.seed) { mutableStateOf(RingStage.Ringing) }
    val line = remember(p) { p.line }
    val clock by rememberClock()
    val is24 = rememberIs24Hour()
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        1f, 0.25f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "live-dot",
    )

    RingPage(Brush.verticalGradient(listOf(News.Navy, News.NavyDeep)), lightBackground = false) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.clip(RoundedCornerShape(6.dp)).background(News.Red).padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).alpha(pulse).clip(CircleShape).background(News.White))
                Spacer(Modifier.width(6.dp))
                Text("LIVE", style = MaterialTheme.typography.labelMedium, color = News.White)
            }
            Spacer(Modifier.width(10.dp))
            Text("${p.name.uppercase()} NEWS 24×7", style = MaterialTheme.typography.labelMedium, color = News.Mist, modifier = Modifier.weight(1f))
            Text(ClockFormat.full(clock, is24), style = MaterialTheme.typography.titleMedium, color = News.White)
        }

        // The studio: anchor at the desk, a slowly turning globe behind.
        Box(
            Modifier.fillMaxWidth().aspectRatio(16f / 10f).clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(News.Studio, News.Navy))),
        ) {
            Globe(Modifier.fillMaxSize())
            Column(Modifier.align(Alignment.CenterStart).padding(start = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CharacterFace(p.character, p.level, Modifier.size(104.dp))
                Spacer(Modifier.size(8.dp))
                Text("ANCHOR: ${p.name.uppercase()}", style = MaterialTheme.typography.labelSmall, color = News.Gold)
            }
        }

        // Lower third.
        Column {
            Row {
                Text(
                    if (p.level == 0) "BREAKING" else "DEVELOPING STORY",
                    style = MaterialTheme.typography.labelMedium,
                    color = News.White,
                    modifier = Modifier.background(News.Red).padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Column(Modifier.fillMaxWidth().background(News.White).padding(16.dp)) {
                Text(line, style = MaterialTheme.typography.titleLarge, color = News.Navy, fontWeight = FontWeight.Bold)
                Text(
                    "Snooze #${p.level} · ${p.name} rage at ${Rage.percent(p.level)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF51607E),
                )
            }
            Text(
                listOf(
                    "SNOOZES SO FAR: ${p.level}",
                    "STREAK: ${p.streak} DAYS",
                    "SHARMA JI'S SON ALREADY AWAKE",
                    "WEATHER: PERFECT FOR GETTING UP",
                    if (p.label.isNotBlank()) "TODAY: ${p.label.uppercase()}" else "EXPERTS AGREE: NOW IS A GOOD TIME",
                ).joinToString("   ✦   "),
                style = MaterialTheme.typography.labelMedium,
                color = News.Gold,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 8.dp).basicMarquee(iterations = Int.MAX_VALUE),
            )
        }

        AnimatedContent(stage, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "news") { s ->
            when (s) {
                RingStage.Ringing -> StyleActions(
                    p = p,
                    upText = "END THE BROADCAST · ${p.taskCall}",
                    snoozeText = "Commercial break (${p.snoozeMinutes} min)",
                    noSnoozeText = "No more commercial breaks. We are live until you're up.",
                    colors = ActionColors(News.Red, News.White, News.Mist, News.Mist),
                    onUp = { stage = RingStage.Task },
                    onSnooze = onSnooze,
                    shape = RoundedCornerShape(10.dp),
                )
                RingStage.Task -> WakeTaskCard(p, News.Tasks, onSolved = onAwake)
            }
        }
    }
}

/** Meridians and latitude rings, turning slowly: the studio backdrop. */
@Composable
private fun Globe(modifier: Modifier) {
    val turn by rememberInfiniteTransition(label = "globe").animateFloat(
        0f, 1f, infiniteRepeatable(tween(16_000)), label = "globe-turn",
    )
    Canvas(modifier) {
        val r = size.height * 0.42f
        val c = Offset(size.width * 0.72f, size.height * 0.5f)
        val line = Stroke(width = 1.5.dp.toPx())
        val ink = Color.White.copy(alpha = 0.18f)
        drawCircle(ink, r, c, style = line)
        listOf(0.35f, 0.7f).forEach { f ->
            drawOval(ink, Offset(c.x - r, c.y - r * f), androidx.compose.ui.geometry.Size(2 * r, 2 * r * f), style = line)
        }
        repeat(3) { i ->
            val phase = ((turn + i / 3f) % 1f)
            val w = r * kotlin.math.abs(kotlin.math.cos(phase * Math.PI)).toFloat()
            drawOval(ink, Offset(c.x - w, c.y - r), androidx.compose.ui.geometry.Size(2 * w, 2 * r), style = line)
        }
    }
}
