package com.likehome.app.ui.premium

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.data.SettingsRepository
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.common.Tag
import com.likehome.app.ui.theme.LH
import java.util.Locale

private val Perks = listOf(
    "Every style: Breaking news, Retro game, Filmy drama and more",
    "Every character: Papa, Dadi, the Boss",
    "Record your own Mom's voice",
    "All wake-up tasks, including photo and voice checks",
    "Fresh lines every week",
    "No ads, ever",
)

/** What Premium will include. Billing arrives in v2; for now users can join the waitlist. */
@Composable
fun PremiumScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context) }
    val settings by repo.settings.collectAsState()
    val india = remember { Locale.getDefault().country == "IN" }

    PageScaffold(title = null, onBack = onBack) {
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Halo()
            MoodFace(0, Modifier.size(110.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
            Tag("Coming soon")
        }
        Text(
            "Like Home Premium",
            style = MaterialTheme.typography.headlineLarge,
            color = LH.Cream,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "The whole family, every style, and Mom in her own voice.",
            style = MaterialTheme.typography.bodyLarge,
            color = LH.Oat,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Card {
            Perks.forEach { perk ->
                Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = LH.Saffron, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(perk, style = MaterialTheme.typography.bodyLarge, color = LH.Cream)
                }
            }
        }
        Text(
            if (india) "Plans from ₹49 a month, with a yearly plan." else "Plans from \$1.99 a month.",
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (settings.premiumWaitlist) {
            SecondaryButton("You're on the list. Mom will tell you.", onClick = {
                repo.update { it.copy(premiumWaitlist = false) }
            })
        } else {
            PrimaryButton("Tell me when it launches", onClick = {
                repo.update { it.copy(premiumWaitlist = true) }
            })
        }
        Text(
            "Everything you use today stays free.",
            style = MaterialTheme.typography.bodySmall,
            color = LH.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** A slow, breathing warm glow behind Mom. */
@Composable
private fun Halo() {
    val t = rememberInfiniteTransition(label = "halo")
    val pulse by t.animateFloat(0.85f, 1f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "halo-pulse")
    Canvas(Modifier.size(200.dp)) {
        drawCircle(
            Brush.radialGradient(listOf(LH.Saffron.copy(alpha = 0.35f * pulse), LH.Saffron.copy(alpha = 0f))),
            radius = size.minDimension / 2 * pulse,
        )
    }
}
