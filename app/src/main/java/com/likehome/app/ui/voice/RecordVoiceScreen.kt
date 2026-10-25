package com.likehome.app.ui.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.likehome.app.alarm.Voices
import com.likehome.app.core.Character
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.styles.CharacterPickerRow
import com.likehome.app.ui.theme.LH
import java.io.File
import androidx.compose.ui.res.stringResource
import com.likehome.app.R
import com.likehome.app.ui.common.moodRes

private const val MAX_CLIP_MS = 15_000

/**
 * Record your own voice for any character: one clip per drama level, played instead of
 * the phone's voice when the alarm rings. Real Mom, real drama.
 */
@Composable
fun RecordVoiceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var character by remember { mutableStateOf(Character.MOM) }
    var recordingLevel by remember { mutableStateOf<Int?>(null) }
    var playingLevel by remember { mutableStateOf<Int?>(null) }
    // Bumped after every change so "recorded" badges refresh.
    var version by remember { mutableIntStateOf(0) }
    val recorder = remember { ClipRecorder(context) }
    val player = remember { ClipPlayer() }
    var allowed by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed = it }

    DisposableEffect(Unit) {
        onDispose {
            recorder.stop()
            player.stop()
        }
    }

    PageScaffold(title = stringResource(R.string.record_voice), onBack = onBack) {
        Text(
            stringResource(R.string.voice_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        SectionLabel(stringResource(R.string.whose_voice), Modifier.padding(top = 8.dp))
        CharacterPickerRow(selected = character, onPick = {
            recorder.stop(); player.stop()
            recordingLevel = null; playingLevel = null
            character = it
        })

        SectionLabel(stringResource(R.string.clips), Modifier.padding(top = 12.dp))
        (0..MomLines.MAX_LEVEL).forEach { level ->
            val file = Voices.recording(context, character, level)
            val has = remember(character, level, version) { file.length() > 0 }
            ClipRow(
                level = level,
                example = remember(character, level) { MomLines.lineFor(level, 1_000L * level, character = character) },
                recorded = has,
                recording = recordingLevel == level,
                playing = playingLevel == level,
                onRecord = {
                    when {
                        !allowed -> permission.launch(Manifest.permission.RECORD_AUDIO)
                        recordingLevel == level -> {
                            recorder.stop(); recordingLevel = null; version++
                        }
                        else -> {
                            player.stop(); playingLevel = null
                            recorder.stop()
                            if (recorder.start(file) { recordingLevel = null; version++ }) recordingLevel = level
                        }
                    }
                },
                onPlay = {
                    if (playingLevel == level) {
                        player.stop(); playingLevel = null
                    } else {
                        recorder.stop(); recordingLevel = null
                        player.play(file) { playingLevel = null }
                        playingLevel = level
                    }
                },
                onDelete = {
                    player.stop(); playingLevel = null
                    file.delete(); version++
                },
            )
        }
        Text(
            stringResource(R.string.voice_tip),
            style = MaterialTheme.typography.bodySmall,
            color = LH.Muted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ClipRow(
    level: Int,
    example: String,
    recorded: Boolean,
    recording: Boolean,
    playing: Boolean,
    onRecord: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
) {
    val blink by rememberInfiniteTransition(label = "rec").animateFloat(1f, 0.3f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "rec-blink")
    Surface(
        color = if (recording) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (recording) LH.Danger else LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.level_mood, level + 1, stringResource(moodRes(level))), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                    Text(
                        when {
                            recording -> stringResource(R.string.recording_now)
                            recorded -> stringResource(R.string.recorded)
                            else -> stringResource(R.string.not_recorded)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (recording) LH.Danger else if (recorded) LH.Success else LH.Muted,
                    )
                }
                if (recorded && !recording) {
                    IconButton(onClick = onPlay) {
                        Icon(if (playing) Icons.Outlined.Stop else Icons.Outlined.PlayArrow, contentDescription = stringResource(if (playing) R.string.stop else R.string.play), tint = LH.Cream)
                    }
                    IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = LH.Muted) }
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(if (recording) LH.Danger else LH.Saffron)
                        .alpha(if (recording) blink else 1f),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onRecord, modifier = Modifier.size(52.dp)) {
                        Icon(
                            if (recording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                            contentDescription = stringResource(if (recording) R.string.stop_recording else R.string.record),
                            tint = LH.OnSaffron,
                        )
                    }
                }
            }
            Text(stringResource(R.string.try_saying, example), style = MaterialTheme.typography.bodyMedium, color = LH.Oat, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/** Records up to [MAX_CLIP_MS] of AAC audio to a file. */
private class ClipRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    /** Starts recording; [onAutoStop] runs if the clip reaches its maximum length. */
    fun start(file: File, onAutoStop: () -> Unit): Boolean {
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else @Suppress("DEPRECATION") MediaRecorder()
        return runCatching {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96_000)
            r.setAudioSamplingRate(44_100)
            r.setMaxDuration(MAX_CLIP_MS)
            r.setOutputFile(file.path)
            r.setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stop()
                    onAutoStop()
                }
            }
            r.prepare()
            r.start()
            recorder = r
        }.onFailure {
            r.release()
            file.delete()
        }.isSuccess
    }

    fun stop() {
        recorder?.run {
            runCatching { stop() }
            release()
        }
        recorder = null
    }
}

private class ClipPlayer {
    private var player: MediaPlayer? = null

    fun play(file: File, onDone: () -> Unit) {
        stop()
        player = runCatching {
            MediaPlayer().apply {
                setDataSource(file.path)
                setOnCompletionListener { onDone() }
                prepare()
                start()
            }
        }.getOrNull()
        if (player == null) onDone()
    }

    fun stop() {
        player?.run { runCatching { stop() }; release() }
        player = null
    }
}
