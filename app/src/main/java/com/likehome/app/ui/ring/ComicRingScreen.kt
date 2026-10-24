package com.likehome.app.ui.ring

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likehome.app.core.Rage
import com.likehome.app.core.WakeTask
import com.likehome.app.mom.Language
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.theme.Bangers
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private object Comic {
    val Paper = Color(0xFFFFD23F)
    val Ink = Color(0xFF111111)
    val White = Color(0xFFFFFFFF)
    val Red = Color(0xFFDC2626)
    val Skin = Color(0xFFF2C08F)
    val Water = Color(0xFF3B82C4)
    val Grey = Color(0xFF444444)
    val Track = Color(0xFFD4D4D4)
    fun bangers(size: Int) = TextStyle(fontFamily = Bangers, fontSize = size.sp, lineHeight = (size * 1.1f).sp, letterSpacing = 0.5.sp)
    val Body = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)

    val Tasks = TaskColors(
        card = White, border = Ink, text = Ink, subtext = Grey,
        accent = Red, onAccent = White, track = Track, error = Color(0xFFB91C1C), borderWidth = 4,
    )
}

/** Comic drama: one huge speech bubble, a drawn panel of Mom, and a rage meter. */
@Composable
fun ComicRingScreen(
    level: Int,
    seed: Long,
    label: String,
    language: Language,
    task: WakeTask,
    streak: Int,
    onSnooze: () -> Unit,
    onAwake: () -> Unit,
) {
    val line = remember(level, seed, language) { MomLines.lineFor(level, seed, language) }
    var stage by remember(level, seed) { mutableStateOf(RingStage.Ringing) }
    var popped by remember(line) { mutableStateOf(false) }
    LaunchedEffect(line) { delay(150); popped = true }
    val pop by animateFloatAsState(
        if (popped) 1f else 0.6f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bubble-pop",
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(Comic.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ComicHeader(level, label)

        Column(Modifier.scale(pop)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Comic.White)
                    .border(4.dp, Comic.Ink, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(line.uppercase(), style = Comic.bangers(if (stage == RingStage.Ringing) 34 else 24), color = Comic.Ink)
            }
            BubbleTail(Modifier.padding(start = 60.dp))
        }

        AnimatedContent(
            targetState = stage,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "comic-stage",
        ) { s ->
            when (s) {
                RingStage.Ringing -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MomPanel(level)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("Mom rage ${Rage.percent(level)}%", dark = true, modifier = Modifier.weight(1f))
                        Pill(
                            when {
                                level > 0 -> "Streak lost"
                                streak > 0 -> "Streak safe · $streak"
                                else -> MomLines.moodLabel(level)
                            },
                            dark = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    ComicActions(level, task, onUp = { stage = RingStage.Task }, onSnooze = onSnooze)
                }
                RingStage.Task -> WakeTaskCard(task, Comic.Tasks, onSolved = onAwake)
            }
        }
    }
}

@Composable
private fun ComicHeader(level: Int, label: String) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); now = LocalTime.now() } }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(now.format(DateTimeFormatter.ofPattern("h:mm")), style = Comic.bangers(64), color = Comic.Ink)
            if (label.isNotBlank()) Text(label.uppercase(), style = Comic.Body, color = Comic.Ink)
        }
        Surface(color = Comic.Ink, shape = RoundedCornerShape(8.dp)) {
            Text(
                if (level == 0) "FIRST CALL" else "SNOOZE #$level",
                style = Comic.Body,
                color = Comic.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun BubbleTail(modifier: Modifier = Modifier) {
    Canvas(modifier.width(36.dp).height(22.dp)) {
        val p = Path().apply {
            moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width * 0.3f, size.height); close()
        }
        drawPath(p, Comic.Ink)
    }
}

/** The comic panel: Mom's face gets angrier, and the bucket comes out at full drama. */
@Composable
private fun MomPanel(level: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Comic.White)
            .border(4.dp, Comic.Ink, RoundedCornerShape(18.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) { drawMom(level) }
        Text(
            Rage.sound(level),
            style = Comic.bangers(30),
            color = Comic.Red,
            modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
        )
    }
}

private fun DrawScope.drawMom(level: Int) {
    val h = size.height
    val cx = size.width * 0.38f
    val cy = h * 0.52f
    val r = h * 0.3f
    val line = 4.dp.toPx()

    // Face and hair.
    drawCircle(Comic.Skin, r, Offset(cx, cy))
    drawCircle(Comic.Ink, r, Offset(cx, cy), style = Stroke(line))
    val hair = Path().apply {
        moveTo(cx - r * 0.98f, cy - r * 0.2f)
        quadraticTo(cx, cy - r * 2.1f, cx + r * 0.98f, cy - r * 0.2f)
        quadraticTo(cx + r * 0.85f, cy - r * 0.95f, cx, cy - r * 1.02f)
        quadraticTo(cx - r * 0.85f, cy - r * 0.95f, cx - r * 0.98f, cy - r * 0.2f)
        close()
    }
    drawPath(hair, Comic.Ink)
    drawCircle(Comic.Red, r * 0.07f, Offset(cx, cy - r * 0.42f))

    // Brows tilt in with every level, eyes, mouth from smile to shout.
    val tilt = level * r * 0.09f
    val eyeY = cy + r * 0.05f
    val ex = r * 0.32f
    drawLine(Comic.Ink, Offset(cx - ex - r * 0.22f, eyeY - r * 0.32f - tilt), Offset(cx - ex + r * 0.18f, eyeY - r * 0.24f), line * 1.5f, StrokeCap.Round)
    drawLine(Comic.Ink, Offset(cx + ex + r * 0.22f, eyeY - r * 0.32f - tilt), Offset(cx + ex - r * 0.18f, eyeY - r * 0.24f), line * 1.5f, StrokeCap.Round)
    drawCircle(Comic.Ink, r * 0.09f, Offset(cx - ex, eyeY))
    drawCircle(Comic.Ink, r * 0.09f, Offset(cx + ex, eyeY))
    val mouthY = cy + r * 0.48f
    when (level) {
        0 -> drawArc(Comic.Ink, 10f, 160f, false, Offset(cx - r * 0.3f, mouthY - r * 0.25f), Size(r * 0.6f, r * 0.35f), style = Stroke(line, cap = StrokeCap.Round))
        1 -> drawLine(Comic.Ink, Offset(cx - r * 0.25f, mouthY), Offset(cx + r * 0.25f, mouthY), line, StrokeCap.Round)
        else -> drawOval(Comic.Ink, Offset(cx - r * 0.22f, mouthY - r * 0.12f), Size(r * 0.44f, r * (0.2f + level * 0.06f)))
    }

    // Steam at level 2+, the bucket of water at full drama.
    if (level >= 2) {
        listOf(-1f, 1f).forEach { side ->
            val sx = cx + side * r * 1.15f
            drawLine(Comic.Grey, Offset(sx, cy - r * 0.6f), Offset(sx + side * r * 0.15f, cy - r * 0.95f), line, StrokeCap.Round)
        }
    }
    if (level >= 3) {
        val bx = size.width * 0.68f
        val bw = size.width * 0.22f
        val by = cy - r * 0.1f
        val bh = r * 1.1f
        drawRoundRect(Comic.Water, Offset(bx, by), Size(bw, bh), CornerRadius(6.dp.toPx()))
        drawRoundRect(Comic.Ink, Offset(bx, by), Size(bw, bh), CornerRadius(6.dp.toPx()), style = Stroke(line))
        drawArc(Comic.Ink, 180f, 180f, false, Offset(bx, by - bw * 0.5f), Size(bw, bw), style = Stroke(line))
        listOf(0.25f, 0.5f, 0.75f).forEachIndexed { i, f ->
            val dx = bx + bw * f
            drawLine(Comic.Water, Offset(dx, by - r * 0.15f), Offset(dx + (i - 1) * r * 0.1f, by - r * 0.5f), line * 1.2f, StrokeCap.Round)
        }
    }
}

@Composable
private fun Pill(text: String, dark: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (dark) Comic.Ink else Comic.White,
        shape = RoundedCornerShape(8.dp),
        border = if (dark) null else BorderStroke(3.dp, Comic.Ink),
        modifier = modifier,
    ) {
        Text(
            text,
            style = Comic.Body,
            color = if (dark) Comic.White else Comic.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun ComicActions(level: Int, task: WakeTask, onUp: () -> Unit, onSnooze: () -> Unit) {
    val cta = when (task) {
        WakeTask.MATH -> "SOLVE 3 SUMS TO SURVIVE"
        WakeTask.STEPS -> "WALK 30 STEPS TO SURVIVE"
        WakeTask.SHAKE -> "SHAKE 20 TIMES TO SURVIVE"
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onUp,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(4.dp, Comic.Ink),
            colors = ButtonDefaults.buttonColors(containerColor = Comic.Red, contentColor = Comic.White),
            modifier = Modifier.fillMaxWidth().height(66.dp),
        ) { Text(cta, style = Comic.bangers(26)) }

        if (level < MomLines.MAX_LEVEL) {
            Button(
                onClick = onSnooze,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(3.dp, Comic.Ink),
                colors = ButtonDefaults.buttonColors(containerColor = Comic.White, contentColor = Comic.Ink),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(
                    if (level == MomLines.MAX_LEVEL - 1) "Snooze (last chance!)" else "Snooze (are you sure?)",
                    style = Comic.Body.copy(fontSize = 16.sp),
                )
            }
        } else {
            Text(
                "NO MORE SNOOZING. MOM HAS SPOKEN.",
                style = Comic.bangers(22),
                color = Comic.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
