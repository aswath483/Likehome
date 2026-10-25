package com.likehome.app.billing

import android.content.Context
import android.net.Uri
import com.likehome.app.BuildConfig
import com.likehome.app.core.PremiumFeature
import com.likehome.app.core.PremiumRules
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Like Home Premium, through RevenueCat. Off (and hidden) when the app is built without a
 * RevenueCat key. Products, prices and the paywall's look all live in the RevenueCat
 * dashboard: anything that grants the [PremiumRules.ENTITLEMENT] entitlement makes the user
 * premium. The last known state is kept on the phone, so it holds offline and at startup.
 */
object Premium {

    data class State(
        val premium: Boolean = false,
        /** Google Play's page for managing the subscription; null for one-time purchases. */
        val manageUrl: Uri? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    @Volatile private var started = false

    /** True when this build has a RevenueCat key, so the premium screen can be shown. */
    val available: Boolean get() = BuildConfig.REVENUECAT_API_KEY.isNotBlank()

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("premium", Context.MODE_PRIVATE)

    /** Sets up RevenueCat once, at app start. Anonymous: no account or sign-in needed. */
    fun start(context: Context) {
        if (started) return
        started = true
        _state.value = State(premium = prefs(context).getBoolean(KEY_PREMIUM, false))
        if (!available) return
        runCatching {
            Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.WARN
            Purchases.configure(PurchasesConfiguration.Builder(context.applicationContext, BuildConfig.REVENUECAT_API_KEY).build())
            // Called with the cached info right away, then on every purchase, renewal or expiry.
            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { update(context, it) }
        }
    }

    /** Re-reads the customer from RevenueCat, e.g. when the premium screen opens. */
    fun refresh(context: Context) {
        if (!Purchases.isConfigured) return
        Purchases.sharedInstance.getCustomerInfo(callback(context, onDone = {}))
    }

    /** "Restore purchases": brings back what this Google account already bought. */
    fun restore(context: Context, onDone: (Boolean?) -> Unit) {
        if (!Purchases.isConfigured) return onDone(null)
        Purchases.sharedInstance.restorePurchases(callback(context, onDone))
    }

    /** Called by the paywall after a purchase or restore. */
    fun update(context: Context, info: CustomerInfo) {
        val premium = info.entitlements[PremiumRules.ENTITLEMENT]?.isActive == true
        prefs(context).edit().putBoolean(KEY_PREMIUM, premium).apply()
        _state.value = State(premium, info.managementURL)
    }

    /** Whether [feature] can be used now; see [PremiumRules.locked]. */
    fun unlocked(feature: PremiumFeature): Boolean = PremiumRules.unlocked(feature, _state.value.premium)

    /** [onDone] gets the premium state, or null when RevenueCat couldn't be reached. */
    private fun callback(context: Context, onDone: (Boolean?) -> Unit) = object : ReceiveCustomerInfoCallback {
        override fun onReceived(customerInfo: CustomerInfo) {
            update(context, customerInfo)
            onDone(_state.value.premium)
        }

        override fun onError(error: PurchasesError) = onDone(null)
    }

    private const val KEY_PREMIUM = "premium"
}
