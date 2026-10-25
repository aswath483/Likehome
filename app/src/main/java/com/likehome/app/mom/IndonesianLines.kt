package com.likehome.app.mom

import com.likehome.app.core.Character

internal object IndonesianLines {
    val pack = LinePack(
        mom = listOf(
            listOf(
                "Selamat pagi, Nak. Sarapan sudah siap.",
                "Bangun, sayang! Teh manisnya sudah jadi.",
                "Pagi, si tukang tidur. Waktunya bangun.",
                "Matahari sudah terbit, kamu juga harus bangun, sayang.",
                "Ibu bikin nasi goreng kesukaanmu. Ayo sebelum dingin.",
                "Bangun, Nak. Hari ini pasti hari yang baik.",
                "Selamat pagi! Gordennya sudah Ibu buka.",
                "Botol minummu sudah Ibu isi. Ayo bangun.",
                "Bangunnya pelan-pelan, tapi bangun ya. Ibu sayang kamu.",
                "Pagi, cintaku. Sikat gigi dulu, sarapan sudah di meja.",
            ),
            listOf(
                "Lima menit jadi sepuluh. Sarapanmu keburu dingin.",
                "\"Lima menit lagi\" itu kemarin juga Ibu dengar.",
                "Nak, Ibu nggak mau panggil lagi. Bangun!",
                "Alarmmu bunyi terus. Kedengaran sampai dapur.",
                "Tehnya sudah dingin. Ibu nggak bikin lagi.",
                "Ibu nyalakan lampunya ya. Jangan protes.",
                "Selimutmu nggak akan berangkat kuliah untukmu.",
                "Tunda, tunda, tunda. Cuma itu yang kamu bisa?",
                "Ibu berdiri di depan pintumu. Ibu nggak akan pergi.",
                "Katanya hari ini mau bangun pagi. Ingat?",
            ),
            listOf(
                "Anak tetangga sudah lari pagi dari tadi.",
                "Sepupumu sudah sampai kantor. Kamu?",
                "Seumuranmu, Ibu bangun sebelum ayam berkokok.",
                "Anak-anak sebelah sudah di sekolah.",
                "Tukang roti saja sudah selesai keliling.",
                "Anak perempuan tetangga sudah senam DAN masak sarapan.",
                "Ayahmu bangun jam 5. Ibu cuma bilang.",
                "Satu kompleks sudah bangun. Cuma kamu yang tidur.",
                "Anak teman Ibu naik jabatan. Dia bangun tepat waktu.",
                "HP-mu lebih bersemangat daripada kamu sekarang.",
            ),
            listOf(
                "Cukup! Ibu naik bawa seember air!",
                "Ibu hitung sampai tiga. Satu…",
                "Ya sudah, tidur saja. Nanti Ibu bilang ke Ayah.",
                "Peringatan terakhir. Habis ini nggak ada sarapan buat siapa pun!",
                "Ibu telepon Nenek. Biar Nenek yang ngomong.",
                "Selimutnya Ibu ambil. Jangan coba-coba.",
                "Ibu besarkan kamu untuk ini? Begini pagi-pagi Ibu?",
                "Dua… jangan sampai Ibu bilang tiga.",
                "Panci presto sudah bunyi empat kali. Ibu juga.",
                "Ibu kirim ini ke grup keluarga.",
            ),
        ),
        characters = mapOf(
            Character.PAPA to listOf(
                listOf("Pagi. Bangun.", "Sudah waktunya. Bangun.", "Korannya sudah datang. Kamu juga harusnya.", "Pagi, jagoan. Ayo.", "Bangun. Ibumu sudah masak sarapan.", "Bangun. Jalan pagi jam tujuh."),
                listOf("Ayah bilang bangun.", "Ini kedua kalinya Ayah bilang.", "Hmm. Masih tidur.", "Ayah belum marah. Belum.", "Ibumu tanya. Ayah juga tanya.", "Lima menitnya sudah habis."),
                listOf("Seumuranmu Ayah sudah kerja dan punya motor.", "Anak tetangga sudah berangkat kerja.", "Ayah bangun jam 5 selama 30 tahun. Tanpa alarm.", "Ayah yang bayar kuliahmu. Berangkat.", "Sepupumu telepon. Dia sudah bangun.", "Motornya sudah siap. Kamu belum."),
                listOf("Ayah ke sana. Jangan bikin Ayah datang.", "Ayah ganti password WiFi.", "Sekarang Ayah kecewa.", "Sekali lagi tunda, remote TV punya Ayah.", "Ayah telepon pamanmu.", "Diam. Segitu marahnya Ayah."),
            ),
            Character.DADI to listOf(
                listOf("Bangun, sayangku. Dadi bikin kue manis.", "Selamat pagi, rajaku. Semoga Tuhan memberkatimu.", "Hartaku, matahari sudah mencarimu.", "Sini, Dadi buatkan yang hangat.", "Dadi sudah mendoakanmu. Sekarang bangun.", "Dadi simpankan potongan yang paling enak."),
                listOf("Waktu kecil kamu bangun sambil bernyanyi.", "Lutut Dadi sakit karena memanggilmu terus.", "Dadi sudah terlalu tua untuk naik tangga.", "Lima menit lagi, sayang?", "Nak, sarapan nggak bisa makan sendiri.", "Aduh, anak muda zaman sekarang…"),
                listOf("Di desa Dadi, jam segini anak-anak sudah memerah susu sapi.", "Ayahmu dulu sama persis. Lihat jadinya.", "Sepupumu telepon jam 6. Sudah bangun.", "Seumuranmu Dadi sudah punya tiga anak.", "Kakekmu nggak pernah bangun siang.", "Anak perempuan tetangga sudah cuci baju."),
                listOf("Ya Tuhan! Dadi datang bawa tongkat!", "Dadi ceritakan semua kisah bayimu ke semua orang.", "Jangan bikin Dadi naik. Lutut Dadi nggak kuat.", "Dadi bilang ke seluruh keluarga. Semuanya.", "Dadi telepon semua paman!", "Tongkat Dadi saja nggak tahan sama malasnya kamu."),
            ),
            Character.BOSS to listOf(
                listOf("Pagi! Sync singkat jam 9?", "Bangun. Kopinya saya traktir hari ini.", "Pagi! Ayo kejar target.", "Siap untuk hari yang produktif?", "Hari baru, peluang baru!", "Rapat pertamamu dengan kamar mandi."),
                listOf("Menindaklanjuti alarm sebelumnya…", "Sekadar follow up, sudah bangun?", "Melanjutkan tunda yang tadi.", "Saya pasang pengingat untuk pengingatnya.", "Mohon konfirmasi kehadiran untuk hari ini.", "Kami masih menunggu responsmu."),
                listOf("Rekanmu menyelesaikan tiga tugas sebelum sarapan.", "Rekan-rekanmu sudah di kopi kedua.", "Kompetitor sudah bangun.", "Saya cek kalendermu. Kamu terlambat.", "Anak magang datang lebih dulu darimu.", "Ada 47 email belum dibaca."),
                listOf("PENTING: mohon segera bangun.", "Sudah saya eskalasi ke ibumu.", "Penilaian kinerjamu ditunda selamanya.", "Rapat dengan bantal: dibatalkan.", "Rencana perbaikan kinerjamu mulai hari ini.", "Ini akan masuk penilaianmu."),
            ),
            Character.ABUELA to listOf(
                listOf("Buenos días, sayangku. Sarapan sudah siap.", "Bangun, mijo. Matahari sudah terbit.", "Ayo bangun, mi cielo. Ada kopi susu.", "Mi vida, hari ini ada churros.", "Ada kacang hangat, mijo.", "Tuhan memberkatimu, sayang. Bangun."),
                listOf("Ay, mijo, masih tidur?", "Lima menit jadi sepuluh, sayang.", "Churros-nya keburu dingin, lho.", "Jangan bikin aku ke sana.", "Aku dengar kamu mendengkur, lho.", "Mijo, kasur nggak bayar tagihan."),
                listOf("Sepupumu Lupe sudah kerja.", "Seumuranmu aku sudah punya enam anak dan kebun.", "Anak tetangga sudah ke gereja.", "Kakekmu bangun jam 4. Tanpa mengeluh.", "Pamanmu Pepe seumuranmu sudah punya usaha.", "Ayam jago sudah berkokok tiga kali."),
                listOf("Ay Dios mío! Aku ambil sandalnya!", "Aku hitung sampai tiga. Uno…", "Aku bilang ke ibumu. Lihat saja.", "Sandalnya sudah meluncur!", "Sandalnya sudah kulepas!", "Aku bilang ke seluruh keluarga di grup!"),
            ),
        ),
        monday = listOf("Selamat hari Senin, Nak. Minggu ini nggak akan mulai sendiri.", "Hari Senin. Hari ini nggak ada alasan."),
        weekend = listOf("Akhir pekan sih, tapi sarapan nggak menunggu seharian.", "Sabtu atau bukan, Ibu mau lihat kamu di meja makan."),
        newYear = "Selamat Tahun Baru, Nak! Tahun baru, alarm yang sama. Bangun!",
        bedtime = listOf(
            "Nak, tidur sekarang. Sudah jam {now}.",
            "Taruh HP-nya. Alarmmu jam {alarm}.",
            "Matikan lampunya. Ibu mengawasi.",
            "Tidur sekarang, kalau nggak besok datang ember air.",
        ),
        water = listOf(
            "Nak, minum air. Kulitmu bakal berterima kasih.",
            "Satu gelas air. Sekarang. Ibu lihat lho.",
            "Kamu minum air atau cuma teh?",
        ),
        breakfast = listOf(
            "Waktunya sarapan. Jangan berani-berani dilewatkan.",
            "Makan sesuatu, Nak. Perut kosong bukan rencana.",
            "Sarapanmu menunggu. Ibu juga.",
        ),
        promises = listOf(
            "Aku janji besok tidak menunda alarm",
            "Aku sudah bangun dan turun dari kasur",
            "Hari ini aku bangun di panggilan pertama",
        ),
        awakePhrase = "Aku sudah bangun, %s!",
        awakeWords = setOf("bangun", "terbangun", "melek"),
    )
}
