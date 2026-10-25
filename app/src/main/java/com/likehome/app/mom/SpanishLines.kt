package com.likehome.app.mom

import com.likehome.app.core.Character

internal object SpanishLines {
    val pack = LinePack(
        mom = listOf(
            listOf(
                "Buenos días, cariño. El desayuno está listo.",
                "¡Arriba, mi vida! El café ya está hecho.",
                "Buenos días, dormilón. Hora de levantarse.",
                "Ya salió el sol, y tú también deberías, mi amor.",
                "Te hice tus tostadas favoritas. Ven antes de que se enfríen.",
                "Despierta, cariño. Hoy va a ser un buen día.",
                "¡Buenos días! Ya te abrí las cortinas.",
                "Tu botella de agua está llena. Arriba.",
                "Despiértate despacio, pero despiértate. Te quiero.",
                "Buenos días, mi cielo. Lávate los dientes, el desayuno está en la mesa.",
            ),
            listOf(
                "Cinco minutos se volvieron diez. Se te enfría el desayuno.",
                "Lo de \"cinco minutitos más\" ya lo oí ayer.",
                "Cariño, no te vuelvo a llamar. ¡Arriba!",
                "Tu alarma está sonando. La oigo desde la cocina.",
                "El café se enfrió. No lo voy a hacer dos veces.",
                "Voy a prender la luz. Luego no te quejes.",
                "La cobija no va a ir a clase por ti.",
                "Posponer, posponer, posponer. ¿Solo sabes hacer eso?",
                "Estoy en tu puerta. No me voy a ir.",
                "Dijiste que hoy te ibas a levantar temprano. ¿Te acuerdas?",
            ),
            listOf(
                "El hijo de la vecina ya salió a correr.",
                "Tu prima ya está en la oficina. ¿Y tú?",
                "A tu edad, yo me levantaba antes que los pájaros.",
                "Los niños de al lado ya están en el colegio.",
                "Hasta el panadero ya terminó su ronda.",
                "La hija de la vecina hizo yoga Y preparó el desayuno.",
                "Tu padre se levantó a las 5. Solo lo digo.",
                "Todo el barrio está despierto. Solo tú duermes.",
                "Al hijo de mi amiga lo ascendieron. Él sí se levanta a tiempo.",
                "Tu teléfono tiene más energía que tú ahora mismo.",
            ),
            listOf(
                "¡Se acabó! ¡Voy con un balde de agua!",
                "Cuento hasta tres. Uno…",
                "Bueno, duerme. Se lo voy a decir a tu padre.",
                "Último aviso. ¡Después de esto, no hay desayuno para nadie!",
                "Voy a llamar a tu abuela. Ella hablará contigo.",
                "Te quito la cobija. No me provoques.",
                "¿Para esto te crié? ¿ASÍ son mis mañanas?",
                "Dos… no me hagas decir tres.",
                "La olla ya pitó cuatro veces. Y yo también.",
                "Esto lo pongo en el grupo de la familia.",
            ),
        ),
        characters = mapOf(
            Character.PAPA to listOf(
                listOf("Buenos días. Levántate.", "Es la hora. Arriba.", "Ya llegó el periódico. Tú también deberías.", "Arriba, campeón. Vamos.", "Arriba. Tu madre hizo el desayuno.", "Levántate. El paseo es a las siete."),
                listOf("Dije arriba.", "Es la segunda vez que lo digo.", "Mmm. Sigues durmiendo.", "No estoy enojado. Todavía.", "Tu madre pregunta. Yo también pregunto.", "Se acabaron los cinco minutos."),
                listOf("A tu edad yo tenía trabajo y moto.", "El hijo del vecino ya está trabajando.", "Me levanté a las 5 durante 30 años. Sin alarma.", "Pagué esa universidad. Ve.", "Tu primo llamó. Él ya está despierto.", "La moto está lista. Tú no."),
                listOf("Voy para allá. No me hagas ir.", "Voy a cambiar la clave del wifi.", "Ahora sí estoy decepcionado.", "Una posposición más y el control remoto es mío.", "Llamaré a tu tío.", "Silencio. Así de enojado estoy."),
            ),
            Character.DADI to listOf(
                listOf("Despierta, mi pequeño. Dadi hizo dulces.", "Buenos días, mi rey. Que Dios te bendiga.", "Mi tesoro, el sol ya te está buscando.", "Ven, que te preparé algo calentito.", "Dadi rezó por ti. Ahora levántate.", "Te guardé el mejor pedazo."),
                listOf("De chiquito te levantabas cantando.", "Me duelen las rodillas de tanto llamarte.", "Dadi ya no tiene edad para subir escaleras.", "¿Otra vez cinco minutos, corazón?", "Mi niño, el desayuno no se come solo.", "Ay, estos jóvenes de hoy…"),
                listOf("En mi pueblo, a esta hora los niños ya ordeñaban la vaca.", "Tu padre era igualito. Mira cómo salió.", "Tu primo me llamó a las 6. Despierto.", "Yo a tu edad ya tenía tres hijos.", "Tu abuelo nunca durmió hasta tarde.", "La hija de la vecina ya lavó la ropa."),
                listOf("¡Ay, Dios mío! ¡Voy con mi bastón!", "Le voy a contar a todos tus secretos de bebé.", "No me hagas subir. Mis rodillas no aguantan.", "Se lo cuento a toda la familia. A todos.", "¡Voy a llamar a todos los tíos!", "Ni mi bastón aguanta tanta pereza."),
            ),
            Character.BOSS to listOf(
                listOf("¡Buenos días! ¿Reunión rápida a las 9?", "Arriba. Hoy invito el café.", "¡Buen día! A romperla con los objetivos.", "¿Listo para un día productivo?", "¡Nuevo día, nuevas oportunidades!", "Tu primera reunión es con la ducha."),
                listOf("Según mi alarma anterior…", "Solo hago seguimiento de si estás despierto.", "Retomo la posposición anterior.", "Te puse un recordatorio del recordatorio.", "Por favor, confirma tu asistencia al día de hoy.", "Seguimos esperando tu respuesta."),
                listOf("Tu compañero entregó tres tareas antes del desayuno.", "Tus compañeros van por el segundo café.", "La competencia ya está despierta.", "Revisé tu calendario. Estás tarde.", "El becario llegó antes que tú.", "Tu correo tiene 47 mensajes sin leer."),
                listOf("URGENTE: despierta lo antes posible.", "Escalé esto a tu mamá.", "Tu evaluación se pospone para nunca.", "Te agendé una reunión con tu almohada. Cancelada.", "Tu plan de mejora empieza hoy.", "Esto quedará en tu evaluación."),
            ),
            Character.ABUELA to listOf(
                listOf("Buenos días, mi amor. El desayuno está listo.", "Despierta, mijo. Ya salió el sol.", "Arriba, mi cielo. Te hice café con leche.", "Mi vida, hoy hice churros.", "Hay frijolitos calientes, mijo.", "Que Dios te bendiga, mi amor. Arriba."),
                listOf("Ay, mijo, ¿todavía durmiendo?", "Cinco minutos se volvieron diez, mi amor.", "Los churros se enfrían, ¿eh?", "No me hagas ir hasta allá.", "Te estoy oyendo roncar, ¿eh?", "Mijo, la cama no paga las cuentas."),
                listOf("Tu prima Lupe ya está trabajando.", "A tu edad yo tenía seis hijos y una huerta.", "El hijo de la vecina ya fue a misa.", "Tu abuelo se levantaba a las 4. Sin quejarse.", "Tu tío Pepe a tu edad ya tenía negocio.", "Los gallos ya cantaron tres veces."),
                listOf("¡Ay, Dios mío! ¡Voy por la chancla!", "Cuento hasta tres. Uno…", "Se lo digo a tu madre. Ya verás.", "¡La chancla ya está en camino!", "¡Ya me quité la chancla!", "¡Se lo digo a toda la familia en el grupo!"),
            ),
        ),
        monday = listOf("Feliz lunes, cariño. La semana no se empieza sola.", "Es lunes. Hoy no hay excusas, mi amor."),
        weekend = listOf("Es fin de semana, pero el desayuno no se espera solo.", "Sábado o no, quiero verte en la mesa."),
        newYear = "¡Feliz Año Nuevo, cariño! Año nuevo, misma alarma. ¡Arriba!",
        bedtime = listOf(
            "Cariño, ya a dormir. Son las {now}.",
            "Suelta el teléfono. Tu alarma es a las {alarm}.",
            "Apaga la luz. Mamá te está viendo.",
            "A dormir ya, o mañana viene el balde de agua.",
        ),
        water = listOf(
            "Cariño, toma agua. Tu piel me lo agradecerá.",
            "Un vaso de agua. Ahora. Te estoy viendo.",
            "¿Estás tomando agua o solo café?",
        ),
        breakfast = listOf(
            "Hora del desayuno. Ni se te ocurra saltártelo.",
            "Come algo, cariño. Con el estómago vacío no hay plan.",
            "Tu desayuno te espera. Y yo también.",
        ),
        promises = listOf(
            "Prometo que mañana no voy a posponer",
            "Estoy despierto y me estoy levantando",
            "Hoy me levanto a la primera llamada",
        ),
        awakePhrase = "¡Estoy despierto, %s!",
        awakeWords = setOf("despierto", "despierta", "levantado", "levantada", "arriba"),
    )
}
