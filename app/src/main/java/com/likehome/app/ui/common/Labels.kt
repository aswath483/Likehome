package com.likehome.app.ui.common

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.likehome.app.R
import com.likehome.app.core.Appearance
import com.likehome.app.core.Badge
import com.likehome.app.core.BrandGuide
import com.likehome.app.core.Character
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.ReminderKind
import com.likehome.app.core.Report
import com.likehome.app.core.ReportKind
import com.likehome.app.core.RingStyle
import com.likehome.app.core.SleepSound
import com.likehome.app.core.Verdict
import com.likehome.app.core.WakeTask
import com.likehome.app.data.Alarm
import android.text.format.DateFormat
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/*
 * The words for the app's core types. They live in string resources so every language
 * gets its own; core/ only knows the enums.
 */

val Character.nameRes: Int @StringRes get() = when (this) {
    Character.MOM -> R.string.character_mom
    Character.PAPA -> R.string.character_papa
    Character.DADI -> R.string.character_dadi
    Character.BOSS -> R.string.character_boss
    Character.ABUELA -> R.string.character_abuela
}

val Character.detailRes: Int @StringRes get() = when (this) {
    Character.MOM -> R.string.character_mom_detail
    Character.PAPA -> R.string.character_papa_detail
    Character.DADI -> R.string.character_dadi_detail
    Character.BOSS -> R.string.character_boss_detail
    Character.ABUELA -> R.string.character_abuela_detail
}

val RingStyle.shortRes: Int @StringRes get() = when (this) {
    RingStyle.CHAT -> R.string.style_chat_short
    RingStyle.COMIC -> R.string.style_comic_short
    RingStyle.NEWS -> R.string.style_news_short
    RingStyle.RETRO -> R.string.style_retro_short
    RingStyle.FILMY -> R.string.style_filmy_short
    RingStyle.KITCHEN -> R.string.style_kitchen_short
    RingStyle.NIGHT -> R.string.style_night_short
    RingStyle.FRIDGE -> R.string.style_fridge_short
    RingStyle.SMART_MIX -> R.string.style_mix_short
}

val RingStyle.titleRes: Int @StringRes get() = when (this) {
    RingStyle.CHAT -> R.string.style_chat_title
    RingStyle.COMIC -> R.string.style_comic_title
    RingStyle.NEWS -> R.string.style_news_title
    RingStyle.RETRO -> R.string.style_retro_title
    RingStyle.FILMY -> R.string.style_filmy_title
    RingStyle.KITCHEN -> R.string.style_kitchen_title
    RingStyle.NIGHT -> R.string.style_night_title
    RingStyle.FRIDGE -> R.string.style_fridge_title
    RingStyle.SMART_MIX -> R.string.style_mix_title
}

val RingStyle.detailRes: Int @StringRes get() = when (this) {
    RingStyle.CHAT -> R.string.style_chat_detail
    RingStyle.COMIC -> R.string.style_comic_detail
    RingStyle.NEWS -> R.string.style_news_detail
    RingStyle.RETRO -> R.string.style_retro_detail
    RingStyle.FILMY -> R.string.style_filmy_detail
    RingStyle.KITCHEN -> R.string.style_kitchen_detail
    RingStyle.NIGHT -> R.string.style_night_detail
    RingStyle.FRIDGE -> R.string.style_fridge_detail
    RingStyle.SMART_MIX -> R.string.style_mix_detail
}

val WakeTask.shortRes: Int @StringRes get() = when (this) {
    WakeTask.MATH -> R.string.task_math_short
    WakeTask.STEPS -> R.string.task_steps_short
    WakeTask.SHAKE -> R.string.task_shake_short
    WakeTask.PHOTO -> R.string.task_photo_short
    WakeTask.VOICE -> R.string.task_voice_short
    WakeTask.PROMISE -> R.string.task_promise_short
    WakeTask.MEMORY -> R.string.task_memory_short
    WakeTask.BARCODE -> R.string.task_barcode_short
    WakeTask.LIGHT -> R.string.task_light_short
}

val WakeTask.titleRes: Int @StringRes get() = when (this) {
    WakeTask.MATH -> R.string.task_math_title
    WakeTask.STEPS -> R.string.task_steps_title
    WakeTask.SHAKE -> R.string.task_shake_title
    WakeTask.PHOTO -> R.string.task_photo_title
    WakeTask.VOICE -> R.string.task_voice_title
    WakeTask.PROMISE -> R.string.task_promise_title
    WakeTask.MEMORY -> R.string.task_memory_title
    WakeTask.BARCODE -> R.string.task_barcode_title
    WakeTask.LIGHT -> R.string.task_light_title
}

val WakeTask.detailRes: Int @StringRes get() = when (this) {
    WakeTask.MATH -> R.string.task_math_detail
    WakeTask.STEPS -> R.string.task_steps_detail
    WakeTask.SHAKE -> R.string.task_shake_detail
    WakeTask.PHOTO -> R.string.task_photo_detail
    WakeTask.VOICE -> R.string.task_voice_detail
    WakeTask.PROMISE -> R.string.task_promise_detail
    WakeTask.MEMORY -> R.string.task_memory_detail
    WakeTask.BARCODE -> R.string.task_barcode_detail
    WakeTask.LIGHT -> R.string.task_light_detail
}

val Appearance.labelRes: Int @StringRes get() = when (this) {
    Appearance.SOUTH_ASIAN -> R.string.look_south_asian
    Appearance.EAST_ASIAN -> R.string.look_east_asian
    Appearance.AFRICAN -> R.string.look_african
    Appearance.EUROPEAN -> R.string.look_european
    Appearance.LATIN -> R.string.look_latin
    Appearance.MIDDLE_EASTERN -> R.string.look_middle_eastern
}

val Badge.titleRes: Int @StringRes get() = when (this) {
    Badge.EARLY_BIRD -> R.string.badge_early_bird
    Badge.MOMS_FAVOURITE -> R.string.badge_moms_favourite
    Badge.SNOOZE_KING -> R.string.badge_snooze_king
}

val Badge.detailRes: Int @StringRes get() = when (this) {
    Badge.EARLY_BIRD -> R.string.badge_early_bird_detail
    Badge.MOMS_FAVOURITE -> R.string.badge_moms_favourite_detail
    Badge.SNOOZE_KING -> R.string.badge_snooze_king_detail
}

val Verdict.titleRes: Int @StringRes get() = when (this) {
    Verdict.GOOD_CHILD -> R.string.verdict_good_child
    Verdict.TRYING -> R.string.verdict_trying
    Verdict.SHARMA_JI -> R.string.verdict_sharma_ji
}

val Verdict.detailRes: Int @StringRes get() = when (this) {
    Verdict.GOOD_CHILD -> R.string.verdict_good_child_detail
    Verdict.TRYING -> R.string.verdict_trying_detail
    Verdict.SHARMA_JI -> R.string.verdict_sharma_ji_detail
}

val ReminderKind.titleRes: Int @StringRes get() = when (this) {
    ReminderKind.BEDTIME -> R.string.reminder_bedtime
    ReminderKind.WATER -> R.string.reminder_water
    ReminderKind.BREAKFAST -> R.string.reminder_breakfast
}

val SleepSound.labelRes: Int @StringRes get() = when (this) {
    SleepSound.RAIN -> R.string.sleep_rain
    SleepSound.WAVES -> R.string.sleep_waves
    SleepSound.FAN -> R.string.sleep_fan
    SleepSound.BROWN -> R.string.sleep_brown
    SleepSound.WHITE -> R.string.sleep_white
}

val SleepSound.detailRes: Int @StringRes get() = when (this) {
    SleepSound.RAIN -> R.string.sleep_rain_detail
    SleepSound.WAVES -> R.string.sleep_waves_detail
    SleepSound.FAN -> R.string.sleep_fan_detail
    SleepSound.BROWN -> R.string.sleep_brown_detail
    SleepSound.WHITE -> R.string.sleep_white_detail
}

/** "Sweet", "Annoyed", "Comparing you" or "Full drama". */
@StringRes fun moodRes(level: Int): Int = when (level.coerceIn(0, 3)) {
    0 -> R.string.mood_sweet
    1 -> R.string.mood_annoyed
    2 -> R.string.mood_comparing
    else -> R.string.mood_drama
}

/** The comic sound effect for a drama level: "BEEP!", "HMPH!", "WHAT?!", "SPLASH!". */
@StringRes fun comicSoundRes(level: Int): Int = when (level.coerceIn(0, 3)) {
    0 -> R.string.comic_sound_0
    1 -> R.string.comic_sound_1
    2 -> R.string.comic_sound_2
    else -> R.string.comic_sound_3
}

fun Report.title(res: Resources): String = res.getString(
    when (kind) {
        ReportKind.PROUD_STREAK, ReportKind.PROUD -> R.string.report_proud
        ReportKind.ONCE -> R.string.report_once
        ReportKind.TWICE -> R.string.report_twice
        ReportKind.DISAPPOINTED -> R.string.report_disappointed
    },
)

fun Report.body(res: Resources): String = when (kind) {
    ReportKind.PROUD_STREAK -> res.getString(R.string.report_proud_streak_body, streak)
    ReportKind.PROUD -> res.getString(R.string.report_proud_body)
    ReportKind.ONCE -> res.getString(R.string.report_once_body)
    ReportKind.TWICE -> res.getString(R.string.report_twice_body)
    ReportKind.DISAPPOINTED -> res.getString(R.string.report_disappointed_body, snoozes)
}

/** "in 7 h 20 min", or "now". */
fun ClockFormat.Span.text(res: Resources): String = when {
    isNow -> res.getString(R.string.time_now)
    hours == 0 -> res.getString(R.string.time_in_minutes, minutes)
    minutes == 0 -> res.getString(R.string.time_in_hours, hours)
    else -> res.getString(R.string.time_in_hours_minutes, hours, minutes)
}

/** "Today", "Tomorrow" or the weekday's name in the app's language. */
fun ClockFormat.Day.text(res: Resources): String = when (this) {
    ClockFormat.Day.Today -> res.getString(R.string.day_today)
    ClockFormat.Day.Tomorrow -> res.getString(R.string.day_tomorrow)
    is ClockFormat.Day.Weekday -> day.fullName(res)
}

val BrandGuide.brandRes: Int @StringRes get() = when (key) {
    "xiaomi" -> R.string.battery_xiaomi
    "samsung" -> R.string.battery_samsung
    "vivo" -> R.string.battery_vivo
    "oppo" -> R.string.battery_oppo
    "huawei" -> R.string.battery_huawei
    else -> R.string.battery_generic
}

val BrandGuide.stepsRes: Int get() = when (key) {
    "xiaomi" -> R.array.battery_xiaomi_steps
    "samsung" -> R.array.battery_samsung_steps
    "vivo" -> R.array.battery_vivo_steps
    "oppo" -> R.array.battery_oppo_steps
    "huawei" -> R.array.battery_huawei_steps
    else -> R.array.battery_generic_steps
}

/** The resources for composables that need plain [Resources] calls. */
@Composable
fun resources(): Resources = LocalContext.current.resources

fun Resources.locale(): Locale = configuration.locales[0]

/**
 * A date format in the app language's own order, from a skeleton such as "EEEdMMM"
 * ("Mon, 3 Nov" in English, "lun., 3 nov." in French).
 */
fun Resources.dateFormat(skeleton: String): DateTimeFormatter =
    DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale(), skeleton), locale())

/** A weekday's short name ("Mon") or full name ("Monday") in the app's language. */
fun DayOfWeek.shortName(res: Resources): String = getDisplayName(TextStyle.SHORT, res.locale())
fun DayOfWeek.fullName(res: Resources): String =
    getDisplayName(TextStyle.FULL, res.locale()).replaceFirstChar { it.uppercase(res.locale()) }

/** A two-letter weekday ("Mo") for small day buttons; one letter where names are long. */
fun DayOfWeek.letters(res: Resources): String =
    shortName(res).trimEnd('.').let { if (it.length <= 4) it.take(2) else getDisplayName(TextStyle.NARROW, res.locale()) }

/** "Every day", "Weekdays", "Weekends", "Once" or the chosen days ("Mon Wed Fri"). */
fun Alarm.repeatText(res: Resources): String = when {
    days.isEmpty() -> res.getString(R.string.repeat_once)
    days.size == 7 -> res.getString(R.string.repeat_every_day)
    days == Alarm.WEEKDAYS -> res.getString(R.string.repeat_weekdays)
    days == Alarm.WEEKEND -> res.getString(R.string.repeat_weekends)
    else -> DayOfWeek.entries.filter { it in days }.joinToString(" ") { it.shortName(res) }
}

/** One-letter weekday names, Monday first. */
fun Resources.weekdayInitials(): List<String> =
    DayOfWeek.entries.map { it.getDisplayName(TextStyle.NARROW, locale()) }
