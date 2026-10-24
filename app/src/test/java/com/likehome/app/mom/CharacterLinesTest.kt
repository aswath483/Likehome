package com.likehome.app.mom

import com.likehome.app.core.Character
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CharacterLinesTest {
    private val tuesday = LocalDate.of(2026, 10, 27)

    @Test
    fun everyCharacterHasFourLevelsInEveryLanguage() {
        Character.entries.forEach { c ->
            Language.entries.forEach { lang ->
                (0..MomLines.MAX_LEVEL).forEach { level ->
                    val line = MomLines.lineFor(level, 1_793_000_000_000L, lang, tuesday, c)
                    assertTrue("$c $lang $level", line.isNotBlank())
                }
                assertTrue("$c $lang", MomLines.lineCount(lang, c) >= 24)
            }
        }
    }

    @Test
    fun hindiIsWrittenInDevanagari() {
        val line = MomLines.lineFor(2, 99_000L, Language.HINDI, tuesday)
        assertTrue(line, line.any { it in 'ऀ'..'ॿ' })
        assertTrue(MomLines.lineCount(Language.HINDI) >= 40)
    }

    @Test
    fun bossFallsBackToHinglishForHindi() {
        assertEquals(
            MomLines.lineFor(1, 5_000L, Language.HINGLISH, tuesday, Character.BOSS),
            MomLines.lineFor(1, 5_000L, Language.HINDI, tuesday, Character.BOSS),
        )
    }

    @Test
    fun onlyMomGetsFestivalLines() {
        val diwali = LocalDate.of(2026, 11, 8)
        assertTrue(MomLines.lineFor(0, 60_000L, date = diwali).contains("Diwali"))
        assertTrue(!MomLines.lineFor(0, 60_000L, date = diwali, character = Character.PAPA).contains("Diwali"))
    }
}
