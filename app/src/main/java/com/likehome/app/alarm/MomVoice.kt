package com.likehome.app.alarm

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Reads Mom's lines aloud with the phone's own text-to-speech, on the alarm stream.
 * A stand-in until recorded voice packs ship; if no voice is installed it stays quiet
 * and the ringtone carries on. [onSpeaking] runs on the main thread so the caller can
 * lower the ringtone while she talks.
 */
class MomVoice(
    context: Context,
    private val locale: Locale = Locale.forLanguageTag("en-IN"),
    /** Starting pitch: Papa and the Boss speak lower, Dadi a little higher. */
    private val basePitch: Float = 1.1f,
    private val onSpeaking: (Boolean) -> Unit,
) {

    private val handler = Handler(Looper.getMainLooper())
    private var ready = false
    private var pending: Pair<String, Int>? = null
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status -> handler.post { onInit(status) } }
    }

    /** Says [line]; higher drama [level]s are faster and sharper. */
    fun say(line: String, level: Int) {
        if (ready) speak(line, level) else pending = line to level
    }

    /** Stops talking now, e.g. while the user speaks for the voice check. */
    fun quiet() {
        pending = null
        tts?.stop()
    }

    fun shutdown() {
        handler.removeCallbacksAndMessages(null)
        ready = false
        tts?.run { stop(); shutdown() }
        tts = null
    }

    private fun onInit(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) return
        if (engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
            engine.language = locale
        } else {
            val india = Locale.forLanguageTag("en-IN")
            if (engine.isLanguageAvailable(india) >= TextToSpeech.LANG_AVAILABLE) engine.language = india
        }
        engine.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { handler.post { onSpeaking(true) } }
            override fun onDone(utteranceId: String?) { handler.post { onSpeaking(false) } }
            @Deprecated("Required override")
            override fun onError(utteranceId: String?) { handler.post { onSpeaking(false) } }
        })
        ready = true
        pending?.let { (line, level) -> speak(line, level) }
        pending = null
    }

    private fun speak(line: String, level: Int) {
        val engine = tts ?: return
        engine.setPitch(basePitch + level * 0.05f)
        engine.setSpeechRate(0.95f + level * 0.08f)
        engine.speak(line, TextToSpeech.QUEUE_FLUSH, null, "mom-$level")
    }
}
