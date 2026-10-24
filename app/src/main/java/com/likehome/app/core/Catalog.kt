package com.likehome.app.core

/** Whether something in the gallery can be used today. */
enum class Availability(val label: String?) {
    AVAILABLE(null),
    PREMIUM("Premium"),
    COMING_SOON("Coming soon"),
}

data class StyleEntry(
    val title: String,
    val detail: String,
    val availability: Availability,
    /** The style this entry turns on; null while it isn't built yet. */
    val style: RingStyle? = null,
)

data class CharacterEntry(val name: String, val detail: String, val availability: Availability)

/** Everything the plan promises, so users can see what's coming. */
object Catalog {

    val styles: List<StyleEntry> = listOf(
        StyleEntry(RingStyle.CHAT.title, RingStyle.CHAT.detail, Availability.AVAILABLE, RingStyle.CHAT),
        StyleEntry(RingStyle.COMIC.title, RingStyle.COMIC.detail, Availability.AVAILABLE, RingStyle.COMIC),
        StyleEntry(RingStyle.SMART_MIX.title, RingStyle.SMART_MIX.detail, Availability.AVAILABLE, RingStyle.SMART_MIX),
        StyleEntry("Warm kitchen", "A cosy cartoon Mom with a mood meter", Availability.COMING_SOON),
        StyleEntry("Calm night", "Dark, gentle and quiet, with sleep stats", Availability.COMING_SOON),
        StyleEntry("Breaking news", "Mom as a news anchor, with a ticker about your snoozing", Availability.PREMIUM),
        StyleEntry("Retro game", "Mom is the boss fight; walking is your attack", Availability.PREMIUM),
        StyleEntry("Filmy drama", "A tragic Bollywood scene, sad violin included", Availability.PREMIUM),
        StyleEntry("Fridge notes", "Handwritten sticky notes with the day's reminders", Availability.COMING_SOON),
    )

    val characters: List<CharacterEntry> = listOf(
        CharacterEntry("Mummy", "Loving, dramatic, never gives up", Availability.AVAILABLE),
        CharacterEntry("Papa", "Few words. Very disappointed ones.", Availability.PREMIUM),
        CharacterEntry("Dadi", "Has stories about your childhood. Will tell them.", Availability.PREMIUM),
        CharacterEntry("The Boss", "Calendar invites at 7 am", Availability.COMING_SOON),
        CharacterEntry("Abuela", "Mom's Spanish cousin in spirit", Availability.COMING_SOON),
    )
}
