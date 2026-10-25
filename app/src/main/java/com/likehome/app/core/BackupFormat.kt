package com.likehome.app.core

import org.json.JSONArray
import org.json.JSONObject

/**
 * The settings part of a Like Home backup: chosen SharedPreferences files as JSON, with
 * each value's type kept so it restores exactly. Photos and voice clips travel beside it
 * in the same zip.
 */
object BackupFormat {

    const val VERSION = 1

    /** The preferences worth moving to a new phone. Pending snoozes and armed times are not. */
    val PREFS = listOf("alarms", "settings", "history", "important")

    /** The folders in the app's files directory that hold the user's photos and voice clips. */
    val FOLDERS = listOf("faces", "voice", "photos")

    fun encode(prefs: Map<String, Map<String, Any?>>): String {
        val root = JSONObject().put("version", VERSION)
        val all = JSONObject()
        prefs.forEach { (file, values) ->
            val o = JSONObject()
            values.forEach { (key, value) ->
                val typed = when (value) {
                    is Boolean -> JSONObject().put("b", value)
                    is Int -> JSONObject().put("i", value)
                    is Long -> JSONObject().put("l", value)
                    is Float -> JSONObject().put("f", value.toDouble())
                    is String -> JSONObject().put("s", value)
                    is Set<*> -> JSONObject().put("ss", JSONArray(value.filterIsInstance<String>()))
                    else -> null
                }
                if (typed != null) o.put(key, typed)
            }
            all.put(file, o)
        }
        return root.put("prefs", all).toString()
    }

    /** Null when the text isn't a backup this version can read. */
    fun decode(text: String): Map<String, Map<String, Any>>? = runCatching {
        val root = JSONObject(text)
        if (root.optInt("version", 0) !in 1..VERSION) return null
        val all = root.getJSONObject("prefs")
        all.keys().asSequence().filter { it in PREFS }.associateWith { file ->
            val o = all.getJSONObject(file)
            o.keys().asSequence().mapNotNull { key ->
                val t = o.getJSONObject(key)
                val value: Any? = when {
                    t.has("b") -> t.getBoolean("b")
                    t.has("i") -> t.getInt("i")
                    t.has("l") -> t.getLong("l")
                    t.has("f") -> t.getDouble("f").toFloat()
                    t.has("s") -> t.getString("s")
                    t.has("ss") -> t.getJSONArray("ss").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
                    else -> null
                }
                value?.let { key to it }
            }.toMap()
        }
    }.getOrNull()

    /** Zip entry names are checked so a crafted file can't write outside the app's folders. */
    fun safeEntry(name: String): Boolean {
        val parts = name.split('/')
        return parts.size == 2 && parts[0] in FOLDERS && parts[1].isNotEmpty() &&
            parts[1] != "." && parts[1] != ".." && !parts[1].contains('\\')
    }
}
