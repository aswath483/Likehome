package com.likehome.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.likehome.app.alarm.AlarmScheduler
import com.likehome.app.core.BackupFormat
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * A backup file the user keeps or moves to a new phone: alarms, settings, mornings, and
 * the photos and voice clips they made (see [BackupFormat]). Restoring replaces what's
 * on this phone and restarts the app so every screen reloads it.
 */
object Backup {

    private const val SETTINGS_ENTRY = "likehome.json"
    /** Voice clips are small, but a broken file shouldn't fill the phone. */
    private const val MAX_ENTRY_BYTES = 20L * 1024 * 1024

    fun fileName(today: java.time.LocalDate = java.time.LocalDate.now()) = "LikeHome-backup-$today.zip"

    fun export(context: Context, target: Uri): Boolean = runCatching {
        val prefs = BackupFormat.PREFS.associateWith { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE).all
        }
        context.contentResolver.openOutputStream(target)!!.use { out ->
            ZipOutputStream(out).use { zip ->
                zip.putNextEntry(ZipEntry(SETTINGS_ENTRY))
                zip.write(BackupFormat.encode(prefs).toByteArray())
                zip.closeEntry()
                BackupFormat.FOLDERS.forEach { folder ->
                    File(context.filesDir, folder).listFiles()?.filter { it.isFile }?.forEach { file ->
                        zip.putNextEntry(ZipEntry("$folder/${file.name}"))
                        file.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
        }
        true
    }.getOrDefault(false)

    /**
     * Reads [source] fully into a staging folder first, so a broken or foreign file changes
     * nothing. False if it isn't a Like Home backup.
     */
    fun restore(context: Context, source: Uri): Boolean {
        val staging = File(context.cacheDir, "restore").apply { deleteRecursively(); mkdirs() }
        try {
            var settings: Map<String, Map<String, Any>>? = null
            context.contentResolver.openInputStream(source)?.use { input ->
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        when {
                            entry.isDirectory -> Unit
                            entry.name == SETTINGS_ENTRY -> settings = BackupFormat.decode(readLimited(zip).decodeToString())
                            BackupFormat.safeEntry(entry.name) -> File(staging, entry.name).apply { parentFile?.mkdirs() }.writeBytes(readLimited(zip))
                        }
                    }
                }
            } ?: return false
            val prefs = settings ?: return false

            // The old alarms may not exist in the backup: disarm them before replacing.
            AlarmRepository.get(context).alarms.value.forEach { AlarmScheduler.cancel(context, it.id) }
            prefs.forEach { (name, values) ->
                val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
                values.forEach { (key, value) ->
                    @Suppress("UNCHECKED_CAST")
                    when (value) {
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Float -> editor.putFloat(key, value)
                        is String -> editor.putString(key, value)
                        is Set<*> -> editor.putStringSet(key, value as Set<String>)
                    }
                }
                editor.commit()
            }
            BackupFormat.FOLDERS.forEach { folder ->
                val target = File(context.filesDir, folder).apply { deleteRecursively(); mkdirs() }
                File(staging, folder).listFiles()?.forEach { it.copyTo(File(target, it.name), overwrite = true) }
            }
            return true
        } catch (e: Exception) {
            return false
        } finally {
            staging.deleteRecursively()
        }
    }

    private fun readLimited(zip: ZipInputStream): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0L
        while (true) {
            val n = zip.read(buffer)
            if (n < 0) break
            total += n
            if (total > MAX_ENTRY_BYTES) throw IllegalStateException("entry too large")
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }

    /** Starts the app fresh so every repository reads the restored data; alarms re-arm on launch. */
    fun restartApp(context: Context) {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        Runtime.getRuntime().exit(0)
    }
}
