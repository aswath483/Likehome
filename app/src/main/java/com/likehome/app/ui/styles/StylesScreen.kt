package com.likehome.app.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.Appearance
import com.likehome.app.core.Catalog
import com.likehome.app.core.Character
import com.likehome.app.core.RingStyle
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.common.CharacterFace
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import com.likehome.app.ui.common.NavRow
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.Tag
import com.likehome.app.ui.common.withHaptic
import com.likehome.app.ui.theme.LH

/** Every style and every character. Tap to make one the default; preview rings it now. */
@Composable
fun StylesScreen(onBack: () -> Unit, onOpenFaces: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context) }
    val settings by repo.settings.collectAsState()

    PageScaffold(title = "Styles & characters", onBack = onBack) {
        Text(
            "Pick who wakes you and how they show up. Each alarm can also have its own.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        SectionLabel("Who wakes you", Modifier.padding(top = 12.dp))
        NavRow(Icons.Outlined.Face, "Use your own photos", "Put Mom's real face on any character", onClick = onOpenFaces)
        Catalog.characters.forEach { entry ->
            val c = entry.character
            GalleryCard(
                selected = settings.character == c,
                onSelect = { repo.update { it.copy(character = c) } },
                onPreview = { AlarmService.preview(context, character = c) },
                picture = { CharacterFace(c, 0, Modifier.size(56.dp)) },
                title = entry.name,
                detail = entry.detail,
                extra = if (c.defaultAppearance == null) null else {
                    {
                        AppearancePickerRow(c, settings.appearanceOf(c)) { picked ->
                            repo.update {
                                val rest = it.appearances - c
                                it.copy(appearances = if (picked == c.defaultAppearance) rest else rest + (c to picked))
                            }
                        }
                    }
                },
            )
        }
        SectionLabel("Styles", Modifier.padding(top = 16.dp))
        Catalog.styles.forEach { entry ->
            GalleryCard(
                selected = settings.style == entry.style,
                onSelect = { repo.update { it.copy(style = entry.style) } },
                onPreview = { AlarmService.preview(context, style = entry.style) },
                picture = { StyleThumb(entry.style, Modifier.size(width = 88.dp, height = 70.dp)) },
                title = entry.title,
                detail = entry.detail,
            )
        }
    }
}

@Composable
private fun GalleryCard(
    selected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    picture: @Composable () -> Unit,
    title: String,
    detail: String,
    extra: (@Composable () -> Unit)? = null,
) {
    Surface(
        onClick = withHaptic(onSelect),
        color = if (selected) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LH.Saffron else LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                picture()
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = LH.Cream, modifier = Modifier.weight(1f, fill = false))
                        if (selected) {
                            Spacer(Modifier.width(8.dp))
                            Tag("Default")
                        }
                    }
                    Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                }
            }
            if (extra != null) {
                Spacer(Modifier.height(12.dp))
                extra()
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = withHaptic(onPreview)) { Text("Preview", color = LH.Saffron) }
                if (!selected) TextButton(onClick = withHaptic(onSelect)) { Text("Make default", color = LH.Oat) }
            }
        }
    }
}

/** A row of style thumbnails to pick from, for the alarm editor and onboarding. */
@Composable
fun StylePickerRow(selected: RingStyle, onPick: (RingStyle) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(RingStyle.entries.size) { i ->
            val style = RingStyle.entries[i]
            val on = style == selected
            Surface(
                onClick = withHaptic { onPick(style) },
                color = if (on) LH.MyBubble else LH.Crema,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(if (on) 2.dp else 1.dp, if (on) LH.Saffron else LH.Line),
            ) {
                Column(Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    StyleThumb(style, Modifier.size(width = 76.dp, height = 58.dp))
                    Text(style.short, style = MaterialTheme.typography.labelLarge, color = if (on) LH.Saffron else LH.Oat, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

/** A row of faces to pick who wakes the user. */
@Composable
fun CharacterPickerRow(selected: Character, onPick: (Character) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(Character.entries.size) { i ->
            val c = Character.entries[i]
            val on = c == selected
            Surface(
                onClick = withHaptic { onPick(c) },
                color = if (on) LH.MyBubble else LH.Crema,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(if (on) 2.dp else 1.dp, if (on) LH.Saffron else LH.Line),
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CharacterFace(c, 0, Modifier.size(44.dp))
                    Text(c.displayName, style = MaterialTheme.typography.labelLarge, color = if (on) LH.Saffron else LH.Oat, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

/** The looks a family character comes in, so everyone can pick a family like their own. */
@Composable
fun AppearancePickerRow(character: Character, selected: Appearance?, onPick: (Appearance) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(Appearance.entries.size) { i ->
            val a = Appearance.entries[i]
            val on = a == selected
            Surface(
                onClick = withHaptic { onPick(a) },
                color = if (on) LH.MyBubble else LH.Crema,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(if (on) 2.dp else 1.dp, if (on) LH.Saffron else LH.Line),
            ) {
                Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CharacterFace(character, 0, Modifier.size(48.dp), appearance = a)
                    Text(a.label, style = MaterialTheme.typography.labelMedium, color = if (on) LH.Saffron else LH.Oat, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
