package com.likehome.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.likehome.app.alarm.AlarmService
import com.likehome.app.core.RingStyle
import com.likehome.app.data.SettingsRepository
import com.likehome.app.mom.Language
import com.likehome.app.ui.common.ChoiceCard
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.theme.LH

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context) }
    val settings by repo.settings.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(LH.Espresso)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = LH.Cream)
            }
            Text("Settings", style = MaterialTheme.typography.headlineSmall, color = LH.Cream)
        }

        SectionLabel("Mom speaks", Modifier.padding(top = 12.dp))
        Language.entries.forEach { lang ->
            ChoiceCard(
                title = lang.label,
                detail = "\"${lang.sample}\"",
                selected = settings.language == lang,
                onClick = { repo.update { it.copy(language = lang) } },
            )
        }

        SectionLabel("Default style", Modifier.padding(top = 12.dp))
        RingStyle.entries.forEach { style ->
            ChoiceCard(
                title = style.title,
                detail = style.detail,
                selected = settings.style == style,
                onClick = { repo.update { it.copy(style = style) } },
            )
        }

        SectionLabel("Try it", Modifier.padding(top = 12.dp))
        Text(
            "Ring a test alarm now to hear and see Mom. Snoozing a test just stops it.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        PrimaryButton("Preview Mom now", onClick = { AlarmService.preview(context) })
        Spacer(Modifier.height(24.dp).fillMaxWidth())
    }
}
