package com.likehome.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.likehome.app.ui.theme.LH

private val Skin = Color(0xFFE9B98A)
private val Ink = Color(0xFF2A1D14)

/** Mom's drawn face; brows and mouth change with her mood (0 sweet … 3 full drama). */
@Composable
fun MoodFace(level: Int, modifier: Modifier = Modifier) {
    Canvas(modifier.clip(CircleShape).background(LH.Crema)) {
        val w = size.width
        val c = Offset(w / 2, w * 0.56f)
        drawCircle(color = Skin, radius = w * 0.34f, center = c)
        // Hair
        drawArc(
            color = Ink,
            startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(w * 0.14f, w * 0.14f), size = Size(w * 0.72f, w * 0.6f),
        )
        // Bindi
        drawCircle(color = LH.SaffronDeep, radius = w * 0.03f, center = Offset(w / 2, w * 0.46f))
        val eyeY = w * 0.58f
        drawCircle(Ink, radius = w * 0.03f, center = Offset(w * 0.4f, eyeY))
        drawCircle(Ink, radius = w * 0.03f, center = Offset(w * 0.6f, eyeY))
        // Brows tilt more as she gets angrier.
        val tilt = level.coerceIn(0, 3) * w * 0.025f
        val stroke = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
        drawLine(Ink, Offset(w * 0.33f, eyeY - w * 0.08f - tilt), Offset(w * 0.45f, eyeY - w * 0.07f), stroke.width, StrokeCap.Round)
        drawLine(Ink, Offset(w * 0.67f, eyeY - w * 0.08f - tilt), Offset(w * 0.55f, eyeY - w * 0.07f), stroke.width, StrokeCap.Round)
        // Mouth: smile at level 0, flat, then frown.
        val mouthTop = w * 0.68f
        when (level) {
            0 -> drawArc(Ink, 20f, 140f, false, Offset(w * 0.4f, mouthTop - w * 0.06f), Size(w * 0.2f, w * 0.12f), style = stroke)
            1 -> drawLine(Ink, Offset(w * 0.42f, mouthTop + w * 0.02f), Offset(w * 0.58f, mouthTop + w * 0.02f), stroke.width, StrokeCap.Round)
            else -> drawArc(Ink, 200f, 140f, false, Offset(w * 0.4f, mouthTop), Size(w * 0.2f, w * 0.12f), style = stroke)
        }
    }
}
