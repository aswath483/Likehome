package com.likehome.app.ui.ring

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.likehome.app.R
import com.likehome.app.core.BarcodeCheck
import com.likehome.app.core.LightCheck
import com.likehome.app.core.MemoryRound
import kotlinx.coroutines.delay

/** Runs [action] once the phone is unlocked: other apps' screens can't open over the lock screen. */
internal fun afterUnlock(context: Context, action: () -> Unit) {
    val activity = context as? Activity
    val keyguard = context.getSystemService(KeyguardManager::class.java)
    if (activity != null && keyguard != null && keyguard.isKeyguardLocked) {
        keyguard.requestDismissKeyguard(activity, object : KeyguardManager.KeyguardDismissCallback() {
            override fun onDismissSucceeded() { action() }
        })
    } else {
        action()
    }
}

/**
 * Google's code scanner: it runs in Play services, so Like Home needs no camera permission.
 * [onResult] gets the code's text, or null when the user backed out or the scanner failed.
 */
internal object BarcodeScanner {
    fun scan(context: Context, onResult: (String?) -> Unit) {
        val options = GmsBarcodeScannerOptions.Builder().enableAutoZoom().build()
        runCatching {
            GmsBarcodeScanning.getClient(context, options).startScan()
                .addOnSuccessListener { onResult(it.rawValue) }
                .addOnCanceledListener { onResult(null) }
                .addOnFailureListener { onResult(null) }
        }.onFailure { onResult(null) }
    }
}

/** Mom's memory game: watch the tiles light up, then tap them back in order. */
@Composable
internal fun MemoryTask(colors: TaskColors, onSolved: () -> Unit) {
    var round by remember { mutableStateOf(MemoryRound.random()) }
    var showing by remember { mutableStateOf(true) }
    var lit by remember { mutableIntStateOf(-1) }
    var progress by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableStateOf(false) }

    LaunchedEffect(round) {
        showing = true
        progress = 0
        delay(700)
        round.sequence.forEach { tile ->
            lit = tile
            delay(550)
            lit = -1
            delay(200)
        }
        showing = false
    }

    Text(stringResource(R.string.memory_title), style = MaterialTheme.typography.labelMedium, color = colors.subtext)
    Spacer(Modifier.height(10.dp))
    Text(
        when {
            showing -> stringResource(R.string.memory_watch)
            wrong -> stringResource(R.string.memory_wrong)
            else -> stringResource(R.string.memory_your_turn, progress, round.sequence.size)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = if (wrong && !showing) colors.error else colors.text,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(14.dp))
    Column(Modifier.widthIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        (0 until MemoryRound.TILES).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tile ->
                    val colour by animateColorAsState(if (tile == lit) colors.accent else colors.track, tween(120), label = "tile")
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(18.dp)).background(colour)
                            .clickable(enabled = !showing) {
                                when (round.tap(tile)) {
                                    MemoryRound.Result.GOOD -> { wrong = false; progress = round.position }
                                    MemoryRound.Result.DONE -> onSolved()
                                    MemoryRound.Result.WRONG -> { wrong = true; round = MemoryRound.random() }
                                }
                            }
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

/**
 * Lights on: turn the room light on (or open the curtains) and keep it on for a few
 * seconds. Phones without a light sensor fall back to sums.
 */
@Composable
internal fun LightTask(colors: TaskColors, onSolved: () -> Unit, onFallback: () -> Unit) {
    val context = LocalContext.current
    var level by remember { mutableFloatStateOf(0f) }
    var noSensor by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(SensorManager::class.java)
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_LIGHT)
        val check = LightCheck()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                level = check.feed(e.values[0], e.timestamp / 1_000_000)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (sm == null || sensor == null) noSensor = true
        else sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sm?.unregisterListener(listener) }
    }

    LaunchedEffect(level) {
        if (level >= 1f && !done) {
            done = true
            onSolved()
        }
    }

    if (noSensor) {
        LaunchedEffect(Unit) { onFallback() }
        return
    }

    val progress by animateFloatAsState(level, tween(250), label = "light-progress")
    Text(stringResource(R.string.light_title), style = MaterialTheme.typography.labelMedium, color = colors.subtext)
    Spacer(Modifier.height(12.dp))
    Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(150.dp)) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(colors.track, -90f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            drawArc(colors.accent, -90f, 360f * progress, false, Offset(inset, inset), arcSize, style = stroke)
        }
        Icon(Icons.Outlined.LightMode, contentDescription = null, tint = if (level > 0f) colors.accent else colors.subtext, modifier = Modifier.size(56.dp))
    }
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(if (level > 0f) R.string.light_keep else R.string.light_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.subtext,
        textAlign = TextAlign.Center,
    )
    TextButton(onClick = onFallback) { Text(stringResource(R.string.fallback_sums), color = colors.subtext) }
}

/**
 * Barcode check: scan the product chosen when the alarm was set, like the toothpaste in
 * the bathroom. Without a saved code, or without Play services, it falls back to sums.
 */
@Composable
internal fun BarcodeTask(saved: String, colors: TaskColors, onSolved: () -> Unit, onFallback: () -> Unit) {
    val context = LocalContext.current
    if (saved.isBlank()) {
        LaunchedEffect(Unit) { onFallback() }
        return
    }
    var wrong by remember { mutableStateOf(false) }
    var trouble by remember { mutableStateOf(false) }

    fun scan() {
        wrong = false
        afterUnlock(context) {
            BarcodeScanner.scan(context) { code ->
                when {
                    code == null -> trouble = true
                    BarcodeCheck.matches(code, saved) -> onSolved()
                    else -> wrong = true
                }
            }
        }
    }

    Text(stringResource(R.string.barcode_title), style = MaterialTheme.typography.labelMedium, color = colors.subtext)
    Spacer(Modifier.height(12.dp))
    Text(
        when {
            wrong -> stringResource(R.string.barcode_wrong)
            trouble -> stringResource(R.string.barcode_trouble)
            else -> stringResource(R.string.barcode_go)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = if (wrong) colors.error else colors.text,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
    TaskButton(stringResource(if (wrong || trouble) R.string.try_again else R.string.barcode_scan), colors, onClick = ::scan)
    TextButton(onClick = onFallback) { Text(stringResource(R.string.camera_fallback), color = colors.subtext) }
}
