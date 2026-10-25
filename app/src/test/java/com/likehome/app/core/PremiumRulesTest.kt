package com.likehome.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumRulesTest {
    @Test fun nothingIsLockedYet() {
        assertTrue(PremiumRules.locked.isEmpty())
        PremiumFeature.entries.forEach { assertTrue(PremiumRules.unlocked(it, premium = false)) }
    }

    @Test fun lockedFeaturesNeedPremium() {
        val locked = setOf(PremiumFeature.VOICE_PACKS)
        assertFalse(PremiumRules.unlocked(PremiumFeature.VOICE_PACKS, premium = false, lockedSet = locked))
        assertTrue(PremiumRules.unlocked(PremiumFeature.VOICE_PACKS, premium = true, lockedSet = locked))
        assertTrue(PremiumRules.unlocked(PremiumFeature.GENTLE_WAKE, premium = false, lockedSet = locked))
    }
}
