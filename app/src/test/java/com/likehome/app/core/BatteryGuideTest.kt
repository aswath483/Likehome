package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryGuideTest {

    @Test
    fun picksTheBrandFromManufacturerOrBrand() {
        assertEquals("xiaomi", BatteryGuide.forManufacturer("Xiaomi", "Redmi").key)
        assertEquals("xiaomi", BatteryGuide.forManufacturer("Xiaomi", "POCO").key)
        assertEquals("samsung", BatteryGuide.forManufacturer("samsung").key)
        assertEquals("vivo", BatteryGuide.forManufacturer("vivo", "iQOO").key)
        assertEquals("oppo", BatteryGuide.forManufacturer("realme").key)
        assertEquals("oppo", BatteryGuide.forManufacturer("OnePlus").key)
        assertEquals("huawei", BatteryGuide.forManufacturer("HONOR").key)
    }

    @Test
    fun unknownPhonesGetTheGenericGuide() {
        val g = BatteryGuide.forManufacturer("Google", "google")
        assertEquals("generic", g.key)
        assertTrue(g.autostartScreens.isEmpty())
    }
}
