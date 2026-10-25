package com.likehome.app

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.likehome.app.mom.MomLines
import com.likehome.app.mom.RemoteFestivals

/**
 * Analytics, crash reports and Remote Config, all through Firebase, and all silent when
 * the app is built without a google-services.json: then every call here does nothing.
 */
object Telemetry {

    private const val KEY_FESTIVALS = "festival_lines"

    private fun ready(context: Context) = runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)

    /** Called once at app start: applies cached festival lines, then fetches fresh ones. */
    fun start(context: Context) {
        val prefs = context.getSharedPreferences("remote", Context.MODE_PRIVATE)
        MomLines.remoteFestivals = RemoteFestivals.parse(prefs.getString(KEY_FESTIVALS, null))
        if (!ready(context)) return
        runCatching {
            val config = FirebaseRemoteConfig.getInstance()
            config.fetchAndActivate().addOnCompleteListener {
                val json = config.getString(KEY_FESTIVALS)
                prefs.edit().putString(KEY_FESTIVALS, json).apply()
                MomLines.remoteFestivals = RemoteFestivals.parse(json)
            }
        }
    }

    /** Logs an event; values may be strings, numbers or booleans. */
    fun event(context: Context, name: String, vararg params: Pair<String, Any>) {
        if (!ready(context)) return
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is Int -> putLong(key, value.toLong())
                    is Long -> putLong(key, value)
                    is Boolean -> putString(key, value.toString())
                    else -> putString(key, value.toString())
                }
            }
        }
        runCatching { FirebaseAnalytics.getInstance(context).logEvent(name, bundle) }
    }
}
