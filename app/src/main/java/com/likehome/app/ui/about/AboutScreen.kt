package com.likehome.app.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.NavRow
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.theme.LH

private val Licences = listOf(
    "DM Serif Display" to "© Adobe; SIL Open Font License 1.1",
    "Hind" to "© Indian Type Foundry; SIL Open Font License 1.1",
    "Bangers" to "© The Bangers Project Authors; SIL Open Font License 1.1",
)

@Composable
fun AboutScreen(onBack: () -> Unit, onReplayIntro: () -> Unit) {
    val context = LocalContext.current
    val version = remember { versionName(context) }

    PageScaffold(title = null, onBack = onBack) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            MoodFace(0, Modifier.size(84.dp))
            Spacer(Modifier.height(14.dp))
            Text("Like Home", style = MaterialTheme.typography.headlineLarge, color = LH.Cream)
            Text("Version $version", style = MaterialTheme.typography.bodyMedium, color = LH.Muted)
            Spacer(Modifier.height(6.dp))
            Text("Wake up like Mom is still calling you.", style = MaterialTheme.typography.bodyLarge, color = LH.Oat)
        }

        SectionLabel("Your privacy", Modifier.padding(top = 16.dp))
        Card {
            Text(
                "Your alarms, mornings and settings stay on this phone. There is no account, no ads " +
                    "and no tracking in this version. The step sensor is only read while Mom is ringing.",
                style = MaterialTheme.typography.bodyMedium,
                color = LH.Oat,
            )
        }

        SectionLabel("More", Modifier.padding(top = 16.dp))
        NavRow(Icons.Outlined.StarOutline, "Rate Like Home", "Mom reads every review", onClick = { openStore(context) })
        NavRow(Icons.Outlined.Replay, "Show the intro again", "Pick a style and language from the start", onClick = onReplayIntro)

        SectionLabel("Fonts", Modifier.padding(top = 16.dp))
        Card {
            Licences.forEach { (name, licence) ->
                Text(name, style = MaterialTheme.typography.titleSmall, color = LH.Cream)
                Text(licence, style = MaterialTheme.typography.bodySmall, color = LH.Muted, modifier = Modifier.padding(bottom = 10.dp))
            }
        }
    }
}

private fun versionName(context: Context): String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
}.getOrNull() ?: "–"

/** The Play Store app if present, else the Play website. */
private fun openStore(context: Context) {
    val id = context.packageName
    val app = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (runCatching { context.startActivity(app) }.isFailure) runCatching { context.startActivity(web) }
}
