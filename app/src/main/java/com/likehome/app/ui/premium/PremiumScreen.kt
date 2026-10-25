package com.likehome.app.ui.premium

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.likehome.app.R
import com.likehome.app.billing.Premium
import com.likehome.app.ui.common.Card
import com.likehome.app.ui.common.PageScaffold
import com.likehome.app.ui.common.SecondaryButton
import com.likehome.app.ui.theme.LH
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

/**
 * Like Home Premium. Not premium yet: RevenueCat's paywall, showing the current offering
 * from its dashboard (products, prices, design), with restore built in. Premium: a thank-you,
 * plus restore and "manage subscription".
 */
@Composable
fun PremiumScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { Premium.refresh(context) }
    val state by Premium.state.collectAsState()

    if (!state.premium) {
        val options = remember {
            PaywallOptions.Builder(dismissRequest = onBack)
                .setShouldDisplayDismissButton(true)
                .setListener(object : PaywallListener {
                    override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) =
                        Premium.update(context, customerInfo)

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) = Premium.update(context, customerInfo)
                })
                .build()
        }
        BackHandler(onBack = onBack)
        Box(Modifier.fillMaxSize().background(LH.Espresso).safeDrawingPadding()) {
            Paywall(options)
        }
        return
    }

    PageScaffold(title = stringResource(R.string.premium_title), onBack = onBack) {
        Card {
            Text(stringResource(R.string.premium_thanks_title), style = MaterialTheme.typography.headlineSmall, color = LH.Saffron)
            Text(stringResource(R.string.premium_thanks_body), style = MaterialTheme.typography.bodyMedium, color = LH.Oat)
        }
        state.manageUrl?.let { url ->
            SecondaryButton(stringResource(R.string.premium_manage), onClick = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            })
        }
        TextButton(onClick = {
            Premium.restore(context) { premium ->
                val text = when (premium) {
                    true -> R.string.premium_restored
                    false -> R.string.premium_nothing_to_restore
                    null -> R.string.premium_restore_failed
                }
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
        }) { Text(stringResource(R.string.premium_restore), color = LH.Saffron) }
    }
}
