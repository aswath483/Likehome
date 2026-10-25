package com.likehome.app.mom

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RemoteFestivalsTest {

    @After fun clear() { MomLines.remoteFestivals = emptyMap() }

    @Test
    fun parsesAndIgnoresJunk() {
        val map = RemoteFestivals.parse(
            """[{"date":"2027-03-22","english":"Happy Holi, beta!","hinglish":"Holi mubarak beta!"},
               {"date":"not a date","english":"x"}, {"date":"2027-01-14"}]"""
        )
        assertEquals(1, map.size)
        assertEquals("Holi mubarak beta!", map[LocalDate.of(2027, 3, 22)]?.get(Language.HINGLISH))
        assertTrue(RemoteFestivals.parse("{broken").isEmpty())
        assertTrue(RemoteFestivals.parse(null).isEmpty())
    }

    @Test
    fun remoteFestivalWinsOnFirstRingAndFallsBackToEnglish() {
        val holi = LocalDate.of(2027, 3, 22)
        MomLines.remoteFestivals = RemoteFestivals.parse("""[{"date":"2027-03-22","english":"Happy Holi, beta!"}]""")
        assertEquals("Happy Holi, beta!", MomLines.lineFor(0, 60_000L, Language.HINDI, holi))
        assertTrue(MomLines.lineFor(1, 60_000L, Language.ENGLISH, holi) != "Happy Holi, beta!")
    }
}
