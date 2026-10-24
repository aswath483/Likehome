package com.likehome.app.data

import android.content.Context
import com.likehome.app.mom.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** App-wide choices the user makes once: Mom's language and similar. */
data class AppSettings(
    val language: Language = Language.ENGLISH,
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
            .putString(KEY_LANGUAGE, next.language.name)
            .apply()
        _settings.value = next
    }

    private fun load() = AppSettings(
        language = enumOrDefault(prefs.getString(KEY_LANGUAGE, null), Language.ENGLISH),
    )

    companion object {
        private const val KEY_LANGUAGE = "language"

        @Volatile private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}

internal inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
    name?.let { n -> enumValues<E>().firstOrNull { it.name == n } } ?: default
