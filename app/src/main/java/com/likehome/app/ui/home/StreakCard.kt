package com.likehome.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.likehome.app.core.Badge
import com.likehome.app.core.Morning
import com.likehome.app.core.Mornings
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.WeekDots
import com.likehome.app.ui.common.withHaptic
import com.likehome.app.ui.theme.LH
import java.time.LocalDate

/** No-snooze streak, the week at a glance, badges and Mom's monthly verdict. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun StreakCard(mornings: List<Morning>, today: LocalDate, onClick: () -> Unit) {
    val streak = Mornings.streak(mornings)
    val badges = Mornings.badges(mornings)
    val verdict = Mornings.verdict(mornings, today)
    val lastSnoozes = mornings.lastOrNull()?.snoozes

    Surface(
        onClick = withHaptic(onClick),
        color = LH.Roast,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("No-snooze streak", Modifier.weight(1f))
                        Text("See all", style = MaterialTheme.typography.labelLarge, color = LH.Saffron)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (streak == 1) "1 day" else "$streak days",
                        style = MaterialTheme.typography.headlineLarge,
                        color = LH.Cream,
                    )
                    Text(
                        when {
                            mornings.isEmpty() -> "Your first report comes after your next alarm."
                            streak >= 7 -> "Mom is telling the whole family."
                            streak >= 3 -> "Mom is proud of you."
                            streak > 0 -> "Good start. Keep it going."
                            else -> "Mom is waiting for tomorrow."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = LH.Oat,
                    )
                }
                Spacer(Modifier.width(12.dp))
                MoodFace(lastSnoozes ?: 0, Modifier.size(56.dp))
            }
            if (mornings.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                WeekDots(Mornings.week(mornings, today), today, Modifier.fillMaxWidth())
            }
            if (badges.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge.entries.filter { it in badges }.forEach { BadgeChip(it) }
                }
            }
            verdict?.let {
                Spacer(Modifier.height(16.dp))
                Text("Mom's verdict this month", style = MaterialTheme.typography.labelMedium, color = LH.Muted)
                Text(it.title, style = MaterialTheme.typography.titleMedium, color = LH.Saffron)
                Text(it.detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }
    }
}

@Composable
private fun BadgeChip(badge: Badge) {
    Surface(color = LH.MyBubble, shape = RoundedCornerShape(50)) {
        Text(
            badge.title,
            style = MaterialTheme.typography.labelLarge,
            color = LH.Saffron,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
