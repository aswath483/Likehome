package com.likehome.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
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
import com.likehome.app.ui.common.nameRes
import com.likehome.app.ui.common.shortRes
import com.likehome.app.ui.common.text

/** Home screen widget: the next wake-up, who's calling, and the no-snooze streak. */
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
            }
        }
    }
}
