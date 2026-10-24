package com.likehome.app.ui.ring

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.core.RingStyle
import com.likehome.app.core.WakeTask
import com.likehome.app.mom.Language
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** The ringing screen in the alarm's style; Smart mix turns comic after 3 snoozes. */
@Composable
fun RingScreen(
    style: RingStyle,
    level: Int,
    seed: Long,
    label: String,
    language: Language,
    task: WakeTask,
    onSnooze: () -> Unit,
    onAwake: () -> Unit,
) {
    when (style.resolve(level)) {
        RingStyle.COMIC -> ComicRingScreen(level, seed, label, language, task, onSnooze, onAwake)
        else -> ChatRingScreen(level, seed, label, language, task, onSnooze, onAwake)
    }
}

/** Chat style: Mom's messages pile up with every snooze. */
@Composable
private fun ChatRingScreen(
    level: Int,
    seed: Long,
    label: String,
    language: Language,
    task: WakeTask,
    onSnooze: () -> Unit,
    onAwake: () -> Unit,
) {
    val messages = remember(level, seed, language) { MomLines.conversation(level, seed, language) }
    var shown by remember(level, seed) { mutableIntStateOf(0) }
    var stage by remember(level, seed) { mutableStateOf(RingStage.Ringing) }

    LaunchedEffect(messages) {
        // Earlier messages appear quickly (already "sent"), the newest after a typing pause.
        while (shown < messages.size) {
            delay(if (shown < messages.size - 1) 250 else 1400)
            shown++
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(LH.Espresso)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp),
    ) {
        ClockHeader(label)
        MomHeader(level = level, typing = shown < messages.size)
        Spacer(Modifier.height(18.dp))

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            messages.forEachIndexed { i, text ->
                AnimatedVisibility(
                    visible = i < shown,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 },
                ) { MomBubble(text, isNewest = i == messages.lastIndex) }
            }
            if (shown < messages.size) TypingBubble()
        }

        AnimatedContent(
            targetState = stage,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "ring-stage",
        ) { s ->
            when (s) {
                RingStage.Ringing -> RingActions(
                    canSnooze = level < MomLines.MAX_LEVEL,
                    level = level,
                    onUp = { stage = RingStage.Task },
                    onSnooze = onSnooze,
                )
                RingStage.Task -> WakeTaskCard(task, TaskColors.Chat, onSolved = onAwake)
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

internal enum class RingStage { Ringing, Task }

@Composable
private fun ClockHeader(label: String) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); now = LocalTime.now() } }
    Column(Modifier.fillMaxWidth().padding(top = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.ifBlank { "Wake-up call" }.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = LH.Saffron,
        )
        Text(
            now.format(DateTimeFormatter.ofPattern("h:mm")),
            style = MaterialTheme.typography.displayLarge,
            color = LH.Cream,
        )
    }
}

@Composable
private fun MomHeader(level: Int, typing: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MomAvatar(level, Modifier.size(52.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Mummy", style = MaterialTheme.typography.titleMedium, color = LH.Cream)
            Text(
                if (typing) "typing…" else "online",
                style = MaterialTheme.typography.bodyMedium,
                color = if (typing) LH.Saffron else LH.Muted,
            )
        }
        MoodMeter(level)
    }
}

/** A simple drawn face whose brows and mouth change with her mood. */
@Composable
private fun MomAvatar(level: Int, modifier: Modifier = Modifier) {
    Canvas(modifier.clip(CircleShape).background(LH.Crema)) {
        val w = size.width
        val c = Offset(w / 2, w * 0.56f)
        drawCircle(color = androidx.compose.ui.graphics.Color(0xFFE9B98A), radius = w * 0.34f, center = c)
        // Hair
        drawArc(
            color = androidx.compose.ui.graphics.Color(0xFF2A1D14),
            startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(w * 0.14f, w * 0.14f), size = Size(w * 0.72f, w * 0.6f),
        )
        // Bindi
        drawCircle(color = LH.SaffronDeep, radius = w * 0.03f, center = Offset(w / 2, w * 0.46f))
        val ink = androidx.compose.ui.graphics.Color(0xFF2A1D14)
        val eyeY = w * 0.58f
        drawCircle(ink, radius = w * 0.03f, center = Offset(w * 0.4f, eyeY))
        drawCircle(ink, radius = w * 0.03f, center = Offset(w * 0.6f, eyeY))
        // Brows tilt more as she gets angrier.
        val tilt = level * w * 0.025f
        val stroke = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
        drawLine(ink, Offset(w * 0.33f, eyeY - w * 0.08f - tilt), Offset(w * 0.45f, eyeY - w * 0.07f), stroke.width, StrokeCap.Round)
        drawLine(ink, Offset(w * 0.67f, eyeY - w * 0.08f - tilt), Offset(w * 0.55f, eyeY - w * 0.07f), stroke.width, StrokeCap.Round)
        // Mouth: smile at level 0, flat, then frown.
        val mouthTop = w * 0.68f
        when (level) {
            0 -> drawArc(ink, 20f, 140f, false, Offset(w * 0.4f, mouthTop - w * 0.06f), Size(w * 0.2f, w * 0.12f), style = stroke)
            1 -> drawLine(ink, Offset(w * 0.42f, mouthTop + w * 0.02f), Offset(w * 0.58f, mouthTop + w * 0.02f), stroke.width, StrokeCap.Round)
            else -> drawArc(ink, 200f, 140f, false, Offset(w * 0.4f, mouthTop), Size(w * 0.2f, w * 0.12f), style = stroke)
        }
    }
}

@Composable
private fun MoodMeter(level: Int) {
    Column(horizontalAlignment = Alignment.End) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(MomLines.MAX_LEVEL + 1) { i ->
                Box(
                    Modifier
                        .size(width = 14.dp, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i <= level) LH.Saffron else LH.Line)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(MomLines.moodLabel(level), style = MaterialTheme.typography.bodyMedium, color = LH.Muted)
    }
}

@Composable
private fun MomBubble(text: String, isNewest: Boolean) {
    Surface(
        color = if (isNewest) LH.MyBubble else LH.Bubble,
        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 22.dp),
        modifier = Modifier.widthIn(max = 300.dp),
    ) {
        Text(
            text,
            style = if (isNewest) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = if (isNewest) LH.Cream else LH.Oat,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun TypingBubble() {
    val t = rememberInfiniteTransition(label = "typing")
    Surface(color = LH.Bubble, shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                val a by t.animateFloat(
                    initialValue = 0.25f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(500, delayMillis = i * 150), RepeatMode.Reverse),
                    label = "dot$i",
                )
                Box(Modifier.size(7.dp).alpha(a).clip(CircleShape).background(LH.Oat))
            }
        }
    }
}

@Composable
private fun RingActions(canSnooze: Boolean, level: Int, onUp: () -> Unit, onSnooze: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PrimaryPill("I'm up, Mummy!", onUp)
        if (canSnooze) {
            OutlinedButton(
                onClick = onSnooze,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, LH.Line),
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                Text(
                    if (level == MomLines.MAX_LEVEL - 1) "Snooze 5 min · last chance" else "Snooze 5 min · she'll get angrier",
                    color = LH.Oat,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            Text(
                "No more snoozing. Mom has spoken.",
                style = MaterialTheme.typography.bodyMedium,
                color = LH.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun PrimaryPill(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = LH.Saffron, contentColor = LH.OnSaffron,
            disabledContainerColor = LH.Crema, disabledContentColor = LH.Muted,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = Modifier.fillMaxWidth().height(62.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
