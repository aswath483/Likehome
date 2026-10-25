package com.likehome.app.ui.ring

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.likehome.app.core.AccelStepCounter
import com.likehome.app.core.MathPuzzle
import com.likehome.app.core.ShakeCounter
import com.likehome.app.core.WakeTask
import com.likehome.app.mom.Language
import com.likehome.app.ui.theme.LH

/** Colours for a task card, so each ringing style can dress it its own way. */
data class TaskColors(
    val card: Color,
    val border: Color,
    val text: Color,
    val subtext: Color,
    val accent: Color,
    val onAccent: Color,
    val track: Color,
    val error: Color,
    val borderWidth: Int = 1,
) {
    companion object {
        val Chat = TaskColors(
            card = LH.Roast, border = LH.Line, text = LH.Cream, subtext = LH.Muted,
            accent = LH.Saffron, onAccent = LH.OnSaffron, track = LH.Line, error = LH.Danger,
        )
    }
}

/**
 * The wake-up proof. Steps and shake fall back to sums when the phone has no usable
 * sensor, and the user can always switch to sums (for a bad knee or a sleeping partner).
 */
@Composable
fun WakeTaskCard(p: RingProps, colors: TaskColors, onSolved: () -> Unit) =
    WakeTaskCard(p.task, colors, onSolved, photoRef = p.photoRef, name = p.name, language = p.language, seed = p.seed)

@Composable
fun WakeTaskCard(
    task: WakeTask,
    colors: TaskColors,
    onSolved: () -> Unit,
    photoRef: String = "",
    name: String = "Mummy",
    language: Language = Language.ENGLISH,
    seed: Long = 0L,
) {
    var current by remember(task) { mutableStateOf(task) }
    val haptic = LocalHapticFeedback.current
    val solved: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onSolved()
    }
    val switchToMath = { current = WakeTask.MATH }
    TaskSurface(colors) {
        when (current) {
            WakeTask.MATH -> MathTask(colors, solved)
            WakeTask.STEPS -> MotionTask(WakeTask.STEPS, colors, solved, onFallback = switchToMath)
            WakeTask.SHAKE -> MotionTask(WakeTask.SHAKE, colors, solved, onFallback = switchToMath)
            WakeTask.PHOTO -> PhotoTask(photoRef, colors, solved, onFallback = switchToMath)
            WakeTask.VOICE -> VoiceTask(name, language, colors, solved, onFallback = switchToMath)
            WakeTask.PROMISE -> PromiseTask(seed, language, colors, solved)
        }
    }
}

@Composable
private fun TaskSurface(colors: TaskColors, content: @Composable () -> Unit) {
    Surface(
        color = colors.card,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(colors.borderWidth.dp, colors.border),
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
    ) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

/** Prove you're awake: three quick sums. */
@Composable
private fun MathTask(colors: TaskColors, onSolved: () -> Unit) {
    val sums = remember { MathPuzzle.sums() }
    var index by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    val sum = sums[index]
    val submit: () -> Unit = {
        if (answer.toIntOrNull() == sum.answer) {
            if (index == sums.lastIndex) onSolved() else { index++; answer = "" }
        } else {
            wrong = true
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(sums.size) { i ->
            Box(
                Modifier.size(width = 26.dp, height = 5.dp).clip(RoundedCornerShape(3.dp))
                    .background(if (i < index) colors.accent else if (i == index) colors.subtext else colors.track)
            )
        }
    }
    Spacer(Modifier.height(14.dp))
    Text("Prove it, beta", style = MaterialTheme.typography.labelMedium, color = colors.subtext)
    Text(sum.toString(), style = MaterialTheme.typography.displayMedium, color = colors.text)
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = answer,
        onValueChange = { v -> answer = v.filter(Char::isDigit).take(3); wrong = false },
        singleLine = true,
        placeholder = { Text("Answer") },
        isError = wrong,
        supportingText = if (wrong) {
            { Text("Mom saw that. Try again.", color = colors.error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (answer.isNotEmpty()) submit() }),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.track,
            cursorColor = colors.accent,
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            focusedPlaceholderColor = colors.subtext,
            unfocusedPlaceholderColor = colors.subtext,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    TaskButton(if (index == sums.lastIndex) "Done, I'm awake" else "Next", colors, enabled = answer.isNotEmpty(), onClick = submit)
}

/** Walk or shake, counted live from the phone's sensors. */
@Composable
private fun MotionTask(task: WakeTask, colors: TaskColors, onSolved: () -> Unit, onFallback: () -> Unit) {
    val context = LocalContext.current
    var count by remember { mutableIntStateOf(0) }
    var noSensor by remember { mutableStateOf(false) }
    var solved by remember { mutableStateOf(false) }

    DisposableEffect(task) {
        val sm = context.getSystemService(SensorManager::class.java)
        val canUseStepSensor = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED
        val stepSensor = if (task == WakeTask.STEPS && canUseStepSensor)
            sm?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) else null
        val accel = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val shakes = ShakeCounter()
        val steps = AccelStepCounter()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val counted = when (e.sensor.type) {
                    Sensor.TYPE_STEP_DETECTOR -> true
                    Sensor.TYPE_ACCELEROMETER -> {
                        val (x, y, z) = Triple(e.values[0], e.values[1], e.values[2])
                        val ms = e.timestamp / 1_000_000
                        if (task == WakeTask.SHAKE) shakes.feed(x, y, z, ms) else steps.feed(x, y, z, ms)
                    }
                    else -> false
                }
                if (counted) count++
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val sensor = stepSensor ?: accel
        if (sm == null || sensor == null) noSensor = true
        else sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sm?.unregisterListener(listener) }
    }

    LaunchedEffect(count) {
        if (count >= task.target && !solved) {
            solved = true
            onSolved()
        }
    }

    if (noSensor) {
        LaunchedEffect(Unit) { onFallback() }
        return
    }

    val progress by animateFloatAsState(
        (count.toFloat() / task.target).coerceIn(0f, 1f), tween(250), label = "task-progress",
    )
    val left = (task.target - count).coerceAtLeast(0)

    Text(
        if (task == WakeTask.STEPS) "Out of bed, beta" else "Shake it, beta",
        style = MaterialTheme.typography.labelMedium,
        color = colors.subtext,
    )
    Spacer(Modifier.height(12.dp))
    Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(150.dp)) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(colors.track, -90f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            drawArc(colors.accent, -90f, 360f * progress, false, Offset(inset, inset), arcSize, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$left", style = MaterialTheme.typography.displayMedium, color = colors.text)
            Text(
                if (task == WakeTask.STEPS) "steps to go" else "shakes to go",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.subtext,
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(
        if (task == WakeTask.STEPS) "Walk to the kitchen. Mom is counting." else "Harder! Mom can't hear you shaking.",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.subtext,
        textAlign = TextAlign.Center,
    )
    TextButton(onClick = onFallback) {
        Text("Can't do this now? Solve sums instead", color = colors.subtext)
    }
}

@Composable
internal fun TaskButton(text: String, colors: TaskColors, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent, contentColor = colors.onAccent,
            disabledContainerColor = colors.track, disabledContentColor = colors.subtext,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
