package com.likehome.app.core

/**
 * Phone makers' battery savers are the main reason alarms fail. Each guide's brand name
 * and steps (in the brand's own menu words) are the string resources named after [key];
 * it also lists the maker's "autostart" screens to try opening (they move between OS
 * versions).
 */
data class BrandGuide(
    val key: String,
    /** Package and activity class names, tried in order. */
    val autostartScreens: List<Pair<String, String>> = emptyList(),
)

object BatteryGuide {

    private val xiaomi = BrandGuide(
        key = "xiaomi",
        autostartScreens = listOf(
            "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
        ),
    )

    private val samsung = BrandGuide(
        key = "samsung",
    )

    private val vivo = BrandGuide(
        key = "vivo",
        autostartScreens = listOf(
            "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
        ),
    )

    private val oppo = BrandGuide(
        key = "oppo",
        autostartScreens = listOf(
            "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
            "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
        ),
    )

    private val huawei = BrandGuide(
        key = "huawei",
        autostartScreens = listOf(
            "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        ),
    )

    private val generic = BrandGuide(
        key = "generic",
    )

    /** The guide for a [android.os.Build.MANUFACTURER] / BRAND value. */
    fun forManufacturer(manufacturer: String, brand: String = manufacturer): BrandGuide {
        val names = "$manufacturer $brand".lowercase()
        return when {
            listOf("xiaomi", "redmi", "poco").any { it in names } -> xiaomi
            "samsung" in names -> samsung
            listOf("vivo", "iqoo").any { it in names } -> vivo
            listOf("oppo", "realme", "oneplus").any { it in names } -> oppo
            listOf("huawei", "honor").any { it in names } -> huawei
            else -> generic
        }
    }
}
