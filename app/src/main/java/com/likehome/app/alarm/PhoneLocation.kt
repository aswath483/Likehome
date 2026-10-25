package com.likehome.app.alarm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * A rough location (about 1 km) for prayer times: the newest one the phone already knows,
 * or one fresh network fix. Nothing is kept but the rounded coordinates in settings.
 */
object PhoneLocation {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun get(context: Context, onResult: (Pair<Double, Double>?) -> Unit) {
        if (!hasPermission(context)) return onResult(null)
        val lm = context.getSystemService(LocationManager::class.java) ?: return onResult(null)
        val known = runCatching {
            lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time }
        }.getOrNull()
        if (known != null) return onResult(rounded(known))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            runCatching {
                lm.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, ContextCompat.getMainExecutor(context)) { onResult(it?.let(::rounded)) }
            }.onFailure { onResult(null) }
        } else {
            onResult(null)
        }
    }

    private fun rounded(l: Location) = round(l.latitude) to round(l.longitude)
    private fun round(d: Double) = (d * 100).roundToInt() / 100.0
}
