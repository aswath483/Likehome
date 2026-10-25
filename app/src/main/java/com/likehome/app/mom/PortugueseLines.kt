package com.likehome.app.mom

import com.likehome.app.core.Character

internal object PortugueseLines {
    val pack = LinePack(
        mom = listOf(
            listOf(
                "Bom dia, filho. O café da manhã está pronto.",
                "Acorda, meu amor! O café já está passado.",
                "Bom dia, dorminhoco. Hora de levantar.",
                "O sol já nasceu, e você também deveria, meu bem.",
                "Fiz aquele pão de queijo que você gosta. Vem antes que esfrie.",
                "Acorda, querido. Hoje vai ser um dia bom.",
                "Bom dia! Já abri a cortina para você.",
                "Sua garrafinha de água está cheia. Levanta.",
                "Acorda devagar, mas acorda. Te amo.",
                "Bom dia, meu anjo. Escova os dentes, o café está na mesa.",
            ),
            listOf(
                "Cinco minutos viraram dez. Seu café está esfriando.",
                "Esse \"só mais cinco minutinhos\" eu ouvi ontem também.",
                "Filho, não vou chamar de novo. Levanta!",
                "Seu despertador está tocando. Dá para ouvir da cozinha.",
                "O café esfriou. Não vou passar outro.",
                "Vou acender a luz. Depois não reclama.",
                "O cobertor não vai para a aula por você.",
                "Soneca, soneca, soneca. Só sabe fazer isso?",
                "Estou na porta do seu quarto. Não vou sair daqui.",
                "Você disse que ia acordar cedo hoje. Lembra?",
            ),
            listOf(
                "O filho da vizinha já foi correr.",
                "Sua prima já está no trabalho. E você?",
                "Na sua idade eu acordava antes dos passarinhos.",
                "As crianças do lado já estão na escola.",
                "Até o padeiro já terminou as entregas.",
                "A filha da vizinha fez ioga E preparou o café.",
                "Seu pai levantou às 5. Só estou dizendo.",
                "A rua inteira já acordou. Só você está dormindo.",
                "O filho da minha amiga foi promovido. Ele acorda na hora.",
                "Seu celular tem mais energia que você agora.",
            ),
            listOf(
                "Chega! Vou subir com um balde de água!",
                "Vou contar até três. Um…",
                "Tudo bem, dorme. Vou contar para o seu pai.",
                "Último aviso. Depois disso, não tem café para ninguém!",
                "Vou ligar para a sua avó. Ela vai falar com você.",
                "Vou tirar o cobertor. Não me testa.",
                "Eu te criei para isso? É ASSIM que são as minhas manhãs?",
                "Dois… não me faça dizer três.",
                "A panela de pressão já apitou quatro vezes. Eu também.",
                "Vou mandar isso no grupo da família.",
            ),
        ),
        characters = mapOf(
            Character.PAPA to listOf(
                listOf("Bom dia. Levanta.", "Está na hora. Levanta.", "O jornal chegou. Você também devia.", "Bom dia, campeão. Vamos.", "Levanta. Sua mãe fez o café.", "Acorda. A caminhada é às sete."),
                listOf("Eu disse levanta.", "É a segunda vez que eu falo.", "Hum. Ainda dormindo.", "Não estou bravo. Ainda.", "Sua mãe está perguntando. Eu também.", "Os cinco minutos acabaram."),
                listOf("Na sua idade eu tinha emprego e moto.", "O filho do vizinho já está trabalhando.", "Acordei às 5 durante 30 anos. Sem despertador.", "Eu paguei essa faculdade. Vai.", "Seu primo ligou. Ele já acordou.", "A moto está pronta. Você não."),
                listOf("Estou indo aí. Não me faça ir.", "Vou trocar a senha do wi-fi.", "Agora estou decepcionado.", "Mais uma soneca e o controle remoto é meu.", "Vou ligar para o seu tio.", "Silêncio. É o quanto estou bravo."),
            ),
            Character.DADI to listOf(
                listOf("Acorda, meu pequeno. A Dadi fez doce.", "Bom dia, meu rei. Deus te abençoe.", "Meu tesouro, o sol já está procurando você.", "Vem, preparei algo quentinho.", "A Dadi rezou por você. Agora levanta.", "Guardei o melhor pedaço para você."),
                listOf("Quando pequeno você acordava cantando.", "Meus joelhos doem de tanto te chamar.", "A Dadi já não tem idade para subir escada.", "Mais cinco minutos de novo, querido?", "Meu menino, o café não se toma sozinho.", "Ai, esses jovens de hoje…"),
                listOf("Na minha vila, a essa hora as crianças já tiravam leite da vaca.", "Seu pai era igualzinho. Olha no que deu.", "Seu primo me ligou às 6. Acordado.", "Na sua idade eu já tinha três filhos.", "Seu avô nunca dormiu até tarde.", "A filha da vizinha já lavou a roupa."),
                listOf("Meu Deus do céu! Vou aí com a minha bengala!", "Vou contar para todo mundo os seus segredos de bebê.", "Não me faça subir. Meus joelhos não aguentam.", "Vou contar para a família inteira. Todo mundo.", "Vou ligar para todos os tios!", "Nem a minha bengala aguenta tanta preguiça."),
            ),
            Character.BOSS to listOf(
                listOf("Bom dia! Alinhamento rápido às 9?", "Levanta. O café hoje é por minha conta.", "Bom dia! Bora bater as metas.", "Pronto para um dia produtivo?", "Novo dia, novas oportunidades!", "Sua primeira reunião é com o chuveiro."),
                listOf("Conforme meu último despertador…", "Só passando para ver se você acordou.", "Retomando a soneca anterior.", "Coloquei um lembrete do lembrete.", "Por favor, confirme presença no dia de hoje.", "Ainda aguardamos seu retorno."),
                listOf("Seu colega entregou três tarefas antes do café.", "Seus colegas já estão no segundo café.", "A concorrência já acordou.", "Olhei sua agenda. Você está atrasado.", "O estagiário chegou antes de você.", "Seu e-mail tem 47 mensagens não lidas."),
                listOf("URGENTE: acorde o quanto antes.", "Escalei isso para a sua mãe.", "Sua avaliação foi adiada para nunca.", "Marquei uma reunião com seu travesseiro. Cancelada.", "Seu plano de melhoria começa hoje.", "Isso vai constar na sua avaliação."),
            ),
            Character.ABUELA to listOf(
                listOf("Bom dia, meu amor. O café está pronto.", "Acorda, mijo. O sol já saiu.", "Levanta, meu céu. Fiz café com leite.", "Minha vida, hoje tem churros.", "Tem feijãozinho quente, mijo.", "Deus te abençoe, meu amor. Levanta."),
                listOf("Ai, mijo, ainda dormindo?", "Cinco minutos viraram dez, meu amor.", "Os churros estão esfriando, hein.", "Não me faça ir até aí.", "Estou ouvindo você roncar, hein?", "Mijo, a cama não paga as contas."),
                listOf("Sua prima Lupe já está trabalhando.", "Na sua idade eu tinha seis filhos e uma horta.", "O filho da vizinha já foi à missa.", "Seu avô levantava às 4. Sem reclamar.", "Seu tio Pepe na sua idade já tinha negócio.", "O galo já cantou três vezes."),
                listOf("Ai, meu Deus! Vou buscar o chinelo!", "Vou contar até três. Um…", "Vou contar para a sua mãe. Você vai ver.", "O chinelo já está a caminho!", "Já tirei o chinelo!", "Vou contar para a família toda no grupo!"),
            ),
        ),
        monday = listOf("Feliz segunda, filho. A semana não começa sozinha.", "É segunda-feira. Hoje não tem desculpa, meu amor."),
        weekend = listOf("É fim de semana, mas o café não espera para sempre.", "Sábado ou não, quero você na mesa."),
        newYear = "Feliz Ano Novo, filho! Ano novo, mesmo despertador. Levanta!",
        bedtime = listOf(
            "Filho, vai dormir. Já são {now}.",
            "Larga o celular. Seu despertador é às {alarm}.",
            "Apaga a luz. A mãe está de olho.",
            "Vai dormir agora, senão amanhã vem o balde de água.",
        ),
        water = listOf(
            "Filho, bebe água. Sua pele vai me agradecer.",
            "Um copo de água. Agora. Estou de olho.",
            "Está bebendo água ou só café?",
        ),
        breakfast = listOf(
            "Hora do café da manhã. Nem pense em pular.",
            "Come alguma coisa, filho. Barriga vazia não é plano.",
            "Seu café está esperando. Eu também.",
        ),
        promises = listOf(
            "Prometo que amanhã não vou apertar soneca",
            "Estou acordado e estou saindo da cama",
            "Hoje eu acordo no primeiro chamado",
        ),
        awakePhrase = "Estou acordado, %s!",
        awakeWords = setOf("acordado", "acordada", "acordei", "levantei", "acordar"),
    )
}
