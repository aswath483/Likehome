package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FamilySyncTest {

    @Test fun codesAreSixDigits() {
        repeat(50) { assertTrue(Regex("\\d{6}").matches(PairCode.generate(Random(it)))) }
    }

    @Test fun typedCodesAreCleanedUp() {
        assertEquals("482913", PairCode.normalise(" 482 913 "))
        assertEquals("482913", PairCode.normalise("482-913"))
        assertNull(PairCode.normalise("48291"))
        assertNull(PairCode.normalise("4829134"))
    }

    @Test fun alarmsRoundTripThroughTheCloudShape() {
        val alarm = FamilyAlarm("abc", 6, 30, setOf(1, 2, 3, 4, 5), "College", true, WakeTask.STEPS, RingStyle.COMIC, Character.PAPA)
        assertEquals(alarm, FamilyAlarm.fromMap("abc", alarm.toMap()))
    }

    @Test fun firestoreNumbersArriveAsLongs() {
        val map = mapOf<String, Any?>("hour" to 7L, "minute" to 5L, "days" to listOf(6L, 7L))
        assertEquals(FamilyAlarm("x", 7, 5, setOf(6, 7)), FamilyAlarm.fromMap("x", map))
    }

    @Test fun badDocumentsAreSkippedOrRepaired() {
        assertNull(FamilyAlarm.fromMap("x", mapOf("hour" to 25, "minute" to 0)))
        assertNull(FamilyAlarm.fromMap("x", mapOf("minute" to 0)))
        val odd = FamilyAlarm.fromMap("x", mapOf("hour" to 8, "minute" to 0, "task" to "FUTURE_TASK", "days" to listOf(9, 3), "label" to "x".repeat(99)))!!
        assertEquals(WakeTask.MATH, odd.task)
        assertEquals(setOf(3), odd.days)
        assertEquals(40, odd.label.length)
    }
}
