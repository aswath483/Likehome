package com.likehome.app.core

/** Things that could sit behind Like Home Premium. Which ones do is [PremiumRules.locked]. */
enum class PremiumFeature {
    EXTRA_STYLES,
    EXTRA_CHARACTERS,
    VOICE_PACKS,
    OWN_VOICE,
    SLEEP_INSIGHTS,
    GENTLE_WAKE,
    STREAK_CHALLENGE,
    LIVE_FAMILY,
}

/**
 * The paid tier's rules. Purchases run through RevenueCat: buying anything that grants the
 * [ENTITLEMENT] entitlement makes the user premium.
 */
object PremiumRules {
    /** The entitlement id to create in the RevenueCat dashboard and attach products to. */
    const val ENTITLEMENT = "premium"

    /**
     * Features that need premium. Empty for now: everything is free and buying is a way to
     * support the app. Add features here to lock them (screens check [unlocked]).
     */
    val locked: Set<PremiumFeature> = emptySet()

    fun unlocked(feature: PremiumFeature, premium: Boolean, lockedSet: Set<PremiumFeature> = locked): Boolean =
        premium || feature !in lockedSet
}
