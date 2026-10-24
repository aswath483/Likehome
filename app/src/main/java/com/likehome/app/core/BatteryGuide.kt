package com.likehome.app.core

/**
 * Phone makers' battery savers are the main reason alarms fail in India. Each guide
 * says, in the brand's own menu words, how to let Like Home run, and lists the
 * maker's "autostart" screens to try opening (they move between OS versions).
 */
data class BrandGuide(
    val brand: String,
    val steps: List<String>,
    /** Package and activity class names, tried in order. */
    val autostartScreens: List<Pair<String, String>> = emptyList(),
)

object BatteryGuide {

    private val xiaomi = BrandGuide(
        brand = "Xiaomi, Redmi or POCO",
        steps = listOf(
            "Settings › Apps › Manage apps › Like Home › Autostart: turn on",
            "Same page › Battery saver › No restrictions",
            "Open recent apps, long-press Like Home and tap the lock icon",
        ),
        autostartScreens = listOf(
            "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
        ),
    )

    private val samsung = BrandGuide(
        brand = "Samsung",
        steps = listOf(
            "Settings › Apps › Like Home › Battery › Unrestricted",
            "Settings › Battery › Background usage limits › Never sleeping apps › add Like Home",
        ),
    )

    private val vivo = BrandGuide(
        brand = "Vivo or iQOO",
        steps = listOf(
            "Settings › Battery › Background power consumption management › Like Home: Allow",
            "i Manager › App manager › Autostart manager › Like Home: on",
        ),
        autostartScreens = listOf(
            "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
        ),
    )

    private val oppo = BrandGuide(
        brand = "Oppo, Realme or OnePlus",
        steps = listOf(
            "Settings › Apps › Like Home › Battery usage › Allow background activity: on",
            "Same page › Allow auto launch: on",
            "Open recent apps, tap the menu on Like Home's card and choose Lock",
        ),
        autostartScreens = listOf(
            "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
            "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
        ),
    )

    private val huawei = BrandGuide(
        brand = "Huawei or Honor",
        steps = listOf(
            "Settings › Battery › App launch › Like Home: Manage manually",
            "Turn on Auto-launch, Secondary launch and Run in background",
        ),
        autostartScreens = listOf(
            "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        ),
    )

    private val generic = BrandGuide(
        brand = "your phone",
        steps = listOf(
            "Settings › Apps › Like Home › Battery › Unrestricted",
        ),
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
