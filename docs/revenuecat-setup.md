# Like Home Premium: RevenueCat setup

The app side is done. Purchases, restore, the paywall and the premium status all work
once these steps are finished. Nothing is locked yet: buying makes the user premium, and
`PremiumRules.locked` decides what that unlocks (see the end).

| What | Where |
|---|---|
| RevenueCat wrapper | `app/src/main/java/com/likehome/app/billing/Premium.kt` |
| Paywall screen | `app/src/main/java/com/likehome/app/ui/premium/PremiumScreen.kt` |
| What premium unlocks | `app/src/main/java/com/likehome/app/core/PremiumFeature.kt` |

## 1. Google Play Console

1. Upload a build to a testing track (internal testing is enough). Play only allows
   products once the app has been uploaded with billing in it.
2. **Monetize > Products**: create your subscriptions and/or one-time products, for
   example `premium_monthly`, `premium_yearly`, or `premium_lifetime`. Activate them.
3. Add your Google account under **Setup > License testing** so test purchases are free.

## 2. RevenueCat dashboard

1. Create a project and add a **Google Play** app with package name `com.likehome.app`.
2. Upload the Play service account JSON RevenueCat asks for (it explains how to make
   one), so RevenueCat can check purchases.
3. **Products**: import the Play products from step 1.
4. **Entitlements**: create one with the identifier **`premium`** (exactly that) and
   attach every product that should make someone premium.
5. **Offerings**: create an offering, add packages (monthly, yearly, lifetime) with the
   products, and mark it **Current**. This is what the app shows.
6. **Paywalls**: design a paywall for that offering. The app shows it as is; changes in
   the dashboard reach phones without an app update.

## 3. Give the app its key

Copy the **public Google Play API key** (starts with `goog_`) from
**Project settings > API keys**. It's a public key, made to ship inside apps.

- Local builds: add `revenuecat.apiKey=goog_...` to `local.properties`.
- CI builds: add a GitHub secret named `REVENUECAT_API_KEY`.

Without a key, the Premium row in Settings is hidden and nothing calls RevenueCat.

## 4. Test

Install a build with the key from the testing track, open **Settings > Like Home
Premium**, buy with a license-test account, and check that Settings says you're premium.
Then reinstall and use **Restore purchases**.

## Locking features later

Add features to `PremiumRules.locked` (the `PremiumFeature` list). Screens check
`Premium.unlocked(feature)` before opening a locked feature and open the paywall instead.
