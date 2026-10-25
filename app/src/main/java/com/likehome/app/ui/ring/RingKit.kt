package com.likehome.app.ui.ring

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.likehome.app.core.Character
import com.likehome.app.core.WakeTask
import com.likehome.app.mom.Language
import com.likehome.app.mom.MomLines
import com.likehome.app.mom.RamadanLines
import com.likehome.app.ui.common.MaxContentWidth
import com.likehome.app.ui.common.pressScale
import com.likehome.app.ui.common.withHaptic
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

/** Everything a ringing screen needs to know, whatever its style. */
data class RingProps(
    val level: Int,
    val seed: Long,
    val label: String,
    val language: Language,
    val task: WakeTask,
    val streak: Int,
    val snoozeMinutes: Int,
    val character: Character,
    /** The character's name and the task's title in the app's language. */
    val name: String,
    val taskTitle: String,
    /** Reference photo file name for the photo check; blank when none was taken. */
    val photoRef: String = "",
    /** The saved barcode for the barcode check; blank when none was scanned. */
    val barcode: String = "",
    /** The Ramadan suhoor ring: Mom's first line is about suhoor. */
    val suhoor: Boolean = false,
    /** Mom's first line on a day the user told her about (an exam, a birthday), if any. */
    val special: String? = null,
) {
    /** The newest line: what the character says right now. */
    val line: String get() = conversation.last()

    /** One line per level reached, oldest first. */
    val conversation: List<String> get() = MomLines.conversation(level, seed, language, LocalDate.now(), character)
        .let { lines ->
            when {
                suhoor -> listOf(RamadanLines.suhoorLine(seed, language)) + lines.drop(1)
                special != null -> listOf(special) + lines.drop(1)
                else -> lines
            }
        }

    val canSnooze: Boolean get() = level < MomLines.MAX_LEVEL
    val lastChance: Boolean get() = level == MomLines.MAX_LEVEL - 1
    /** "WALK 30 STEPS" and friends, for call-to-action buttons. */
    val taskCall: String get() = taskTitle.uppercase()
}

/**
 * The page every ringing style sits on: full-bleed [background] behind the system bars,
 * content inside the safe area (cutouts, gesture bar, keyboard), capped at a readable
 * width, scrollable when the screen is short. [lightBackground] flips bar icons dark.
 */
@Composable
fun RingPage(
    background: Brush,
    lightBackground: Boolean,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    SystemBarIcons(dark = lightBackground)
    Box(
        Modifier
            .fillMaxSize()
            .background(background)
            // After the background, so a caller's drawBehind decoration paints on top of it.
            .then(modifier)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

/** Dark status and navigation bar icons on light pages, light icons on dark ones. */
@Composable
fun SystemBarIcons(dark: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, dark) {
        val window = (view.context as? Activity)?.window
        val bars = window?.let { WindowCompat.getInsetsController(it, view) }
        bars?.isAppearanceLightStatusBars = dark
        bars?.isAppearanceLightNavigationBars = dark
        onDispose {
            bars?.isAppearanceLightStatusBars = false
            bars?.isAppearanceLightNavigationBars = false
        }
    }
}

/** The current time, ticking every second. */
@Composable
fun rememberClock(): State<LocalTime> {
    val now = remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now.value = LocalTime.now()
        }
    }
    return now
}

/** Colours for a style's two buttons. */
data class ActionColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val note: Color,
)

/**
 * "I'm up" and "Snooze" in a style's colours. The snooze button disappears at full drama
 * and [noSnoozeText] takes its place.
 */
@Composable
fun StyleActions(
    p: RingProps,
    upText: String,
    snoozeText: String,
    noSnoozeText: String,
    colors: ActionColors,
    onUp: () -> Unit,
    onSnooze: () -> Unit,
    shape: Shape = RoundedCornerShape(50),
    textStyle: TextStyle? = null,
) {
    val upSource = remember { MutableInteractionSource() }
    val label = textStyle ?: MaterialTheme.typography.labelLarge
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = withHaptic(onUp),
            interactionSource = upSource,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).pressScale(upSource),
        ) {
            Text(upText, style = label, textAlign = TextAlign.Center)
        }
        if (p.canSnooze) {
            OutlinedButton(
                onClick = withHaptic(onSnooze),
                shape = shape,
                border = BorderStroke(1.5.dp, colors.secondary),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) {
                Text(
                    snoozeText,
                    color = colors.secondary,
                    style = if (textStyle != null) textStyle.copy(fontSize = textStyle.fontSize * 0.75f) else label,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Text(
                noSnoozeText,
                color = colors.note,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            )
        }
    }
}
