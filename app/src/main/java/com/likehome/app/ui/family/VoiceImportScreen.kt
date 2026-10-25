package com.likehome.app.ui.family

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.alarm.Voices
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.moodRes
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.styles.CharacterPickerRow
import com.likehome.app.ui.theme.LH
import java.io.File

/** Longest clip kept: a voice note that runs on would drown the rest of the alarm. */
private const val MAX_SECONDS = 60

/**
 * A voice note shared from WhatsApp (or any app): pick who it is and which drama level,
 * and it plays when that level rings, like a clip recorded in the app.
 */
@Composable
fun VoiceImportScreen(file: File, onDone: () -> Unit) {
    val context = LocalContext.current
    var character by rememberSaveable { mutableStateOf(SettingsRepository.get(context).current.character) }
    var level by rememberSaveable { mutableStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    val seconds = remember(file) {
        runCatching {
            MediaMetadataRetriever().run {
                setDataSource(file.path)
                val ms = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                release()
                ms?.let { (it / 1000).toInt() }
            }
        }.getOrNull()
    }
    val player = remember { MediaPlayer() }
    DisposableEffect(Unit) { onDispose { runCatching { player.release() } } }

    PageScaffold(title = stringResource(R.string.voice_import_title), onBack = onDone) {
        Text(stringResource(R.string.voice_import_intro), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        if (seconds != null && seconds > MAX_SECONDS) {
            Text(stringResource(R.string.voice_import_long, MAX_SECONDS), style = MaterialTheme.typography.bodyMedium, color = LH.Danger)
        }
        SecondaryButton(stringResource(if (playing) R.string.stop else R.string.play), onClick = {
            if (playing) {
                runCatching { player.stop() }
                playing = false
            } else {
                runCatching {
                    player.reset()
                    player.setDataSource(file.path)
                    player.setOnCompletionListener { playing = false }
                    player.prepare()
                    player.start()
                    playing = true
                }
            }
        })

        SectionLabel(stringResource(R.string.whose_voice), Modifier.padding(top = 12.dp))
        CharacterPickerRow(character, onPick = { character = it })

        SectionLabel(stringResource(R.string.voice_import_level), Modifier.padding(top = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..MomLines.MAX_LEVEL).forEach { l ->
                ChoicePill(stringResource(moodRes(l)), selected = l == level, onClick = { level = l }, modifier = Modifier.weight(1f))
            }
        }

        val tooLong = seconds != null && seconds > MAX_SECONDS
        PrimaryButton(stringResource(R.string.voice_import_save), enabled = !tooLong, onClick = {
            val saved = runCatching { file.copyTo(Voices.recording(context, character, level), overwrite = true) }.isSuccess
            val mood = context.getString(moodRes(level))
            Toast.makeText(
                context,
                if (saved) context.getString(R.string.voice_import_saved, context.getString(character.nameRes), mood)
                else context.getString(R.string.photo_failed),
                Toast.LENGTH_LONG,
            ).show()
            if (saved) onDone()
        })
    }
}

/** Where an incoming shared voice note waits until it's saved or dropped. */
fun incomingVoiceFile(context: android.content.Context): File =
    File(context.cacheDir, "incoming").apply { mkdirs() }.let { File(it, "voice-note") }
