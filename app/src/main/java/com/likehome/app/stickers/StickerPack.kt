package com.likehome.app.stickers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

/** The bundled "Like Home" WhatsApp sticker pack (drawn by tools/make_stickers.py). */
object StickerPack {
    const val ID = "likehome"
    const val NAME = "Like Home: Mom says"
    const val PUBLISHER = "Like Home"
    const val TRAY = "tray.png"
    const val ASSET_DIR = "stickers/$ID"
    /** Bump when the images change so WhatsApp refreshes its copy. */
    const val IMAGE_DATA_VERSION = "1"

    fun authority(context: Context) = "${context.packageName}.stickercontentprovider"

    /** File name to comma-separated emojis, from the pack's contents.txt. */
    fun stickers(context: Context): List<Pair<String, String>> = runCatching {
        context.assets.open("$ASSET_DIR/contents.txt").bufferedReader().readLines()
            .mapNotNull { line -> line.split('|').takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() } }
    }.getOrDefault(emptyList())

    /** Opens WhatsApp's "Add sticker pack" screen; a friendly message when WhatsApp isn't installed. */
    fun addToWhatsApp(context: Context) {
        val intent = Intent("com.whatsapp.intent.action.ENABLE_STICKER_PACK")
            .putExtra("sticker_pack_id", ID)
            .putExtra("sticker_pack_authority", authority(context))
            .putExtra("sticker_pack_name", NAME)
        com.likehome.app.Telemetry.event(context, "sticker_add")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Install WhatsApp to use Mom's stickers.", Toast.LENGTH_LONG).show()
        }
    }
}
