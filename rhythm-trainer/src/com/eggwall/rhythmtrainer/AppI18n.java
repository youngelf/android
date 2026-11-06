package com.eggwall.rhythmtrainer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Lightweight 20-language runtime translation + auto-fit Canvas text helper for Rhythm Trainer.
 */
public final class AppI18n {
    private static String currentLang = "en";
    private static final Map<String, Map<String, String>> EXACT = new HashMap<String, Map<String, String>>();
    private static String cachedResLang = "";
    private static Resources cachedRes = null;

    static {
        add("Rhythm Trainer",
                "en", "Rhythm Trainer", "fr", "Entraîneur de Rythme", "it", "Rhythm Trainer",
                "de", "Rhythmus-Trainer", "es", "Entrenador de Ritmo", "zh", "节奏训练师",
                "ja", "リズム・トレーナー", "ko", "리듬 트레이너",
                "pt", "Treinador de Ritmo", "ru", "Тренажёр ритма", "ar", "مدرب الإيقاع",
                "hi", "रिदम ट्रेनर", "bn", "রিদম ট্রেইনার", "in", "Pelatih Ritme",
                "tr", "Ritim Eğitmeni", "vi", "Luyện Tiết Tấu", "pl", "Trener Rytmu",
                "nl", "Ritmetrainer", "th", "ฝึกจังหวะดนตรี", "uk", "Тренажер ритму");
        add("RHYTHM TRAINER",
                "en", "RHYTHM TRAINER", "fr", "ENTRAÎNEUR DE RYTHME", "it", "RHYTHM TRAINER",
                "de", "RHYTHMUS-TRAINER", "es", "ENTRENADOR DE RITMO", "zh", "节奏训练师",
                "ja", "リズム・トレーナー", "ko", "리듬 트레이너",
                "pt", "TREINADOR DE RITMO", "ru", "ТРЕНАЖЁР РИТМА", "ar", "مدرب الإيقاع",
                "hi", "रिदम ट्रेनर", "bn", "রিদম ট্রেইনার", "in", "PELATIH RITME",
                "tr", "RİTİM EĞİTMENİ", "vi", "LUYỆN TIẾT TẤU", "pl", "TRENER RYTMU",
                "nl", "RITMETRAINER", "th", "ฝึกจังหวะดนตรี", "uk", "ТРЕНАЖЕР РИТМУ");
        add("Single-Frequency Musical Rhythm Practice (440 Hz)",
                "en", "Single-Frequency Musical Rhythm Practice (440 Hz)",
                "fr", "Pratique du rythme musical à fréquence fixe (440 Hz)",
                "it", "Pratica ritmica musicale a frequenza fissa (440 Hz)",
                "de", "Rhythmustraining mit fester Tonhöhe (440 Hz)",
                "es", "Práctica de ritmo musical de frecuencia fija (440 Hz)",
                "zh", "单频音高节奏视唱与律动训练 (440 Hz)",
                "ja", "単一周波数による音楽リズム練習 (440 Hz)",
                "ko", "단일 주파수 음악 리듬 연습 (440 Hz)",
                "pt", "Prática de Ritmo Musical em Frequência Única (440 Hz)",
                "ru", "Практика музыкального ритма на одной частоте (440 Гц)",
                "ar", "تدريب الإيقاع الموسيقي بتردد ثابت (440 هرتز)",
                "hi", "एकल-आवृत्ति संगीत ताल अभ्यास (440 Hz)",
                "bn", "একক-ফ্রিকোয়েন্সি মিউজিক্যাল রিদম অনুশীলন (440 Hz)",
                "in", "Latihan Ritme Musik Frekuensi Tunggal (440 Hz)",
                "tr", "Tek Frekanslı Müzikal Ritim Çalışması (440 Hz)",
                "vi", "Luyện tiết tấu âm nhạc tần số đơn (440 Hz)",
                "pl", "Ćwiczenie rytmu muzycznego o stałej częstotliwości (440 Hz)",
                "nl", "Muzikale ritme-oefening op vaste frequentie (440 Hz)",
                "th", "ฝึกจังหวะดนตรีด้วยความถี่คงที่ (440 Hz)",
                "uk", "Практика музичного ритму на одній частоті (440 Гц)");
        add("Edit Mode",
                "en", "Edit Mode", "fr", "Mode Édition", "it", "Modifica", "de", "Bearbeiten",
                "es", "Modo Edición", "zh", "编辑模式", "ja", "編集モード", "ko", "편집 모드",
                "pt", "Modo Edição", "ru", "Редактор", "ar", "وضع التعديل", "hi", "एडिट मोड",
                "bn", "এডিট মোড", "in", "Mode Edit", "tr", "Düzenleme", "vi", "Chỉnh sửa",
                "pl", "Tryb Edycji", "nl", "Bewerken", "th", "โหมดแก้ไข", "uk", "Редактор");
        add("Done Editing",
                "en", "Done Editing", "fr", "Terminer", "it", "Fine Modifica", "de", "Fertig",
                "es", "Listo", "zh", "完成编辑", "ja", "編集完了", "ko", "편집 완료",
                "pt", "Concluir", "ru", "Готово", "ar", "تم التعديل", "hi", "संपादन पूर्ण",
                "bn", "সম্পাদনা শেষ", "in", "Selesai", "tr", "Bitti", "vi", "Xong",
                "pl", "Gotowe", "nl", "Klaar", "th", "เสร็จสิ้น", "uk", "Готово");
        add("Play",
                "en", "Play", "fr", "Jouer", "it", "Avvia", "de", "Start",
                "es", "Reproducir", "zh", "播放", "ja", "再生", "ko", "재생",
                "pt", "Tocar", "ru", "Пуск", "ar", "تشغيل", "hi", "चलाएं",
                "bn", "চালান", "in", "Mainkan", "tr", "Oynat", "vi", "Phát",
                "pl", "Graj", "nl", "Speel", "th", "เล่น", "uk", "Пуск");
        add("Pause",
                "en", "Pause", "fr", "Pause", "it", "Pausa", "de", "Pause",
                "es", "Pausa", "zh", "暂停", "ja", "一時停止", "ko", "일시정지",
                "pt", "Pausa", "ru", "Пауза", "ar", "إيقاف", "hi", "रोकें",
                "bn", "বিরতি", "in", "Jeda", "tr", "Duraklat", "vi", "Tạm dừng",
                "pl", "Pauza", "nl", "Pauze", "th", "หยุด", "uk", "Пауза");
        add("Loop",
                "en", "Loop", "fr", "Boucle", "it", "Loop", "de", "Schleife",
                "es", "Bucle", "zh", "循环", "ja", "ループ", "ko", "루프",
                "pt", "Loop", "ru", "Цикл", "ar", "تكرار", "hi", "लूप",
                "bn", "লুপ", "in", "Ulang", "tr", "Döngü", "vi", "Lặp",
                "pl", "Pętla", "nl", "Lus", "th", "ลูป", "uk", "Цикл");
        add("Edit",
                "en", "Edit", "fr", "Éditer", "it", "Modifica", "de", "Ändern",
                "es", "Editar", "zh", "编辑", "ja", "編集", "ko", "편집",
                "pt", "Editar", "ru", "Изм.", "ar", "تعديل", "hi", "बदलें",
                "bn", "এডিট", "in", "Edit", "tr", "Düzenle", "vi", "Sửa",
                "pl", "Edytuj", "nl", "Bewerk", "th", "แก้ไข", "uk", "Змін.");
        add("Ready to play. Press Play for a 4..3..2..1 click count-in followed by the rhythm.",
                "en", "Ready to play. Press Play for a 4..3..2..1 click count-in followed by the rhythm.",
                "fr", "Prêt à jouer. Appuyez sur Jouer pour un décompte 4..3..2..1 suivi du rythme.",
                "it", "Pronto. Premi Avvia per il conteggio 4..3..2..1 seguito dal ritmo.",
                "de", "Spielbereit. Drücke Start für einen 4..3..2..1-Einzähler gefolgt vom Rhythmus.",
                "es", "Listo para tocar. Pulsa Reproducir para una cuenta 4..3..2..1 seguida del ritmo.",
                "zh", "准备就绪。点击“播放”开始 4..3..2..1 预备拍并演奏节奏。",
                "ja", "準備完了。「再生」を押すと 4..3..2..1 のカウントインに続いてリズムが再生されます。",
                "ko", "준비 완료. 재생을 누르면 4..3..2..1 예비박 후 리듬이 연주됩니다.",
                "pt", "Pronto para tocar. Pressione Tocar para a contagem 4..3..2..1 seguida do ritmo.",
                "ru", "Готово. Нажмите «Пуск» для отсчёта 4..3..2..1 и воспроизведения ритма.",
                "ar", "جاهز للتشغيل. اضغط تشغيل لعد تمهيدي 4..3..2..1 متبوعاً بالإيقاع.",
                "hi", "तैयार है। 4..3..2..1 काउंट-इन और ताल सुनने के लिए चलाएं दबाएं।",
                "bn", "প্রস্তুত। ৪..৩..২..১ কাউন্ট-ইন এবং রিদম শুনতে চালান চাপুন।",
                "in", "Siap dimainkan. Tekan Mainkan untuk hitungan 4..3..2..1 diikuti ritme.",
                "tr", "Çalmaya hazır. 4..3..2..1 ön sayım ve ritim için Oynat'a basın.",
                "vi", "Sẵn sàng. Nhấn Phát để đếm nhịp 4..3..2..1 trước khi vào tiết tấu.",
                "pl", "Gotowe. Naciśnij Graj, aby usłyszeć odliczanie 4..3..2..1 i rytm.",
                "nl", "Klaar om te spelen. Druk op Speel voor 4..3..2..1 voorafgaand aan het ritme.",
                "th", "พร้อมเล่น กดเล่นเพื่อฟังเสียงนับ 4..3..2..1 ตามด้วยจังหวะ",
                "uk", "Готово. Натисніть «Пуск» для відліку 4..3..2..1 та відтворення ритму.");
        add("Add Bar",
                "en", "Add Bar", "fr", "Mesure +", "it", "Battuta +", "de", "Takt +",
                "es", "Compás +", "zh", "添加小节", "ja", "小節 +", "ko", "마디 +",
                "pt", "Compasso +", "ru", "Такт +", "ar", "+ مازورة", "hi", "+ बार",
                "bn", "+ বার", "in", "+ Bar", "tr", "Ölçü +", "vi", "+ Ô nhịp",
                "pl", "Takt +", "nl", "Maat +", "th", "+ ห้อง", "uk", "Такт +");
        add("Remove Bar",
                "en", "Remove Bar", "fr", "Mesure -", "it", "Battuta -", "de", "Takt -",
                "es", "Compás -", "zh", "删除小节", "ja", "小節 -", "ko", "마디 -",
                "pt", "Compasso -", "ru", "Такт -", "ar", "- مازورة", "hi", "- बार",
                "bn", "- বার", "in", "- Bar", "tr", "Ölçü -", "vi", "- Ô nhịp",
                "pl", "Takt -", "nl", "Maat -", "th", "- ห้อง", "uk", "Такт -");
        add("Randomize",
                "en", "Randomize", "fr", "Aléatoire", "it", "Casuale", "de", "Zufall",
                "es", "Aleatorio", "zh", "随机生成", "ja", "ランダム", "ko", "무작위",
                "pt", "Aleatório", "ru", "Случайно", "ar", "عشوائي", "hi", "रैंडम",
                "bn", "র‍্যান্ডম", "in", "Acak", "tr", "Rastgele", "vi", "Ngẫu nhiên",
                "pl", "Losuj", "nl", "Willekeurig", "th", "สุ่มจังหวะ", "uk", "Випадково");
        add("Quarter Notes",
                "en", "Quarter Notes", "fr", "Noires (1/4)", "it", "Semiminime", "de", "Viertelnoten",
                "es", "Negras (1/4)", "zh", "四分音符", "ja", "4分音符", "ko", "4분음표",
                "pt", "Semínimas", "ru", "Четвертные", "ar", "نغمات ربعية", "hi", "चौथाई स्वर",
                "bn", "কোয়ার্টার নোট", "in", "Not 1/4", "tr", "Dörtlük", "vi", "Nốt Đen",
                "pl", "Ćwierćnuty", "nl", "Kwartnoten", "th", "โน้ตตัวดำ", "uk", "Чвертні");
        add("Exit Edit Mode",
                "en", "Exit Edit Mode", "fr", "Quitter l'édition", "it", "Esci da Modifica",
                "de", "Fertig", "es", "Salir de Edición", "zh", "退出编辑",
                "ja", "編集を終了", "ko", "편집 종료", "pt", "Sair da Edição",
                "ru", "Готово", "ar", "إنهاء التعديل", "hi", "बाहर निकलें",
                "bn", "সম্পাদনা শেষ", "in", "Keluar Edit", "tr", "Bitti",
                "vi", "Thoát sửa", "pl", "Gotowe", "nl", "Klaar",
                "th", "เสร็จสิ้น", "uk", "Готово");
        add("Count-In Clicks",
                "en", "Count-In Clicks", "fr", "Décompte", "it", "Conteggio iniziale",
                "de", "Einzähler", "es", "Cuenta inicial", "zh", "预备拍倒计时",
                "ja", "カウントイン", "ko", "예비박 카운트인");
        add("NOTES",
                "en", "NOTES", "fr", "NOTES", "it", "NOTE", "de", "NOTEN",
                "es", "NOTAS", "zh", "音符", "ja", "音符", "ko", "음표");
        add("RESTS",
                "en", "RESTS", "fr", "SILENCES", "it", "PAUSE", "de", "PAUSEN",
                "es", "SILENCIOS", "zh", "休止符", "ja", "休符", "ko", "쉼표");
        add("Whole",
                "en", "Whole", "fr", "Ronde", "it", "Semibreve", "de", "Ganze",
                "es", "Redonda", "zh", "全音符", "ja", "全音符", "ko", "온음표");
        add("Half",
                "en", "Half", "fr", "Blanche", "it", "Minima", "de", "Halbe",
                "es", "Blanca", "zh", "二分", "ja", "2分", "ko", "2분");
        add("Quarter",
                "en", "Quarter", "fr", "Noire", "it", "Semimin.", "de", "Viertel",
                "es", "Negra", "zh", "四分", "ja", "4分", "ko", "4분");
        add("Eighth",
                "en", "Eighth", "fr", "Croche", "it", "Croma", "de", "Achtel",
                "es", "Corchea", "zh", "八分", "ja", "8分", "ko", "8분");
        add("Sixteenth",
                "en", "Sixteenth", "fr", "D-croche", "it", "Semicr.", "de", "16tel",
                "es", "Semicor.", "zh", "十六分", "ja", "16分", "ko", "16분");
    }

    private AppI18n() {}

    private static void add(String key, String... kv) {
        Map<String, String> m = new HashMap<String, String>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        EXACT.put(key, m);
    }

    public static void initFromIntent(Context context, Intent intent) {
        String lang = null;
        if (intent != null && intent.hasExtra("demo_lang")) {
            lang = intent.getStringExtra("demo_lang");
        }
        if (lang == null || lang.trim().isEmpty()) {
            Locale loc = Locale.getDefault();
            lang = loc != null ? loc.getLanguage() : "en";
        }
        setLanguage(lang);
    }

    public static void setLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            currentLang = "en";
            return;
        }
        String norm = lang.trim().toLowerCase(Locale.US);
        int dash = norm.indexOf('-');
        if (dash > 0) norm = norm.substring(0, dash);
        int under = norm.indexOf('_');
        if (under > 0) norm = norm.substring(0, under);
        if ("id".equals(norm)) norm = "in";
        currentLang = norm;
    }

    public static String getLanguage() {
        return currentLang;
    }

    @SuppressWarnings("deprecation")
    private static Locale resolveLocale(String lang) {
        switch (lang) {
            case "en": return Locale.ENGLISH;
            case "fr": return Locale.FRENCH;
            case "it": return Locale.ITALIAN;
            case "de": return Locale.GERMAN;
            case "zh": return Locale.SIMPLIFIED_CHINESE;
            case "ja": return Locale.JAPANESE;
            case "ko": return Locale.KOREAN;
            default: break;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if ("pt".equals(lang)) {
                return Locale.forLanguageTag("pt-BR");
            }
            if ("in".equals(lang) || "id".equals(lang)) {
                return Locale.forLanguageTag("id");
            }
            return Locale.forLanguageTag(lang);
        }
        if ("pt".equals(lang)) {
            return new Locale("pt", "BR");
        }
        if ("in".equals(lang) || "id".equals(lang)) {
            return new Locale("in");
        }
        return new Locale(lang);
    }

    public static String str(Context context, int resId) {
        try {
            String lang = (currentLang == null || currentLang.isEmpty()) ? "en" : currentLang;
            Resources res = cachedRes;
            if (res == null || !lang.equals(cachedResLang)) {
                Configuration cfg = new Configuration(context.getResources().getConfiguration());
                Locale loc = resolveLocale(lang);
                cfg.setLocale(loc);
                res = context.createConfigurationContext(cfg).getResources();
                cachedRes = res;
                cachedResLang = lang;
            }
            return res.getString(resId);
        } catch (Exception e) {
            return context.getResources().getString(resId);
        }
    }

    public static String tr(String text) {
        if (text == null || text.isEmpty() || "en".equals(currentLang)) {
            return text;
        }
        Map<String, String> m = EXACT.get(text);
        if (m != null) {
            String hit = m.get(currentLang);
            if (hit != null) return hit;
        }
        if (text.startsWith("Time: ")) {
            String sig = text.substring(6);
            if ("fr".equals(currentLang)) return "Mesure: " + sig;
            if ("it".equals(currentLang)) return "Tempo: " + sig;
            if ("de".equals(currentLang)) return "Takt: " + sig;
            if ("es".equals(currentLang)) return "Compás: " + sig;
            if ("zh".equals(currentLang)) return "拍号: " + sig;
            if ("ja".equals(currentLang)) return "拍子: " + sig;
            if ("ko".equals(currentLang)) return "박자: " + sig;
        }
        if (text.startsWith("Tempo: ") && text.endsWith(" BPM")) {
            String num = text.substring(7, text.length() - 4);
            if ("zh".equals(currentLang)) return "速度: " + num + " BPM";
            if ("ja".equals(currentLang)) return "テンポ: " + num + " BPM";
            if ("ko".equals(currentLang)) return "템포: " + num + " BPM";
        }
        if (text.startsWith("Rhythm Note Durations (")) {
            String sub = text.substring(23, text.length() - 1);
            if ("fr".equals(currentLang)) return "Durées des notes (" + sub + ")";
            if ("it".equals(currentLang)) return "Durate delle note (" + sub + ")";
            if ("de".equals(currentLang)) return "Notenwerte (" + sub + ")";
            if ("es".equals(currentLang)) return "Duración de notas (" + sub + ")";
            if ("zh".equals(currentLang)) return "当前小节音符时值 (" + sub + ")";
            if ("ja".equals(currentLang)) return "リズム音価一覧 (" + sub + ")";
            if ("ko".equals(currentLang)) return "리듬 음표 길이 (" + sub + ")";
        }
        if (text.contains(" quarter beats (")) {
            int idx = text.indexOf(" quarter beats (");
            String n = text.substring(0, idx);
            String sig = text.substring(idx + 16);
            if ("fr".equals(currentLang)) return n + " temps (" + sig;
            if ("it".equals(currentLang)) return n + " movimenti (" + sig;
            if ("de".equals(currentLang)) return n + " Viertelschläge (" + sig;
            if ("es".equals(currentLang)) return n + " tiempos (" + sig;
            if ("zh".equals(currentLang)) return n + " 拍 (" + sig;
            if ("ja".equals(currentLang)) return n + " 拍 (" + sig;
            if ("ko".equals(currentLang)) return n + " 박 (" + sig;
        }
        if (text.startsWith("Bar ")) {
            String barWord = "Bar ";
            if ("fr".equals(currentLang)) barWord = "Mesure ";
            else if ("it".equals(currentLang)) barWord = "Battuta ";
            else if ("de".equals(currentLang)) barWord = "Takt ";
            else if ("es".equals(currentLang)) barWord = "Compás ";
            else if ("zh".equals(currentLang)) barWord = "小节 ";
            else if ("ja".equals(currentLang)) barWord = "小节 ".replace("小节", "小節");
            else if ("ko".equals(currentLang)) barWord = "마디 ";
            String r = barWord + text.substring(4);
            if ("fr".equals(currentLang)) {
                r = r.replace("Quarter Note", "Noire").replace("Eighth Note", "Croche")
                     .replace("Quarter Rest", "Soupir").replace("Half Note", "Blanche")
                     .replace("Sixteenth Note", "Double-croche").replace("Whole Note", "Ronde");
            } else if ("it".equals(currentLang)) {
                r = r.replace("Quarter Note", "Semiminima").replace("Eighth Note", "Croma")
                     .replace("Quarter Rest", "Pausa 1/4").replace("Half Note", "Minima")
                     .replace("Sixteenth Note", "Semicroma").replace("Whole Note", "Semibreve");
            } else if ("de".equals(currentLang)) {
                r = r.replace("Quarter Note", "Viertelnote").replace("Eighth Note", "Achtelnote")
                     .replace("Quarter Rest", "Viertelpause").replace("Half Note", "Halbe Note")
                     .replace("Sixteenth Note", "Sechzehntel").replace("Whole Note", "Ganze Note");
            } else if ("es".equals(currentLang)) {
                r = r.replace("Quarter Note", "Negra").replace("Eighth Note", "Corchea")
                     .replace("Quarter Rest", "Silencio 1/4").replace("Half Note", "Blanca")
                     .replace("Sixteenth Note", "Semicorchea").replace("Whole Note", "Redonda");
            } else if ("zh".equals(currentLang)) {
                r = r.replace("Quarter Note", "四分音符").replace("Eighth Note", "八分音符")
                     .replace("Quarter Rest", "四分休止符").replace("Half Note", "二分音符")
                     .replace("Sixteenth Note", "十六分音符").replace("Whole Note", "全音符");
            } else if ("ja".equals(currentLang)) {
                r = r.replace("Quarter Note", "4分音符").replace("Eighth Note", "8分音符")
                     .replace("Quarter Rest", "4分休符").replace("Half Note", "2分音符")
                     .replace("Sixteenth Note", "16分音符").replace("Whole Note", "全音符");
            } else if ("ko".equals(currentLang)) {
                r = r.replace("Quarter Note", "4분음표").replace("Eighth Note", "8분음표")
                     .replace("Quarter Rest", "4분쉼표").replace("Half Note", "2분음표")
                     .replace("Sixteenth Note", "16분음표").replace("Whole Note", "온음표");
            }
            return r;
        }
        return text;
    }

    public static void drawText(Canvas canvas, String text, float x, float y, Paint paint) {
        canvas.drawText(tr(text), x, y, paint);
    }

    public static void drawTextFit(Canvas canvas, String text, float x, float y, float maxWidth, Paint paint) {
        String s = tr(text);
        if (maxWidth <= 0f) {
            canvas.drawText(s, x, y, paint);
            return;
        }
        float origSize = paint.getTextSize();
        float w = paint.measureText(s);
        if (w > maxWidth && w > 0f) {
            float scale = maxWidth / w;
            if (scale < 0.58f) scale = 0.58f;
            paint.setTextSize(origSize * scale);
        }
        canvas.drawText(s, x, y, paint);
        paint.setTextSize(origSize);
    }

    @SuppressWarnings("deprecation")
    private static StaticLayout createStaticLayout(
            CharSequence text, TextPaint paint, int width, float spacingMult) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, spacingMult)
                    .setIncludePad(false)
                    .build();
        }
        return new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, spacingMult, 0f, false);
    }

    public static float drawScrollableHelpBlock(
            Canvas canvas,
            RectF contentArea,
            float scrollY,
            String[] sectionTitles,
            String[] sectionBodies,
            int[] accentColors,
            float titleSp,
            float bodySp,
            int bodyColor,
            int cardBgColor,
            float density) {
        TextPaint tpTitle = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tpTitle.setFakeBoldText(true);
        tpTitle.setTextSize(titleSp * density);

        TextPaint tpBody = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tpBody.setColor(bodyColor);
        tpBody.setTextSize(bodySp * density);

        Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float pad = 12f * density;
        float gap = 10f * density;
        int availW = Math.max(60, (int) (contentArea.width() - pad * 2f));

        int n = Math.min(sectionTitles.length, sectionBodies.length);
        StaticLayout[] titleLayouts = new StaticLayout[n];
        StaticLayout[] bodyLayouts = new StaticLayout[n];
        float[] cardHeights = new float[n];
        float totalH = 0f;

        for (int i = 0; i < n; i++) {
            String t = sectionTitles[i] != null ? sectionTitles[i] : "";
            String b = sectionBodies[i] != null ? sectionBodies[i] : "";
            titleLayouts[i] = createStaticLayout(t, tpTitle, availW, 1.12f);
            bodyLayouts[i] = createStaticLayout(b, tpBody, availW, 1.24f);
            float ch = pad * 1.6f + titleLayouts[i].getHeight() + 6f * density + bodyLayouts[i].getHeight();
            cardHeights[i] = ch;
            totalH += ch + (i < n - 1 ? gap : 0f);
        }

        float maxScroll = Math.max(0f, totalH - contentArea.height());
        float clampedScroll = Math.max(0f, Math.min(scrollY, maxScroll));

        int save = canvas.save();
        canvas.clipRect(contentArea);

        float curY = contentArea.top - clampedScroll;
        RectF cardRect = new RectF();
        for (int i = 0; i < n; i++) {
            float ch = cardHeights[i];
            cardRect.set(contentArea.left, curY, contentArea.right - (maxScroll > 1f ? 8f * density : 0f), curY + ch);
            if (cardRect.bottom >= contentArea.top && cardRect.top <= contentArea.bottom) {
                cardPaint.setStyle(Paint.Style.FILL);
                cardPaint.setColor(cardBgColor);
                canvas.drawRoundRect(cardRect, 12f * density, 12f * density, cardPaint);

                int accent = (accentColors != null && accentColors.length > 0)
                        ? accentColors[i % accentColors.length] : 0xFF38BDF8;
                cardPaint.setStyle(Paint.Style.STROKE);
                cardPaint.setStrokeWidth(1.5f * density);
                cardPaint.setColor((accent & 0x00FFFFFF) | 0x66000000);
                canvas.drawRoundRect(cardRect, 12f * density, 12f * density, cardPaint);

                tpTitle.setColor(accent);
                int s1 = canvas.save();
                canvas.translate(cardRect.left + pad, cardRect.top + pad * 0.75f);
                titleLayouts[i].draw(canvas);
                canvas.restoreToCount(s1);

                int s2 = canvas.save();
                canvas.translate(cardRect.left + pad, cardRect.top + pad * 0.75f + titleLayouts[i].getHeight() + 5f * density);
                bodyLayouts[i].draw(canvas);
                canvas.restoreToCount(s2);
            }
            curY += ch + gap;
        }

        if (maxScroll > 1f) {
            float trackL = contentArea.right - 4f * density;
            float trackR = contentArea.right - 1f * density;
            cardPaint.setStyle(Paint.Style.FILL);
            cardPaint.setColor(0x3394A3B8);
            canvas.drawRoundRect(new RectF(trackL, contentArea.top, trackR, contentArea.bottom), 2f * density, 2f * density, cardPaint);

            float thumbH = Math.max(24f * density, contentArea.height() * (contentArea.height() / totalH));
            float thumbTop = contentArea.top + (contentArea.height() - thumbH) * (clampedScroll / maxScroll);
            cardPaint.setColor(0xAA38BDF8);
            canvas.drawRoundRect(new RectF(trackL, thumbTop, trackR, thumbTop + thumbH), 2f * density, 2f * density, cardPaint);
        }

        canvas.restoreToCount(save);
        return maxScroll;
    }
}
