package com.likehome.app.core

/**
 * Who wakes the user. Each has their own lines, face and way of losing patience.
 * Characters with a [defaultAppearance] can be shown in any [Appearance]; Dadi and
 * Abuela belong to one culture by design.
 */
enum class Character(val defaultAppearance: Appearance?) {
    MOM(Appearance.SOUTH_ASIAN),
    PAPA(Appearance.SOUTH_ASIAN),
    DADI(null),
    BOSS(Appearance.AFRICAN),
    ABUELA(null),
}
