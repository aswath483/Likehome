package com.likehome.app.ui.sleep

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.alarm.SleepSoundService
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.SleepSound
import com.likehome.app.core.SleepTimer
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.bedside.BedsideActivity
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.detailRes
import com.likehome.app.ui.common.labelRes
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.withHaptic
import com.likehome.app.ui.theme.LH
import java.time.Instant
import java.time.ZoneId

/** Fall asleep to rain, waves or a fan; the timer fades them out and an alarm stops them. */
@Composable
fun SleepScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val playing by SleepSoundService.playing.collectAsState()
    var sound by rememberSaveable { mutableStateOf(playing?.sound ?: SleepSound.RAIN) }
    var minutes by rememberSaveable { mutableStateOf(30) }
    val is24 = rememberIs24Hour()

    PageScaffold(title = stringResource(R.string.sleep_title), onBack = onBack) {
        Text(stringResource(R.string.sleep_intro), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)

        SectionLabel(stringResource(R.string.sleep_sound), Modifier.padding(top = 12.dp))
        SleepSound.entries.forEach { s ->
            SoundCard(s, selected = s == sound, onClick = {
                sound = s
                // Switching sounds while one plays swaps it straight away.
                if (playing != null) SleepSoundService.start(context, s, minutes)
            })
        }

        SectionLabel(stringResource(R.string.sleep_timer), Modifier.padding(top = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SleepTimer.choices.forEach { m ->
                ChoicePill(
                    text = if (m == 0) stringResource(R.string.sleep_forever) else stringResource(R.string.tile_minutes, m),
                    selected = m == minutes,
                    onClick = { minutes = m },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val now = playing
        if (now == null) {
            PrimaryButton(stringResource(R.string.sleep_start), onClick = { SleepSoundService.start(context, sound, minutes) })
            TextButton(onClick = { BedsideActivity.open(context) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.bedside_open), color = LH.Saffron)
            }
        } else {
            val until = now.endsAt?.let {
                val time = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
                stringResource(R.string.sleep_until, ClockFormat.full(time, is24))
            } ?: stringResource(R.string.sleep_no_timer)
            Text(
                stringResource(R.string.dot_pair, stringResource(R.string.sleep_playing, stringResource(now.sound.labelRes)), until),
                style = MaterialTheme.typography.bodyLarge,
                color = LH.Saffron,
            )
            SecondaryButton(stringResource(R.string.stop), onClick = { SleepSoundService.stop(context) })
        }
        Text(stringResource(R.string.sleep_alarm_note), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
    }
}

@Composable
private fun SoundCard(sound: SleepSound, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = withHaptic(onClick),
        color = if (selected) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LH.Saffron else LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(sound.icon, contentDescription = null, tint = if (selected) LH.Saffron else LH.Oat, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(stringResource(sound.labelRes), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(stringResource(sound.detailRes), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }
    }
}

private val SleepSound.icon: ImageVector get() = when (this) {
    SleepSound.RAIN -> Icons.Outlined.WaterDrop
    SleepSound.WAVES -> Icons.Outlined.Waves
    SleepSound.FAN -> Icons.Outlined.Air
    SleepSound.BROWN -> Icons.Outlined.Bedtime
    SleepSound.WHITE -> Icons.Outlined.Grain
}
