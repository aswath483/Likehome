package com.likehome.app.core

/**
 * Whether something in the gallery can be used. Everything ships included today; this is
 * the one switch to flip if a paid tier is ever added.
 */
enum class Availability { AVAILABLE, PREMIUM }

data class StyleEntry(val style: RingStyle, val availability: Availability = Availability.AVAILABLE)

data class CharacterEntry(val character: Character, val availability: Availability = Availability.AVAILABLE)

/** Every style and character, in the order the gallery shows them. */
object Catalog {
    val styles: List<StyleEntry> = RingStyle.entries.map { StyleEntry(it) }
    val characters: List<CharacterEntry> = Character.entries.map { CharacterEntry(it) }
}
