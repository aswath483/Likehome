package com.likehome.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.likehome.app.ui.theme.LH
import java.time.LocalDate

/**
 * The last 7 days, oldest first: a filled dot is a morning with no snooze, a ring is a
 * snoozed morning, and a faint speck is a day with no alarm.
 */
@Composable
fun WeekDots(week: List<Int?>, today: LocalDate, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        week.forEachIndexed { i, snoozes ->
            val day = today.minusDays((week.size - 1 - i).toLong())
            val name = day.dayOfWeek.name.lowercase().replaceFirstChar(Char::uppercase)
            val said = when (snoozes) {
                null -> "$name: no alarm"
                0 -> "$name: no snooze"
                else -> "$name: snoozed $snoozes"
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = said },
            ) {
                Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                    when (snoozes) {
                        null -> Box(Modifier.size(8.dp).clip(CircleShape).background(LH.Line))
                        0 -> Box(Modifier.size(26.dp).clip(CircleShape).background(LH.Saffron))
                        else -> Box(Modifier.size(24.dp).border(3.dp, LH.Saffron, CircleShape))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(name.take(1), style = MaterialTheme.typography.labelMedium, color = LH.Muted)
            }
        }
    }
}
