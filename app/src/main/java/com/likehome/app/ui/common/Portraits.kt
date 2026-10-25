package com.likehome.app.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.likehome.app.core.Appearance
import com.likehome.app.core.Character
import com.likehome.app.core.Portrait
import com.likehome.app.core.PortraitPick
import com.likehome.app.core.PortraitSource
import com.likehome.app.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Natural pictures of the characters. The user's own photos (Settings › Use your own
 * photos) come first, then art bundled in `assets/characters/<character>/<level>.webp`
 * (or a subfolder per [Appearance]), and the drawn face when neither exists.
 * See docs/character-art-brief.md.
 */
object Portraits {

    private val extensions = listOf("webp", "png", "jpg")

    private val _version = MutableStateFlow(0)
    /** Bumped whenever the user adds or removes a photo, so faces redraw. */
    val version: StateFlow<Int> = _version.asStateFlow()

    private val cache = LruCache<String, Bitmap>(24)
    private val assetLevels = mutableMapOf<String, Map<Int, String>>()

    private fun dir(context: Context) = File(context.filesDir, "faces").apply { mkdirs() }

    fun userFile(context: Context, character: Character, level: Int) =
        File(dir(context), "${character.name.lowercase()}_$level.jpg")

    fun userLevels(context: Context, character: Character): Set<Int> =
        (0..3).filter { userFile(context, character, it).length() > 0 }.toSet()

    /** The user's chosen appearance for [character], unless [appearance] asks for another. */
    private fun appearanceOf(context: Context, character: Character, appearance: Appearance?) =
        appearance ?: SettingsRepository.get(context).current.appearanceOf(character)

    /** Bundled art in [folder], by level; read once from the assets folder. */
    private fun bundled(context: Context, folder: String): Map<Int, String> = synchronized(assetLevels) {
        assetLevels.getOrPut(folder) {
            val files = runCatching { context.assets.list(folder)?.toList() }.getOrNull().orEmpty()
            (0..3).mapNotNull { level ->
                extensions.firstOrNull { "$level.$it" in files }?.let { level to "$folder/$level.$it" }
            }.toMap()
        }
    }

    private fun folder(context: Context, character: Character, appearance: Appearance?) =
        Appearance.assetFolder(character, appearanceOf(context, character, appearance))

    /** Picks [character]'s picture at drama [level]; [appearance] defaults to the user's choice. */
    fun pick(context: Context, character: Character, level: Int, appearance: Appearance? = null): Portrait =
        PortraitPick.pick(
            level.coerceIn(0, 3),
            userLevels(context, character),
            bundled(context, folder(context, character, appearance)).keys,
        )

    /** The picture for [portrait], decoded at about [size] px; null for the drawn face. */
    fun load(context: Context, character: Character, portrait: Portrait, size: Int = 512, appearance: Appearance? = null): Bitmap? {
        val level = portrait.level ?: return null
        val folder = folder(context, character, appearance)
        val key = "${portrait.source}/${character.name}/$folder/$level/${_version.value}/$size"
        cache.get(key)?.let { return it }
        val bitmap = when (portrait.source) {
            PortraitSource.USER -> decodeFile(userFile(context, character, level), size)
            PortraitSource.BUNDLED -> bundled(context, folder)[level]?.let { path ->
                runCatching { context.assets.open(path).use { BitmapFactory.decodeStream(it) } }.getOrNull()
            }
            PortraitSource.DRAWN -> null
        }
        bitmap?.let { cache.put(key, it) }
        return bitmap
    }

    /** Saves a cropped square photo for [character] at drama [level]. */
    fun save(context: Context, character: Character, level: Int, square: Bitmap) {
        userFile(context, character, level).outputStream().use { square.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        changed()
    }

    fun delete(context: Context, character: Character, level: Int) {
        userFile(context, character, level).delete()
        changed()
    }

    private fun changed() {
        cache.evictAll()
        _version.value += 1
    }

    private fun decodeFile(file: File, target: Int): Bitmap? {
        if (file.length() <= 0) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= target) sample *= 2
        return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}
