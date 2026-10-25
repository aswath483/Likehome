package com.likehome.app.mom

import com.likehome.app.core.Character

internal object FrenchLines {
    val pack = LinePack(
        mom = listOf(
            listOf(
                "Bonjour mon chéri. Le petit-déjeuner est prêt.",
                "Debout, mon trésor ! Le café est prêt.",
                "Bonjour, ma marmotte. C'est l'heure de se lever.",
                "Le soleil est levé, et tu devrais l'être aussi, mon cœur.",
                "J'ai fait tes tartines préférées. Viens avant qu'elles refroidissent.",
                "Réveille-toi, mon chéri. Ça va être une belle journée.",
                "Bonjour ! J'ai ouvert les volets pour toi.",
                "Ta gourde est remplie. Allez, debout.",
                "Réveille-toi doucement, mais réveille-toi. Je t'aime.",
                "Bonjour, mon ange. Brosse-toi les dents, le petit-déj est sur la table.",
            ),
            listOf(
                "Cinq minutes sont devenues dix. Ton petit-déjeuner refroidit.",
                "Le « encore cinq minutes », je l'ai déjà entendu hier.",
                "Mon chéri, je ne t'appelle pas une deuxième fois. Debout !",
                "Ton réveil sonne. Je l'entends depuis la cuisine.",
                "Le café est froid. Je n'en referai pas.",
                "J'allume la lumière. Ne viens pas te plaindre.",
                "Ta couette ne va pas aller en cours à ta place.",
                "Répéter, répéter, répéter. Tu ne sais faire que ça ?",
                "Je suis devant ta porte. Je ne bouge pas.",
                "Tu avais dit que tu te lèverais tôt aujourd'hui. Tu te souviens ?",
            ),
            listOf(
                "Le fils des voisins est déjà parti courir.",
                "Ta cousine est déjà au bureau. Et toi ?",
                "À ton âge, je me levais avant les oiseaux.",
                "Les enfants d'à côté sont déjà à l'école.",
                "Même le boulanger a fini sa fournée.",
                "La fille des voisins a fait son yoga ET le petit-déjeuner.",
                "Ton père était debout à 5 h. Je dis ça, je dis rien.",
                "Tout l'immeuble est réveillé. Il n'y a que toi qui dors.",
                "Le fils de mon amie a eu une promotion. Lui, il se lève à l'heure.",
                "Ton téléphone a plus d'énergie que toi en ce moment.",
            ),
            listOf(
                "Ça suffit. Je monte avec un seau d'eau !",
                "Je compte jusqu'à trois. Un…",
                "Très bien, dors. Je vais le dire à ton père.",
                "Dernier avertissement. Après, plus de petit-déjeuner pour personne !",
                "J'appelle ta grand-mère. Elle va te parler, elle.",
                "Je prends la couette. Ne me cherche pas.",
                "Je t'ai élevé pour ça ? C'est ÇA, mes matins ?",
                "Deux… je ne dirai pas trois, mon chéri.",
                "La cocotte-minute a sifflé quatre fois. Moi aussi.",
                "Je le mets sur le groupe de la famille.",
            ),
        ),
        characters = mapOf(
            Character.PAPA to listOf(
                listOf("Bonjour. Lève-toi.", "C'est l'heure. Debout.", "Le journal est arrivé. Toi aussi, tu devrais.", "Bonjour, champion. On y va.", "Debout. Ta mère a fait le petit-déjeuner.", "Lève-toi. La promenade est à sept heures."),
                listOf("J'ai dit debout.", "C'est la deuxième fois que je le dis.", "Hum. Toujours au lit.", "Je ne suis pas fâché. Pas encore.", "Ta mère demande. Moi aussi.", "Les cinq minutes sont finies."),
                listOf("À ton âge, j'avais un travail et une mobylette.", "Le fils des voisins est déjà au travail.", "Je me suis levé à 5 h pendant 30 ans. Sans réveil.", "J'ai payé ces études. Vas-y.", "Ton cousin a appelé. Il est réveillé.", "La mobylette est prête. Toi non."),
                listOf("J'arrive. Ne m'oblige pas à venir.", "Je change le mot de passe du wifi.", "Là, je suis déçu.", "Encore une répétition et la télécommande est à moi.", "Je vais appeler ton oncle.", "Silence. Voilà à quel point je suis fâché."),
            ),
            Character.DADI to listOf(
                listOf("Réveille-toi, mon petit. Dadi a fait des douceurs.", "Bonjour, mon roi. Que Dieu te bénisse.", "Mon trésor, le soleil te cherche déjà.", "Viens, je t'ai préparé quelque chose de chaud.", "Dadi a prié pour toi. Maintenant, debout.", "Je t'ai gardé le meilleur morceau."),
                listOf("Petit, tu te réveillais en chantant.", "J'ai mal aux genoux à force de t'appeler.", "Dadi n'a plus l'âge de monter les escaliers.", "Encore cinq minutes, mon cœur ?", "Mon petit, le petit-déj ne se mange pas tout seul.", "Ah, les jeunes d'aujourd'hui…"),
                listOf("Dans mon village, les enfants avaient déjà trait la vache.", "Ton père était pareil. Regarde ce qu'il est devenu.", "Ton cousin m'a appelée à 6 h. Réveillé.", "À ton âge, j'avais déjà trois enfants.", "Ton grand-père ne faisait jamais la grasse matinée.", "La fille des voisins a déjà fait la lessive."),
                listOf("Mon Dieu ! J'arrive avec ma canne !", "Je vais raconter à tout le monde tes bêtises de bébé.", "Ne me fais pas monter. Mes genoux ne tiennent plus.", "Je le dis à toute la famille. Tout le monde.", "Je vais appeler tous les oncles !", "Même ma canne n'en peut plus de cette paresse."),
            ),
            Character.BOSS to listOf(
                listOf("Bonjour ! Petit point rapide à 9 h ?", "Debout. Le café est pour moi aujourd'hui.", "Bonjour ! On explose les objectifs.", "Prêt pour une journée productive ?", "Nouveau jour, nouvelles opportunités !", "Ta première réunion est avec la douche."),
                listOf("Suite à mon précédent réveil…", "Je reviens vers toi concernant ton réveil.", "Je relance au sujet de la répétition précédente.", "J'ai mis un rappel pour le rappel.", "Merci de confirmer ta présence à la journée d'aujourd'hui.", "Toujours en attente de ton retour."),
                listOf("Ton collègue a livré trois tâches avant le petit-déj.", "Tes collègues en sont à leur deuxième café.", "La concurrence est déjà réveillée.", "J'ai regardé ton agenda. Tu es en retard.", "Le stagiaire est arrivé avant toi.", "Tu as 47 e-mails non lus."),
                listOf("URGENT : merci de te réveiller au plus vite.", "J'ai fait remonter le sujet à ta mère.", "Ton entretien annuel est reporté à jamais.", "Réunion avec ton oreiller : annulée.", "Ton plan d'amélioration commence aujourd'hui.", "Ce sera noté dans ton évaluation."),
            ),
            Character.ABUELA to listOf(
                listOf("Buenos días, mon amour. Le petit-déj est prêt.", "Réveille-toi, mijo. Le soleil est levé.", "Debout, mi cielo. J'ai fait un café au lait.", "Mi vida, aujourd'hui il y a des churros.", "Il y a des haricots tout chauds, mijo.", "Que Dieu te bénisse, mon amour. Debout."),
                listOf("Ay, mijo, tu dors encore ?", "Cinq minutes sont devenues dix, mon amour.", "Les churros refroidissent, hein.", "Ne m'oblige pas à venir.", "Je t'entends ronfler, hein ?", "Mijo, le lit ne paie pas les factures."),
                listOf("Ta cousine Lupe travaille déjà.", "À ton âge, j'avais six enfants et un potager.", "Le fils de la voisine est déjà allé à la messe.", "Ton grand-père se levait à 4 h. Sans se plaindre.", "Ton oncle Pepe avait déjà son commerce à ton âge.", "Le coq a déjà chanté trois fois."),
                listOf("Ay Dios mío ! Je vais chercher la chancla !", "Je compte jusqu'à trois. Uno…", "Je vais le dire à ta mère. Tu vas voir.", "La chancla est en route !", "J'ai déjà enlevé ma chancla !", "Je le dis à toute la famille sur le groupe !"),
            ),
        ),
        monday = listOf("Bon lundi, mon chéri. La semaine ne va pas commencer toute seule.", "C'est lundi. Aujourd'hui, pas d'excuses."),
        weekend = listOf("C'est le week-end, mais le petit-déjeuner n'attend pas toute la journée.", "Samedi ou pas, je veux te voir à table."),
        newYear = "Bonne année, mon chéri ! Nouvelle année, même réveil. Debout !",
        bedtime = listOf(
            "Mon chéri, au lit. Il est {now}.",
            "Pose ton téléphone. Ton réveil est à {alarm}.",
            "Éteins la lumière. Maman te surveille.",
            "Au lit, sinon demain c'est le seau d'eau.",
        ),
        water = listOf(
            "Mon chéri, bois de l'eau. Ta peau me remerciera.",
            "Un verre d'eau. Maintenant. Je te surveille.",
            "Tu bois de l'eau ou seulement du café ?",
        ),
        breakfast = listOf(
            "C'est l'heure du petit-déjeuner. N'ose même pas le sauter.",
            "Mange quelque chose, mon chéri. Le ventre vide, ce n'est pas un plan.",
            "Ton petit-déjeuner t'attend. Moi aussi.",
        ),
        promises = listOf(
            "Je promets de ne pas répéter demain",
            "Je suis réveillé et je sors du lit",
            "Aujourd'hui je me lève au premier appel",
        ),
        awakePhrase = "Je suis réveillé, %s !",
        awakeWords = setOf("réveillé", "réveillée", "debout", "levé", "levée", "réveil"),
    )
}
