package com.likehome.app.ui.ring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.delay

/** Shown once the wake-up task is done. */
@Composable
fun AwakeScreen(level: Int, onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(2_600); onDone() }
    val (title, body) = when (level) {
        0 -> "Good child!" to "No snoozes. Mom is proud. Breakfast is waiting."
        1 -> "Okay, okay." to "One snooze. Mom is mostly happy."
        2 -> "Finally." to "Two snoozes. Mom will mention this at dinner."
        else -> "Mom is disappointed." to "Three snoozes. Sharma ji's son would never."
    }
    Column(
        Modifier.fillMaxSize().background(LH.Espresso).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = LH.Cream)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge, color = LH.Oat, textAlign = TextAlign.Center)
    }
}
