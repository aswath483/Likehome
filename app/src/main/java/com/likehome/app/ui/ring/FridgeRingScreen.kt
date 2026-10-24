package com.likehome.app.ui.ring

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likehome.app.R
import com.likehome.app.core.ClockFormat
import com.likehome.app.ui.common.rememberIs24Hour

private object Fridge {
    val Door = Color(0xFFF2F4F5)
    val DoorShade = Color(0xFFDDE2E5)
    val Handle = Color(0xFFB2BEC3)
    val Ink = Color(0xFF2D3436)
    val Grey = Color(0xFF636E72)
    val Yellow = Color(0xFFFFE680)
    val Pink = Color(0xFFFFC2D1)
    val Blue = Color(0xFFBDE3FF)
    val Green = Color(0xFFC9F2C7)

    /** Kalam, a handwriting face by the Indian Type Foundry; it writes Hindi too. */
    val Hand = FontFamily(Font(R.font.kalam_regular), Font(R.font.kalam_bold, FontWeight.Bold))
    fun hand(size: Int, bold: Boolean = false) =
        TextStyle(fontFamily = Hand, fontSize = size.sp, lineHeight = (size * 1.25f).sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)

    val Tasks = TaskColors(
        card = Color(0xFFFFFDF5), border = Ink.copy(alpha = 0.25f), text = Ink, subtext = Grey,
        accent = Ink, onAccent = Color.White, track = Color(0xFFDFE6E9), error = Color(0xFFD63031),
    )
}

/** Fridge notes: sticky notes on the fridge door, in handwriting, held by magnets. */
@Composable
fun FridgeRingScreen(p: RingProps, onSnooze: () -> Unit, onAwake: () -> Unit) {
    var stage by remember(p.level, p.seed) { mutableStateOf(RingStage.Ringing) }
    val line = remember(p) { p.line }
    val clock by rememberClock()
    val is24 = rememberIs24Hour()

    RingPage(
        Brush.horizontalGradient(listOf(Fridge.Door, Fridge.DoorShade)),
        lightBackground = true,
        horizontalPadding = 34.dp,
        modifier = Modifier.drawBehind {
            // The door handle and the seam between the two doors.
            drawRoundRect(
                Fridge.Handle, Offset(size.width - 14.dp.toPx(), size.height * 0.2f),
                Size(6.dp.toPx(), size.height * 0.3f), CornerRadius(3.dp.toPx()),
            )
            drawRect(Fridge.Handle.copy(alpha = 0.5f), Offset(0f, size.height * 0.62f), Size(size.width, 2.dp.toPx()))
        },
    ) {
        Note(Fridge.Yellow, -3f, magnet = Color(0xFFE17055)) {
            Text(line, style = Fridge.hand(26, bold = true), color = Fridge.Ink)
            Text("— ${p.name} ♥", style = Fridge.hand(20), color = Fridge.Grey, modifier = Modifier.padding(top = 8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Note(Fridge.Pink, 4f, magnet = Color(0xFF0984E3), modifier = Modifier.weight(1f)) {
                Text("Now", style = Fridge.hand(16), color = Fridge.Grey)
                Text(ClockFormat.full(clock, is24), style = Fridge.hand(28, bold = true), color = Fridge.Ink)
                if (p.label.isNotBlank()) Text(p.label, style = Fridge.hand(18), color = Fridge.Ink)
            }
            Note(Fridge.Blue, -2f, magnet = Color(0xFF00B894), modifier = Modifier.weight(1f)) {
                Text("Streak", style = Fridge.hand(16), color = Fridge.Grey)
                Text("${p.streak} days", style = Fridge.hand(28, bold = true), color = Fridge.Ink)
                Text(if (p.level == 0) "No snoozes yet!" else "Snoozed ${p.level}×", style = Fridge.hand(18), color = Fridge.Ink)
            }
        }
        Note(Fridge.Green, 1.5f, magnet = Color(0xFFFDCB6E)) {
            Text("Today", style = Fridge.hand(16), color = Fridge.Grey)
            listOf("Wake up", p.task.title, "Drink water", "Eat breakfast").forEachIndexed { i, item ->
                Text(if (i == 0 && p.level > 0) "☐ $item (you're late!)" else "☐ $item", style = Fridge.hand(21), color = Fridge.Ink)
            }
        }

        AnimatedContent(stage, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "fridge") { s ->
            when (s) {
                RingStage.Ringing -> StyleActions(
                    p = p,
                    upText = "Done! · ${p.task.title}",
                    snoozeText = "Snooze ${p.snoozeMinutes} min",
                    noSnoozeText = "No more snoozing. It's written on the fridge.",
                    colors = ActionColors(Fridge.Ink, Color.White, Fridge.Grey, Fridge.Grey),
                    onUp = { stage = RingStage.Task },
                    onSnooze = onSnooze,
                    shape = RoundedCornerShape(14.dp),
                )
                RingStage.Task -> WakeTaskCard(p.task, Fridge.Tasks, onSolved = onAwake)
            }
        }
    }
}

/** A slightly crooked sticky note with a round magnet at the top. */
@Composable
private fun Note(
    color: Color,
    tilt: Float,
    magnet: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.rotate(tilt)) {
        Surface(color = color, shape = RoundedCornerShape(3.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp)) { content() }
        }
        Box(
            Modifier.align(Alignment.TopCenter).offset(y = 0.dp).size(18.dp).clip(CircleShape).background(magnet)
        )
    }
}
