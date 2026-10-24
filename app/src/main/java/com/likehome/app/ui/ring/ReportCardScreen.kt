package com.likehome.app.ui.ring

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.WeekDots
import com.likehome.app.ui.theme.LH
import java.time.format.DateTimeFormatter

/** The morning report card: shown after the wake-up task, with a one-tap share. */
@Composable
fun ReportCardScreen(report: ReportData, onShare: () -> Unit, onDone: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(LH.Espresso)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionLabel("Morning report")
        Text(
            report.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
            style = MaterialTheme.typography.bodyMedium,
            color = LH.Oat,
        )
        Spacer(Modifier.height(24.dp))
        MoodFace(report.snoozes.coerceAtMost(3), Modifier.size(120.dp))
        Spacer(Modifier.height(20.dp))
        Text(report.title, style = MaterialTheme.typography.headlineLarge, color = LH.Cream, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(report.body, style = MaterialTheme.typography.bodyLarge, color = LH.Oat, textAlign = TextAlign.Center)

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("${report.snoozes}", if (report.snoozes == 1) "snooze" else "snoozes", Modifier.weight(1f))
            Stat(report.wokeAt.format(DateTimeFormatter.ofPattern("h:mm")), "up at", Modifier.weight(1f))
            Stat("${report.streak}", "day streak", Modifier.weight(1f))
        }

        if (report.newBadges.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            report.newBadges.forEach { badge ->
                Surface(color = LH.MyBubble, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        Text("New badge: ${badge.title}", style = MaterialTheme.typography.titleMedium, color = LH.Saffron)
                        Text(badge.detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("This week", Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        WeekDots(report.week, report.date, Modifier.fillMaxWidth())

        report.verdict?.let {
            Spacer(Modifier.height(18.dp))
            Text("Mom's verdict this month: $it", style = MaterialTheme.typography.bodyMedium, color = LH.Oat, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton("Share with friends", onClick = onShare)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onDone,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, LH.Line),
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) {
            Text("Done", color = LH.Oat, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        color = LH.Roast,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = LH.Cream)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        }
    }
}
