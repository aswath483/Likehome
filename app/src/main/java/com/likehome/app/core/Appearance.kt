package com.likehome.app.core

/**
 * How a family character looks, so people everywhere can see their own family.
 * Each one has its own portraits in `assets/characters/<character>/<folder>/`; a
 * character's [Character.defaultAppearance] lives in the character's own folder.
 */
enum class Appearance(val folder: String) {
    SOUTH_ASIAN("south_asian"),
    EAST_ASIAN("east_asian"),
    AFRICAN("african"),
    EUROPEAN("european"),
    LATIN("latin"),
    MIDDLE_EASTERN("middle_eastern");

    companion object {
        /** The assets folder holding [character]'s portraits when they look like [appearance]. */
        fun assetFolder(character: Character, appearance: Appearance?): String {
            val base = "characters/${character.name.lowercase()}"
            val default = character.defaultAppearance
            return if (default == null || appearance == null || appearance == default) base else "$base/${appearance.folder}"
        }

        /** Saved form: "MOM=EAST_ASIAN,BOSS=LATIN". Unknown names are skipped. */
        fun encode(choices: Map<Character, Appearance>): String =
            choices.entries.joinToString(",") { "${it.key.name}=${it.value.name}" }

        fun decode(saved: String?): Map<Character, Appearance> =
            saved.orEmpty().split(",").mapNotNull { pair ->
                val (c, a) = pair.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
                val character = Character.entries.firstOrNull { it.name == c } ?: return@mapNotNull null
                val appearance = entries.firstOrNull { it.name == a } ?: return@mapNotNull null
                if (character.defaultAppearance == null) null else character to appearance
            }.toMap()
    }
}
