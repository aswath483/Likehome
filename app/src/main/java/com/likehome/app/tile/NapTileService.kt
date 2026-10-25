package com.likehome.app.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.text.format.DateFormat
import com.likehome.app.alarm.Nap
import com.likehome.app.core.ClockFormat

/** Quick Settings tile: tap for a 20-minute power nap, tap again to cancel it. */
class NapTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        render()
    }

    override fun onClick() {
        super.onClick()
        Nap.toggle(this)
        render()
    }

    private fun render() {
        val tile = qsTile ?: return
        val nap = Nap.current(this)
        tile.state = if (nap != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = nap?.let { "Until ${ClockFormat.full(it.time, DateFormat.is24HourFormat(this))}" } ?: "${Nap.MINUTES} min"
        }
        tile.updateTile()
    }
}
