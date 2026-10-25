package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupFormatTest {

    @Test fun roundTripKeepsEveryType() {
        val prefs = mapOf(
            "settings" to mapOf("onboarded" to true, "bedtime_hour" to 23, "first" to 1_793_000_000_000L, "vol" to 0.5f, "language" to "SPANISH", "tags" to setOf("a", "b")),
            "alarms" to mapOf("alarms_json" to "[{\"id\":1}]"),
        )
        assertEquals(prefs, BackupFormat.decode(BackupFormat.encode(prefs)))
    }

    @Test fun ignoresFilesItDoesNotRestore() {
        val text = BackupFormat.encode(mapOf("snoozes" to mapOf("1" to "x"), "history" to mapOf("k" to "v")))
        assertEquals(setOf("history"), BackupFormat.decode(text)!!.keys)
    }

    @Test fun rejectsJunkAndFutureVersions() {
        assertNull(BackupFormat.decode("not json"))
        assertNull(BackupFormat.decode("{\"version\":99,\"prefs\":{}}"))
    }

    @Test fun zipEntriesCannotEscape() {
        assertTrue(BackupFormat.safeEntry("faces/mom_0.jpg"))
        assertTrue(BackupFormat.safeEntry("voice/papa_3.m4a"))
        assertFalse(BackupFormat.safeEntry("../shared_prefs/x.xml"))
        assertFalse(BackupFormat.safeEntry("faces/../../x"))
        assertFalse(BackupFormat.safeEntry("faces/.."))
        assertFalse(BackupFormat.safeEntry("databases/x"))
        assertFalse(BackupFormat.safeEntry("faces/a/b"))
    }
}
