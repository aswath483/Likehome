package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryGuideTest {

    @Test
    fun picksTheBrandFromManufacturerOrBrand() {
        assertEquals("Xiaomi, Redmi or POCO", BatteryGuide.forManufacturer("Xiaomi", "Redmi").brand)
        assertEquals("Xiaomi, Redmi or POCO", BatteryGuide.forManufacturer("Xiaomi", "POCO").brand)
        assertEquals("Samsung", BatteryGuide.forManufacturer("samsung").brand)
        assertEquals("Vivo or iQOO", BatteryGuide.forManufacturer("vivo", "iQOO").brand)
        assertEquals("Oppo, Realme or OnePlus", BatteryGuide.forManufacturer("realme").brand)
        assertEquals("Oppo, Realme or OnePlus", BatteryGuide.forManufacturer("OnePlus").brand)
        assertEquals("Huawei or Honor", BatteryGuide.forManufacturer("HONOR").brand)
    }

    @Test
    fun unknownPhonesGetTheGenericGuide() {
        val g = BatteryGuide.forManufacturer("Google", "google")
        assertEquals("your phone", g.brand)
        assertTrue(g.steps.isNotEmpty())
        assertTrue(g.autostartScreens.isEmpty())
    }
}
