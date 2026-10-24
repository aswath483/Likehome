package com.likehome.app.ui.ring

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.Promise
import com.likehome.app.core.VoiceCheck
import com.likehome.app.mom.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Photo check: go to the spot chosen when the alarm was set (sink, toothbrush, tiffin)
 * and photograph it again. Without a reference photo it falls back to sums.
 */
@Composable
internal fun PhotoTask(photoRef: String, colors: TaskColors, onSolved: () -> Unit, onFallback: () -> Unit) {
    val context = LocalContext.current
    val reference = remember(photoRef) { photoRef.takeIf { it.isNotBlank() }?.let { PhotoStore.file(context, it) }?.takeIf { it.exists() } }
    if (reference == null) {
        LaunchedEffect(Unit) { onFallback() }
        return
    }
    val thumb = remember(photoRef) { PhotoStore.thumbnail(context, photoRef, 320) }
    var shot by remember { mutableStateOf<File?>(null) }
    var checking by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val file = shot
        if (ok && file != null) {
            checking = true
            scope.launch {
                val same = withContext(Dispatchers.Default) { PhotoStore.matches(reference, file) }
                file.delete()
                checking = false
                if (same) onSolved() else failed = true
            }
        }
    }

    fun openCamera() {
        val file = PhotoStore.newFile(context, "morning")
        shot = file
        failed = false
        val launch = { runCatching { camera.launch(PhotoStore.uri(context, file)) }.onFailure { onFallback() } }
        // The camera app can't open over the lock screen, so unlock first. Unlocking is proof too.
        val activity = context as? Activity
        val keyguard = context.getSystemService(KeyguardManager::class.java)
        if (activity != null && keyguard.isKeyguardLocked) {
            keyguard.requestDismissKeyguard(activity, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() { launch() }
            })
        } else {
            launch()
        }
    }

    Text("Show me you're up", style = MaterialTheme.typography.labelMedium, color = colors.subtext)
    Spacer(Modifier.height(12.dp))
    if (thumb != null) {
        Image(
            thumb.asImageBitmap(),
            contentDescription = "The spot you chose",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(18.dp)),
        )
        Spacer(Modifier.height(12.dp))
    }
    Text(
        when {
            checking -> "Checking your photo…"
            failed -> "That doesn't look like the same spot. Stand where you took this and try again."
            else -> "Go to this spot and take the same photo."
        },
        style = MaterialTheme.typography.bodyLarge,
        color = if (failed) colors.error else colors.text,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(12.dp))
    TaskButton(if (failed) "Try again" else "Open camera", colors, enabled = !checking, onClick = ::openCamera)
    TextButton(onClick = onFallback) { Text("Camera not working? Solve sums instead", color = colors.subtext) }
}

/**
 * Voice check: say "I'm awake!" out loud. The ringtone hushes while listening so the
 * microphone can hear. No recogniser or no microphone permission falls back to sums.
 */
@Composable
internal fun VoiceTask(name: String, language: Language, colors: TaskColors, onSolved: () -> Unit, onFallback: () -> Unit) {
    val context = LocalContext.current
    val usable = remember {
        SpeechRecognizer.isRecognitionAvailable(context) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
    if (!usable) {
        LaunchedEffect(Unit) { onFallback() }
        return
    }
    val phrase = remember(name, language) { VoiceCheck.phrase(name, language) }
    var listening by remember { mutableStateOf(false) }
    var heard by remember { mutableStateOf<String?>(null) }
    var problem by remember { mutableStateOf<String?>(null) }
    val recognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    DisposableEffect(recognizer) {
        AlarmService.hush(context, true)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                listening = false
                val guesses = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                if (VoiceCheck.matches(guesses)) onSolved()
                else {
                    heard = guesses.firstOrNull()
                    problem = "Louder, with feeling. Try again."
                }
            }
            override fun onError(error: Int) {
                listening = false
                problem = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
                    "Didn't catch that. Tap and say it again." else "The microphone had trouble. Try again."
            }
            override fun onReadyForSpeech(params: Bundle?) { problem = null }
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose {
            recognizer.destroy()
            AlarmService.hush(context, false)
        }
    }

    fun listen() {
        heard = null
        listening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == Language.HINDI) "hi-IN" else "en-IN")
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        runCatching { recognizer.startListening(intent) }.onFailure { onFallback() }
    }

    val pulse by rememberInfiniteTransition(label = "mic").animateFloat(
        1f, 1.15f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "mic-pulse",
    )
    Text("Say it out loud", style = MaterialTheme.typography.labelMedium, color = colors.subtext)
    Spacer(Modifier.height(10.dp))
    Text("“$phrase”", style = MaterialTheme.typography.headlineSmall, color = colors.text, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    Box(
        Modifier.size(88.dp).scale(if (listening) pulse else 1f).clip(CircleShape).background(colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = ::listen, enabled = !listening, modifier = Modifier.size(88.dp)) {
            Icon(Icons.Outlined.Mic, contentDescription = "Speak", tint = colors.onAccent, modifier = Modifier.size(40.dp))
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(
        when {
            listening -> "Listening…"
            problem != null -> problem!!
            else -> "Tap the mic and say it."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = if (problem != null && !listening) colors.error else colors.subtext,
        textAlign = TextAlign.Center,
    )
    heard?.let { Text("Heard: \"$it\"", style = MaterialTheme.typography.bodySmall, color = colors.subtext) }
    TextButton(onClick = onFallback) { Text("Can't speak now? Solve sums instead", color = colors.subtext) }
}

/** Type today's promise word for word. Case and punctuation don't matter; spelling does. */
@Composable
internal fun PromiseTask(seed: Long, language: Language, colors: TaskColors, onSolved: () -> Unit) {
    val sentence = remember(seed, language) { Promise.sentence(seed, language) }
    var typed by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    val submit: () -> Unit = { if (Promise.matches(typed, sentence)) onSolved() else wrong = true }

    Text("Type your promise", style = MaterialTheme.typography.labelMedium, color = colors.subtext)
    Spacer(Modifier.height(10.dp))
    Text("“$sentence”", style = MaterialTheme.typography.titleLarge, color = colors.text, textAlign = TextAlign.Center)
    Spacer(Modifier.height(14.dp))
    OutlinedTextField(
        value = typed,
        onValueChange = { typed = it.take(120); wrong = false },
        placeholder = { Text("Type it here") },
        isError = wrong,
        supportingText = if (wrong) {
            { Text("Not quite. Word for word, please.", color = colors.error) }
        } else null,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, autoCorrectEnabled = false, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { submit() }),
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
    TaskButton("I promise", colors, enabled = typed.isNotBlank(), onClick = submit)
}
