package com.likehome.app.core

/** Who wakes the user. Each has their own lines, face and way of losing patience. */
enum class Character(val displayName: String, val detail: String) {
    MOM("Mummy", "Loving, dramatic, never gives up"),
    PAPA("Papa", "Few words. Very disappointed ones."),
    DADI("Dadi", "Halwa, blessings and your childhood stories"),
    BOSS("The Boss", "Calendar invites at 7 am"),
    ABUELA("Abuela", "Café con leche, and the chancla"),
}
