package com.likehome.app.data

import android.content.Context
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
    val reviewAsked: Boolean = false,
    val momSpeaks: Boolean = true,
    val weekendMode: Boolean = false,
    val bedtimeEnabled: Boolean = false,
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    /** Asked to hear when Premium launches. */
    val premiumWaitlist: Boolean = false,
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
            .putBoolean(KEY_REVIEW_ASKED, next.reviewAsked)
            .putBoolean(KEY_MOM_SPEAKS, next.momSpeaks)
            .putBoolean(KEY_WEEKEND_MODE, next.weekendMode)
            .putBoolean(KEY_BEDTIME_ON, next.bedtimeEnabled)
            .putInt(KEY_BEDTIME_HOUR, next.bedtimeHour)
            .putInt(KEY_BEDTIME_MINUTE, next.bedtimeMinute)
            .putBoolean(KEY_PREMIUM_WAITLIST, next.premiumWaitlist)
            .apply()
        _settings.value = next
    }

    private fun load() = AppSettings(
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false),
        language = enumOrDefault(prefs.getString(KEY_LANGUAGE, null), Language.ENGLISH),
        style = enumOrDefault(prefs.getString(KEY_STYLE, null), RingStyle.CHAT),
        reviewAsked = prefs.getBoolean(KEY_REVIEW_ASKED, false),
        momSpeaks = prefs.getBoolean(KEY_MOM_SPEAKS, true),
        weekendMode = prefs.getBoolean(KEY_WEEKEND_MODE, false),
        bedtimeEnabled = prefs.getBoolean(KEY_BEDTIME_ON, false),
        bedtimeHour = prefs.getInt(KEY_BEDTIME_HOUR, 23),
        bedtimeMinute = prefs.getInt(KEY_BEDTIME_MINUTE, 0),
        premiumWaitlist = prefs.getBoolean(KEY_PREMIUM_WAITLIST, false),
    )

    companion object {
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_STYLE = "style"
        private const val KEY_REVIEW_ASKED = "review_asked"
        private const val KEY_MOM_SPEAKS = "mom_speaks"
        private const val KEY_WEEKEND_MODE = "weekend_mode"
        private const val KEY_BEDTIME_ON = "bedtime_on"
        private const val KEY_BEDTIME_HOUR = "bedtime_hour"
        private const val KEY_BEDTIME_MINUTE = "bedtime_minute"
        private const val KEY_PREMIUM_WAITLIST = "premium_waitlist"

        @Volatile private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}

internal inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
    name?.let { n -> enumValues<E>().firstOrNull { it.name == n } } ?: default
