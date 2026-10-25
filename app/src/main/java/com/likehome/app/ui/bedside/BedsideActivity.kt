package com.likehome.app.ui.bedside

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.likehome.app.R
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.BedsideClock
import com.likehome.app.core.ClockFormat
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.BedtimeLog
import com.likehome.app.ui.MainActivity
import com.likehome.app.ui.common.dateFormat
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.text
import com.likehome.app.ui.theme.LikeHomeTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * Bedside clock: a big, dim clock for the night table while the phone charges. Ember-red
 * on black so it doesn't wake anyone, nudged a little every minute so the screen doesn't
 * burn in. Opening it in the evening counts as going to bed for sleep insights.
 */
class BedsideActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply { screenBrightness = 0.02f }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        BedtimeLog.get(this).wentToBed()
        setContent {
            LikeHomeTheme {
                BedsideScreen(
                    onSleepSounds = {
                        startActivity(Intent(this, MainActivity::class.java).setAction(MainActivity.ACTION_SLEEP_SOUNDS))
                    },
                    onClose = { finish() },
                )
            }
        }
    }

    companion object {
        fun open(context: Context) =
            context.startActivity(Intent(context, BedsideActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private val Ember = Color(0xFFB4442E)
private val EmberDim = Color(0xFF6E2A1D)

@Composable
private fun BedsideScreen(onSleepSounds: () -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val is24 = rememberIs24Hour()
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var controls by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000L * (60 - now.second).coerceAtLeast(1))
        }
    }
    LaunchedEffect(controls) {
        if (controls) {
            delay(6_000)
            controls = false
        }
    }
    val alarms by AlarmRepository.get(context).alarms.collectAsState()
    val zoned = remember(now) { ZonedDateTime.now() }
    val next = remember(alarms, now) {
        alarms.filter { it.enabled }.map { it to AlarmScheduler.nextRing(context, it, zoned) }.minByOrNull { it.second }
    }
    val res = context.resources

    BoxWithConstraints(
        Modifier.fillMaxSize().background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { controls = !controls },
    ) {
        val shift = BedsideClock.offset(now)
        val landscape = maxWidth > maxHeight
        Column(
            Modifier.fillMaxWidth().align(Alignment.Center)
                .offset(x = maxWidth * shift.first, y = maxHeight * shift.second),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    ClockFormat.digits(now.toLocalTime(), is24),
                    color = Ember,
                    fontSize = if (landscape) 150.sp else 104.sp,
                    fontWeight = FontWeight.Light,
                )
                ClockFormat.period(now.toLocalTime(), is24)?.let {
                    Text(it, color = EmberDim, fontSize = 28.sp, modifier = Modifier.padding(start = 8.dp, bottom = 24.dp))
                }
            }
            Text(now.format(res.dateFormat("EEEEdMMMM")), color = EmberDim, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(18.dp))
            Text(
                next?.let { (alarm, at) ->
                    stringResource(
                        R.string.bedside_next,
                        stringResource(alarm.character.nameRes),
                        ClockFormat.full(at.toLocalTime(), is24),
                        ClockFormat.until(zoned, at).text(res),
                    )
                } ?: stringResource(R.string.bedside_no_alarm),
                color = EmberDim,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
        AnimatedVisibility(
            visible = controls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(24.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onSleepSounds) { Text(stringResource(R.string.sleep_title), color = Ember) }
                OutlinedButton(onClick = onClose) { Text(stringResource(R.string.bedside_close), color = Ember) }
            }
        }
        Box(Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(top = 16.dp)) {
            if (controls) Text(stringResource(R.string.bedside_hint), color = EmberDim, style = MaterialTheme.typography.bodySmall)
        }
    }
}
