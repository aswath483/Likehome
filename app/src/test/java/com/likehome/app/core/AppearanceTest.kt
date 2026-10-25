package com.likehome.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTest {

    @Test fun defaultAppearanceUsesTheCharactersOwnFolder() {
        assertEquals("characters/mom", Appearance.assetFolder(Character.MOM, Appearance.SOUTH_ASIAN))
        assertEquals("characters/mom", Appearance.assetFolder(Character.MOM, null))
        assertEquals("characters/boss", Appearance.assetFolder(Character.BOSS, Appearance.AFRICAN))
    }

    @Test fun otherAppearancesHaveSubfolders() {
        assertEquals("characters/mom/east_asian", Appearance.assetFolder(Character.MOM, Appearance.EAST_ASIAN))
        assertEquals("characters/boss/south_asian", Appearance.assetFolder(Character.BOSS, Appearance.SOUTH_ASIAN))
    }

    @Test fun fixedCharactersIgnoreAppearance() {
        assertEquals("characters/dadi", Appearance.assetFolder(Character.DADI, Appearance.EUROPEAN))
    }

    @Test fun choicesRoundTrip() {
        val choices = mapOf(Character.MOM to Appearance.LATIN, Character.BOSS to Appearance.EAST_ASIAN)
        assertEquals(choices, Appearance.decode(Appearance.encode(choices)))
    }

    @Test fun decodeSkipsJunkAndFixedCharacters() {
        assertEquals(
            mapOf(Character.PAPA to Appearance.AFRICAN),
            Appearance.decode("PAPA=AFRICAN,DADI=LATIN,NOPE=LATIN,MOM=NOPE,garbage"),
        )
        assertEquals(emptyMap<Character, Appearance>(), Appearance.decode(null))
    }
}
