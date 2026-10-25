package com.likehome.app.ui.sunrise

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.alarm.SunriseService
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.GentleWake
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.common.nameRes
import androidx.compose.ui.platform.LocalContext
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.theme.LH
import com.likehome.app.ui.theme.LikeHomeTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * The sunrise screen: the sky warms from night to morning and the screen brightens with it,
 * so the light wakes you before Mom does. Closes itself when the sunrise ends.
 */
class SunriseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            LikeHomeTheme {
                val state by SunriseService.state.collectAsState()
                var shown by remember { mutableStateOf(false) }
                LaunchedEffect(state) {
                    if (state != null) shown = true
                    // The sunrise is over (Mom is calling, or the user got up): this screen is done.
                    else if (shown) finish()
                }
                // Opened late, after the sunrise already ended.
                LaunchedEffect(Unit) {
                    delay(1_500)
                    if (SunriseService.state.value == null) finish()
                }
                state?.let { s ->
                    SunriseScreen(
                        s,
                        onBrightness = ::setBrightness,
                        onUp = { SunriseService.up(this) },
                        onSleep = { finish() },
                    )
                }
            }
        }
    }

    private fun setBrightness(level: Float) {
        window.attributes = window.attributes.apply { screenBrightness = level.coerceIn(0.01f, 1f) }
    }
}

@Composable
private fun SunriseScreen(
    s: SunriseService.State,
    onBrightness: (Float) -> Unit,
    onUp: () -> Unit,
    onSleep: () -> Unit,
) {
    val minutes = ((s.ringAt - s.startedAt) / 60_000L).toInt().coerceAtLeast(1)
    var progress by remember { mutableFloatStateOf(0f) }
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(s) {
        while (true) {
            progress = GentleWake.progress(System.currentTimeMillis() - s.startedAt, minutes)
            now = LocalTime.now()
            onBrightness(GentleWake.brightness(progress))
            delay(1_000)
        }
    }
    val is24 = rememberIs24Hour()
    val context = LocalContext.current
    val caller = remember(s.alarmId) {
        AlarmRepository.get(context).get(s.alarmId)?.character ?: SettingsRepository.get(context).current.character
    }
    val ringAt = Instant.ofEpochMilli(s.ringAt).atZone(ZoneId.systemDefault()).toLocalTime()

    // Night blue → dawn ember → saffron morning.
    val top = if (progress < 0.5f) lerp(LH.Night, LH.Dawn, progress * 2) else lerp(LH.Dawn, LH.SaffronDeep, (progress - 0.5f) * 2)
    val bottom = if (progress < 0.5f) lerp(LH.Dawn, LH.SaffronDeep, progress * 2) else lerp(LH.SaffronDeep, Sky, (progress - 0.5f) * 2)
    val text = lerp(LH.Cream, LH.OnSaffron, ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f))

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
        Canvas(Modifier.fillMaxSize()) {
            // The sun climbs from below the horizon to a third of the way up.
            val radius = size.width * 0.22f
            val y = size.height * (1.05f - 0.45f * progress)
            drawCircle(Brush.radialGradient(listOf(Sun.copy(alpha = 0.55f), Color.Transparent), Offset(size.width / 2, y), radius * 2.4f), radius * 2.4f, Offset(size.width / 2, y))
            drawCircle(Sun, radius, Offset(size.width / 2, y))
        }
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(48.dp))
                Text(ClockFormat.digits(now, is24), style = MaterialTheme.typography.displayLarge, color = text)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.sunrise_good_morning), style = MaterialTheme.typography.titleLarge, color = text, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.sunrise_calls_at, stringResource(caller.nameRes), ClockFormat.full(ringAt, is24)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = text.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                PrimaryButton(stringResource(R.string.sunrise_up), onClick = onUp)
                TextButton(onClick = onSleep) {
                    Text(stringResource(R.string.sunrise_sleep), color = text.copy(alpha = 0.8f))
                }
            }
        }
    }
}

private val Sun = Color(0xFFFFD27A)
private val Sky = Color(0xFFFFE7C2)
