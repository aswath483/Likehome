package com.likehome.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.likehome.app.core.Character
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.theme.LH

private val Ink = Color(0xFF2A1D14)
private val Grey = Color(0xFFB8B2AA)

/** Skin, hair and the little things that make each character recognisable. */
private data class Look(val skin: Color, val hair: Color)

private fun lookOf(c: Character) = when (c) {
    Character.MOM -> Look(Color(0xFFE9B98A), Ink)
    Character.PAPA -> Look(Color(0xFFD9A577), Color(0xFF3B2E25))
    Character.DADI -> Look(Color(0xFFE6B48C), Grey)
    Character.BOSS -> Look(Color(0xFFF0C9A0), Color(0xFF4A3B30))
    Character.ABUELA -> Look(Color(0xFFDDA67C), Color(0xFFD8D2CB))
}

/** Mom's drawn face; brows and mouth change with her mood (0 sweet … 3 full drama). */
@Composable
fun MoodFace(level: Int, modifier: Modifier = Modifier) = CharacterFace(Character.MOM, level, modifier)

/** A character's face at a drama level, on a soft round tile. */
@Composable
fun CharacterFace(character: Character, level: Int, modifier: Modifier = Modifier) {
    val said = "${character.displayName}, ${MomLines.moodLabel(level).lowercase()}"
    Canvas(modifier.clip(CircleShape).background(LH.Crema).semantics { contentDescription = said }) {
        drawCharacter(character, level.coerceIn(0, 3))
    }
}

private fun DrawScope.drawCharacter(character: Character, level: Int) {
    val w = size.width
    val look = lookOf(character)
    val faceCenter = Offset(w / 2, w * 0.56f)
    val faceR = w * 0.34f

    // Hair behind the face.
    when (character) {
        Character.DADI, Character.MOM -> drawCircle(look.hair, w * 0.11f, Offset(w / 2, w * 0.17f)) // bun
        Character.ABUELA -> listOf(0.22f, 0.36f, 0.5f, 0.64f, 0.78f).forEach { x ->
            drawCircle(look.hair, w * 0.1f, Offset(w * x, w * 0.27f))
        }
        else -> Unit
    }
    drawCircle(look.skin, faceR, faceCenter)
    when (character) {
        Character.MOM, Character.DADI -> drawArc(
            look.hair, 180f, 180f, true,
            topLeft = Offset(w * 0.14f, w * 0.14f), size = Size(w * 0.72f, w * 0.6f),
        )
        Character.PAPA, Character.BOSS -> drawArc(
            look.hair, 185f, 170f, true,
            topLeft = Offset(w * 0.18f, w * 0.2f), size = Size(w * 0.64f, w * 0.42f),
        )
        Character.ABUELA -> drawArc(
            look.hair, 180f, 180f, true,
            topLeft = Offset(w * 0.17f, w * 0.18f), size = Size(w * 0.66f, w * 0.5f),
        )
    }
    if (character == Character.MOM) drawCircle(LH.SaffronDeep, w * 0.03f, Offset(w / 2, w * 0.46f)) // bindi

    val eyeY = w * 0.58f
    drawCircle(Ink, w * 0.03f, Offset(w * 0.4f, eyeY))
    drawCircle(Ink, w * 0.03f, Offset(w * 0.6f, eyeY))
    val stroke = w * 0.035f
    if (character == Character.DADI || character == Character.PAPA) {
        // Round reading glasses.
        val ring = Stroke(width = w * 0.022f)
        drawCircle(Ink, w * 0.075f, Offset(w * 0.4f, eyeY), style = ring)
        drawCircle(Ink, w * 0.075f, Offset(w * 0.6f, eyeY), style = ring)
        drawLine(Ink, Offset(w * 0.475f, eyeY), Offset(w * 0.525f, eyeY), w * 0.02f)
    }

    // Brows tilt more as they get angrier.
    val tilt = level * w * 0.025f
    drawLine(Ink, Offset(w * 0.33f, eyeY - w * 0.1f - tilt), Offset(w * 0.45f, eyeY - w * 0.09f), stroke, StrokeCap.Round)
    drawLine(Ink, Offset(w * 0.67f, eyeY - w * 0.1f - tilt), Offset(w * 0.55f, eyeY - w * 0.09f), stroke, StrokeCap.Round)

    if (character == Character.PAPA) {
        val moustache = Path().apply {
            moveTo(w * 0.38f, w * 0.69f)
            quadraticTo(w * 0.5f, w * 0.62f, w * 0.62f, w * 0.69f)
            quadraticTo(w * 0.5f, w * 0.67f, w * 0.38f, w * 0.69f)
            close()
        }
        drawPath(moustache, look.hair)
    }

    // Mouth: smile at level 0, flat, then frown.
    val mouthTop = w * 0.7f
    val mouth = Stroke(width = stroke, cap = StrokeCap.Round)
    when (level) {
        0 -> drawArc(Ink, 20f, 140f, false, Offset(w * 0.4f, mouthTop - w * 0.06f), Size(w * 0.2f, w * 0.12f), style = mouth)
        1 -> drawLine(Ink, Offset(w * 0.42f, mouthTop + w * 0.02f), Offset(w * 0.58f, mouthTop + w * 0.02f), stroke, StrokeCap.Round)
        else -> drawArc(Ink, 200f, 140f, false, Offset(w * 0.4f, mouthTop), Size(w * 0.2f, w * 0.12f), style = mouth)
    }

    when (character) {
        Character.BOSS -> {
            // Collar and tie peeking in at the bottom.
            drawRoundRect(Color(0xFFF4ECDF), Offset(w * 0.3f, w * 0.88f), Size(w * 0.4f, w * 0.2f), CornerRadius(w * 0.04f))
            drawRect(Color(0xFFB4413A), Offset(w * 0.465f, w * 0.88f), Size(w * 0.07f, w * 0.14f))
        }
        Character.ABUELA -> {
            drawCircle(LH.Saffron, w * 0.03f, Offset(w * 0.17f, w * 0.64f)) // earrings
            drawCircle(LH.Saffron, w * 0.03f, Offset(w * 0.83f, w * 0.64f))
        }
        else -> Unit
    }
}
