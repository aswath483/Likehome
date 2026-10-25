package com.likehome.app.ui.ring

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import com.likehome.app.core.ImageMatch
import java.io.File

/** Reference and morning photos for the photo check, kept in the app's private storage. */
object PhotoStore {

    private fun dir(context: Context) = File(context.filesDir, "photos").apply { mkdirs() }

    fun file(context: Context, name: String) = File(dir(context), name)

    /** A fresh file for the camera to write into. */
    fun newFile(context: Context, prefix: String) = File(dir(context), "$prefix-${System.currentTimeMillis()}.jpg")

    fun uri(context: Context, file: File): Uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)

    /** A small preview for the editor; null when the file is missing or unreadable. */
    fun thumbnail(context: Context, name: String, size: Int = 160): Bitmap? =
        name.takeIf { it.isNotBlank() }?.let { decode(file(context, it), size) }

    /**
     * True when [photo] shows the same place as [reference]. Phone cameras may store
     * photos rotated, so every quarter turn of the new photo is tried.
     */
    fun matches(reference: File, photo: File): Boolean {
        val ref = decode(reference, 256)?.let(::signature) ?: return false
        val shot = decode(photo, 256) ?: return false
        val best = listOf(0f, 90f, 180f, 270f).maxOf { angle ->
            val (hash, hist) = signature(rotate(shot, angle))
            ImageMatch.similarity(ref.first, ref.second, hash, hist)
        }
        return best >= ImageMatch.THRESHOLD
    }

    private fun signature(bitmap: Bitmap): Pair<Long, FloatArray> {
        val small = Bitmap.createScaledBitmap(bitmap, ImageMatch.HASH_W, ImageMatch.HASH_H, true)
        val grey = IntArray(ImageMatch.HASH_W * ImageMatch.HASH_H) { i ->
            val c = small.getPixel(i % ImageMatch.HASH_W, i / ImageMatch.HASH_W)
            (Color.red(c) * 299 + Color.green(c) * 587 + Color.blue(c) * 114) / 1000
        }
        val colour = Bitmap.createScaledBitmap(bitmap, 24, 24, true)
        val pixels = IntArray(24 * 24).also { colour.getPixels(it, 0, 24, 0, 0, 24, 24) }
        return ImageMatch.dHash(grey) to ImageMatch.histogram(pixels)
    }

    private fun rotate(bitmap: Bitmap, angle: Float): Bitmap =
        if (angle == 0f) bitmap
        else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(angle) }, true)

    /** Decodes at roughly [target] px on the long side, never loading the full photo. */
    private fun decode(file: File, target: Int): Bitmap? {
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        if (longest <= 0) return null
        var sample = 1
        while (longest / (sample * 2) >= target) sample *= 2
        return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}
