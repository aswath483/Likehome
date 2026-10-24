package com.likehome.app.mom

import com.likehome.app.core.Character

/**
 * Lines for everyone except Mom (she lives in [MomLines]), by language and drama level.
 * A language a character doesn't speak falls back to Hinglish, then English.
 */
internal object CharacterLines {

    fun pools(character: Character, language: Language): List<List<String>> {
        val byLanguage = all.getValue(character)
        return byLanguage[language]
            ?: byLanguage[Language.HINGLISH].takeIf { language == Language.HINDI }
            ?: byLanguage.getValue(Language.ENGLISH)
    }

    private val all: Map<Character, Map<Language, List<List<String>>>> = mapOf(
        Character.PAPA to mapOf(
            Language.ENGLISH to listOf(
                listOf("Good morning. Get up.", "Beta, it's time. Up.", "Morning. The newspaper is here. So should you be.",
                    "Up. Your mother made breakfast.", "Good morning, champion. Let's go.", "Rise. Walk is at seven."),
                listOf("I said up.", "Second time I'm saying this.", "Hmm. Still sleeping.",
                    "Your mother is asking. I am also asking.", "Five minutes is over.", "I am not angry. Yet."),
                listOf("When I was your age, I had a job and a scooter.", "Sharma ji's son. Already at work.",
                    "I woke up at 5 for 30 years. Without an alarm.", "Your cousin called. He is awake.",
                    "I paid for that college. Go.", "The scooter is ready. You are not."),
                listOf("I am coming. Don't make me.", "I am changing the WiFi password.", "Now I am disappointed.",
                    "One more snooze and the TV remote is mine.", "I will call your Mama.", "Silence. That's how angry I am."),
            ),
            Language.HINGLISH to listOf(
                listOf("Good morning. Utho.", "Beta, time ho gaya. Utho.", "Akhbaar aa gaya. Tum bhi aa jao.",
                    "Utho. Mummy ne nashta banaya hai.", "Good morning champion. Chalo.", "Utho. Saat baje walk hai."),
                listOf("Maine kaha utho.", "Doosri baar bol raha hoon.", "Hmm. Abhi bhi so rahe ho.",
                    "Mummy pooch rahi hai. Main bhi pooch raha hoon.", "Paanch minute khatam.", "Main gussa nahi hoon. Abhi tak."),
                listOf("Tumhari umar mein meri naukri thi aur scooter bhi.", "Sharma ji ka beta. Office pahunch gaya.",
                    "Main tees saal paanch baje utha. Bina alarm ke.", "Tumhare cousin ka phone aaya. Woh jaag gaya.",
                    "College ki fees maine bhari hai. Jao.", "Scooter ready hai. Tum nahi."),
                listOf("Main aa raha hoon. Mujhe mat bulao.", "WiFi ka password badal raha hoon.", "Ab main nirash hoon.",
                    "Ek aur snooze aur TV ka remote mera.", "Main tumhare Mama ko phone karunga.", "Chup. Itna gussa hoon main."),
            ),
            Language.HINDI to listOf(
                listOf("सुप्रभात। उठो।", "बेटा, समय हो गया। उठो।", "अख़बार आ गया। तुम भी आ जाओ।",
                    "उठो। मम्मी ने नाश्ता बनाया है।", "सुप्रभात चैंपियन। चलो।", "उठो। सात बजे सैर है।"),
                listOf("मैंने कहा उठो।", "दूसरी बार बोल रहा हूँ।", "हम्म। अभी भी सो रहे हो।",
                    "मम्मी पूछ रही हैं। मैं भी पूछ रहा हूँ।", "पाँच मिनट ख़त्म।", "मैं ग़ुस्सा नहीं हूँ। अभी तक।"),
                listOf("तुम्हारी उम्र में मेरी नौकरी थी और स्कूटर भी।", "शर्मा जी का बेटा। दफ़्तर पहुँच गया।",
                    "मैं तीस साल पाँच बजे उठा। बिना अलार्म के।", "तुम्हारे चचेरे भाई का फ़ोन आया। वो जाग गया।",
                    "कॉलेज की फ़ीस मैंने भरी है। जाओ।", "स्कूटर तैयार है। तुम नहीं।"),
                listOf("मैं आ रहा हूँ। मुझे मत बुलाओ।", "वाई-फ़ाई का पासवर्ड बदल रहा हूँ।", "अब मैं निराश हूँ।",
                    "एक और स्नूज़ और टीवी का रिमोट मेरा।", "मैं तुम्हारे मामा को फ़ोन करूँगा।", "चुप। इतना ग़ुस्सा हूँ मैं।"),
            ),
        ),
        Character.DADI to mapOf(
            Language.ENGLISH to listOf(
                listOf("Wake up, my little one. Dadi made halwa.", "Good morning, raja beta. God bless you.",
                    "Up, up! Your Dadi has been awake since 4.", "The tulsi is watered. Now you.",
                    "Wake up, my heart. The sun is waiting.", "Come, sit with Dadi. Tea is ready."),
                listOf("When you were small you woke up singing.", "Beta, Dadi's knees hurt from calling you.",
                    "Your grandfather never once snoozed.", "Hai Ram, still sleeping?",
                    "I will tell you a story only if you get up.", "Your milk is getting cold, beta."),
                listOf("In my village, children milked the cow by now.", "Your father was just like this. Look how he turned out.",
                    "Sharma ji's mother says her grandson is already awake.", "At your age, I had three children and a farm.",
                    "The neighbours will think Dadi spoiled you.", "Even the parrot is awake. Talking, too."),
                listOf("Hai bhagwan! I am coming with my walking stick!", "I will tell everyone about the time you wet the bed.",
                    "I am fasting until you wake up.", "I will call the pandit to wake you.",
                    "Don't make Dadi climb these stairs!", "I'm not angry, I'm just very, very old. Get up!"),
            ),
            Language.HINGLISH to listOf(
                listOf("Utho mere laal, Dadi ne halwa banaya hai.", "Good morning raja beta. Bhagwan bhala kare.",
                    "Utho utho! Dadi chaar baje se jaag rahi hai.", "Tulsi ko paani de diya. Ab tum utho.",
                    "Utho mere jigar, sooraj intezaar kar raha hai.", "Aao, Dadi ke paas baitho. Chai ready hai."),
                listOf("Bachpan mein tum gaate hue uthte the.", "Beta, tumhe bulaate bulaate Dadi ke ghutne dukh gaye.",
                    "Tumhare Dadaji ne kabhi snooze nahi kiya.", "Hai Ram, abhi bhi so rahe ho?",
                    "Kahani tabhi sunaungi jab uthoge.", "Doodh thanda ho raha hai beta."),
                listOf("Hamare gaon mein bachche ab tak gaay ka doodh nikaal lete the.", "Tumhare papa bhi aise hi the. Dekho kya ban gaye.",
                    "Sharma ji ki maa kehti hain unka pota jaag gaya.", "Tumhari umar mein mere teen bachche aur khet the.",
                    "Padosi sochenge Dadi ne bigaad diya.", "Tota bhi jaag gaya. Bol bhi raha hai."),
                listOf("Hai bhagwan! Main apni chhadi lekar aa rahi hoon!", "Sabko bata dungi jab tumne bistar geela kiya tha.",
                    "Jab tak nahi uthoge, main vrat rakhungi.", "Pandit ji ko bulaungi tumhe uthaane.",
                    "Dadi ko seedhiyan mat chadhwao!", "Gussa nahi hoon, bas bahut boodhi hoon. Utho!"),
            ),
            Language.HINDI to listOf(
                listOf("उठो मेरे लाल, दादी ने हलवा बनाया है।", "सुप्रभात राजा बेटा। भगवान भला करे।",
                    "उठो उठो! दादी चार बजे से जाग रही है।", "तुलसी को पानी दे दिया। अब तुम उठो।",
                    "उठो मेरे जिगर, सूरज इंतज़ार कर रहा है।", "आओ, दादी के पास बैठो। चाय तैयार है।"),
                listOf("बचपन में तुम गाते हुए उठते थे।", "बेटा, तुम्हें बुलाते-बुलाते दादी के घुटने दुख गए।",
                    "तुम्हारे दादाजी ने कभी स्नूज़ नहीं किया।", "हाय राम, अभी भी सो रहे हो?",
                    "कहानी तभी सुनाऊँगी जब उठोगे।", "दूध ठंडा हो रहा है बेटा।"),
                listOf("हमारे गाँव में बच्चे अब तक गाय का दूध निकाल लेते थे।", "तुम्हारे पापा भी ऐसे ही थे। देखो क्या बन गए।",
                    "शर्मा जी की माँ कहती हैं उनका पोता जाग गया।", "तुम्हारी उम्र में मेरे तीन बच्चे और खेत थे।",
                    "पड़ोसी सोचेंगे दादी ने बिगाड़ दिया।", "तोता भी जाग गया। बोल भी रहा है।"),
                listOf("हे भगवान! मैं अपनी छड़ी लेकर आ रही हूँ!", "सबको बता दूँगी जब तुमने बिस्तर गीला किया था।",
                    "जब तक नहीं उठोगे, मैं व्रत रखूँगी।", "पंडित जी को बुलाऊँगी तुम्हें उठाने।",
                    "दादी को सीढ़ियाँ मत चढ़वाओ!", "ग़ुस्सा नहीं हूँ, बस बहुत बूढ़ी हूँ। उठो!"),
            ),
        ),
        Character.BOSS to mapOf(
            Language.ENGLISH to listOf(
                listOf("Good morning! Quick sync at 9?", "Rise and grind. Coffee's on me today.", "Morning! Let's crush today's goals.",
                    "Hi! Just a gentle nudge about the 9:30.", "Good morning, team player. Laptop open?",
                    "Morning! Calendar invite sent: Waking Up."),
                listOf("Per my last alarm…", "Just circling back on you being awake.", "Following up on the previous snooze.",
                    "Can we take this snooze offline?", "Gentle reminder: the standup is not optional.",
                    "Let's align on what 'five minutes' means."),
                listOf("Sharma ji's son shipped three features before breakfast.", "Your peers are on their second coffee.",
                    "I'm seeing low engagement from your side.", "Let's discuss your sleep in our next one-on-one.",
                    "The intern is awake. The intern.", "This is going in your performance review."),
                listOf("URGENT: please wake up ASAP.", "I've escalated this to your mother.", "Moving your appraisal to never.",
                    "All-hands meeting in your bedroom in five.", "Your access card has been… paused.", "Reply-all incoming. Wake up."),
            ),
            Language.HINGLISH to listOf(
                listOf("Good morning! 9 baje quick sync?", "Uthiye, aaj coffee meri taraf se.", "Morning! Aaj ke goals crush karte hain.",
                    "Hi! Bas 9:30 wali meeting ka reminder.", "Good morning team player. Laptop on kiya?",
                    "Morning! Calendar invite bheja hai: Utho."),
                listOf("Per my last alarm…", "Bas follow up kar raha tha, uthe ki nahi?", "Pichhle snooze par follow up.",
                    "Is snooze ko offline discuss karein?", "Reminder: standup optional nahi hai.",
                    "Chalo 'paanch minute' ka matlab align karte hain."),
                listOf("Sharma ji ke bete ne nashte se pehle teen features ship kar diye.", "Tumhare colleagues doosri coffee pe hain.",
                    "Tumhari taraf se engagement kam dikh raha hai.", "Next one-on-one mein tumhari neend discuss karenge.",
                    "Intern jaag gaya. Intern.", "Yeh performance review mein jayega."),
                listOf("URGENT: jaldi uthiye.", "Maine yeh tumhari mummy ko escalate kar diya hai.", "Appraisal ab kabhi nahi.",
                    "Paanch minute mein bedroom mein all-hands.", "Tumhara access card… pause ho gaya hai.", "Reply-all aa raha hai. Utho."),
            ),
        ),
        Character.ABUELA to mapOf(
            Language.ENGLISH to listOf(
                listOf("Buenos días, mi amor. Breakfast is ready.", "Despierta, mijo. The sun is out.",
                    "Good morning, corazón. I made churros.", "Up, up, mi cielo. Café con leche is waiting.",
                    "Buenos días! Abuela has been up since five.", "Wake up, precioso. Today is a good day."),
                listOf("Ay, mijo, still sleeping?", "Five minutes became ten, mi amor.", "The churros are getting cold, eh.",
                    "I'm not calling again. Bueno, I am.", "Your abuelo never snoozed. Never.",
                    "Despierta! The rooster gave up already."),
                listOf("Your cousin Lupe is already at work.", "At your age I had six kids and a garden.",
                    "The neighbour's grandson is awake. Imagínate.", "Even the cat is up, mijo.",
                    "Sharma ji's son? Ay, everyone knows him.", "In my village, we were awake before the bread."),
                listOf("Ay Dios mío! I'm getting the chancla!", "I'm counting to three. Uno…",
                    "I'm telling the whole family at Sunday lunch.", "The chancla is coming up the stairs.",
                    "I'm lighting a candle so you wake up.", "No more snoozing. Abuela has spoken."),
            ),
        ),
    )
}
