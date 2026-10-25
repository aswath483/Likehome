package com.likehome.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likehome.app.core.RingStyle
import com.likehome.app.core.WakeTask
import com.likehome.app.data.Alarm
import com.likehome.app.data.AlarmRepository
import com.likehome.app.mom.Language
import com.likehome.app.ui.common.ChoiceCard
import com.likehome.app.ui.common.MaxContentWidth
import com.likehome.app.ui.common.rememberIs24Hour
import com.likehome.app.ui.styles.StyleThumb
import com.likehome.app.ui.common.CharacterFace
import com.likehome.app.core.Character
import com.likehome.app.ui.common.ChoicePill
import com.likehome.app.ui.common.MoodFace
import com.likehome.app.ui.common.PrimaryButton
import com.likehome.app.ui.common.SectionLabel
import com.likehome.app.ui.setup.SetupPanel
import com.likehome.app.ui.setup.SetupStatus
import com.likehome.app.ui.theme.Bangers
import com.likehome.app.ui.theme.LH
import androidx.compose.ui.res.stringResource
import com.likehome.app.R
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.common.detailRes
import com.likehome.app.ui.common.titleRes
import com.likehome.app.ui.common.shortRes

private enum class Step { Welcome, Character, Style, Language, Alarm, Setup }

/** What the user chose during onboarding. [firstAlarm] is null when they skipped it. */
data class OnboardingResult(val character: Character, val style: RingStyle, val language: Language, val firstAlarm: Alarm?)

/** First open, in under two minutes: style, language, first alarm, permissions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    status: SetupStatus,
    onRequestNotifications: () -> Unit,
    onFinish: (OnboardingResult) -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(Step.Welcome) }
    var style by rememberSaveable { mutableStateOf(RingStyle.CHAT) }
    var character by rememberSaveable { mutableStateOf(Character.MOM) }
    var language by rememberSaveable { mutableStateOf(Language.ENGLISH) }
    var task by rememberSaveable { mutableStateOf(WakeTask.MATH) }
    var wantsAlarm by rememberSaveable { mutableStateOf(true) }
    val time = rememberTimePickerState(initialHour = 7, initialMinute = 0, is24Hour = rememberIs24Hour())

    if (step != Step.Welcome) BackHandler { step = Step.entries[step.ordinal - 1] }

    Box(
        Modifier
            .fillMaxSize()
            .background(LH.Espresso)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
    Column(
        Modifier
            .widthIn(max = MaxContentWidth)
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        if (step != Step.Welcome) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1 until Step.entries.size).forEach { i ->
                    Box(
                        Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                            .background(if (i <= step.ordinal) LH.Saffron else LH.Line)
                    )
                }
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "onboarding",
            modifier = Modifier.weight(1f),
        ) { s ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                when (s) {
                    Step.Welcome -> Welcome()
                    Step.Character -> {
                        Title(stringResource(R.string.onb_who), stringResource(R.string.onb_who_detail))
                        Character.entries.forEach { c ->
                            CharacterOption(c, selected = character == c, onClick = { character = c })
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    Step.Style -> {
                        Title(stringResource(R.string.onb_style, stringResource(character.nameRes)), stringResource(R.string.onb_style_detail))
                        RingStyle.entries.forEach { st ->
                            StyleOption(st, selected = style == st, onClick = { style = st })
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    Step.Language -> {
                        Title(stringResource(R.string.onb_language), stringResource(R.string.onb_language_detail))
                        Language.entries.forEach { lang ->
                            ChoiceCard(lang.label, stringResource(R.string.quote, lang.sample), selected = language == lang, onClick = { language = lang })
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    Step.Alarm -> {
                        Title(stringResource(R.string.onb_when), stringResource(R.string.onb_when_detail))
                        TimeInput(
                            state = time,
                            colors = TimePickerDefaults.colors(
                                periodSelectorBorderColor = LH.Line,
                                periodSelectorSelectedContainerColor = LH.Saffron,
                                periodSelectorSelectedContentColor = LH.OnSaffron,
                                periodSelectorUnselectedContainerColor = LH.Roast,
                                periodSelectorUnselectedContentColor = LH.Oat,
                                timeSelectorSelectedContainerColor = LH.MyBubble,
                                timeSelectorSelectedContentColor = LH.Saffron,
                                timeSelectorUnselectedContainerColor = LH.Crema,
                                timeSelectorUnselectedContentColor = LH.Cream,
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                        SectionLabel(stringResource(R.string.onb_to_stop), Modifier.padding(top = 8.dp, bottom = 12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WakeTask.entries.forEach { t ->
                                ChoicePill(stringResource(t.shortRes), selected = t == task, onClick = { task = t }, modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(task.titleRes), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
                    }
                    Step.Setup -> {
                        Title(stringResource(R.string.onb_reach), stringResource(R.string.onb_reach_detail))
                        SetupPanel(status, onRequestNotifications)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        when (step) {
            Step.Welcome -> PrimaryButton(stringResource(R.string.onb_start), onClick = { step = Step.Character })
            Step.Alarm -> {
                PrimaryButton(stringResource(R.string.onb_set_alarm), onClick = { wantsAlarm = true; step = Step.Setup })
                TextButton(onClick = { wantsAlarm = false; step = Step.Setup }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.onb_later), color = LH.Oat)
                }
            }
            Step.Setup -> PrimaryButton(stringResource(R.string.done), onClick = {
                val alarm = if (!wantsAlarm) null else Alarm(
                    id = AlarmRepository.NEW_ID,
                    hour = time.hour,
                    minute = time.minute,
                    days = Alarm.WEEKDAYS,
                    task = task,
                    style = style,
                    character = character,
                )
                onFinish(OnboardingResult(character, style, language, alarm))
            })
            else -> PrimaryButton(stringResource(R.string.next), onClick = { step = Step.entries[step.ordinal + 1] })
        }
    }
    }
}

@Composable
private fun Welcome() {
    Column(
        Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MoodFace(0, Modifier.size(140.dp))
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.onb_brand), style = MaterialTheme.typography.labelMedium, color = LH.Saffron)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.onb_welcome),
            style = MaterialTheme.typography.headlineLarge,
            color = LH.Cream,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.onb_welcome_detail),
            style = MaterialTheme.typography.bodyLarge,
            color = LH.Oat,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp),
        )
    }
}

@Composable
private fun Title(title: String, detail: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall, color = LH.Cream, modifier = Modifier.padding(top = 20.dp))
    Spacer(Modifier.height(6.dp))
    Text(detail, style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
    Spacer(Modifier.height(20.dp))
}

/** A style card with a small live preview of how the ringing screen looks. */
@Composable
private fun StyleOption(style: RingStyle, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LH.Saffron else LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            StyleThumb(style, Modifier.size(width = 104.dp, height = 84.dp))
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(stringResource(style.titleRes), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(stringResource(style.detailRes), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }
    }
}

@Composable
private fun CharacterOption(character: Character, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) LH.MyBubble else LH.Roast,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LH.Saffron else LH.Line),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CharacterFace(character, 0, Modifier.size(60.dp))
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(stringResource(character.nameRes), style = MaterialTheme.typography.titleMedium, color = LH.Cream)
                Text(stringResource(character.detailRes), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
            }
        }
    }
}
