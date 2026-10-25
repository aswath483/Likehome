package com.likehome.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.likehome.app.R
import com.likehome.app.core.Mornings
import com.likehome.app.data.HistoryRepository
import com.likehome.app.ui.MainActivity

/** Small home screen widget: the no-snooze streak, big, and the best one so far. */
class StreakWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = views(context)
        ids.forEach { manager.updateAppWidget(it, views) }
    }

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = runCatching { manager.getAppWidgetIds(ComponentName(context, StreakWidget::class.java)) }.getOrNull()
            if (ids == null || ids.isEmpty()) return
            val views = views(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun views(context: Context): RemoteViews {
            val history = HistoryRepository.get(context).mornings.value
            val streak = Mornings.streak(history)
            val best = Mornings.bestStreak(history)
            val res = context.resources
            return RemoteViews(context.packageName, R.layout.widget_streak).apply {
                setTextViewText(R.id.streak_count, "$streak")
                setTextViewText(R.id.streak_label, res.getQuantityString(R.plurals.streak_widget_label, streak))
                setTextViewText(
                    R.id.streak_best,
                    if (streak > 0 && streak >= best) context.getString(R.string.streak_widget_record)
                    else context.getString(R.string.streak_widget_best, best),
                )
                val open = PendingIntent.getActivity(
                    context, 11, Intent(context, MainActivity::class.java).setAction(MainActivity.ACTION_HISTORY),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                setOnClickPendingIntent(R.id.streak_root, open)
            }
        }
    }
}
