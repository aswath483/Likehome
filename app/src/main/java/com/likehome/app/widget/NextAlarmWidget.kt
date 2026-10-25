package com.likehome.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.widget.RemoteViews
import com.likehome.app.R
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.ClockFormat
import com.likehome.app.core.Mornings
import com.likehome.app.data.AlarmRepository
import com.likehome.app.data.HistoryRepository
import com.likehome.app.ui.MainActivity
import java.time.ZoneId
import java.time.ZonedDateTime
import com.likehome.app.mom.MomLines
import com.likehome.app.ui.common.letters
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.common.shortRes
import com.likehome.app.ui.common.text

/** Home screen widget: the next wake-up, who's calling, the no-snooze streak and the last week. */
class NextAlarmWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = views(context)
        ids.forEach { manager.updateAppWidget(it, views) }
    }

    companion object {
        /** Redraws every placed widget; cheap, and a no-op when none is placed. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = runCatching { manager.getAppWidgetIds(ComponentName(context, NextAlarmWidget::class.java)) }.getOrNull()
            if (ids == null || ids.isEmpty()) return
            val views = views(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun views(context: Context): RemoteViews {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val is24 = DateFormat.is24HourFormat(context)
            val next = AlarmRepository.get(context).alarms.value.filter { it.enabled }.minByOrNull { AlarmScheduler.nextRing(context, it, now) }
            val streak = Mornings.streak(HistoryRepository.get(context).mornings.value)

            return RemoteViews(context.packageName, R.layout.widget_next_alarm).apply {
                if (next == null) {
                    setTextViewText(R.id.widget_time, "—")
                    setTextViewText(R.id.widget_period, "")
                    setTextViewText(R.id.widget_detail, context.getString(R.string.widget_no_alarm))
                } else {
                    val at = AlarmScheduler.nextRing(context, next, now)
                    setTextViewText(R.id.widget_time, ClockFormat.digits(at.toLocalTime(), is24))
                    setTextViewText(R.id.widget_period, ClockFormat.period(at.toLocalTime(), is24).orEmpty())
                    setTextViewText(
                        R.id.widget_detail,
                        context.getString(
                            R.string.widget_summary,
                            ClockFormat.dayOf(now.toLocalDate(), at.toLocalDate()).text(context.resources),
                            context.getString(next.character.nameRes),
                            context.getString(next.style.shortRes),
                        ),
                    )
                }
                setTextViewText(R.id.widget_streak, if (streak > 0) context.getString(R.string.widget_streak, context.resources.getQuantityString(R.plurals.days, streak, streak)) else context.getString(R.string.widget_start_streak))
                val open = PendingIntent.getActivity(
                    context, 10, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                setOnClickPendingIntent(R.id.widget_root, open)
                val today = now.toLocalDate()
                Mornings.week(HistoryRepository.get(context).mornings.value, today).forEachIndexed { i, snoozes ->
                    val day = today.minusDays(6L - i)
                    setTextViewText(WEEK_IDS[i], weekDot(context, snoozes, day.dayOfWeek.letters(context.resources)))
                }
            }
        }

        private val WEEK_IDS = intArrayOf(
            R.id.widget_day0, R.id.widget_day1, R.id.widget_day2, R.id.widget_day3,
            R.id.widget_day4, R.id.widget_day5, R.id.widget_day6,
        )

        /** A dot coloured by how the morning went, with the weekday's letter under it. */
        private fun weekDot(context: Context, snoozes: Int?, letter: String): CharSequence {
            val colour = when {
                snoozes == null -> 0xFF3A3129.toInt()
                snoozes == 0 -> 0xFF9CC9A0.toInt()
                snoozes >= MomLines.MAX_LEVEL -> 0xFFE8786A.toInt()
                else -> 0xFFF2A65A.toInt()
            }
            return SpannableStringBuilder()
                .append("●", ForegroundColorSpan(colour), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append("\n")
                .append(letter, ForegroundColorSpan(0xFF8C8073.toInt()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        /** Redraws both widgets, e.g. after a wake-up or an alarm change. */
        fun refreshAll(context: Context) {
            refresh(context)
            StreakWidget.refresh(context)
        }
    }
}
