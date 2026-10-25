package com.likehome.app.data

import android.content.Context
import com.likehome.app.core.Appearance
import com.likehome.app.core.Character
import com.likehome.app.core.RingStyle
import com.likehome.app.mom.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.Locale

/**
 * App-wide choices the user makes once. [style] is the default for new alarms and
 * for test rings. [momSpeaks] reads her lines aloud with the phone's voice.
 */
data class AppSettings(
    val onboarded: Boolean = false,
    val language: Language = Language.ENGLISH,
    val style: RingStyle = RingStyle.CHAT,
    /** Who wakes the user on new alarms and test rings. */
    val character: Character = Character.MOM,
    /** How each family character looks, when the user changed it from the default. */
    val appearances: Map<Character, Appearance> = emptyMap(),
    val reviewAsked: Boolean = false,
    val momSpeaks: Boolean = true,
    val weekendMode: Boolean = false,
    val bedtimeEnabled: Boolean = false,
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    val waterEnabled: Boolean = false,
    val breakfastEnabled: Boolean = false,
    val breakfastHour: Int = 8,
    val breakfastMinute: Int = 30,
    /** The real Mom's WhatsApp number, for "Tell Mom I'm up"; blank when not set. */
    val familyNumber: String = "",
    /** Vacation mode: repeating alarms stay quiet up to and including this day. */
    val pausedUntil: LocalDate? = null,
    /** Ramadan mode: a suhoor alarm [ramadanLead] minutes before Fajr, and an iftar nudge. */
    val ramadanEnabled: Boolean = false,
    val ramadanLead: Int = 45,
    val iftarReminder: Boolean = true,
    /** Ring only on Ramadan days (Umm al-Qura calendar); off = every day while enabled. */
    val ramadanAuto: Boolean = true,
    /** Where prayer times are worked out for; null until location is shared once. */
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** What the morning briefing on the report card includes. */
    val briefingWeather: Boolean = false,
    val briefingCalendar: Boolean = false,
) {
    fun appearanceOf(character: Character): Appearance? =
        appearances[character] ?: character.defaultAppearance
}

/** Settings in SharedPreferences, exposed as a [StateFlow] like [AlarmRepository]. */
class SettingsRepository private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    val current: AppSettings get() = _settings.value

    fun update(change: (AppSettings) -> AppSettings) {
        val next = change(_settings.value)
        prefs.edit()
            .putBoolean(KEY_ONBOARDED, next.onboarded)
            .putString(KEY_LANGUAGE, next.language.name)
            .putString(KEY_STYLE, next.style.name)
            .putString(KEY_CHARACTER, next.character.name)
            .putString(KEY_APPEARANCES, Appearance.encode(next.appearances))
            .putBoolean(KEY_REVIEW_ASKED, next.reviewAsked)
            .putBoolean(KEY_MOM_SPEAKS, next.momSpeaks)
            .putBoolean(KEY_WEEKEND_MODE, next.weekendMode)
            .putBoolean(KEY_BEDTIME_ON, next.bedtimeEnabled)
            .putInt(KEY_BEDTIME_HOUR, next.bedtimeHour)
            .putInt(KEY_BEDTIME_MINUTE, next.bedtimeMinute)
            .putBoolean(KEY_WATER_ON, next.waterEnabled)
            .putBoolean(KEY_BREAKFAST_ON, next.breakfastEnabled)
            .putInt(KEY_BREAKFAST_HOUR, next.breakfastHour)
            .putInt(KEY_BREAKFAST_MINUTE, next.breakfastMinute)
            .putString(KEY_FAMILY_NUMBER, next.familyNumber)
            .putString(KEY_PAUSED_UNTIL, next.pausedUntil?.toString().orEmpty())
            .putBoolean(KEY_RAMADAN, next.ramadanEnabled)
            .putInt(KEY_RAMADAN_LEAD, next.ramadanLead)
            .putBoolean(KEY_IFTAR, next.iftarReminder)
            .putBoolean(KEY_RAMADAN_AUTO, next.ramadanAuto)
            .putString(KEY_LAT, next.latitude?.toString().orEmpty())
            .putString(KEY_LON, next.longitude?.toString().orEmpty())
            .putBoolean(KEY_BRIEF_WEATHER, next.briefingWeather)
            .putBoolean(KEY_BRIEF_CALENDAR, next.briefingCalendar)
            .apply()
        _settings.value = next
    }

    private fun load() = AppSettings(
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false),
        // A new user's Mom starts out speaking the phone's language.
        language = enumOrDefault(prefs.getString(KEY_LANGUAGE, null), Language.forLocale(Locale.getDefault().toLanguageTag())),
        style = enumOrDefault(prefs.getString(KEY_STYLE, null), RingStyle.CHAT),
        character = enumOrDefault(prefs.getString(KEY_CHARACTER, null), Character.MOM),
        appearances = Appearance.decode(prefs.getString(KEY_APPEARANCES, null)),
        reviewAsked = prefs.getBoolean(KEY_REVIEW_ASKED, false),
        momSpeaks = prefs.getBoolean(KEY_MOM_SPEAKS, true),
        weekendMode = prefs.getBoolean(KEY_WEEKEND_MODE, false),
        bedtimeEnabled = prefs.getBoolean(KEY_BEDTIME_ON, false),
        bedtimeHour = prefs.getInt(KEY_BEDTIME_HOUR, 23),
        bedtimeMinute = prefs.getInt(KEY_BEDTIME_MINUTE, 0),
        waterEnabled = prefs.getBoolean(KEY_WATER_ON, false),
        breakfastEnabled = prefs.getBoolean(KEY_BREAKFAST_ON, false),
        breakfastHour = prefs.getInt(KEY_BREAKFAST_HOUR, 8),
        breakfastMinute = prefs.getInt(KEY_BREAKFAST_MINUTE, 30),
        familyNumber = prefs.getString(KEY_FAMILY_NUMBER, null).orEmpty(),
        pausedUntil = prefs.getString(KEY_PAUSED_UNTIL, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.takeIf { !it.isBefore(LocalDate.now()) },
        ramadanEnabled = prefs.getBoolean(KEY_RAMADAN, false),
        ramadanLead = prefs.getInt(KEY_RAMADAN_LEAD, 45),
        iftarReminder = prefs.getBoolean(KEY_IFTAR, true),
        ramadanAuto = prefs.getBoolean(KEY_RAMADAN_AUTO, true),
        latitude = prefs.getString(KEY_LAT, null)?.toDoubleOrNull(),
        longitude = prefs.getString(KEY_LON, null)?.toDoubleOrNull(),
        briefingWeather = prefs.getBoolean(KEY_BRIEF_WEATHER, false),
        briefingCalendar = prefs.getBoolean(KEY_BRIEF_CALENDAR, false),
    )

    companion object {
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_STYLE = "style"
        private const val KEY_CHARACTER = "character"
        private const val KEY_APPEARANCES = "appearances"
        private const val KEY_REVIEW_ASKED = "review_asked"
        private const val KEY_MOM_SPEAKS = "mom_speaks"
        private const val KEY_WEEKEND_MODE = "weekend_mode"
        private const val KEY_BEDTIME_ON = "bedtime_on"
        private const val KEY_BEDTIME_HOUR = "bedtime_hour"
        private const val KEY_BEDTIME_MINUTE = "bedtime_minute"
        private const val KEY_WATER_ON = "water_on"
        private const val KEY_BREAKFAST_ON = "breakfast_on"
        private const val KEY_BREAKFAST_HOUR = "breakfast_hour"
        private const val KEY_BREAKFAST_MINUTE = "breakfast_minute"
        private const val KEY_FAMILY_NUMBER = "family_number"
        private const val KEY_PAUSED_UNTIL = "paused_until"
        private const val KEY_RAMADAN = "ramadan"
        private const val KEY_RAMADAN_LEAD = "ramadan_lead"
        private const val KEY_IFTAR = "iftar"
        private const val KEY_RAMADAN_AUTO = "ramadan_auto"
        private const val KEY_LAT = "lat"
        private const val KEY_LON = "lon"
        private const val KEY_BRIEF_WEATHER = "brief_weather"
        private const val KEY_BRIEF_CALENDAR = "brief_calendar"

        @Volatile private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}

internal inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
    name?.let { n -> enumValues<E>().firstOrNull { it.name == n } } ?: default
