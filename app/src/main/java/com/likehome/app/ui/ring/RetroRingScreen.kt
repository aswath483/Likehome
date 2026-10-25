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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likehome.app.R
import com.likehome.app.core.Character
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Rage
import com.likehome.app.ui.common.rememberIs24Hour

private object Retro {
    val Black = Color(0xFF0D0D0D)
    val Green = Color(0xFF39FF14)
    val Pale = Color(0xFFE8FFE0)
    val Dim = Color(0xFF7CFF6B)
    val Red = Color(0xFFFF4D4D)
    val Pixel = FontFamily(Font(R.font.press_start))
    fun text(size: Int) = TextStyle(fontFamily = Pixel, fontSize = size.sp, lineHeight = (size * 1.6f).sp)

    val Tasks = TaskColors(
        card = Black, border = Green, text = Pale, subtext = Dim,
        accent = Green, onAccent = Black, track = Color(0xFF1E3A1E), error = Red, borderWidth = 3,
    )
}

/** 12×12 pixel portrait. H hair, S skin, E eye, B brow, M mouth. */
private fun sprite(level: Int): List<String> {
    val brows = if (level >= 2) ".SBBSSSSBBS." else ".SSBBSSBBSS."
    val (m1, m2) = when (level) {
        0 -> ".SSMSSSSMSS." to "..SSMMMMSS.."
        1 -> ".SSSSSSSSSS." to "..SMMMMMMS.."
        else -> ".SSSMMMMSSS." to "..SMSSSSMS.."
    }
    return listOf(
        "....HHHH....", "..HHHHHHHH..", ".HHSSSSSSHH.", ".HSSSSSSSSH.",
        brows, ".SSESSSSESS.", ".SSSSSSSSSS.", m1, m2, "..SSSSSSSS..", "...SSSSSS...", "....SSSS....",
    )
}

/** Retro game: the character is the level boss; the wake-up task is your attack. */
@Composable
fun RetroRingScreen(p: RingProps, onSnooze: () -> Unit, onAwake: () -> Unit) {
    var stage by remember(p.level, p.seed) { mutableStateOf(RingStage.Ringing) }
    val line = remember(p) { p.line }
    val clock by rememberClock()
    val is24 = rememberIs24Hour()
    val blink by rememberInfiniteTransition(label = "coin").animateFloat(
        1f, 0f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "coin-blink",
    )

    RingPage(SolidColor(Retro.Black), lightBackground = false, modifier = Modifier.drawBehind {
        // CRT scanlines.
        var y = 0f
        while (y < size.height) {
            drawRect(Color.White.copy(alpha = 0.035f), Offset(0f, y), Size(size.width, 2f))
            y += 6f
        }
    }) {
        Row(Modifier.fillMaxWidth()) {
            Text("HI-SCORE ${p.streak}", style = Retro.text(10), color = Retro.Dim, modifier = Modifier.weight(1f))
            Text(ClockFormat.digits(clock, is24), style = Retro.text(10), color = Retro.Dim)
        }
        Text(
            "LEVEL ${p.level + 1} BOSS",
            style = Retro.text(12),
            color = Retro.Red,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            p.name.uppercase(),
            style = Retro.text(22),
            color = Retro.Pale,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PixelSprite(p.character, p.level, Modifier.size(156.dp))
        }

        // Boss rage bar, in blocks.
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("RAGE", style = Retro.text(9), color = Retro.Dim)
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                val filled = Rage.percent(p.level) / 10
                repeat(10) { i ->
                    Box(Modifier.weight(1f).height(12.dp).background(if (i < filled) Retro.Red else Color(0xFF2A2A2A)))
                }
            }
        }

        // Dialogue box with a double border.
        Box(
            Modifier.fillMaxWidth().border(3.dp, Retro.Pale, RectangleShape).padding(4.dp)
                .border(1.dp, Retro.Pale, RectangleShape).padding(14.dp),
        ) {
            Text(line, style = Retro.text(11), color = Retro.Pale)
        }

        AnimatedContent(stage, transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) }, label = "retro") { s ->
            when (s) {
                RingStage.Ringing -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StyleActions(
                        p = p,
                        upText = "▶ FIGHT: ${p.taskCall}",
                        snoozeText = "RUN AWAY (SNOOZE ${p.snoozeMinutes} MIN)",
                        noSnoozeText = "YOU CANNOT RUN. THE BOSS BLOCKS THE DOOR.",
                        colors = ActionColors(Retro.Green, Retro.Black, Retro.Pale, Retro.Red),
                        onUp = { stage = RingStage.Task },
                        onSnooze = onSnooze,
                        shape = RectangleShape,
                        textStyle = Retro.text(11),
                    )
                    Text(
                        "INSERT COIN",
                        style = Retro.text(9),
                        color = Retro.Dim,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().alpha(blink),
                    )
                }
                RingStage.Task -> WakeTaskCard(p, Retro.Tasks, onSolved = onAwake)
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PixelSprite(character: Character, level: Int, modifier: Modifier) {
    val rows = remember(level) { sprite(level.coerceIn(0, 3)) }
    val hair = when (character) {
        Character.DADI, Character.ABUELA -> Color(0xFFCFCFCF)
        Character.BOSS -> Color(0xFF6B4F3A)
        else -> Color(0xFF2A1D14)
    }
    Canvas(modifier.fillMaxSize()) {
        val cell = size.minDimension / 12f
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, ch ->
                val color = when (ch) {
                    'H' -> hair
                    'S' -> Color(0xFFE9B98A)
                    'E', 'B', 'M' -> Color(0xFF1A1A1A)
                    else -> null
                }
                if (color != null) drawRect(color, Offset(x * cell, y * cell), Size(cell + 0.5f, cell + 0.5f))
            }
        }
    }
}
