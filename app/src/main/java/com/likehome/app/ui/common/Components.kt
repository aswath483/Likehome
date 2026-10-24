package com.likehome.app.ui.common

import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.theme.LH

/** Widest a column of content gets, so tablets and landscape don't stretch it. */
val MaxContentWidth: Dp = 600.dp

/** Follows the phone's 12/24-hour setting. */
@Composable
fun rememberIs24Hour(): Boolean {
    val context = LocalContext.current
    return remember(context) { DateFormat.is24HourFormat(context) }
}

/** Shrinks a pressed element a touch, like a real button giving way. */
fun Modifier.pressScale(source: InteractionSource, pressedScale: Float = 0.97f): Modifier = composed {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, spring(stiffness = 600f), label = "press")
    graphicsLayer { scaleX = scale; scaleY = scale }
}

/** A light tick under the finger; wraps [onClick]. */
@Composable
fun withHaptic(onClick: () -> Unit): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() }
}

/**
 * A full page: fills the window, pads for status bar, navigation bar, display cutout and
 * keyboard, scrolls, and centres content at [MaxContentWidth] on wide screens.
 */
@Composable
fun PageScaffold(
    title: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(LH.Espresso)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = LH.Cream)
                }
            }
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = LH.Cream,
                    modifier = Modifier.padding(top = if (onBack == null) 24.dp else 0.dp, bottom = 4.dp),
                )
            }
            content()
        }
    }
}

/** Small caps label above a group, e.g. "REPEAT". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = LH.Muted,
        modifier = modifier,
    )
}

/** A selectable card with a title and one line of detail; saffron border when chosen. */
@Composable
fun ChoiceCard(
    title: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = withHaptic(onClick),
        interactionSource = source,
        color = if (selected) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LH.Saffron else LH.Line),
        modifier = modifier.fillMaxWidth().pressScale(source),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = LH.Cream, modifier = Modifier.weight(1f, fill = false))
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Tag(badge)
                }
            }
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        }
    }
}

/** A tiny rounded label: "Premium", "Coming soon", "New". */
@Composable
fun Tag(text: String, color: Color = LH.Saffron) {
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
        )
    }
}

/** One option in a row of pills, e.g. the wake-up task picker. */
@Composable
fun ChoicePill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = withHaptic(onClick),
        interactionSource = source,
        shape = RoundedCornerShape(50),
        color = if (selected) LH.Saffron else LH.Crema,
        contentColor = if (selected) LH.OnSaffron else LH.Oat,
        border = if (selected) null else BorderStroke(1.dp, LH.Line),
        modifier = modifier.heightIn(min = 44.dp).pressScale(source, 0.94f),
    ) {
        Box(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** A setting that is on or off, with one line explaining it. */
@Composable
fun SwitchRow(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        onClick = withHaptic { onChange(!checked) },
        color = LH.Roast,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LH.OnSaffron,
                    checkedTrackColor = LH.Saffron,
                    uncheckedThumbColor = LH.Muted,
                    uncheckedTrackColor = LH.Crema,
                    uncheckedBorderColor = LH.Line,
                ),
            )
        }
    }
}

/** A row that opens another screen: icon, title, detail, chevron. */
@Composable
fun NavRow(icon: ImageVector, title: String, detail: String?, onClick: () -> Unit, trailing: String? = null) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = withHaptic(onClick),
        interactionSource = source,
        color = LH.Roast,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = Modifier.fillMaxWidth().pressScale(source),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.background(LH.Crema, RoundedCornerShape(12.dp)).padding(8.dp),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, contentDescription = null, tint = LH.Saffron) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                if (detail != null) Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                Text(trailing, style = MaterialTheme.typography.bodyMedium, color = LH.Muted, maxLines = 1)
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = LH.Muted)
        }
    }
}

/** Rounded card used to group content. */
@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = LH.Roast,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(22.dp), content = content)
    }
}

/** The main saffron call to action. Grows with the system font size instead of clipping. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = withHaptic(onClick),
        enabled = enabled,
        interactionSource = source,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = LH.Saffron, contentColor = LH.OnSaffron,
            disabledContainerColor = LH.Crema, disabledContentColor = LH.Muted,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 58.dp).pressScale(source),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** The quiet outlined alternative to [PrimaryButton]. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = withHaptic(onClick),
        interactionSource = source,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, LH.Line),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 54.dp).pressScale(source),
    ) {
        Text(text, color = LH.Oat, style = MaterialTheme.typography.labelLarge)
    }
}
