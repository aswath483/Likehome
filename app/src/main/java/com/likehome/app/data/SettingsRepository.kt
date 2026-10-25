package com.likehome.app.data

import android.content.Context
import com.likehome.app.core.Character
import com.likehome.app.core.RingStyle
import com.likehome.app.mom.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
)

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
            .apply()
        _settings.value = next
    }

    private fun load() = AppSettings(
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false),
        language = enumOrDefault(prefs.getString(KEY_LANGUAGE, null), Language.ENGLISH),
        style = enumOrDefault(prefs.getString(KEY_STYLE, null), RingStyle.CHAT),
        character = enumOrDefault(prefs.getString(KEY_CHARACTER, null), Character.MOM),
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
    )

    companion object {
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_STYLE = "style"
        private const val KEY_CHARACTER = "character"
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

        @Volatile private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}

internal inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
    name?.let { n -> enumValues<E>().firstOrNull { it.name == n } } ?: default
