package com.likehome.app.mom

import com.likehome.app.core.Character
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay

/** The language Mom speaks in. The app's own buttons stay in English. */
enum class Language(val label: String, val sample: String) {
    ENGLISH("English", "Good morning beta. Breakfast is ready."),
    HINGLISH("Hinglish", "Good morning beta, nashta ready hai."),
    HINDI("हिन्दी", "सुप्रभात बेटा, नाश्ता तैयार है।"),
}

/**
 * Mom's lines by drama level. Level 0 is the first ring; each snooze moves up one level.
 * Kept in code for v1; later these come from voice packs and Remote Config.
 */
object MomLines {

    const val MAX_LEVEL = 3

    private val english: List<List<String>> = listOf(
        // 0 – Sweet
        listOf(
            "Good morning beta. Breakfast is ready.",
            "Rise and shine! Chai is getting ready.",
            "Morning, my sleepyhead. Time to get up.",
            "The sun is up, and so should you be, my baby.",
            "I made your favourite paratha. Come before it gets cold.",
            "Wake up, beta. Today is going to be a good day.",
            "Good morning! I opened the curtains for you.",
            "Beta, your water bottle is filled. Up you get.",
            "Wake up slowly, but wake up. I love you.",
            "Morning, raja. Brush your teeth, breakfast is on the table.",
        ),
        // 1 – Annoyed
        listOf(
            "Five minutes became ten. Your breakfast is getting cold.",
            "I heard \"five more minutes\" yesterday also.",
            "Beta, I'm not calling again. Up!",
            "Your alarm is ringing. I can hear it from the kitchen.",
            "The chai has gone cold. I'm not making it twice.",
            "I'm switching the lights on. Don't blame me.",
            "Your blanket is not going to college for you.",
            "Snooze, snooze, snooze. Is that all you know?",
            "I'm standing at your door. I'm not leaving.",
            "You said you'd wake up early today. Remember?",
        ),
        // 2 – Comparing
        listOf(
            "Sharma ji's son already went for a run.",
            "Your cousin is at office already. And you?",
            "When I was your age, I woke up before the birds.",
            "The neighbours' kids are already at school.",
            "Even the milkman has finished his round.",
            "Sharma ji's daughter did yoga AND made breakfast.",
            "Your father was up at 5. Just saying.",
            "The whole colony is awake. Only you are sleeping.",
            "Pinky aunty's son got promoted. He wakes up on time.",
            "Your phone has more energy than you right now.",
        ),
        // 3 – Full drama
        listOf(
            "That's it. I'm coming up with a bucket of water!",
            "I am counting to three. One…",
            "Fine, sleep. I'll tell your father.",
            "Last warning. After this, no more breakfast for anyone!",
            "I'm calling your Nani. She will talk to you.",
            "I'm taking the blanket. Don't test me.",
            "I raised you for this? THIS is how my mornings go?",
            "Two… I'm not saying three, beta.",
            "The pressure cooker has whistled four times. So have I.",
            "I'm putting this in the family WhatsApp group.",
        ),
    )

    private val hinglish: List<List<String>> = listOf(
        // 0 – Sweet
        listOf(
            "Good morning beta, nashta ready hai.",
            "Uth jao beta, sooraj nikal aaya.",
            "Utho mere raja, chai ban rahi hai.",
            "Beta, aaj ka din bahut accha hoga. Uth jao.",
            "Tumhare favourite aloo parathe bane hain. Jaldi aao.",
            "Good morning! Maine parde khol diye hain.",
            "Uth jao beta, paani ki bottle bhar di hai.",
            "Dheere dheere utho, par utho. Love you beta.",
            "Chalo uth jao, brush karo, nashta table pe hai.",
            "Subah ho gayi, beta! Uth jao.",
        ),
        // 1 – Annoyed
        listOf(
            "Paanch minute ke dus ho gaye. Nashta thanda ho raha hai.",
            "\"Bas paanch minute\" kal bhi suna tha.",
            "Beta, main dobara nahi bulaungi. Utho!",
            "Tumhara alarm kitchen tak sunai de raha hai.",
            "Chai thandi ho gayi. Main dobara nahi banaungi.",
            "Main light on kar rahi hoon. Phir mat kehna.",
            "Kambal tumhare liye college nahi jayega.",
            "Snooze, snooze, snooze. Bas yahi aata hai?",
            "Main darwaze pe khadi hoon. Kahin nahi jaa rahi.",
            "Kal bola tha na, kal se jaldi uthunga?",
        ),
        // 2 – Comparing
        listOf(
            "Sharma ji ka beta toh run karke aa bhi gaya.",
            "Tumhara cousin office pahunch gaya. Aur tum?",
            "Hum tumhari umar mein chidiyon se pehle uthte the.",
            "Padosiyon ke bachche school pahunch gaye.",
            "Doodhwala bhi apna round khatam kar chuka hai.",
            "Sharma ji ki beti ne yoga bhi kiya aur nashta bhi banaya.",
            "Tumhare papa 5 baje uth gaye the. Bas bata rahi hoon.",
            "Poori colony jaag gayi. Bas tum so rahe ho.",
            "Pinky aunty ke bete ka promotion ho gaya. Woh time pe uthta hai.",
            "Tumhare phone mein tumse zyada energy hai.",
        ),
        // 3 – Full drama
        listOf(
            "Bas! Main balti bhar ke paani la rahi hoon!",
            "Main teen tak ginti hoon. Ek…",
            "Theek hai, so jao. Main papa ko bata rahi hoon.",
            "Last warning. Iske baad kisi ko nashta nahi milega!",
            "Main Nani ko phone kar rahi hoon. Wahi samjhayengi.",
            "Kambal le ja rahi hoon. Mujhe test mat karo.",
            "Isi din ke liye paala tha? Roz subah yahi drama?",
            "Do… teen mat bulwao, beta.",
            "Cooker ki chaar seeti baj gayi. Meri bhi.",
            "Yeh baat family WhatsApp group mein jayegi.",
        ),
    )

    private val hindi: List<List<String>> = listOf(
        // 0 – Sweet
        listOf(
            "सुप्रभात बेटा, नाश्ता तैयार है।",
            "उठ जाओ बेटा, सूरज निकल आया।",
            "उठो मेरे राजा, चाय बन रही है।",
            "बेटा, आज का दिन बहुत अच्छा होगा। उठ जाओ।",
            "तुम्हारे पसंदीदा आलू के पराठे बने हैं। जल्दी आओ।",
            "सुप्रभात! मैंने पर्दे खोल दिए हैं।",
            "उठ जाओ बेटा, पानी की बोतल भर दी है।",
            "धीरे-धीरे उठो, पर उठो। बहुत प्यार, बेटा।",
            "चलो उठो, ब्रश करो, नाश्ता मेज़ पर है।",
            "सुबह हो गई बेटा! उठ जाओ।",
        ),
        // 1 – Annoyed
        listOf(
            "पाँच मिनट के दस हो गए। नाश्ता ठंडा हो रहा है।",
            "\"बस पाँच मिनट\" कल भी सुना था।",
            "बेटा, मैं दोबारा नहीं बुलाऊँगी। उठो!",
            "तुम्हारा अलार्म रसोई तक सुनाई दे रहा है।",
            "चाय ठंडी हो गई। मैं दोबारा नहीं बनाऊँगी।",
            "मैं बत्ती जला रही हूँ। फिर मत कहना।",
            "कंबल तुम्हारे लिए कॉलेज नहीं जाएगा।",
            "स्नूज़, स्नूज़, स्नूज़। बस यही आता है?",
            "मैं दरवाज़े पर खड़ी हूँ। कहीं नहीं जा रही।",
            "कल कहा था न, कल से जल्दी उठूँगा?",
        ),
        // 2 – Comparing
        listOf(
            "शर्मा जी का बेटा तो दौड़ लगाकर आ भी गया।",
            "तुम्हारा चचेरा भाई दफ़्तर पहुँच गया। और तुम?",
            "हम तुम्हारी उम्र में चिड़ियों से पहले उठते थे।",
            "पड़ोसियों के बच्चे स्कूल पहुँच गए।",
            "दूधवाला भी अपना चक्कर पूरा कर चुका है।",
            "शर्मा जी की बेटी ने योग भी किया और नाश्ता भी बनाया।",
            "तुम्हारे पापा पाँच बजे उठ गए थे। बस बता रही हूँ।",
            "पूरी कॉलोनी जाग गई। बस तुम सो रहे हो।",
            "पिंकी आंटी के बेटे का प्रमोशन हो गया। वो समय पर उठता है।",
            "तुम्हारे फ़ोन में तुमसे ज़्यादा ताक़त है।",
        ),
        // 3 – Full drama
        listOf(
            "बस! मैं बाल्टी भर के पानी ला रही हूँ!",
            "मैं तीन तक गिनती हूँ। एक…",
            "ठीक है, सो जाओ। मैं पापा को बता रही हूँ।",
            "आख़िरी चेतावनी। इसके बाद किसी को नाश्ता नहीं मिलेगा!",
            "मैं नानी को फ़ोन कर रही हूँ। वही समझाएँगी।",
            "कंबल ले जा रही हूँ। मुझे परखो मत।",
            "इसी दिन के लिए पाला था? रोज़ सुबह यही नाटक?",
            "दो… तीन मत बुलवाओ, बेटा।",
            "कुकर की चार सीटी बज गईं। मेरी भी।",
            "यह बात फ़ैमिली व्हाट्सऐप ग्रुप में जाएगी।",
        ),
    )

    /** Special first-ring lines; picked instead of a normal sweet line on these days. */
    private enum class Occasion { MONDAY, WEEKEND, NEW_YEAR, DIWALI }

    private val occasionLines: Map<Occasion, Map<Language, List<String>>> = mapOf(
        Occasion.MONDAY to mapOf(
            Language.ENGLISH to listOf(
                "It's Monday, beta. New week, fresh start. Up!",
                "Mondays are hard, I know. That's why I made halwa.",
            ),
            Language.HINGLISH to listOf(
                "Monday hai beta. Naya hafta, nayi shuruaat. Utho!",
                "Monday mushkil hota hai, pata hai. Isliye halwa banaya hai.",
            ),
            Language.HINDI to listOf(
                "सोमवार है बेटा। नया हफ़्ता, नई शुरुआत। उठो!",
                "सोमवार मुश्किल होता है, पता है। इसलिए हलवा बनाया है।",
            ),
        ),
        Occasion.WEEKEND to mapOf(
            Language.ENGLISH to listOf(
                "It's the weekend, so I let you sleep a little. Now up!",
                "No college today, but breakfast still has a time.",
            ),
            Language.HINGLISH to listOf(
                "Weekend hai, isliye thoda sone diya. Ab utho!",
                "Aaj chhutti hai, par nashte ka time toh hai.",
            ),
            Language.HINDI to listOf(
                "छुट्टी है, इसलिए थोड़ा सोने दिया। अब उठो!",
                "आज छुट्टी है, पर नाश्ते का समय तो है।",
            ),
        ),
        Occasion.NEW_YEAR to mapOf(
            Language.ENGLISH to listOf("Happy New Year, beta! New year, same alarm. Up!"),
            Language.HINGLISH to listOf("Happy New Year beta! Naya saal, wahi alarm. Utho!"),
            Language.HINDI to listOf("नया साल मुबारक बेटा! नया साल, वही अलार्म। उठो!"),
        ),
        Occasion.DIWALI to mapOf(
            Language.ENGLISH to listOf("Happy Diwali, beta! Wake up, the rangoli won't make itself."),
            Language.HINGLISH to listOf("Happy Diwali beta! Utho, rangoli khud nahi banegi."),
            Language.HINDI to listOf("दिवाली मुबारक बेटा! उठो, रंगोली ख़ुद नहीं बनेगी।"),
        ),
    )

    /** Diwali moves every year. Hard-coded for now; Remote Config takes over later. */
    private val diwaliDates = setOf(
        LocalDate.of(2026, 11, 8),
        LocalDate.of(2027, 10, 29),
        LocalDate.of(2028, 10, 17),
    )

    private val bedtimeEnglish = listOf(
        "Beta, sleep now. It's already {now}.",
        "Phone down, beta. Your alarm is at {alarm}.",
        "Lights off. Mom is watching.",
        "Sleep now, or tomorrow I'm bringing the bucket.",
    )

    private val bedtimeHinglish = listOf(
        "Beta, ab so jao. {now} baj gaye.",
        "Phone rakho beta. Alarm {alarm} ka hai.",
        "Light band karo. Mummy dekh rahi hai.",
        "Abhi so jao, warna kal balti aayegi.",
    )

    private val bedtimeHindi = listOf(
        "बेटा, अब सो जाओ। {now} बज गए।",
        "फ़ोन रखो बेटा। अलार्म {alarm} का है।",
        "बत्ती बंद करो। मम्मी देख रही है।",
        "अभी सो जाओ, वरना कल बाल्टी आएगी।",
    )

    /** Total number of everyday lines per language (occasion lines not counted). */
    fun lineCount(language: Language, character: Character = Character.MOM): Int =
        pools(language, character).sumOf { it.size }

    fun lineFor(
        level: Int,
        seed: Long = System.currentTimeMillis(),
        language: Language = Language.ENGLISH,
        date: LocalDate = LocalDate.now(),
        character: Character = Character.MOM,
    ): String {
        val lvl = level.coerceIn(0, MAX_LEVEL)
        val pick = mix(seed, lvl)
        if (lvl == 0 && character == Character.MOM) {
            occasionFor(date, pick)?.let { occasion ->
                val pool = occasionLines.getValue(occasion).getValue(language)
                return pool[pick % pool.size]
            }
        }
        val pool = pools(language, character)[lvl]
        return pool[pick % pool.size]
    }

    /** The conversation so far: one line per level reached, oldest first. */
    fun conversation(
        level: Int,
        seed: Long,
        language: Language = Language.ENGLISH,
        date: LocalDate = LocalDate.now(),
        character: Character = Character.MOM,
    ): List<String> =
        (0..level.coerceIn(0, MAX_LEVEL)).map { lineFor(it, seed, language, date, character) }

    fun moodLabel(level: Int): String = when (level.coerceIn(0, MAX_LEVEL)) {
        0 -> "Sweet"
        1 -> "Annoyed"
        2 -> "Comparing you"
        else -> "Full drama"
    }

    /**
     * A bedtime nudge. [now] is the current time as text ("11:00 pm"), [alarm] the next
     * alarm's time, or null when none is set (lines mentioning it are skipped then).
     */
    fun bedtimeLine(seed: Long, language: Language, now: String, alarm: String?): String {
        val all = when (language) {
            Language.HINGLISH -> bedtimeHinglish
            Language.HINDI -> bedtimeHindi
            Language.ENGLISH -> bedtimeEnglish
        }
        val pool = if (alarm == null) all.filterNot { "{alarm}" in it } else all
        return pool[mix(seed, BEDTIME_SALT) % pool.size].replace("{now}", now).replace("{alarm}", alarm.orEmpty())
    }

    private const val BEDTIME_SALT = 99

    /**
     * Turns a seed into a well-spread non-negative number. Alarm seeds are trigger times,
     * which are whole minutes, so a plain modulo would pick the same line every day.
     */
    internal fun mix(seed: Long, salt: Int): Int {
        var z = seed + salt * -7046029254386353131L
        z = (z xor (z ushr 30)) * -4658895280553007687L
        z = (z xor (z ushr 27)) * -7723592293110705685L
        z = z xor (z ushr 31)
        return (z ushr 33).toInt()
    }

    private fun pools(language: Language, character: Character = Character.MOM): List<List<String>> = when {
        character != Character.MOM -> CharacterLines.pools(character, language)
        language == Language.HINGLISH -> hinglish
        language == Language.HINDI -> hindi
        else -> english
    }

    /** Festivals always win; Mondays and weekends get a special line about half the time. */
    private fun occasionFor(date: LocalDate, pick: Int): Occasion? = when {
        MonthDay.from(date) == MonthDay.of(1, 1) -> Occasion.NEW_YEAR
        date in diwaliDates -> Occasion.DIWALI
        pick % 2 == 1 -> null
        date.dayOfWeek == DayOfWeek.MONDAY -> Occasion.MONDAY
        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY -> Occasion.WEEKEND
        else -> null
    }
}
