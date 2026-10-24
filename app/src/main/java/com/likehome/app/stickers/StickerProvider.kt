package com.likehome.app.stickers

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Serves the sticker pack to WhatsApp, following its sticker app contract: pack metadata,
 * the sticker list with emojis, and the image files from `assets/stickers/<pack>/`.
 */
class StickerProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, args: Array<String>?, sortOrder: String?): Cursor? {
        val context = context ?: return null
        val segments = uri.pathSegments
        return when (segments.firstOrNull()) {
            METADATA -> if (segments.size == 1 || segments[1] == StickerPack.ID) metadata(context) else MatrixCursor(METADATA_COLUMNS)
            STICKERS -> if (segments.getOrNull(1) == StickerPack.ID) stickers(context) else MatrixCursor(STICKER_COLUMNS)
            else -> null
        }
    }

    private fun metadata(context: Context) = MatrixCursor(METADATA_COLUMNS).apply {
        addRow(
            arrayOf<Any>(
                StickerPack.ID, StickerPack.NAME, StickerPack.PUBLISHER, StickerPack.TRAY,
                "https://play.google.com/store/apps/details?id=${context.packageName}", "",
                "", "", "", "",
                StickerPack.IMAGE_DATA_VERSION, 0, 0,
            )
        )
    }

    private fun stickers(context: Context) = MatrixCursor(STICKER_COLUMNS).apply {
        StickerPack.stickers(context).forEach { (file, emojis) -> addRow(arrayOf<Any>(file, emojis)) }
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val segments = uri.pathSegments
        if (segments.size != 3 || segments[0] != STICKERS_ASSET || segments[1] != StickerPack.ID) return null
        val name = segments[2]
        // Only files that belong to the pack; never a path outside it.
        if (name.contains('/') || name.contains("..")) return null
        return context?.assets?.openFd("${StickerPack.ASSET_DIR}/$name")
    }

    override fun getType(uri: Uri): String? {
        val authority = uri.authority
        val segments = uri.pathSegments
        return when (segments.firstOrNull()) {
            METADATA -> if (segments.size == 1) "vnd.android.cursor.dir/vnd.$authority.$METADATA" else "vnd.android.cursor.item/vnd.$authority.$METADATA"
            STICKERS -> "vnd.android.cursor.dir/vnd.$authority.$STICKERS"
            STICKERS_ASSET -> if (segments.lastOrNull()?.endsWith(".png") == true) "image/png" else "image/webp"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException("Read only")
    override fun delete(uri: Uri, selection: String?, args: Array<String>?): Int = throw UnsupportedOperationException("Read only")
    override fun update(uri: Uri, values: ContentValues?, selection: String?, args: Array<String>?): Int =
        throw UnsupportedOperationException("Read only")

    private companion object {
        const val METADATA = "metadata"
        const val STICKERS = "stickers"
        const val STICKERS_ASSET = "stickers_asset"

        val METADATA_COLUMNS = arrayOf(
            "sticker_pack_identifier", "sticker_pack_name", "sticker_pack_publisher", "sticker_pack_icon",
            "android_play_store_link", "ios_app_download_link",
            "sticker_pack_publisher_email", "sticker_pack_publisher_website",
            "sticker_pack_privacy_policy_website", "sticker_pack_license_agreement_website",
            "image_data_version", "whatsapp_will_not_cache_stickers", "animated_sticker_pack",
        )
        val STICKER_COLUMNS = arrayOf("sticker_file_name", "sticker_emoji")
    }
}
