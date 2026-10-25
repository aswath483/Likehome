package com.likehome.app.mom

import com.likehome.app.core.Character

internal object GermanLines {
    val pack = LinePack(
        mom = listOf(
            listOf(
                "Guten Morgen, Schatz. Das Frühstück ist fertig.",
                "Aufstehen, mein Herz! Der Kaffee ist gekocht.",
                "Guten Morgen, du Schlafmütze. Zeit zum Aufstehen.",
                "Die Sonne ist schon auf, und du solltest es auch sein.",
                "Ich habe dir deine Lieblingsbrötchen gemacht. Komm, bevor sie kalt werden.",
                "Wach auf, Schatz. Heute wird ein schöner Tag.",
                "Guten Morgen! Ich habe dir die Vorhänge aufgemacht.",
                "Deine Wasserflasche ist voll. Hoch mit dir.",
                "Wach langsam auf, aber wach auf. Ich hab dich lieb.",
                "Guten Morgen, mein Engel. Zähne putzen, das Frühstück steht auf dem Tisch.",
            ),
            listOf(
                "Aus fünf Minuten wurden zehn. Dein Frühstück wird kalt.",
                "Das „nur noch fünf Minuten“ habe ich gestern auch gehört.",
                "Schatz, ich rufe nicht noch mal. Aufstehen!",
                "Dein Wecker klingelt. Ich höre ihn bis in die Küche.",
                "Der Kaffee ist kalt. Ich koche keinen zweiten.",
                "Ich mache jetzt das Licht an. Beschwer dich nicht.",
                "Deine Bettdecke geht nicht für dich zur Schule.",
                "Schlummern, schlummern, schlummern. Kannst du sonst nichts?",
                "Ich stehe vor deiner Tür. Ich gehe nicht weg.",
                "Du wolltest heute früh aufstehen. Weißt du noch?",
            ),
            listOf(
                "Der Sohn der Nachbarn ist schon joggen gegangen.",
                "Deine Cousine ist schon im Büro. Und du?",
                "In deinem Alter war ich vor den Vögeln wach.",
                "Die Kinder von nebenan sind schon in der Schule.",
                "Sogar der Bäcker ist schon fertig mit der Lieferung.",
                "Die Tochter der Nachbarn hat Yoga gemacht UND Frühstück.",
                "Dein Vater war um 5 auf. Ich sag ja nur.",
                "Das ganze Haus ist wach. Nur du schläfst noch.",
                "Der Sohn meiner Freundin wurde befördert. Der steht pünktlich auf.",
                "Dein Handy hat gerade mehr Energie als du.",
            ),
            listOf(
                "Jetzt reicht's. Ich komme mit einem Eimer Wasser!",
                "Ich zähle bis drei. Eins…",
                "Gut, schlaf. Ich sag's deinem Vater.",
                "Letzte Warnung. Danach gibt es für niemanden Frühstück!",
                "Ich rufe deine Oma an. Die redet mit dir.",
                "Ich nehme dir die Decke weg. Fordere mich nicht heraus.",
                "Dafür habe ich dich großgezogen? SO sehen meine Morgen aus?",
                "Zwei… ich sage nicht drei, Schatz.",
                "Der Wasserkocher hat viermal gepfiffen. Ich auch.",
                "Das schreibe ich in die Familiengruppe.",
            ),
        ),
        characters = mapOf(
            Character.PAPA to listOf(
                listOf("Guten Morgen. Aufstehen.", "Es ist Zeit. Hoch.", "Die Zeitung ist da. Du solltest es auch sein.", "Guten Morgen, Champion. Los geht's.", "Hoch. Deine Mutter hat Frühstück gemacht.", "Aufstehen. Spaziergang ist um sieben."),
                listOf("Ich sagte: aufstehen.", "Ich sage es zum zweiten Mal.", "Hm. Immer noch im Bett.", "Ich bin nicht sauer. Noch nicht.", "Deine Mutter fragt. Ich frage auch.", "Die fünf Minuten sind um."),
                listOf("In deinem Alter hatte ich einen Job und ein Moped.", "Der Sohn der Nachbarn ist schon bei der Arbeit.", "Ich bin 30 Jahre lang um 5 aufgestanden. Ohne Wecker.", "Ich habe das Studium bezahlt. Los.", "Dein Cousin hat angerufen. Er ist wach.", "Das Moped ist bereit. Du nicht."),
                listOf("Ich komme jetzt. Lass es nicht so weit kommen.", "Ich ändere das WLAN-Passwort.", "Jetzt bin ich enttäuscht.", "Noch einmal schlummern und die Fernbedienung gehört mir.", "Ich rufe deinen Onkel an.", "Schweigen. So sauer bin ich."),
            ),
            Character.DADI to listOf(
                listOf("Wach auf, mein Kleines. Dadi hat Süßes gemacht.", "Guten Morgen, mein König. Gott segne dich.", "Mein Schatz, die Sonne sucht dich schon.", "Komm, ich habe dir etwas Warmes gemacht.", "Dadi hat für dich gebetet. Jetzt steh auf.", "Ich habe dir das beste Stück aufgehoben."),
                listOf("Als Kind bist du singend aufgewacht.", "Mir tun die Knie weh vom vielen Rufen.", "Dadi ist zu alt für die Treppe.", "Schon wieder fünf Minuten, mein Herz?", "Mein Kind, das Frühstück isst sich nicht von allein.", "Ach, die jungen Leute heute…"),
                listOf("In meinem Dorf haben die Kinder um diese Zeit schon die Kuh gemolken.", "Dein Vater war genauso. Schau, was aus ihm geworden ist.", "Dein Cousin hat mich um 6 angerufen. Wach.", "In deinem Alter hatte ich schon drei Kinder.", "Dein Opa hat nie lange geschlafen.", "Die Tochter der Nachbarn hat schon Wäsche gewaschen."),
                listOf("Oh Gott! Ich komme mit meinem Stock!", "Ich erzähle allen deine Babygeschichten.", "Lass mich nicht die Treppe hoch. Meine Knie machen das nicht mit.", "Ich erzähle es der ganzen Familie. Allen.", "Ich rufe alle Onkel an!", "Nicht mal mein Stock hält so viel Faulheit aus."),
            ),
            Character.BOSS to listOf(
                listOf("Guten Morgen! Kurzes Sync um 9?", "Aufstehen. Der Kaffee geht heute auf mich.", "Guten Morgen! Lass uns die Ziele rocken.", "Bereit für einen produktiven Tag?", "Neuer Tag, neue Chancen!", "Dein erstes Meeting ist mit der Dusche."),
                listOf("Bezugnehmend auf meinen letzten Wecker…", "Ich hake nur mal nach, ob du wach bist.", "Ich komme auf das letzte Schlummern zurück.", "Ich habe eine Erinnerung an die Erinnerung gestellt.", "Bitte bestätige deine Teilnahme am heutigen Tag.", "Wir warten weiter auf deine Rückmeldung."),
                listOf("Dein Kollege hat vor dem Frühstück drei Tickets erledigt.", "Deine Kollegen sind beim zweiten Kaffee.", "Die Konkurrenz ist schon wach.", "Ich habe in deinen Kalender geschaut. Du bist spät dran.", "Der Praktikant war vor dir da.", "Du hast 47 ungelesene Mails."),
                listOf("DRINGEND: bitte schnellstmöglich aufwachen.", "Ich habe das an deine Mutter eskaliert.", "Dein Jahresgespräch ist auf nie verschoben.", "Termin mit deinem Kissen: abgesagt.", "Dein Verbesserungsplan startet heute.", "Das kommt in deine Beurteilung."),
            ),
            Character.ABUELA to listOf(
                listOf("Buenos días, mein Schatz. Das Frühstück ist fertig.", "Wach auf, mijo. Die Sonne scheint.", "Aufstehen, mi cielo. Ich habe Milchkaffee gemacht.", "Mi vida, heute gibt es Churros.", "Es gibt warme Bohnen, mijo.", "Gott segne dich, mein Schatz. Aufstehen."),
                listOf("Ay, mijo, schläfst du immer noch?", "Aus fünf Minuten wurden zehn, mein Schatz.", "Die Churros werden kalt, hörst du?", "Lass mich nicht zu dir kommen.", "Ich höre dich schnarchen, hm?", "Mijo, das Bett zahlt keine Rechnungen."),
                listOf("Deine Cousine Lupe arbeitet schon.", "In deinem Alter hatte ich sechs Kinder und einen Garten.", "Der Sohn der Nachbarin war schon in der Messe.", "Dein Opa ist um 4 aufgestanden. Ohne zu jammern.", "Dein Onkel Pepe hatte in deinem Alter schon ein Geschäft.", "Der Hahn hat schon dreimal gekräht."),
                listOf("Ay Dios mío! Ich hole die Chancla!", "Ich zähle bis drei. Uno…", "Ich sag's deiner Mutter. Du wirst sehen.", "Die Chancla ist unterwegs!", "Ich habe die Chancla schon in der Hand!", "Ich schreibe es der ganzen Familie in die Gruppe!"),
            ),
        ),
        monday = listOf("Schönen Montag, Schatz. Die Woche fängt nicht von allein an.", "Es ist Montag. Heute keine Ausreden."),
        weekend = listOf("Es ist Wochenende, aber das Frühstück wartet nicht ewig.", "Samstag hin oder her, ich will dich am Tisch sehen."),
        newYear = "Frohes neues Jahr, Schatz! Neues Jahr, gleicher Wecker. Aufstehen!",
        bedtime = listOf(
            "Schatz, ab ins Bett. Es ist {now}.",
            "Leg das Handy weg. Dein Wecker klingelt um {alarm}.",
            "Licht aus. Mama passt auf.",
            "Jetzt schlafen, sonst kommt morgen der Wassereimer.",
        ),
        water = listOf(
            "Schatz, trink Wasser. Deine Haut wird es mir danken.",
            "Ein Glas Wasser. Jetzt. Ich sehe dich.",
            "Trinkst du Wasser oder nur Kaffee?",
        ),
        breakfast = listOf(
            "Frühstückszeit. Wehe, du lässt es aus.",
            "Iss etwas, Schatz. Mit leerem Magen ist das kein Plan.",
            "Dein Frühstück wartet. Ich auch.",
        ),
        promises = listOf(
            "Ich verspreche morgen nicht zu schlummern",
            "Ich bin wach und stehe jetzt auf",
            "Heute stehe ich beim ersten Ruf auf",
        ),
        awakePhrase = "Ich bin wach, %s!",
        awakeWords = setOf("wach", "aufgewacht", "aufgestanden", "auf"),
    )
}
