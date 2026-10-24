package com.likehome.app.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.likehome.app.core.Character
import com.likehome.app.mom.Language
import java.io.File

/** Something that can say a character's line while the alarm rings. */
interface Speaker {
    fun say(line: String, level: Int)
    fun quiet()
    fun shutdown()
}

/**
 * Where a character's voice comes from, best first:
 * 1. the user's own recording for that drama level (Settings › Record a voice),
 * 2. a bundled voice pack clip in `assets/voices/<character>/<language>/<level>/`,
 * 3. the phone's text-to-speech ([MomVoice]).
 */
object Voices {

    /** The user's recording for [character] at [level], whether or not it exists yet. */
    fun recording(context: Context, character: Character, level: Int): File =
        File(File(context.filesDir, "voice").apply { mkdirs() }, "${character.name.lowercase()}_$level.m4a")

    fun hasRecording(context: Context, character: Character, level: Int) = recording(context, character, level).length() > 0

    /** A random clip from an installed voice pack, as an asset path; null when there's none. */
    fun packClip(context: Context, character: Character, language: Language, level: Int, seed: Long): String? {
        val dir = "voices/${character.name.lowercase()}/${language.name.lowercase()}/$level"
        val clips = runCatching { context.assets.list(dir)?.toList() }.getOrNull().orEmpty()
            .filter { it.endsWith(".mp3") || it.endsWith(".ogg") || it.endsWith(".m4a") }
            .sorted()
        if (clips.isEmpty()) return null
        return "$dir/${clips[Math.floorMod(seed / 60_000, clips.size.toLong()).toInt()]}"
    }
}

/** Plays a recorded or bundled clip on the alarm stream instead of text-to-speech. */
class ClipVoice(
    private val context: Context,
    private val file: File? = null,
    private val asset: String? = null,
    private val onSpeaking: (Boolean) -> Unit,
) : Speaker {

    private var player: MediaPlayer? = null

    override fun say(line: String, level: Int) {
        quiet()
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                when {
                    file != null -> setDataSource(file.path)
                    asset != null -> context.assets.openFd(asset).use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
                }
                setOnCompletionListener { onSpeaking(false) }
                prepare()
                start()
            }
        }.getOrNull()
        if (player != null) onSpeaking(true)
    }

    override fun quiet() {
        player?.run { runCatching { stop() }; release() }
        player = null
        onSpeaking(false)
    }

    override fun shutdown() = quiet()
}
