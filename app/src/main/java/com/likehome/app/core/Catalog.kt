package com.likehome.app.core

/**
 * Whether something in the gallery can be used. Everything ships included today; this is
 * the one switch to flip if a paid tier is ever added.
 */
enum class Availability(val label: String?) {
    AVAILABLE(null),
    PREMIUM("Premium"),
}

data class StyleEntry(val style: RingStyle, val availability: Availability = Availability.AVAILABLE) {
    val title: String get() = style.title
    val detail: String get() = style.detail
}

data class CharacterEntry(val character: Character, val availability: Availability = Availability.AVAILABLE) {
    val name: String get() = character.displayName
    val detail: String get() = character.detail
}

/** Every style and character, in the order the gallery shows them. */
object Catalog {
    val styles: List<StyleEntry> = RingStyle.entries.map { StyleEntry(it) }
    val characters: List<CharacterEntry> = Character.entries.map { CharacterEntry(it) }
}
