package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FamilyLinkTest {

    @Test fun acceptsTheWaysPeopleTypeNumbers() {
        assertEquals("919876543210", FamilyLink.normalise("+91 98765 43210"))
        assertEquals("919876543210", FamilyLink.normalise("0091-98765-43210"))
        assertEquals("5511987654321", FamilyLink.normalise(" +55 (11) 98765-4321 "))
        assertEquals("4915112345678", FamilyLink.normalise("+49 151 12345678"))
    }

    @Test fun rejectsNumbersWithoutACountryCode() {
        assertNull(FamilyLink.normalise("098765 43210"))
        assertNull(FamilyLink.normalise("12345"))
        assertNull(FamilyLink.normalise(""))
    }

    @Test fun buildsAnEncodedWhatsAppLink() {
        assertEquals(
            "https://wa.me/919876543210?text=I%27m%20up%21%20%F0%9F%92%AA",
            FamilyLink.whatsApp("+91 98765 43210", "I'm up! 💪"),
        )
        assertNull(FamilyLink.whatsApp("123", "hi"))
    }
}
