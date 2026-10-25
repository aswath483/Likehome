package com.likehome.app.ui.history

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Morning
import com.likehome.app.core.SleepInsights
import com.likehome.app.core.SleepSummary
import com.likehome.app.core.SleepTip
import com.likehome.app.data.BedtimeLog
import com.likehome.app.ui.bedside.BedsideActivity
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.fullName
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.common.resources
import com.likehome.app.ui.theme.LH
import java.time.LocalDate

/** Mom's read on the last four weeks: when you get up, how steady it is, how long you sleep. */
@Composable
internal fun SleepInsightsCard(history: List<Morning>, today: LocalDate) {
    val context = LocalContext.current
    val bedtimes by remember { BedtimeLog.get(context) }.bedtimes.collectAsState()
    val s = remember(history, bedtimes, today) { SleepInsights.summary(history, bedtimes, today) }
    val is24 = rememberIs24Hour()

    SectionLabel(stringResource(R.string.insights_title), Modifier.padding(top = 8.dp))
    Card {
        Text(tipTitle(s), style = MaterialTheme.typography.titleLarge, color = LH.Saffron)
        Spacer(Modifier.height(4.dp))
        Text(tipBody(s), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        if (s.tip != SleepTip.NEED_MORE) {
            Spacer(Modifier.height(12.dp))
            s.averageWake?.let { Line(stringResource(R.string.insights_up_at, ClockFormat.full(it, is24))) }
            s.wakeSpread?.let { Line(stringResource(R.string.insights_spread, it)) }
            val sleep = s.averageSleepMinutes
            if (sleep != null) {
                Line(stringResource(R.string.insights_sleep, stringResource(R.string.hours_minutes, sleep / 60, sleep % 60)))
            } else {
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.insights_sleep_none), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
                TextButton(onClick = { BedsideActivity.open(context) }) { Text(stringResource(R.string.bedside_title), color = LH.Saffron) }
            }
            Spacer(Modifier.height(6.dp))
            Text(pluralStringResource(R.plurals.insights_based_on, s.mornings, s.mornings), style = MaterialTheme.typography.bodySmall, color = LH.Muted)
        }
    }
}

@Composable
private fun Line(text: String) = Text(text, style = MaterialTheme.typography.bodyLarge, color = LH.Cream)

@Composable
private fun tipTitle(s: SleepSummary): String = when (s.tip) {
    SleepTip.SHORT_NIGHTS -> stringResource(R.string.tip_short_nights_title)
    SleepTip.IRREGULAR -> stringResource(R.string.tip_irregular_title)
    SleepTip.BAD_WEEKDAY -> stringResource(R.string.tip_bad_weekday_title, s.worstDay?.fullName(resources()).orEmpty())
    SleepTip.IMPROVING -> stringResource(R.string.tip_improving_title)
    SleepTip.GREAT -> stringResource(R.string.tip_great_title)
    SleepTip.NEED_MORE -> stringResource(R.string.tip_need_more_title)
}

@Composable
private fun tipBody(s: SleepSummary): String = when (s.tip) {
    SleepTip.SHORT_NIGHTS -> stringResource(R.string.tip_short_nights)
    SleepTip.IRREGULAR -> stringResource(R.string.tip_irregular)
    SleepTip.BAD_WEEKDAY -> stringResource(R.string.tip_bad_weekday, s.worstDay?.fullName(resources()).orEmpty())
    SleepTip.IMPROVING -> stringResource(R.string.tip_improving)
    SleepTip.GREAT -> stringResource(R.string.tip_great)
    SleepTip.NEED_MORE -> stringResource(R.string.tip_need_more)
}
