package com.likehome.app.core

import com.likehome.app.mom.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProofsTest {

    @Test
    fun promiseIgnoresCaseAndPunctuation() {
        val target = Promise.sentence(0L, Language.ENGLISH)
        assertTrue(Promise.matches(target.uppercase() + "!!", target))
        assertTrue(Promise.matches("  sharma JI'S son, is not better than me.", "Sharma ji's son is not better than me"))
        assertFalse(Promise.matches("I promise", target))
    }

    @Test
    fun hindiPromiseIsDevanagari() {
        assertTrue(Promise.sentence(0L, Language.HINDI).any { it in 'ऀ'..'ॿ' })
    }

    @Test
    fun voiceAcceptsMishearings() {
        assertTrue(VoiceCheck.matches(listOf("i'm awake mommy")))
        assertTrue(VoiceCheck.matches(listOf("hmm", "I am awake")))
        assertTrue(VoiceCheck.matches(listOf("main jag gaya")))
        assertFalse(VoiceCheck.matches(listOf("awake")))
        assertFalse(VoiceCheck.matches(listOf("five more minutes")))
    }

    @Test
    fun identicalImagesMatchAndOppositesDoNot() {
        val grey = IntArray(72) { (it * 37) % 256 }
        val hist = ImageMatch.histogram(IntArray(256) { 0xFF336699.toInt() })
        val same = ImageMatch.similarity(ImageMatch.dHash(grey), hist, ImageMatch.dHash(grey), hist)
        assertEquals(1f, same, 1e-6f)

        val inverted = IntArray(72) { 255 - grey[it] }
        val otherHist = ImageMatch.histogram(IntArray(256) { 0xFFEECC11.toInt() })
        val different = ImageMatch.similarity(ImageMatch.dHash(grey), hist, ImageMatch.dHash(inverted), otherHist)
        assertTrue("$different", different < ImageMatch.THRESHOLD)
    }

    @Test
    fun histogramSumsToOne() {
        val h = ImageMatch.histogram(IntArray(100) { it * 0x010203 })
        assertEquals(1f, h.sum(), 1e-4f)
    }
}
