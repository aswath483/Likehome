package com.likehome.app.data

import android.content.Context
import com.likehome.app.core.ImportantDay
import com.likehome.app.core.ImportantDays
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** Days the user told Mom about (exams, birthdays…), kept on the phone and in backups. */
class ImportantDayStore private constructor(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _days = MutableStateFlow(ImportantDays.decode(prefs.getString(KEY, null)))
    val days: StateFlow<List<ImportantDay>> = _days.asStateFlow()

    fun today(day: LocalDate = LocalDate.now()): ImportantDay? = ImportantDays.today(_days.value, day)

    fun save(day: ImportantDay) {
        val withId = if (day.id == 0L) day.copy(id = System.currentTimeMillis()) else day
        persist(_days.value.filterNot { it.id == withId.id } + withId)
    }

    fun delete(id: Long) = persist(_days.value.filterNot { it.id == id })

    private fun persist(list: List<ImportantDay>) {
        prefs.edit().putString(KEY, ImportantDays.encode(list)).apply()
        _days.value = list
    }

    companion object {
        const val PREFS = "important"
        private const val KEY = "days"
        @Volatile private var instance: ImportantDayStore? = null
        fun get(context: Context): ImportantDayStore =
            instance ?: synchronized(this) { instance ?: ImportantDayStore(context).also { instance = it } }
    }
}
