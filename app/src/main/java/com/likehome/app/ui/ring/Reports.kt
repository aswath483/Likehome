package com.likehome.app.ui.ring

import android.content.res.Resources
import com.likehome.app.core.Badge
import com.likehome.app.core.Morning
import com.likehome.app.core.Mornings
import com.likehome.app.ui.common.titleRes
import com.likehome.app.ui.common.title
import com.likehome.app.ui.common.body

object Reports {
    /**
     * The report card for [morning] as it stood that day: the streak, week and verdict
     * count only mornings up to its date. [history] should include [morning].
     */
    fun forMorning(res: Resources, history: List<Morning>, morning: Morning, newBadges: Set<Badge> = emptySet()): ReportData {
        val upTo = history.filter { !it.date.isAfter(morning.date) }
        val streak = Mornings.streak(upTo)
        val copy = Mornings.report(morning.snoozes, streak)
        return ReportData(
            date = morning.date,
            wokeAt = morning.wokeAt,
            snoozes = morning.snoozes,
            streak = streak,
            title = copy.title(res),
            body = copy.body(res),
            week = Mornings.week(upTo, morning.date),
            newBadges = newBadges,
            verdict = Mornings.verdict(upTo, morning.date)?.let { res.getString(it.titleRes) },
        )
    }
}
