package com.likehome.app.core

/**
 * Who wakes the user. Each has their own lines, face and way of losing patience.
 * Characters with a [defaultAppearance] can be shown in any [Appearance]; Dadi and
 * Abuela belong to one culture by design.
 */
enum class Character(val displayName: String, val detail: String, val defaultAppearance: Appearance?) {
    MOM("Mummy", "Loving, dramatic, never gives up", Appearance.SOUTH_ASIAN),
    PAPA("Papa", "Few words. Very disappointed ones.", Appearance.SOUTH_ASIAN),
    DADI("Dadi", "Halwa, blessings and your childhood stories", null),
    BOSS("The Boss", "Calendar invites at 7 am", Appearance.AFRICAN),
    ABUELA("Abuela", "Café con leche, and the chancla", null),
}
