package com.likehome.app.mom

/** Mom in Ramadan: the suhoor wake-up (her first line) and the iftar reminder. */
object RamadanLines {

    private val suhoor: Map<Language, List<String>> = mapOf(
        Language.ENGLISH to listOf(
            "Wake up, beta, it's suhoor. Eat something before Fajr.",
            "Suhoor is ready. Dates, water, and your paratha. Come.",
            "Up, up. Fajr is close, don't fast on an empty stomach.",
        ),
        Language.HINGLISH to listOf(
            "Utho beta, sehri ka time hai. Fajr se pehle kuch kha lo.",
            "Sehri ready hai. Khajoor, paani aur paratha. Aao.",
            "Utho utho, Fajr hone wali hai. Khaali pet roza mat rakho.",
        ),
        Language.HINDI to listOf(
            "उठो बेटा, सहरी का वक़्त है। फ़ज्र से पहले कुछ खा लो।",
            "सहरी तैयार है। खजूर, पानी और पराठा। आओ।",
            "उठो उठो, फ़ज्र होने वाली है। ख़ाली पेट रोज़ा मत रखो।",
        ),
        Language.ARABIC to listOf(
            "قم يا حبيبي، إنه وقت السحور. كُل شيئًا قبل الفجر.",
            "السحور جاهز: تمر وماء ولبن. تعال.",
            "قم قم، الفجر قريب. لا تصم على معدة فارغة.",
        ),
        Language.INDONESIAN to listOf(
            "Bangun, Nak, sudah waktunya sahur. Makan dulu sebelum Subuh.",
            "Sahur sudah siap. Kurma, air putih, dan nasi hangat. Ayo.",
            "Ayo bangun, sebentar lagi imsak. Jangan puasa dengan perut kosong.",
        ),
        Language.SPANISH to listOf(
            "Despierta, cariño, es la hora del suhur. Come algo antes del Fajr.",
            "El suhur está listo: dátiles, agua y algo caliente. Ven.",
            "Arriba, que el Fajr está cerca. No ayunes con el estómago vacío.",
        ),
        Language.PORTUGUESE to listOf(
            "Acorda, filho, é hora do suhur. Come alguma coisa antes do Fajr.",
            "O suhur está pronto: tâmaras, água e algo quentinho. Vem.",
            "Levanta, o Fajr está chegando. Não jejue de barriga vazia.",
        ),
        Language.FRENCH to listOf(
            "Réveille-toi, mon chéri, c'est l'heure du shour. Mange avant le Fajr.",
            "Le shour est prêt : dattes, eau et quelque chose de chaud. Viens.",
            "Debout, le Fajr approche. Ne jeûne pas le ventre vide.",
        ),
        Language.GERMAN to listOf(
            "Wach auf, Schatz, es ist Zeit für Suhur. Iss etwas vor dem Fadschr.",
            "Suhur ist fertig: Datteln, Wasser und etwas Warmes. Komm.",
            "Hoch mit dir, Fadschr ist bald. Faste nicht mit leerem Magen.",
        ),
    )

    private val iftar: Map<Language, List<String>> = mapOf(
        Language.ENGLISH to listOf(
            "Iftar time! Break your fast, beta. Dates first.",
            "It's Maghrib. Eat slowly, and drink water.",
            "Iftar is ready. Come before the pakoras disappear.",
        ),
        Language.HINGLISH to listOf(
            "Iftar ka time! Roza kholo beta. Pehle khajoor.",
            "Maghrib ho gayi. Aaram se khao, paani piyo.",
            "Iftar ready hai. Pakode khatam hone se pehle aa jao.",
        ),
        Language.HINDI to listOf(
            "इफ़्तार का वक़्त! रोज़ा खोलो बेटा। पहले खजूर।",
            "मग़रिब हो गई। आराम से खाओ, पानी पियो।",
            "इफ़्तार तैयार है। पकौड़े ख़त्म होने से पहले आ जाओ।",
        ),
        Language.ARABIC to listOf(
            "حان وقت الإفطار! افطر يا حبيبي، التمر أولًا.",
            "أذّن المغرب. كُل على مهلك واشرب الماء.",
            "الإفطار جاهز. تعال قبل أن تنتهي السمبوسة.",
        ),
        Language.INDONESIAN to listOf(
            "Waktunya berbuka! Batalkan puasamu, Nak. Kurma dulu.",
            "Sudah Magrib. Makan pelan-pelan, dan minum air.",
            "Takjil sudah siap. Ayo sebelum gorengannya habis.",
        ),
        Language.SPANISH to listOf(
            "¡Hora del iftar! Rompe el ayuno, cariño. Primero los dátiles.",
            "Ya es Magrib. Come despacio y bebe agua.",
            "El iftar está listo. Ven antes de que se acabe todo.",
        ),
        Language.PORTUGUESE to listOf(
            "Hora do iftar! Quebra o jejum, filho. Tâmaras primeiro.",
            "Já é Maghrib. Come devagar e bebe água.",
            "O iftar está pronto. Vem antes que acabe tudo.",
        ),
        Language.FRENCH to listOf(
            "C'est l'heure de l'iftar ! Romps le jeûne, mon chéri. Les dattes d'abord.",
            "C'est le Maghrib. Mange doucement et bois de l'eau.",
            "L'iftar est prêt. Viens avant que tout disparaisse.",
        ),
        Language.GERMAN to listOf(
            "Zeit für Iftar! Brich dein Fasten, Schatz. Zuerst die Datteln.",
            "Es ist Maghrib. Iss langsam und trink Wasser.",
            "Iftar ist fertig. Komm, bevor alles weg ist.",
        ),
    )

    fun suhoorLine(seed: Long, language: Language): String =
        suhoor.getValue(language).let { it[MomLines.mix(seed, 41) % it.size] }

    fun iftarLine(seed: Long, language: Language): String =
        iftar.getValue(language).let { it[MomLines.mix(seed, 42) % it.size] }
}
