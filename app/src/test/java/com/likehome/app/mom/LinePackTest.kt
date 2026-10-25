package com.likehome.app.mom

import com.likehome.app.core.Character
import com.likehome.app.core.Promise
import com.likehome.app.core.VoiceCheck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LinePackTest {

    private val packed = Language.entries.filter { LinePacks.forLanguage(it) != null }

    @Test
    fun everyNewLanguageHasAPack() {
        assertEquals(6, packed.size)
    }

    @Test
    fun everyCharacterSpeaksEveryLanguageAtEveryLevel() {
        packed.forEach { lang ->
            Character.entries.forEach { c ->
                (0..MomLines.MAX_LEVEL).forEach { level ->
                    val line = MomLines.lineFor(level, 42L, lang, LocalDate.of(2026, 10, 27), c)
                    assertTrue("$lang $c $level", line.isNotBlank())
                }
            }
        }
    }

    @Test
    fun packsHaveFullLevelsAndUsableReminders() {
        LinePacks.all.forEach { pack ->
            assertEquals(4, pack.mom.size)
            pack.characters.values.forEach { assertEquals(4, it.size); it.forEach { level -> assertTrue(level.isNotEmpty()) } }
            assertEquals(Character.entries.toSet() - Character.MOM, pack.characters.keys)
            assertTrue(pack.bedtime.any { "{alarm}" !in it })
            (pack.bedtime + pack.water + pack.breakfast).forEach {
                assertFalse(it, it.replace("{now}", "").replace("{alarm}", "").contains("{"))
            }
            assertTrue(pack.awakePhrase.contains("%s"))
            assertTrue(pack.promises.isNotEmpty() && pack.monday.isNotEmpty() && pack.weekend.isNotEmpty())
        }
    }

    @Test
    fun bedtimeWithoutAnAlarmNeverLeavesAPlaceholder() {
        packed.forEach { lang ->
            (0L..30L).forEach { s ->
                val line = MomLines.bedtimeLine(s * 60_000, lang, "23:00", alarm = null)
                assertFalse("$lang: $line", line.contains("{"))
            }
        }
    }

    @Test
    fun newYearIsSpecialAndDiwaliFallsThrough() {
        packed.forEach { lang ->
            val newYear = MomLines.lineFor(0, 60_000, lang, LocalDate.of(2027, 1, 1))
            assertEquals(LinePacks.forLanguage(lang)!!.newYear, newYear)
            val diwali = MomLines.lineFor(0, 60_000, lang, LocalDate.of(2026, 11, 8))
            assertTrue(diwali.isNotBlank())
        }
    }

    @Test
    fun eachLanguagesOwnAwakePhrasePassesTheVoiceCheck() {
        packed.forEach { lang ->
            assertTrue(lang.name, VoiceCheck.matches(listOf(VoiceCheck.phrase("Mamá", lang))))
        }
        assertFalse(VoiceCheck.matches(listOf("buenas noches")))
    }

    @Test
    fun hindiVowelSignsSurviveTheVoiceCheck() {
        assertTrue(VoiceCheck.matches(listOf("मैं जाग गया मम्मी")))
    }

    @Test
    fun promisesMatchThemselvesIgnoringPunctuation() {
        packed.forEach { lang ->
            val sentence = Promise.sentence(120_000, lang)
            assertTrue(Promise.matches("$sentence!", sentence))
        }
    }

    @Test
    fun phoneLanguagePicksMomsLanguage() {
        assertEquals(Language.SPANISH, Language.forLocale("es-MX"))
        assertEquals(Language.PORTUGUESE, Language.forLocale("pt-BR"))
        assertEquals(Language.INDONESIAN, Language.forLocale("in-ID"))
        assertEquals(Language.INDONESIAN, Language.forLocale("id"))
        assertEquals(Language.HINDI, Language.forLocale("hi-IN"))
        assertEquals(Language.ARABIC, Language.forLocale("ar-EG"))
        assertEquals(Language.ENGLISH, Language.forLocale("en-IN"))
        assertEquals(Language.ENGLISH, Language.forLocale("ja-JP"))
    }
}
