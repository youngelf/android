package com.eggwall.rhythmtrainer

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

object AppI18n {
    var currentLang by mutableStateOf("en")
        private set

    private val EXACT: Map<String, Map<String, String>> = buildMap {
        put("Rhythm Trainer", mapOf(
            "en" to "Rhythm Trainer", "fr" to "Entraîneur de Rythme", "it" to "Rhythm Trainer", "de" to "Rhythmus-Trainer", "es" to "Entrenador de Ritmo", "zh" to "节奏训练师", "ja" to "リズム・トレーナー", "ko" to "리듬 트레이너", "pt" to "Treinador de Ritmo", "ru" to "Тренажёр ритма", "ar" to "مدرب الإيقاع", "hi" to "रिदम ट्रेनर", "bn" to "রিদম ট্রেইনার", "in" to "Pelatih Ritme", "tr" to "Ritim Eğitmeni", "vi" to "Luyện Tiết Tấu", "pl" to "Trener Rytmu", "nl" to "Ritmetrainer", "th" to "ฝึกจังหวะดนตรี", "uk" to "Тренажер ритму"
        ))
        put("Single-Frequency Musical Rhythm Practice (440 Hz)", mapOf(
            "en" to "Single-Frequency Musical Rhythm Practice (440 Hz)", "fr" to "Pratique du rythme musical à fréquence fixe (440 Hz)", "it" to "Pratica ritmica musicale a frequenza fissa (440 Hz)", "de" to "Rhythmustraining mit fester Tonhöhe (440 Hz)", "es" to "Práctica de ritmo musical de frecuencia fija (440 Hz)", "zh" to "单频音高节奏视唱与律动训练 (440 Hz)", "ja" to "単一周波数による音楽リズム練習 (440 Hz)", "ko" to "단일 주파수 음악 리듬 연습 (440 Hz)", "pt" to "Prática de Ritmo Musical em Frequência Única (440 Hz)", "ru" to "Практика музыкального ритма на одной частоте (440 Гц)", "ar" to "تدريب الإيقاع الموسيقي بتردد ثابت (440 هرتز)", "hi" to "एकल-आवृत्ति संगीत ताल अभ्यास (440 Hz)", "bn" to "একক-ফ্রিকোয়েন্সি মিউজিক্যাল রিদম অনুশীলন (440 Hz)", "in" to "Latihan Ritme Musik Frekuensi Tunggal (440 Hz)", "tr" to "Tek Frekanslı Müzikal Ritim Çalışması (440 Hz)", "vi" to "Luyện tiết tấu âm nhạc tần số đơn (440 Hz)", "pl" to "Ćwiczenie rytmu muzycznego o stałej częstotliwości (440 Hz)", "nl" to "Muzikale ritme-oefening op vaste frequentie (440 Hz)", "th" to "ฝึกจังหวะดนตรีด้วยความถี่คงที่ (440 Hz)", "uk" to "Практика музичного ритму на одній частоті (440 Гц)"
        ))
        put("Edit Mode", mapOf(
            "en" to "Edit Mode", "fr" to "Mode Édition", "it" to "Modifica", "de" to "Bearbeiten", "es" to "Modo Edición", "zh" to "编辑模式", "ja" to "編集モード", "ko" to "편집 모드", "pt" to "Modo Edição", "ru" to "Редактор", "ar" to "وضع التعديل", "hi" to "एडिट मोड", "bn" to "এডিট মোড", "in" to "Mode Edit", "tr" to "Düzenleme", "vi" to "Chỉnh sửa", "pl" to "Tryb Edycji", "nl" to "Bewerken", "th" to "โหมดแก้ไข", "uk" to "Редактор"
        ))
        put("Done Editing", mapOf(
            "en" to "Done Editing", "fr" to "Terminer", "it" to "Fine Modifica", "de" to "Fertig", "es" to "Listo", "zh" to "完成编辑", "ja" to "編集完了", "ko" to "편집 완료", "pt" to "Concluir", "ru" to "Готово", "ar" to "تم التعديل", "hi" to "संपादन पूर्ण", "bn" to "সম্পাদনা শেষ", "in" to "Selesai", "tr" to "Bitti", "vi" to "Xong", "pl" to "Gotowe", "nl" to "Klaar", "th" to "เสร็จสิ้น", "uk" to "Готово"
        ))
        put("Play", mapOf(
            "en" to "Play", "fr" to "Jouer", "it" to "Avvia", "de" to "Start", "es" to "Reproducir", "zh" to "播放", "ja" to "再生", "ko" to "재생", "pt" to "Tocar", "ru" to "Пуск", "ar" to "تشغيل", "hi" to "चलाएं", "bn" to "চালান", "in" to "Mainkan", "tr" to "Oynat", "vi" to "Phát", "pl" to "Graj", "nl" to "Speel", "th" to "เล่น", "uk" to "Пуск"
        ))
        put("Pause", mapOf(
            "en" to "Pause", "fr" to "Pause", "it" to "Pausa", "de" to "Pause", "es" to "Pausa", "zh" to "暂停", "ja" to "一時停止", "ko" to "일시정지", "pt" to "Pausa", "ru" to "Пауза", "ar" to "إيقاف", "hi" to "रोकें", "bn" to "বিরতি", "in" to "Jeda", "tr" to "Duraklat", "vi" to "Tạm dừng", "pl" to "Pauza", "nl" to "Pauze", "th" to "หยุด", "uk" to "Пауза"
        ))
        put("Loop", mapOf(
            "en" to "Loop", "fr" to "Boucle", "it" to "Loop", "de" to "Schleife", "es" to "Bucle", "zh" to "循环", "ja" to "ループ", "ko" to "루프", "pt" to "Loop", "ru" to "Цикл", "ar" to "تكرار", "hi" to "लूप", "bn" to "লুপ", "in" to "Ulang", "tr" to "Döngü", "vi" to "Lặp", "pl" to "Pętla", "nl" to "Lus", "th" to "ลูป", "uk" to "Цикл"
        ))
        put("Edit", mapOf(
            "en" to "Edit", "fr" to "Éditer", "it" to "Modifica", "de" to "Ändern", "es" to "Editar", "zh" to "编辑", "ja" to "編集", "ko" to "편집", "pt" to "Editar", "ru" to "Изм.", "ar" to "تعديل", "hi" to "बदलें", "bn" to "এডিট", "in" to "Edit", "tr" to "Düzenle", "vi" to "Sửa", "pl" to "Edytuj", "nl" to "Bewerk", "th" to "แก้ไข", "uk" to "Змін."
        ))
        put("Ready to play. Press Play for a 4..3..2..1 click count-in followed by the rhythm.", mapOf(
            "en" to "Ready to play. Press Play for a 4..3..2..1 click count-in followed by the rhythm.", "fr" to "Prêt à jouer. Appuyez sur Jouer pour un décompte 4..3..2..1 suivi du rythme.", "it" to "Pronto. Premi Avvia per il conteggio 4..3..2..1 seguito dal ritmo.", "de" to "Spielbereit. Drücke Start für einen 4..3..2..1-Einzähler gefolgt vom Rhythmus.", "es" to "Listo para tocar. Pulsa Reproducir para una cuenta 4..3..2..1 seguida del ritmo.", "zh" to "准备就绪。点击“播放”开始 4..3..2..1 预备拍并演奏节奏。", "ja" to "準備完了。「再生」を押すと 4..3..2..1 のカウントインに続いてリズムが再生されます。", "ko" to "준비 완료. 재생을 누르면 4..3..2..1 예비박 후 리듬이 연주됩니다.", "pt" to "Pronto para tocar. Pressione Tocar para a contagem 4..3..2..1 seguida do ritmo.", "ru" to "Готово. Нажмите «Пуск» для отсчёта 4..3..2..1 и воспроизведения ритма.", "ar" to "جاهز للتشغيل. اضغط تشغيل لعد تمهيدي 4..3..2..1 متبوعاً بالإيقاع.", "hi" to "तैयार है। 4..3..2..1 काउंट-इन और ताल सुनने के लिए चलाएं दबाएं।", "bn" to "প্রস্তুত। ৪..৩..২..১ কাউন্ট-ইন এবং রিদম শুনতে চালান চাপুন।", "in" to "Siap dimainkan. Tekan Mainkan untuk hitungan 4..3..2..1 diikuti ritme.", "tr" to "Çalmaya hazır. 4..3..2..1 ön sayım ve ritim için Oynat'a basın.", "vi" to "Sẵn sàng. Nhấn Phát để đếm nhịp 4..3..2..1 trước khi vào tiết tấu.", "pl" to "Gotowe. Naciśnij Graj, aby usłyszeć odliczanie 4..3..2..1 i rytm.", "nl" to "Klaar om te spelen. Druk op Speel voor 4..3..2..1 voorafgaand aan het ritme.", "th" to "พร้อมเล่น กดเล่นเพื่อฟังเสียงนับ 4..3..2..1 ตามด้วยจังหวะ", "uk" to "Готово. Натисніть «Пуск» для відліку 4..3..2..1 та відтворення ритму."
        ))
        put("Add Bar", mapOf(
            "en" to "Add Bar", "fr" to "Ajouter Mesure", "it" to "Aggiungi Battuta", "de" to "Takt +", "es" to "Añadir Compás", "zh" to "添加小节", "ja" to "小節を追加", "ko" to "마디 추가", "pt" to "Adicionar Compasso", "ru" to "Добавить такт", "ar" to "إضافة مازورة", "hi" to "बार जोड़ें", "bn" to "বার যোগ করুন", "in" to "Tambah Bar", "tr" to "Ölçü Ekle", "vi" to "Thêm Ô Nhịp", "pl" to "Dodaj Takt", "nl" to "Maat +", "th" to "เพิ่มห้องเพลง", "uk" to "Додати такт"
        ))
        put("Remove Bar", mapOf(
            "en" to "Remove Bar", "fr" to "Suppr. Mesure", "it" to "Rimuovi Battuta", "de" to "Takt -", "es" to "Quitar Compás", "zh" to "删除小节", "ja" to "小節を削除", "ko" to "마디 삭제", "pt" to "Remover Compasso", "ru" to "Удалить такт", "ar" to "حذف مازورة", "hi" to "बार हटाएं", "bn" to "বার সরান", "in" to "Hapus Bar", "tr" to "Ölçü Sil", "vi" to "Xóa Ô Nhịp", "pl" to "Usuń Takt", "nl" to "Maat -", "th" to "ลบห้องเพลง", "uk" to "Видалити такт"
        ))
        put("Randomize", mapOf(
            "en" to "Randomize", "fr" to "Aléatoire", "it" to "Casuale", "de" to "Zufall", "es" to "Aleatorio", "zh" to "随机生成", "ja" to "ランダム", "ko" to "무작위 생성", "pt" to "Aleatório", "ru" to "Случайно", "ar" to "عشوائي", "hi" to "रैंडम", "bn" to "র‍্যান্ডম", "in" to "Acak", "tr" to "Rastgele", "vi" to "Ngẫu nhiên", "pl" to "Losuj", "nl" to "Willekeurig", "th" to "สุ่มจังหวะ", "uk" to "Випадково"
        ))
        put("Quarter Notes", mapOf(
            "en" to "Quarter Notes", "fr" to "Noires (1/4)", "it" to "Semiminime", "de" to "Viertelnoten", "es" to "Negras (1/4)", "zh" to "重置四分音符", "ja" to "4分音符に初期化", "ko" to "4분음표 초기화", "pt" to "Semínimas", "ru" to "Четвертные", "ar" to "نغمات ربعية", "hi" to "चौथाई स्वर", "bn" to "কোয়ার্টার নোট", "in" to "Not Seperempat", "tr" to "Dörtlük Notalar", "vi" to "Nốt Đen", "pl" to "Ćwierćnuty", "nl" to "Kwartnoten", "th" to "โน้ตตัวดำ", "uk" to "Чвертні ноти"
        ))
        put("Exit Edit Mode", mapOf(
            "en" to "Exit Edit Mode", "fr" to "Quitter l'édition", "it" to "Esci da Modifica", "de" to "Bearbeitung beenden", "es" to "Salir de Edición", "zh" to "退出编辑", "ja" to "編集を終了", "ko" to "편집 종료", "pt" to "Sair da Edição", "ru" to "Выйти из ред.", "ar" to "إنهاء التعديل", "hi" to "एडिट मोड से बाहर", "bn" to "এডিট মোড ত্যাগ", "in" to "Keluar Edit", "tr" to "Düzenlemeden Çık", "vi" to "Thoát Chỉnh Sửa", "pl" to "Zakończ Edycję", "nl" to "Sluit Bewerken", "th" to "ออกจากโหมดแก้ไข", "uk" to "Вийти з ред."
        ))
    }

    fun initFromIntent(context: Context, intent: Intent?) {
        var lang: String? = null
        if (intent != null && intent.hasExtra("demo_lang")) {
            lang = intent.getStringExtra("demo_lang")
        }
        if (lang.isNullOrBlank()) {
            lang = Locale.getDefault()?.language ?: "en"
        }
        setLanguage(lang)
    }

    fun setLanguage(lang: String?) {
        if (lang.isNullOrBlank()) {
            currentLang = "en"
            return
        }
        var norm = lang.trim().lowercase(Locale.US)
        val dash = norm.indexOf('-')
        if (dash > 0) norm = norm.substring(0, dash)
        val under = norm.indexOf('_')
        if (under > 0) norm = norm.substring(0, under)
        if (norm == "id") norm = "in"
        currentLang = norm
    }

    private var cachedResLang: String = ""
    private var cachedRes: android.content.res.Resources? = null

    fun str(context: Context, resId: Int): String {
        return try {
            val lang = currentLang.ifBlank { "en" }
            var res = cachedRes
            if (res == null || lang != cachedResLang) {
                val cfg = android.content.res.Configuration(context.resources.configuration)
                val loc = when (lang) {
                    "zh" -> Locale.SIMPLIFIED_CHINESE
                    "pt" -> Locale("pt", "BR")
                    "in", "id" -> Locale("in")
                    else -> Locale(lang)
                }
                cfg.setLocale(loc)
                res = context.createConfigurationContext(cfg).resources
                cachedRes = res
                cachedResLang = lang
            }
            res!!.getString(resId)
        } catch (e: Exception) {
            context.resources.getString(resId)
        }
    }

    fun tr(text: String): String {
        val lang = currentLang
        if (text.isEmpty() || lang == "en") return text
        EXACT[text]?.get(lang)?.let { return it }
        if (text.startsWith("Time: ")) {
            val sig = text.substring(6)
            return when (lang) {
                "fr" -> "Mesure: $sig"
                "it" -> "Tempo: $sig"
                "de" -> "Takt: $sig"
                "es" -> "Compás: $sig"
                "zh" -> "拍号: $sig"
                "ja" -> "拍子: $sig"
                "ko" -> "박자: $sig"
                else -> text
            }
        }
        if (text.startsWith("Tempo: ") && text.endsWith(" BPM")) {
            val num = text.substring(7, text.length - 4)
            return when (lang) {
                "zh" -> "速度: $num BPM"
                "ja" -> "テンポ: $num BPM"
                "ko" -> "템포: $num BPM"
                else -> text
            }
        }
        if (text.startsWith("Rhythm Note Durations (")) {
            val sub = text.substring(23, text.length - 1)
            return when (lang) {
                "fr" -> "Durées des notes ($sub)"
                "it" -> "Durate delle note ($sub)"
                "de" -> "Notenwerte ($sub)"
                "es" -> "Duración de notas ($sub)"
                "zh" -> "当前小节音符时值 ($sub)"
                "ja" -> "リズム音価一覧 ($sub)"
                "ko" -> "리듬 음표 길이 ($sub)"
                else -> text
            }
        }
        if (text.contains(" quarter beats (")) {
            val idx = text.indexOf(" quarter beats (")
            val n = text.substring(0, idx)
            val sig = text.substring(idx + 16)
            return when (lang) {
                "fr" -> "$n temps ($sig"
                "it" -> "$n movimenti ($sig"
                "de" -> "$n Viertelschläge ($sig"
                "es" -> "$n tiempos ($sig"
                "zh" -> "$n 拍 ($sig"
                "ja" -> "$n 拍 ($sig"
                "ko" -> "$n 박 ($sig"
                else -> text
            }
        }
        if (text.startsWith("Bar ")) {
            var r = text
            val barWord = when (lang) {
                "fr" -> "Mesure "
                "it" -> "Battuta "
                "de" -> "Takt "
                "es" -> "Compás "
                "zh" -> "小节 "
                "ja" -> "小節 "
                "ko" -> "마디 "
                else -> "Bar "
            }
            r = barWord + r.substring(4)
            r = when (lang) {
                "fr" -> r.replace("Quarter Note", "Noire").replace("Eighth Note", "Croche").replace("Quarter Rest", "Soupir").replace("Half Note", "Blanche").replace("Sixteenth Note", "Double-croche")
                "it" -> r.replace("Quarter Note", "Semiminima").replace("Eighth Note", "Croma").replace("Quarter Rest", "Pausa 1/4").replace("Half Note", "Minima").replace("Sixteenth Note", "Semicroma")
                "de" -> r.replace("Quarter Note", "Viertelnote").replace("Eighth Note", "Achtelnote").replace("Quarter Rest", "Viertelpause").replace("Half Note", "Halbe Note").replace("Sixteenth Note", "Sechzehntel")
                "es" -> r.replace("Quarter Note", "Negra").replace("Eighth Note", "Corchea").replace("Quarter Rest", "Silencio 1/4").replace("Half Note", "Blanca").replace("Sixteenth Note", "Semicorchea")
                "zh" -> r.replace("Quarter Note", "四分音符").replace("Eighth Note", "八分音符").replace("Quarter Rest", "四分休止符").replace("Half Note", "二分音符").replace("Sixteenth Note", "十六分音符")
                "ja" -> r.replace("Quarter Note", "4分音符").replace("Eighth Note", "8分音符").replace("Quarter Rest", "4分休符").replace("Half Note", "2分音符").replace("Sixteenth Note", "16分音符")
                "ko" -> r.replace("Quarter Note", "4분음표").replace("Eighth Note", "8분음표").replace("Quarter Rest", "4분쉼표").replace("Half Note", "2분음표").replace("Sixteenth Note", "16분음표")
                else -> r
            }
            return r
        }
        return text
    }
}
