package com.likehome.app.data

import android.content.Context
import com.likehome.app.core.SleepInsights
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * When the user went to bed, for sleep insights: recorded when they open the bedside clock
 * or start sleep sounds in the evening. Kept on the phone (and in backups) for four weeks.
 */
class BedtimeLog private constructor(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _bedtimes = MutableStateFlow(SleepInsights.decode(prefs.getString(KEY, null)))
    val bedtimes: StateFlow<List<LocalDateTime>> = _bedtimes.asStateFlow()

    /** Records going to bed now, if it's a plausible bedtime (evening or small hours). */
    fun wentToBed(at: LocalDateTime = LocalDateTime.now()) {
        if (at.hour in 5 until 19) return
        val next = SleepInsights.add(_bedtimes.value, at)
        prefs.edit().putString(KEY, SleepInsights.encode(next)).apply()
        _bedtimes.value = next
    }

    companion object {
        const val PREFS = "bedtimes"
        private const val KEY = "list"
        @Volatile private var instance: BedtimeLog? = null
        fun get(context: Context): BedtimeLog =
            instance ?: synchronized(this) { instance ?: BedtimeLog(context).also { instance = it } }
    }
}
