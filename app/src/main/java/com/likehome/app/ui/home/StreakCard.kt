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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.likehome.app.R
import com.likehome.app.ui.common.detailRes
import com.likehome.app.ui.common.titleRes

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
                        SectionLabel(stringResource(R.string.streak_title), Modifier.weight(1f))
                        Text(stringResource(R.string.see_all), style = MaterialTheme.typography.labelLarge, color = LH.Saffron)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        pluralStringResource(R.plurals.days, streak, streak),
                        style = MaterialTheme.typography.headlineLarge,
                        color = LH.Cream,
                    )
                    Text(
                        when {
                            mornings.isEmpty() -> stringResource(R.string.streak_first)
                            streak >= 7 -> stringResource(R.string.streak_family)
                            streak >= 3 -> stringResource(R.string.streak_proud)
                            streak > 0 -> stringResource(R.string.streak_start)
                            else -> stringResource(R.string.streak_waiting)
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
                Text(stringResource(R.string.verdict_this_month), style = MaterialTheme.typography.labelMedium, color = LH.Muted)
                Text(stringResource(it.titleRes), style = MaterialTheme.typography.titleMedium, color = LH.Saffron)
                Text(stringResource(it.detailRes), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }
    }
}

@Composable
private fun BadgeChip(badge: Badge) {
    Surface(color = LH.MyBubble, shape = RoundedCornerShape(50)) {
        Text(
            stringResource(badge.titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = LH.Saffron,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
