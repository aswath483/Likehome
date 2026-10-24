package com.likehome.app.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.Availability
import com.likehome.app.core.Catalog
import com.likehome.app.core.CharacterEntry
import com.likehome.app.core.RingStyle
import com.likehome.app.core.StyleEntry
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.Tag
import com.likehome.app.ui.common.withHaptic
import com.likehome.app.ui.theme.Bangers
import com.likehome.app.ui.theme.LH

/** Every style and character, with what's ready, what's premium and what's coming. */
@Composable
fun StylesScreen(onBack: () -> Unit, onOpenPremium: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context) }
    val settings by repo.settings.collectAsState()

    PageScaffold(title = "Styles & characters", onBack = onBack) {
        Text(
            "Same Mom, same lines. Only how she shows up changes.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        SectionLabel("Styles", Modifier.padding(top = 12.dp))
        Catalog.styles.forEach { entry ->
            StyleCard(
                entry = entry,
                isDefault = entry.style != null && entry.style == settings.style,
                onUse = { style -> repo.update { it.copy(style = style) } },
                onPreview = { style -> AlarmService.preview(context, style) },
                onLocked = onOpenPremium,
            )
        }
        SectionLabel("Characters", Modifier.padding(top = 16.dp))
        Catalog.characters.forEach { CharacterCard(it, onLocked = onOpenPremium) }
    }
}

@Composable
private fun StyleCard(
    entry: StyleEntry,
    isDefault: Boolean,
    onUse: (RingStyle) -> Unit,
    onPreview: (RingStyle) -> Unit,
    onLocked: () -> Unit,
) {
    val style = entry.style
    val locked = entry.availability != Availability.AVAILABLE
    Surface(
        onClick = withHaptic { if (style != null) onUse(style) else onLocked() },
        color = if (isDefault) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(if (isDefault) 2.dp else 1.dp, if (isDefault) LH.Saffron else LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(width = 96.dp, height = 76.dp).clip(RoundedCornerShape(14.dp))
                        .alpha(if (locked) 0.55f else 1f)
                ) { StyleThumb(entry, style) }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium, color = LH.Cream, modifier = Modifier.weight(1f, fill = false))
                        Spacer(Modifier.width(8.dp))
                        if (isDefault) {
                            Tag("Default")
                        } else {
                            entry.availability.label?.let { Tag(it, if (entry.availability == Availability.PREMIUM) LH.Saffron else LH.Muted) }
                        }
                    }
                    Text(entry.detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                }
                if (locked) Icon(Icons.Outlined.Lock, contentDescription = "Locked", tint = LH.Muted, modifier = Modifier.padding(start = 8.dp))
            }
            if (style != null) {
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = withHaptic { onPreview(style) }) { Text("Preview", color = LH.Saffron) }
                    if (!isDefault) TextButton(onClick = withHaptic { onUse(style) }) { Text("Make default", color = LH.Oat) }
                }
            }
        }
    }
}

/** A tiny picture of each style, so the gallery reads at a glance. */
@Composable
private fun StyleThumb(entry: StyleEntry, style: RingStyle?) {
    when (style) {
        RingStyle.CHAT -> ChatThumb(Modifier.fillMaxSize())
        RingStyle.COMIC -> ComicThumb(Modifier.fillMaxSize())
        RingStyle.SMART_MIX -> Row(Modifier.fillMaxSize()) {
            ChatThumb(Modifier.weight(1f).fillMaxHeight())
            ComicThumb(Modifier.weight(1f).fillMaxHeight())
        }
        null -> Box(Modifier.fillMaxSize().background(LH.Crema), contentAlignment = Alignment.Center) {
            Text(entry.title.take(1), style = MaterialTheme.typography.headlineMedium, color = LH.Oat)
        }
    }
}

@Composable
fun ChatThumb(modifier: Modifier) {
    Column(modifier.background(LH.Espresso).padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf(0.9f, 0.7f, 0.8f).forEachIndexed { i, w ->
            Box(
                Modifier.fillMaxWidth(w).height(13.dp).clip(RoundedCornerShape(7.dp))
                    .background(if (i == 2) LH.MyBubble else LH.Bubble)
            )
        }
    }
}

@Composable
fun ComicThumb(modifier: Modifier) {
    Box(modifier.background(Color(0xFFFFD23F)).padding(8.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White)
                .border(2.dp, Color(0xFF111111), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text("UTH JAO!", style = TextStyle(fontFamily = Bangers, fontSize = 15.sp), color = Color(0xFF111111))
        }
    }
}

@Composable
private fun CharacterCard(entry: CharacterEntry, onLocked: () -> Unit) {
    val locked = entry.availability != Availability.AVAILABLE
    Surface(
        onClick = withHaptic { if (locked) onLocked() },
        color = LH.Roast,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (locked) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(LH.Crema), contentAlignment = Alignment.Center) {
                    Text(entry.name.take(1), style = MaterialTheme.typography.titleLarge, color = LH.Muted)
                }
            } else {
                MoodFace(0, Modifier.size(48.dp))
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.name, style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                    entry.availability.label?.let {
                        Spacer(Modifier.width(8.dp))
                        Tag(it, if (entry.availability == Availability.PREMIUM) LH.Saffron else LH.Muted)
                    }
                }
                Text(entry.detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }
    }
}
