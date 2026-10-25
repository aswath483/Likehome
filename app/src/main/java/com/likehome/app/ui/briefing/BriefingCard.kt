package com.likehome.app.ui.briefing

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.likehome.app.R
import com.likehome.app.alarm.CalendarEvents
import com.likehome.app.core.Briefing
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Weather
import com.likehome.app.core.WeatherAdvice
import com.likehome.app.data.ImportantDayStore
import com.likehome.app.data.SettingsRepository
import com.likehome.app.data.WeatherClient
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.theme.LH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Mom's morning briefing, under the report card once the user is up: an important day,
 * the weather with her advice, and the first meeting. Each part shows only when it's on
 * and has something to say; with nothing to say, the card isn't shown.
 */
@Composable
fun BriefingCard() {
    val context = LocalContext.current
    val settings = remember { SettingsRepository.get(context).current }
    val is24 = rememberIs24Hour()
    val important = remember { ImportantDayStore.get(context).today() }
    val event = remember {
        if (!settings.briefingCalendar) null else {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            CalendarEvents.firstEvent(context, now, now.toLocalDate().plusDays(1).atStartOfDay(now.zone))
        }
    }
    val weather by produceState<Weather?>(null) {
        if (settings.briefingWeather) value = withContext(Dispatchers.IO) { WeatherClient.today(context) }
    }
    if (important == null && event == null && weather == null) return

    Card(Modifier.padding(top = 16.dp)) {
        SectionLabel(stringResource(R.string.briefing_title))
        important?.let { Line(Icons.Outlined.Celebration, stringResource(R.string.briefing_today, it.title)) }
        weather?.let { w ->
            Line(Icons.Outlined.WbSunny, stringResource(R.string.briefing_weather, w.maxC, w.minC) + " " + advice(w))
        }
        event?.let { (start, title) ->
            Line(
                Icons.Outlined.CalendarMonth,
                stringResource(R.string.briefing_event, title.ifBlank { stringResource(R.string.briefing_event_untitled) }, ClockFormat.full(start.toLocalTime(), is24)),
            )
        }
    }
}

@Composable
private fun advice(w: Weather): String = when (Briefing.advice(w)) {
    WeatherAdvice.STORM -> stringResource(R.string.advice_storm)
    WeatherAdvice.SNOW -> stringResource(R.string.advice_snow)
    WeatherAdvice.RAIN -> stringResource(R.string.advice_rain, w.rainChance)
    WeatherAdvice.HOT -> stringResource(R.string.advice_hot)
    WeatherAdvice.COLD -> stringResource(R.string.advice_cold)
    WeatherAdvice.LOVELY -> stringResource(R.string.advice_lovely)
}

@Composable
private fun Line(icon: ImageVector, text: String) {
    Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = LH.Saffron, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = LH.Cream)
    }
}

