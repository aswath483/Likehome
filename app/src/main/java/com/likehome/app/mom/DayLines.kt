package com.likehome.app.mom

import com.likehome.app.core.DayKind

/** Mom's first line on a day the user told her about; {title} is what they called it. */
object DayLines {

    private val lines: Map<Language, Map<DayKind, String>> = mapOf(
        Language.ENGLISH to mapOf(
            DayKind.EXAM to "All the best for your {title} today, beta! Eat curd and sugar before you go.",
            DayKind.INTERVIEW to "Big day: {title}! Iron your shirt, and smile. They'll love you.",
            DayKind.BIRTHDAY to "Today is {title}! Call, wish, and don't forget like last year.",
            DayKind.TRIP to "Today is {title}! Did you pack your charger? I'm asking twice.",
            DayKind.OTHER to "Today is {title}. Get up, beta, don't be late for it.",
        ),
        Language.HINGLISH to mapOf(
            DayKind.EXAM to "Aaj {title} hai beta, all the best! Jaane se pehle dahi-cheeni kha lena.",
            DayKind.INTERVIEW to "Aaj {title} hai! Shirt press karo, smile karo. Sab pasand karenge.",
            DayKind.BIRTHDAY to "Aaj {title} hai! Phone karke wish karo, pichhle saal ki tarah bhoolna mat.",
            DayKind.TRIP to "Aaj {title} hai! Charger rakh liya? Do baar pooch rahi hoon.",
            DayKind.OTHER to "Aaj {title} hai. Utho beta, late mat hona.",
        ),
        Language.HINDI to mapOf(
            DayKind.EXAM to "आज {title} है बेटा, ऑल द बेस्ट! जाने से पहले दही-चीनी खा लेना।",
            DayKind.INTERVIEW to "आज {title} है! कमीज़ प्रेस करो, मुस्कुराओ। सबको पसंद आओगे।",
            DayKind.BIRTHDAY to "आज {title} है! फ़ोन करके बधाई दो, पिछले साल की तरह भूलना मत।",
            DayKind.TRIP to "आज {title} है! चार्जर रख लिया? दो बार पूछ रही हूँ।",
            DayKind.OTHER to "आज {title} है। उठो बेटा, देर मत करना।",
        ),
        Language.SPANISH to mapOf(
            DayKind.EXAM to "¡Mucha suerte en tu {title} hoy, cariño! Desayuna bien antes de ir.",
            DayKind.INTERVIEW to "Gran día: ¡{title}! Plancha la camisa y sonríe. Les vas a encantar.",
            DayKind.BIRTHDAY to "¡Hoy es {title}! Llama y felicita, no se te olvide como el año pasado.",
            DayKind.TRIP to "¡Hoy es {title}! ¿Llevas el cargador? Te lo pregunto dos veces.",
            DayKind.OTHER to "Hoy es {title}. Arriba, cariño, que no llegues tarde.",
        ),
        Language.PORTUGUESE to mapOf(
            DayKind.EXAM to "Boa sorte na sua {title} hoje, filho! Toma um bom café antes de ir.",
            DayKind.INTERVIEW to "Dia importante: {title}! Passa a camisa e sorri. Vão adorar você.",
            DayKind.BIRTHDAY to "Hoje é {title}! Liga e dá os parabéns, não esquece como no ano passado.",
            DayKind.TRIP to "Hoje é {title}! Pegou o carregador? Estou perguntando duas vezes.",
            DayKind.OTHER to "Hoje é {title}. Levanta, filho, não se atrasa.",
        ),
        Language.FRENCH to mapOf(
            DayKind.EXAM to "Bonne chance pour ton {title} aujourd'hui, mon chéri ! Prends un vrai petit-déj avant.",
            DayKind.INTERVIEW to "Grand jour : {title} ! Repasse ta chemise et souris. Ils vont t'adorer.",
            DayKind.BIRTHDAY to "Aujourd'hui, c'est {title} ! Appelle et souhaite-le, pas comme l'an dernier.",
            DayKind.TRIP to "Aujourd'hui, c'est {title} ! Tu as pris ton chargeur ? Je demande deux fois.",
            DayKind.OTHER to "Aujourd'hui, c'est {title}. Debout, mon chéri, ne sois pas en retard.",
        ),
        Language.GERMAN to mapOf(
            DayKind.EXAM to "Viel Glück bei deiner {title} heute, Schatz! Iss vorher ordentlich.",
            DayKind.INTERVIEW to "Großer Tag: {title}! Bügel dein Hemd und lächle. Die werden dich lieben.",
            DayKind.BIRTHDAY to "Heute ist {title}! Ruf an und gratuliere, nicht vergessen wie letztes Jahr.",
            DayKind.TRIP to "Heute ist {title}! Hast du das Ladekabel? Ich frage zweimal.",
            DayKind.OTHER to "Heute ist {title}. Aufstehen, Schatz, sei nicht zu spät.",
        ),
        Language.ARABIC to mapOf(
            DayKind.EXAM to "بالتوفيق في {title} اليوم يا حبيبي! كُل فطورك قبل أن تخرج.",
            DayKind.INTERVIEW to "يوم كبير: {title}! اكوِ قميصك وابتسم. سيحبونك.",
            DayKind.BIRTHDAY to "اليوم {title}! اتصل وهنّئ، ولا تنسَ مثل السنة الماضية.",
            DayKind.TRIP to "اليوم {title}! هل أخذت الشاحن؟ أسألك مرتين.",
            DayKind.OTHER to "اليوم {title}. قم يا حبيبي، لا تتأخر.",
        ),
        Language.INDONESIAN to mapOf(
            DayKind.EXAM to "Semoga lancar {title} hari ini, Nak! Sarapan dulu sebelum berangkat.",
            DayKind.INTERVIEW to "Hari besar: {title}! Setrika bajumu dan tersenyumlah. Mereka pasti suka.",
            DayKind.BIRTHDAY to "Hari ini {title}! Telepon dan ucapkan, jangan lupa seperti tahun lalu.",
            DayKind.TRIP to "Hari ini {title}! Charger sudah dibawa? Ibu tanya dua kali.",
            DayKind.OTHER to "Hari ini {title}. Bangun, Nak, jangan sampai telat.",
        ),
    )

    fun line(kind: DayKind, title: String, language: Language): String =
        lines.getValue(language).getValue(kind).replace("{title}", title)
}
