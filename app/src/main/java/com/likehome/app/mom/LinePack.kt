package com.likehome.app.mom

import com.likehome.app.core.Character

/**
 * Everything one language says, for the languages added after English, Hinglish and
 * Hindi (those live in [MomLines] and [CharacterLines]). Each pack is written for its
 * culture rather than translated line by line: the neighbour's kid, not Sharma ji's son.
 */
internal class LinePack(
    /** Mom's everyday lines, one list per drama level (sweet, annoyed, comparing, drama). */
    val mom: List<List<String>>,
    /** The other characters' lines, same shape as [mom]. */
    val characters: Map<Character, List<List<String>>>,
    val monday: List<String>,
    val weekend: List<String>,
    val newYear: String,
    /** Bedtime nudges; may use {now} and {alarm}. At least one must not mention {alarm}. */
    val bedtime: List<String>,
    val water: List<String>,
    val breakfast: List<String>,
    /** Sentences for the typed promise. */
    val promises: List<String>,
    /** The spoken check, with %s for the character's name: "¡Estoy despierto, %s!". */
    val awakePhrase: String,
    /** Words that count as "I'm awake" when the speech recogniser hears them. */
    val awakeWords: Set<String>,
)

internal object LinePacks {
    private val packs: Map<Language, LinePack> by lazy {
        mapOf(
            Language.SPANISH to SpanishLines.pack,
            Language.PORTUGUESE to PortugueseLines.pack,
            Language.FRENCH to FrenchLines.pack,
            Language.GERMAN to GermanLines.pack,
            Language.ARABIC to ArabicLines.pack,
            Language.INDONESIAN to IndonesianLines.pack,
        )
    }

    fun forLanguage(language: Language): LinePack? = packs[language]

    val all: Collection<LinePack> get() = packs.values
}
