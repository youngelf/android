package com.eggwall.licktrainer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AppI18n {
    private static volatile String sLang = "en";
    private static final Map<String, Map<String, String>> EXACT = new HashMap<>();

    static {
        putExact_0();
        putExact_1();
        putExact_2();
        putExact_3();
        putExact_4();
        putExact_5();
        putExact_6();
        putExact_7();
        putExact_8();
        putExact_9();
        putExact_10();
        putExact_11();
        putExact_12();
        putExact_13();
        putExact_14();
        putExact_15();
        putExact_16();
        putExact_17();
        putExact_18();
        putExact_19();
        putExact_20();
        putExact_21();
        putExact_22();
        putExact_23();
        putExact_24();
        putExact_25();
        putExact_26();
        putExact_27();
        putExact_28();
        putExact_29();
        putExact_30();
        putExact_31();
        putExact_32();
        putExact_33();
        putExact_34();
        putExact_35();
        putExact_36();
        putExact_37();
        putExact_38();
        putExact_39();
        putExact_40();
        putExact_41();
        putExact_42();
        putExact_43();
        putExact_44();
        putExact_45();
        putExact_46();
        putExact_47();
        putExact_48();
        putExact_49();
        putExact_50();
        putExact_51();
        putExact_52();
        putExact_53();
        putExact_54();
        putExact_55();
        putExact_56();
        putExact_57();
        putExact_58();
        putExact_59();
        putExact_60();
        putExact_61();
        putExact_62();
        putExact_63();
        putExact_64();
        putExact_65();
        putExact_66();
        putExact_67();
        putExact_68();
        putExact_69();
        putExact_70();
        putExact_71();
        putExact_72();
        putExact_73();
        putExact_74();
        putExact_75();
        putExact_76();
        putExact_77();
        putExact_78();
        putExact_79();
        putExact_80();
        putExact_81();
        putExact_82();
        putExact_83();
        putExact_84();
        putExact_85();
        putExact_86();
        putExact_87();
        putExact_88();
        putExact_89();
        putExact_90();
        putExact_91();
        putExact_92();
        putExact_93();
        putExact_94();
        putExact_95();
        putExact_96();
        putExact_97();
        putExact_98();
        putExact_99();
        putExact_100();
        putExact_101();
        putExact_102();
        putExact_103();
        putExact_104();
        putExact_105();
        putExact_106();
        putExact_107();
        putExact_108();
        putExact_109();
        putExact_110();
        putExact_111();
        putExact_112();
        putExact_113();
        putExact_114();
        putExact_115();
        putExact_116();
        putExact_117();
        putExact_118();
        putExact_119();
        putExact_120();
        putExact_121();
        putExact_122();
        putExact_123();
        putExact_124();
        putExact_125();
        putExact_126();
        putExact_127();
        putExact_128();
        putExact_129();
        putExact_130();
        putExact_131();
        putExact_132();
        putExact_133();
        putExact_134();
        putExact_135();
        putExact_136();
        putExact_137();
        putExact_138();
        putExact_139();
        putExact_140();
        putExact_141();
        putExact_142();
        putExact_143();
        putExact_144();
        putExact_145();
        putExact_146();
    }

    private static void putExact_0() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "LOCK");
        m.put("fr", "VERR.");
        m.put("it", "BLOCCA");
        m.put("de", "SPERR.");
        m.put("es", "BLOQ.");
        m.put("zh", "锁定");
        m.put("ja", "固定");
        m.put("ko", "잠금");
        m.put("pt", "TRAVAR");
        m.put("ru", "БЛОК");
        m.put("ar", "قفل");
        m.put("hi", "लॉक");
        m.put("bn", "লক");
        m.put("in", "KUNCI");
        m.put("tr", "KİLİT");
        m.put("vi", "KHÓA");
        m.put("pl", "BLOKUJ");
        m.put("nl", "SLOT");
        m.put("th", "ล็อก");
        m.put("uk", "БЛОК");
        EXACT.put("LOCK", m);
    }
    private static void putExact_1() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "7THS");
        m.put("fr", "7ÈMES");
        m.put("it", "7ª");
        m.put("de", "SEPT.");
        m.put("es", "7ª");
        m.put("zh", "七和弦");
        m.put("ja", "7th");
        m.put("ko", "7화음");
        m.put("pt", "7ªs");
        m.put("ru", "СЕПТ");
        m.put("ar", "سباعية");
        m.put("hi", "7वें");
        m.put("bn", "৭ম");
        m.put("in", "AKOR 7");
        m.put("tr", "7'Lİ");
        m.put("vi", "HỢP ÂM 7");
        m.put("pl", "7-KI");
        m.put("nl", "7e");
        m.put("th", "คอร์ด 7");
        m.put("uk", "СЕПТ");
        EXACT.put("7THS", m);
    }
    private static void putExact_2() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "TRIADS");
        m.put("fr", "TRIADES");
        m.put("it", "TRIADI");
        m.put("de", "DREIKL.");
        m.put("es", "TRÍADAS");
        m.put("zh", "三和弦");
        m.put("ja", "三和音");
        m.put("ko", "3화음");
        m.put("pt", "TRÍADES");
        m.put("ru", "ТРИЗВ.");
        m.put("ar", "ثلاثية");
        m.put("hi", "ट्रायड");
        m.put("bn", "ট্রায়াড");
        m.put("in", "TRIAD");
        m.put("tr", "ÜÇLÜ");
        m.put("vi", "BA ÂM");
        m.put("pl", "TRÓJDŹW.");
        m.put("nl", "DRIEKL.");
        m.put("th", "ไทรแอด");
        m.put("uk", "ТРИЗВ.");
        EXACT.put("TRIADS", m);
    }
    private static void putExact_3() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "TAP / SWIPE");
        m.put("fr", "TOUCHER / GLISSER");
        m.put("it", "TOCCA / SCORRI");
        m.put("de", "TIPPEN / WISCHEN");
        m.put("es", "TOCAR / DESLIZAR");
        m.put("zh", "点击 / 滑动");
        m.put("ja", "タップ / スワイプ");
        m.put("ko", "탭 / 스와이프");
        m.put("pt", "TOCAR / DESLIZAR");
        m.put("ru", "НАЖМ. / СВАЙП");
        m.put("ar", "انقر / اسحب");
        m.put("hi", "टैप / स्वाइप");
        m.put("bn", "ট্যাপ / সোয়াইপ");
        m.put("in", "KETUK / GESER");
        m.put("tr", "DOKUN / KAYDIR");
        m.put("vi", "CHẠM / VUỐT");
        m.put("pl", "DOTKNIJ / PRZESUŃ");
        m.put("nl", "TIK / SWIPE");
        m.put("th", "แตะ / ปัด");
        m.put("uk", "ТОРКН. / СВАЙП");
        EXACT.put("TAP / SWIPE", m);
    }
    private static void putExact_4() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "< 1/3 Open/CAGED >");
        m.put("fr", "< 1/3 Ouvert/CAGED >");
        m.put("it", "< 1/3 Aperto/CAGED >");
        m.put("de", "< 1/3 Offen/CAGED >");
        m.put("es", "< 1/3 Abierto/CAGED >");
        m.put("zh", "< 1/3 开放/CAGED >");
        m.put("ja", "< 1/3 オープン/CAGED >");
        m.put("ko", "< 1/3 오픈/CAGED >");
        m.put("pt", "< 1/3 Aberto/CAGED >");
        m.put("ru", "< 1/3 Откр./CAGED >");
        m.put("ar", "< 1/3 مفتوح/CAGED >");
        m.put("hi", "< 1/3 ओपन/CAGED >");
        m.put("bn", "< 1/3 ওপেন/CAGED >");
        m.put("in", "< 1/3 Terbuka/CAGED >");
        m.put("tr", "< 1/3 Açık/CAGED >");
        m.put("vi", "< 1/3 Mở/CAGED >");
        m.put("pl", "< 1/3 Otwarte/CAGED >");
        m.put("nl", "< 1/3 Open/CAGED >");
        m.put("th", "< 1/3 เปิด/CAGED >");
        m.put("uk", "< 1/3 Відкр./CAGED >");
        EXACT.put("< 1/3 Open/CAGED >", m);
    }
    private static void putExact_5() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "< 2/3 E-Barre (6str) >");
        m.put("fr", "< 2/3 Barré Mi (6c) >");
        m.put("it", "< 2/3 Barré Mi (6c) >");
        m.put("de", "< 2/3 E-Barré (6S) >");
        m.put("es", "< 2/3 Cejilla Mi (6c) >");
        m.put("zh", "< 2/3 E型横按 (6弦) >");
        m.put("ja", "< 2/3 Eバレー (6弦) >");
        m.put("ko", "< 2/3 E바레 (6현) >");
        m.put("pt", "< 2/3 Pestana Mi (6c) >");
        m.put("ru", "< 2/3 E-баррэ (6стр) >");
        m.put("ar", "< 2/3 باريه E (6) >");
        m.put("hi", "< 2/3 E-बार (6तार) >");
        m.put("bn", "< 2/3 E-বার (6তার) >");
        m.put("in", "< 2/3 Barre E (6snr) >");
        m.put("tr", "< 2/3 E-Bare (6tel) >");
        m.put("vi", "< 2/3 Chặn E (6dây) >");
        m.put("pl", "< 2/3 E-Barre (6str) >");
        m.put("nl", "< 2/3 E-Barré (6sn) >");
        m.put("th", "< 2/3 ทาบ E (6สาย) >");
        m.put("uk", "< 2/3 E-баре (6стр) >");
        EXACT.put("< 2/3 E-Barre (6str) >", m);
    }
    private static void putExact_6() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "< 3/3 A-Barre (5str) >");
        m.put("fr", "< 3/3 Barré La (5c) >");
        m.put("it", "< 3/3 Barré La (5c) >");
        m.put("de", "< 3/3 A-Barré (5S) >");
        m.put("es", "< 3/3 Cejilla La (5c) >");
        m.put("zh", "< 3/3 A型横按 (5弦) >");
        m.put("ja", "< 3/3 Aバレー (5弦) >");
        m.put("ko", "< 3/3 A바레 (5현) >");
        m.put("pt", "< 3/3 Pestana Lá (5c) >");
        m.put("ru", "< 3/3 A-баррэ (5стр) >");
        m.put("ar", "< 3/3 باريه A (5) >");
        m.put("hi", "< 3/3 A-बार (5तार) >");
        m.put("bn", "< 3/3 A-বার (5তার) >");
        m.put("in", "< 3/3 Barre A (5snr) >");
        m.put("tr", "< 3/3 A-Bare (5tel) >");
        m.put("vi", "< 3/3 Chặn A (5dây) >");
        m.put("pl", "< 3/3 A-Barre (5str) >");
        m.put("nl", "< 3/3 A-Barré (5sn) >");
        m.put("th", "< 3/3 ทาบ A (5สาย) >");
        m.put("uk", "< 3/3 A-баре (5стр) >");
        EXACT.put("< 3/3 A-Barre (5str) >", m);
    }
    private static void putExact_7() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "> LOOP");
        m.put("fr", "> BOUCLE");
        m.put("it", "> LOOP");
        m.put("de", "> SCHLEIFE");
        m.put("es", "> BUCLE");
        m.put("zh", "> 循环");
        m.put("ja", "> ループ");
        m.put("ko", "> 루프");
        m.put("pt", "> LOOP");
        m.put("ru", "> ЦИКЛ");
        m.put("ar", "> تكرار");
        m.put("hi", "> लूप");
        m.put("bn", "> লুপ");
        m.put("in", "> ULANG");
        m.put("tr", "> DÖNGÜ");
        m.put("vi", "> LẶP");
        m.put("pl", "> PĘTLA");
        m.put("nl", "> LUS");
        m.put("th", "> ลูป");
        m.put("uk", "> ЦИКЛ");
        EXACT.put("> LOOP", m);
    }
    private static void putExact_8() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "|| PAUSE");
        m.put("fr", "|| PAUSE");
        m.put("it", "|| PAUSA");
        m.put("de", "|| PAUSE");
        m.put("es", "|| PAUSA");
        m.put("zh", "|| 暂停");
        m.put("ja", "|| 一時停止");
        m.put("ko", "|| 일시정지");
        m.put("pt", "|| PAUSA");
        m.put("ru", "|| ПАУЗА");
        m.put("ar", "|| إيقاف");
        m.put("hi", "|| विराम");
        m.put("bn", "|| বিরতি");
        m.put("in", "|| JEDA");
        m.put("tr", "|| DURAKLAT");
        m.put("vi", "|| TẠM DỪNG");
        m.put("pl", "|| PAUZA");
        m.put("nl", "|| PAUZE");
        m.put("th", "|| หยุดชั่วคราว");
        m.put("uk", "|| ПАУЗА");
        EXACT.put("|| PAUSE", m);
    }
    private static void putExact_9() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "+PROG");
        m.put("fr", "+PROG");
        m.put("it", "+PROG");
        m.put("de", "+PROG");
        m.put("es", "+PROG");
        m.put("zh", "+进行");
        m.put("ja", "+進行");
        m.put("ko", "+진행");
        m.put("pt", "+PROG");
        m.put("ru", "+ПРОГ");
        m.put("ar", "+تقدم");
        m.put("hi", "+प्रोग");
        m.put("bn", "+প্রোগ");
        m.put("in", "+PROG");
        m.put("tr", "+AKOR");
        m.put("vi", "+TIẾN TRÌNH");
        m.put("pl", "+PROG");
        m.put("nl", "+PROG");
        m.put("th", "+ทางคอร์ด");
        m.put("uk", "+ПРОГ");
        EXACT.put("+PROG", m);
    }
    private static void putExact_10() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "PRESETS");
        m.put("fr", "PRÉRÉGL.");
        m.put("it", "PRESET");
        m.put("de", "PRESETS");
        m.put("es", "AJUSTES");
        m.put("zh", "预设");
        m.put("ja", "プリセット");
        m.put("ko", "프리셋");
        m.put("pt", "PRESETS");
        m.put("ru", "ПРЕСЕТЫ");
        m.put("ar", "قوالب");
        m.put("hi", "प्रीसेट");
        m.put("bn", "প্রিসেট");
        m.put("in", "PRESET");
        m.put("tr", "HAZIR");
        m.put("vi", "MẪU SẴN");
        m.put("pl", "PRESETY");
        m.put("nl", "PRESETS");
        m.put("th", "พรีเซ็ต");
        m.put("uk", "ПРЕСЕТИ");
        EXACT.put("PRESETS", m);
    }
    private static void putExact_11() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "CLOSE");
        m.put("fr", "FERMER");
        m.put("it", "CHIUDI");
        m.put("de", "SCHLIESSEN");
        m.put("es", "CERRAR");
        m.put("zh", "关闭");
        m.put("ja", "閉じる");
        m.put("ko", "닫기");
        m.put("pt", "FECHAR");
        m.put("ru", "ЗАКРЫТЬ");
        m.put("ar", "إغلاق");
        m.put("hi", "बंद करें");
        m.put("bn", "বন্ধ");
        m.put("in", "TUTUP");
        m.put("tr", "KAPAT");
        m.put("vi", "ĐÓNG");
        m.put("pl", "ZAMKNIJ");
        m.put("nl", "SLUITEN");
        m.put("th", "ปิด");
        m.put("uk", "ЗАКРИТИ");
        EXACT.put("CLOSE", m);
    }
    private static void putExact_12() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Tonic Major");
        m.put("fr", "Tonique Majeure");
        m.put("it", "Tonica Maggiore");
        m.put("de", "Tonika Dur");
        m.put("es", "Tónica Mayor");
        m.put("zh", "主音大三和弦");
        m.put("ja", "トニック・メジャー");
        m.put("ko", "으뜸 장화음 (Tonic)");
        m.put("pt", "Tônica Maior");
        m.put("ru", "Тоника мажор");
        m.put("ar", "قرار كبير");
        m.put("hi", "टॉनिक मेजर");
        m.put("bn", "টনিক মেজর");
        m.put("in", "Tonika Mayor");
        m.put("tr", "Tonik Majör");
        m.put("vi", "Chủ âm Trưởng");
        m.put("pl", "Tonika dur");
        m.put("nl", "Tonica groot");
        m.put("th", "โทนิกเมเจอร์");
        m.put("uk", "Тоніка мажор");
        EXACT.put("Tonic Major", m);
    }
    private static void putExact_13() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Subdominant");
        m.put("fr", "Sous-dominante");
        m.put("it", "Sottodominante");
        m.put("de", "Subdominante");
        m.put("es", "Subdominante");
        m.put("zh", "下属和弦");
        m.put("ja", "サブドミナント");
        m.put("ko", "버금딸림화음 (Subdominant)");
        m.put("pt", "Subdominante");
        m.put("ru", "Субдоминанта");
        m.put("ar", "تحت المهيمن");
        m.put("hi", "सबडोमिनेंट");
        m.put("bn", "সাবডমিন্যান্ট");
        m.put("in", "Subdominan");
        m.put("tr", "Subdominant");
        m.put("vi", "Hạ át âm");
        m.put("pl", "Subdominanta");
        m.put("nl", "Subdominant");
        m.put("th", "ซับโดมินันต์");
        m.put("uk", "Субдомінанта");
        EXACT.put("Subdominant", m);
    }
    private static void putExact_14() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Dominant");
        m.put("fr", "Dominante");
        m.put("it", "Dominante");
        m.put("de", "Dominante");
        m.put("es", "Dominante");
        m.put("zh", "属和弦");
        m.put("ja", "ドミナント");
        m.put("ko", "딸림화음 (Dominant)");
        m.put("pt", "Dominante");
        m.put("ru", "Доминанта");
        m.put("ar", "المهيمن");
        m.put("hi", "डोमिनेंट");
        m.put("bn", "ডমিন্যান্ট");
        m.put("in", "Dominan");
        m.put("tr", "Dominant");
        m.put("vi", "Át âm");
        m.put("pl", "Dominanta");
        m.put("nl", "Dominant");
        m.put("th", "โดมินันต์");
        m.put("uk", "Домінанта");
        EXACT.put("Dominant", m);
    }
    private static void putExact_15() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Relative Minor");
        m.put("fr", "Relatif Mineur");
        m.put("it", "Relativa Minore");
        m.put("de", " Mollparallele");
        m.put("es", "Relativo Menor");
        m.put("zh", "关系小调");
        m.put("ja", "平行短調");
        m.put("ko", "나란한 단조");
        m.put("pt", "Relativo Menor");
        m.put("ru", "Параллельный минор");
        m.put("ar", "الصغير النسبي");
        m.put("hi", "रिलेटिव माइनर");
        m.put("bn", "রিলেটিভ মাইনর");
        m.put("in", "Minor Relatif");
        m.put("tr", "İlgili Minör");
        m.put("vi", "Thứ tương đương");
        m.put("pl", "Mol paralelny");
        m.put("nl", "Parallel mineur");
        m.put("th", "ไมเนอร์สัมพันธ์");
        m.put("uk", "Паралельний мінор");
        EXACT.put("Relative Minor", m);
    }
    private static void putExact_16() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Supertonic Min");
        m.put("fr", "Sus-tonique Min");
        m.put("it", "Sopratonica Min");
        m.put("de", "Supertonika Moll");
        m.put("es", "Supertónica Men");
        m.put("zh", "上主音小和弦");
        m.put("ja", "スーパートニック・マイナー");
        m.put("ko", "위으뜸 단화음");
        m.put("pt", "Supertônica Men");
        m.put("ru", "Супертоника минор");
        m.put("ar", "فوق القرار صغير");
        m.put("hi", "सुपरटॉनिक माइनर");
        m.put("bn", "সুপারটনিক মাইনর");
        m.put("in", "Supertonika Minor");
        m.put("tr", "Süpertonik Minör");
        m.put("vi", "Thượng chủ âm Thứ");
        m.put("pl", "Supertonika mol");
        m.put("nl", "Supertonica mineur");
        m.put("th", "ซูเปอร์โทนิกไมเนอร์");
        m.put("uk", "Супертоніка мінор");
        EXACT.put("Supertonic Min", m);
    }
    private static void putExact_17() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Mediant Minor");
        m.put("fr", "Médiante Mineure");
        m.put("it", "Mediante Minore");
        m.put("de", "Mediante Moll");
        m.put("es", "Mediante Menor");
        m.put("zh", "中音小和弦");
        m.put("ja", "メディアント・マイナー");
        m.put("ko", "가온 단화음");
        m.put("pt", "Mediante Menor");
        m.put("ru", "Медианта минор");
        m.put("ar", "الوسطى الصغير");
        m.put("hi", "मीडिएंट माइनर");
        m.put("bn", "মিডিয়ান্ট মাইনর");
        m.put("in", "Median Minor");
        m.put("tr", "Mediant Minör");
        m.put("vi", "Trung âm Thứ");
        m.put("pl", "Medianta mol");
        m.put("nl", "Mediant mineur");
        m.put("th", "มีเดียนต์ไมเนอร์");
        m.put("uk", "Медіанта мінор");
        EXACT.put("Mediant Minor", m);
    }
    private static void putExact_18() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Leading-Tone Dim");
        m.put("fr", "Sensible Diminuée");
        m.put("it", "Sensibile Diminuita");
        m.put("de", "Leitton Vermindert");
        m.put("es", "Sensible Disminuida");
        m.put("zh", "导音减和弦");
        m.put("ja", "導音ディミニッシュ");
        m.put("ko", "이끔 감화음");
        m.put("pt", "Sensível Diminuta");
        m.put("ru", "Вводный уменьш.");
        m.put("ar", "حساس ناقص");
        m.put("hi", "लीडिंग-टोन डिम");
        m.put("bn", "লিডিং-টোন ডিম");
        m.put("in", " Nada Peka Dim");
        m.put("tr", "Yeden Eksik");
        m.put("vi", "Âm dẫn Giảm");
        m.put("pl", "Dźwięk prowadzący zm.");
        m.put("nl", "Leidtoon verm.");
        m.put("th", "ลีดดิงโทนดิม");
        m.put("uk", "Ввідний зменш.");
        EXACT.put("Leading-Tone Dim", m);
    }
    private static void putExact_19() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "FRETBOARD");
        m.put("fr", "MANCHE");
        m.put("it", "TASTIERA");
        m.put("de", "GRIFFBRETT");
        m.put("es", "DIAPASÓN");
        m.put("zh", "指板探索");
        m.put("ja", "指板ナビ");
        m.put("ko", "프렛보드");
        m.put("pt", "BRAÇO");
        m.put("ru", "ГРИФ");
        m.put("ar", "الفريت");
        m.put("hi", "फ़्रेटबोर्ड");
        m.put("bn", "ফ্রেটবোর্ড");
        m.put("in", "FRETBOARD");
        m.put("tr", "KLAVYE");
        m.put("vi", "CẦN ĐÀN");
        m.put("pl", "GRYF");
        m.put("nl", "HALS");
        m.put("th", "คอฟิงเกอร์บอร์ด");
        m.put("uk", "ГРИФ");
        EXACT.put("FRETBOARD", m);
    }
    private static void putExact_20() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Scales");
        m.put("fr", "Gammes");
        m.put("it", "Scale");
        m.put("de", "Skalen");
        m.put("es", "Escalas");
        m.put("zh", "音阶");
        m.put("ja", "スケール");
        m.put("ko", "스케일");
        m.put("pt", "Escalas");
        m.put("ru", "Гаммы");
        m.put("ar", "سلالم");
        m.put("hi", "स्केल");
        m.put("bn", "স্কেল");
        m.put("in", "Skala");
        m.put("tr", "Gamlar");
        m.put("vi", "Âm giai");
        m.put("pl", "Skale");
        m.put("nl", "Toonladders");
        m.put("th", "สเกล");
        m.put("uk", "Гами");
        EXACT.put("Scales", m);
    }
    private static void putExact_21() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Chords");
        m.put("fr", "Accords");
        m.put("it", "Accordi");
        m.put("de", "Akkorde");
        m.put("es", "Acordes");
        m.put("zh", "和弦");
        m.put("ja", "コード");
        m.put("ko", "코드");
        m.put("pt", "Acordes");
        m.put("ru", "Аккорды");
        m.put("ar", "أوتار");
        m.put("hi", "कॉर्ड्स");
        m.put("bn", "কর্ড");
        m.put("in", "Akor");
        m.put("tr", "Akorlar");
        m.put("vi", "Hợp âm");
        m.put("pl", "Akordy");
        m.put("nl", "Akkoorden");
        m.put("th", "คอร์ด");
        m.put("uk", "Акорди");
        EXACT.put("Chords", m);
    }
    private static void putExact_22() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Finder");
        m.put("fr", "Détecteur");
        m.put("it", "Trova");
        m.put("de", "Finder");
        m.put("es", "Buscador");
        m.put("zh", "查和弦");
        m.put("ja", "検索");
        m.put("ko", "코드 찾기");
        m.put("pt", "Busca");
        m.put("ru", "Поиск");
        m.put("ar", "كاشف");
        m.put("hi", "फ़ाइंडर");
        m.put("bn", "ফাইন্ডার");
        m.put("in", "Pencari");
        m.put("tr", "Bulucu");
        m.put("vi", "Tìm kiếm");
        m.put("pl", "Szukaj");
        m.put("nl", "Zoeker");
        m.put("th", "ค้นหา");
        m.put("uk", "Пошук");
        EXACT.put("Finder", m);
    }
    private static void putExact_23() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "1: Scales");
        m.put("fr", "1: Gammes");
        m.put("it", "1: Scale");
        m.put("de", "1: Skalen");
        m.put("es", "1: Escalas");
        m.put("zh", "1: 音阶");
        m.put("ja", "1: スケール");
        m.put("ko", "1: 스케일");
        m.put("pt", "1: Escalas");
        m.put("ru", "1: Гаммы");
        m.put("ar", "1: سلالم");
        m.put("hi", "1: स्केल");
        m.put("bn", "1: স্কেল");
        m.put("in", "1: Skala");
        m.put("tr", "1: Gamlar");
        m.put("vi", "1: Âm giai");
        m.put("pl", "1: Skale");
        m.put("nl", "1: Ladders");
        m.put("th", "1: สเกล");
        m.put("uk", "1: Гами");
        EXACT.put("1: Scales", m);
    }
    private static void putExact_24() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "2: Chords");
        m.put("fr", "2: Accords");
        m.put("it", "2: Accordi");
        m.put("de", "2: Akkorde");
        m.put("es", "2: Acordes");
        m.put("zh", "2: 和弦");
        m.put("ja", "2: コード");
        m.put("ko", "2: 코드");
        m.put("pt", "2: Acordes");
        m.put("ru", "2: Аккорды");
        m.put("ar", "2: أوتار");
        m.put("hi", "2: कॉर्ड्स");
        m.put("bn", "2: কর্ড");
        m.put("in", "2: Akor");
        m.put("tr", "2: Akorlar");
        m.put("vi", "2: Hợp âm");
        m.put("pl", "2: Akordy");
        m.put("nl", "2: Akkoorden");
        m.put("th", "2: คอร์ด");
        m.put("uk", "2: Акорди");
        EXACT.put("2: Chords", m);
    }
    private static void putExact_25() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "3: Finder");
        m.put("fr", "3: Détecteur");
        m.put("it", "3: Trova");
        m.put("de", "3: Finder");
        m.put("es", "3: Buscador");
        m.put("zh", "3: 查和弦");
        m.put("ja", "3: 検索");
        m.put("ko", "3: 코드 찾기");
        m.put("pt", "3: Busca");
        m.put("ru", "3: Поиск");
        m.put("ar", "3: كاشف");
        m.put("hi", "3: फ़ाइंडर");
        m.put("bn", "3: ফাইন্ডার");
        m.put("in", "3: Pencari");
        m.put("tr", "3: Bulucu");
        m.put("vi", "3: Tìm kiếm");
        m.put("pl", "3: Szukaj");
        m.put("nl", "3: Zoeker");
        m.put("th", "3: ค้นหา");
        m.put("uk", "3: Пошук");
        EXACT.put("3: Finder", m);
    }
    private static void putExact_26() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "▶ Scale");
        m.put("fr", "▶ Gamme");
        m.put("it", "▶ Scala");
        m.put("de", "▶ Skala");
        m.put("es", "▶ Escala");
        m.put("zh", "▶ 音阶");
        m.put("ja", "▶ スケール");
        m.put("ko", "▶ 스케일");
        m.put("pt", "▶ Escala");
        m.put("ru", "▶ Гамма");
        m.put("ar", "▶ سلم");
        m.put("hi", "▶ स्केल");
        m.put("bn", "▶ স্কেল");
        m.put("in", "▶ Skala");
        m.put("tr", "▶ Gam");
        m.put("vi", "▶ Âm giai");
        m.put("pl", "▶ Skala");
        m.put("nl", "▶ Ladder");
        m.put("th", "▶ สเกล");
        m.put("uk", "▶ Гама");
        EXACT.put("▶ Scale", m);
    }
    private static void putExact_27() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "▶ Strum");
        m.put("fr", "▶ Gratter");
        m.put("it", "▶ Pennata");
        m.put("de", "▶ Strum");
        m.put("es", "▶ Rasgueo");
        m.put("zh", "▶ 扫弦");
        m.put("ja", "▶ ストローク");
        m.put("ko", "▶ 스트럼");
        m.put("pt", "▶ Rasguear");
        m.put("ru", "▶ Бой");
        m.put("ar", "▶ عزف");
        m.put("hi", "▶ स्ट्रम");
        m.put("bn", "▶ স্ট্রাম");
        m.put("in", "▶ Genjreng");
        m.put("tr", "▶ Ritim");
        m.put("vi", "▶ Quạt chả");
        m.put("pl", "▶ Bicze");
        m.put("nl", "▶ Slag");
        m.put("th", "▶ ดีดคอร์ด");
        m.put("uk", "▶ Бій");
        EXACT.put("▶ Strum", m);
    }
    private static void putExact_28() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "POSITION");
        m.put("fr", "POSITION");
        m.put("it", "POSIZIONE");
        m.put("de", "LAGE");
        m.put("es", "POSICIÓN");
        m.put("zh", "把位");
        m.put("ja", "ポジション");
        m.put("ko", "포지션");
        m.put("pt", "POSIÇÃO");
        m.put("ru", "ПОЗИЦИЯ");
        m.put("ar", "الموضع");
        m.put("hi", "पोजीशन");
        m.put("bn", "পজিশন");
        m.put("in", "POSISI");
        m.put("tr", "POZİSYON");
        m.put("vi", "THẾ TAY");
        m.put("pl", "POZYCJA");
        m.put("nl", "POSITIE");
        m.put("th", "ตำแหน่ง");
        m.put("uk", "ПОЗИЦІЯ");
        EXACT.put("POSITION", m);
    }
    private static void putExact_29() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "ALL NECK");
        m.put("fr", "TOUT MANCHE");
        m.put("it", "TUTTA TAST.");
        m.put("de", "GANZER HALS");
        m.put("es", "TODO MÁSTIL");
        m.put("zh", "全指板");
        m.put("ja", "全指板");
        m.put("ko", "전체 지판");
        m.put("pt", "TODO BRAÇO");
        m.put("ru", "ВЕСЬ ГРИФ");
        m.put("ar", "كل العنق");
        m.put("hi", "पूरा नेक");
        m.put("bn", "পুরো নেক");
        m.put("in", "SEMUA NECK");
        m.put("tr", "TÜM SAP");
        m.put("vi", "TOÀN CẦN");
        m.put("pl", "CAŁY GRYF");
        m.put("nl", "HELE HALS");
        m.put("th", "ทั้งคอ");
        m.put("uk", "ВЕСЬ ГРИФ");
        EXACT.put("ALL NECK", m);
    }
    private static void putExact_30() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Frets 0–15");
        m.put("fr", "Frettes 0–15");
        m.put("it", "Tasti 0–15");
        m.put("de", "Bünde 0–15");
        m.put("es", "Trastes 0–15");
        m.put("zh", "0–15 品");
        m.put("ja", "0–15 フレット");
        m.put("ko", "0–15 프렛");
        m.put("pt", "Trastes 0–15");
        m.put("ru", "Лады 0–15");
        m.put("ar", "فريت 0–15");
        m.put("hi", "फ़्रेट 0–15");
        m.put("bn", "ফ্রেট 0–15");
        m.put("in", "Fret 0–15");
        m.put("tr", "Perde 0–15");
        m.put("vi", "Phím 0–15");
        m.put("pl", "Progi 0–15");
        m.put("nl", "Frets 0–15");
        m.put("th", "เฟรต 0–15");
        m.put("uk", "Лади 0–15");
        EXACT.put("Frets 0–15", m);
    }
    private static void putExact_31() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Lbl: R/3/5");
        m.put("fr", "Étiq: F/3/5");
        m.put("it", "Etic: R/3/5");
        m.put("de", "Bez: R/3/5");
        m.put("es", "Etiq: T/3/5");
        m.put("zh", "标签: 根/3/5");
        m.put("ja", "表示: R/3/5");
        m.put("ko", "표시: R/3/5");
        m.put("pt", "Rót: T/3/5");
        m.put("ru", "Мет: Т/3/5");
        m.put("ar", "تسمية: R/3/5");
        m.put("hi", "लेबल: R/3/5");
        m.put("bn", "লেবেল: R/3/5");
        m.put("in", "Lbl: R/3/5");
        m.put("tr", "Etk: K/3/5");
        m.put("vi", "Nhãn: R/3/5");
        m.put("pl", "Etyk: P/3/5");
        m.put("nl", "Lbl: R/3/5");
        m.put("th", "ป้าย: R/3/5");
        m.put("uk", "Міт: Т/3/5");
        EXACT.put("Lbl: R/3/5", m);
    }
    private static void putExact_32() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Lbl: Notes");
        m.put("fr", "Étiq: Notes");
        m.put("it", "Etic: Note");
        m.put("de", "Bez: Noten");
        m.put("es", "Etiq: Notas");
        m.put("zh", "标签: 音名");
        m.put("ja", "表示: 音名");
        m.put("ko", "표시: 음이름");
        m.put("pt", "Rót: Notas");
        m.put("ru", "Мет: Ноты");
        m.put("ar", "تسمية: نغمات");
        m.put("hi", "लेबल: स्वर");
        m.put("bn", "লেবেল: নোট");
        m.put("in", "Lbl: Nada");
        m.put("tr", "Etk: Notalar");
        m.put("vi", "Nhãn: Nốt");
        m.put("pl", "Etyk: Nuty");
        m.put("nl", "Lbl: Noten");
        m.put("th", "ป้าย: โน้ต");
        m.put("uk", "Міт: Ноти");
        EXACT.put("Lbl: Notes", m);
    }
    private static void putExact_33() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Lbl: Deg");
        m.put("fr", "Étiq: Deg");
        m.put("it", "Etic: Gradi");
        m.put("de", "Bez: Stufen");
        m.put("es", "Etiq: Grados");
        m.put("zh", "标签: 级数");
        m.put("ja", "表示: 度数");
        m.put("ko", "표시: 도수");
        m.put("pt", "Rót: Graus");
        m.put("ru", "Мет: Ступ.");
        m.put("ar", "تسمية: درجات");
        m.put("hi", "लेबल: डिग्री");
        m.put("bn", "লেবেল: ডিগ্রি");
        m.put("in", "Lbl: Derajat");
        m.put("tr", "Etk: Derece");
        m.put("vi", "Nhãn: Bậc");
        m.put("pl", "Etyk: Stop.");
        m.put("nl", "Lbl: Trap");
        m.put("th", "ป้าย: ขั้นคู่");
        m.put("uk", "Міт: Ступ.");
        EXACT.put("Lbl: Deg", m);
    }
    private static void putExact_34() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Hand: Righty");
        m.put("fr", "Main: Droitier");
        m.put("it", "Mano: Destro");
        m.put("de", "Hand: Rechts");
        m.put("es", "Mano: Diestro");
        m.put("zh", "手型: 右手");
        m.put("ja", "利き手: 右");
        m.put("ko", "손잡이: 오른손");
        m.put("pt", "Mão: Destro");
        m.put("ru", "Рука: Правша");
        m.put("ar", "اليد: يمين");
        m.put("hi", "हाथ: दायां");
        m.put("bn", "হাত: ডানহাতি");
        m.put("in", "Tangan: Kanan");
        m.put("tr", "El: Sağlak");
        m.put("vi", "Tay: Thuận phải");
        m.put("pl", "Ręka: Prawa");
        m.put("nl", "Hand: Rechts");
        m.put("th", "มือ: ถนัดขวา");
        m.put("uk", "Рука: Правша");
        EXACT.put("Hand: Righty", m);
    }
    private static void putExact_35() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Hand: Lefty");
        m.put("fr", "Main: Gaucher");
        m.put("it", "Mano: Mancino");
        m.put("de", "Hand: Links");
        m.put("es", "Mano: Zurdo");
        m.put("zh", "手型: 左手");
        m.put("ja", "利き手: 左");
        m.put("ko", "손잡이: 왼손");
        m.put("pt", "Mão: Canhoto");
        m.put("ru", "Рука: Левша");
        m.put("ar", "اليد: يسار");
        m.put("hi", "हाथ: बायां");
        m.put("bn", "হাত: বাঁহাতি");
        m.put("in", "Tangan: Kidal");
        m.put("tr", "El: Solak");
        m.put("vi", "Tay: Thuận trái");
        m.put("pl", "Ręka: Lewa");
        m.put("nl", "Hand: Links");
        m.put("th", "มือ: ถนัดซ้าย");
        m.put("uk", "Рука: Лівша");
        EXACT.put("Hand: Lefty", m);
    }
    private static void putExact_36() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Quiz: Off");
        m.put("fr", "Quiz: Non");
        m.put("it", "Quiz: Off");
        m.put("de", "Quiz: Aus");
        m.put("es", "Quiz: No");
        m.put("zh", "测验: 关");
        m.put("ja", "クイズ: 切");
        m.put("ko", "퀴즈: 끔");
        m.put("pt", "Quiz: Desl.");
        m.put("ru", "Тест: Выкл");
        m.put("ar", "اختبار: إيقاف");
        m.put("hi", "क्विज़: बंद");
        m.put("bn", "কুইজ: বন্ধ");
        m.put("in", "Kuis: Mati");
        m.put("tr", "Test: Kapalı");
        m.put("vi", "Đố: Tắt");
        m.put("pl", "Quiz: Wył.");
        m.put("nl", "Quiz: Uit");
        m.put("th", "ควิซ: ปิด");
        m.put("uk", "Тест: Вимк");
        EXACT.put("Quiz: Off", m);
    }
    private static void putExact_37() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Quiz: Note");
        m.put("fr", "Quiz: Note");
        m.put("it", "Quiz: Nota");
        m.put("de", "Quiz: Note");
        m.put("es", "Quiz: Nota");
        m.put("zh", "测验: 音名");
        m.put("ja", "クイズ: 音名");
        m.put("ko", "퀴즈: 음이름");
        m.put("pt", "Quiz: Nota");
        m.put("ru", "Тест: Нота");
        m.put("ar", "اختبار: نغمة");
        m.put("hi", "क्विज़: स्वर");
        m.put("bn", "কুইজ: নোট");
        m.put("in", "Kuis: Nada");
        m.put("tr", "Test: Nota");
        m.put("vi", "Đố: Nốt");
        m.put("pl", "Quiz: Nuta");
        m.put("nl", "Quiz: Noot");
        m.put("th", "ควิซ: โน้ต");
        m.put("uk", "Тест: Нота");
        EXACT.put("Quiz: Note", m);
    }
    private static void putExact_38() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Quiz: Intv");
        m.put("fr", "Quiz: Intv");
        m.put("it", "Quiz: Intv");
        m.put("de", "Quiz: Intv");
        m.put("es", "Quiz: Intv");
        m.put("zh", "测验: 音程");
        m.put("ja", "クイズ: 音程");
        m.put("ko", "퀴즈: 음정");
        m.put("pt", "Quiz: Intv");
        m.put("ru", "Тест: Инт.");
        m.put("ar", "اختبار: مسافة");
        m.put("hi", "क्विज़: अंतराल");
        m.put("bn", "কুইজ: ব্যবধান");
        m.put("in", "Kuis: Intv");
        m.put("tr", "Test: Aralık");
        m.put("vi", "Đố: Quãng");
        m.put("pl", "Quiz: Int.");
        m.put("nl", "Quiz: Intv");
        m.put("th", "ควิซ: ขั้นคู่");
        m.put("uk", "Тест: Інт.");
        EXACT.put("Quiz: Intv", m);
    }
    private static void putExact_39() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Dorian Mode ▾");
        m.put("fr", "Mode Dorien ▾");
        m.put("it", "Modo Dorico ▾");
        m.put("de", "Dorischer Modus ▾");
        m.put("es", "Modo Dórico ▾");
        m.put("zh", "多利亚调式 (Dorian) ▾");
        m.put("ja", "ドリアン・モード ▾");
        m.put("ko", "도리안 모드 ▾");
        m.put("pt", "Modo Dórico ▾");
        m.put("ru", "Дорийский лад ▾");
        m.put("ar", "مقام دوريان ▾");
        m.put("hi", "डोरियन मोड ▾");
        m.put("bn", "ডোরিয়ান মোড ▾");
        m.put("in", "Mode Dorian ▾");
        m.put("tr", "Dorian Modu ▾");
        m.put("vi", "Điệu Dorian ▾");
        m.put("pl", "Skala dorycka ▾");
        m.put("nl", "Dorische modus ▾");
        m.put("th", "โหมดดอเรียน ▾");
        m.put("uk", "Дорійський лад ▾");
        EXACT.put("Dorian Mode ▾", m);
    }
    private static void putExact_40() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Minor Pentatonic ▾");
        m.put("fr", "Pentatonique Mineure ▾");
        m.put("it", "Pentatonica Minore ▾");
        m.put("de", "Moll-Pentatonik ▾");
        m.put("es", "Pentatónica Menor ▾");
        m.put("zh", "小调五声音阶 ▾");
        m.put("ja", "マイナー・ペンタトニック ▾");
        m.put("ko", "마이너 펜타토닉 ▾");
        m.put("pt", "Pentatônica Menor ▾");
        m.put("ru", "Минорная пентатоника ▾");
        m.put("ar", "خماسي صغير ▾");
        m.put("hi", "माइनर पेंटाटोनिक ▾");
        m.put("bn", "মাইনর পেন্টাটোনিক ▾");
        m.put("in", "Pentatonik Minor ▾");
        m.put("tr", "Minör Pentatonik ▾");
        m.put("vi", "Ngũ cung Thứ ▾");
        m.put("pl", "Pentatonika molowa ▾");
        m.put("nl", "Mineur pentatonisch ▾");
        m.put("th", "ไมเนอร์เพนทาโทนิก ▾");
        m.put("uk", "Мінорна пентатоніка ▾");
        EXACT.put("Minor Pentatonic ▾", m);
    }
    private static void putExact_41() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Blues Scale ▾");
        m.put("fr", "Gamme Blues ▾");
        m.put("it", "Scala Blues ▾");
        m.put("de", "Blues-Skala ▾");
        m.put("es", "Escala de Blues ▾");
        m.put("zh", "布鲁斯音阶 ▾");
        m.put("ja", "ブルース・スケール ▾");
        m.put("ko", "블루스 스케일 ▾");
        m.put("pt", "Escala de Blues ▾");
        m.put("ru", "Блюзовая гамма ▾");
        m.put("ar", "سلم البلوز ▾");
        m.put("hi", "ब्लूज़ स्केल ▾");
        m.put("bn", "ব্লুজ স্কেল ▾");
        m.put("in", "Skala Blues ▾");
        m.put("tr", "Blues Gamı ▾");
        m.put("vi", "Âm giai Blues ▾");
        m.put("pl", "Skala bluesowa ▾");
        m.put("nl", "Bluesladder ▾");
        m.put("th", "สเกลบลูส์ ▾");
        m.put("uk", "Блюзова гама ▾");
        EXACT.put("Blues Scale ▾", m);
    }
    private static void putExact_42() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Tune: Std E");
        m.put("fr", "Accord: Mi Std");
        m.put("it", "Accord: Mi Std");
        m.put("de", "Stimm: Std E");
        m.put("es", "Afin: Mi Est");
        m.put("zh", "定弦: 标准E");
        m.put("ja", "調弦: 標準E");
        m.put("ko", "튜닝: 표준 E");
        m.put("pt", "Afin: Mi Padrão");
        m.put("ru", "Строй: Станд. E");
        m.put("ar", "ضبط: E قياسي");
        m.put("hi", "ट्यून: मानक E");
        m.put("bn", "টিউন: স্ট্যান্ডার্ড E");
        m.put("in", "Stem: Std E");
        m.put("tr", "Akort: Std Mi");
        m.put("vi", "Dây: Chuẩn E");
        m.put("pl", "Strój: Std E");
        m.put("nl", "Stem: Std E");
        m.put("th", "ตั้งสาย: มาตรฐาน E");
        m.put("uk", "Стрій: Станд. E");
        EXACT.put("Tune: Std E", m);
    }
    private static void putExact_43() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "OPEN");
        m.put("fr", "VIDE");
        m.put("it", "VUOTA");
        m.put("de", "LEER");
        m.put("es", "AIRE");
        m.put("zh", "空弦");
        m.put("ja", "開放");
        m.put("ko", "개방현");
        m.put("pt", "SOLTA");
        m.put("ru", "ОТКР");
        m.put("ar", "مفتوح");
        m.put("hi", "ओपन");
        m.put("bn", "ওপেন");
        m.put("in", "LEPAS");
        m.put("tr", "AÇIK");
        m.put("vi", "BUÔNG");
        m.put("pl", "PUSTA");
        m.put("nl", "OPEN");
        m.put("th", "สายเปล่า");
        m.put("uk", "ВІДКР");
        EXACT.put("OPEN", m);
    }
    private static void putExact_44() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Major (Ionian) ▾");
        m.put("fr", "Majeur (Ionien) ▾");
        m.put("it", "Maggiore (Ionico) ▾");
        m.put("de", "Dur (Ionisch) ▾");
        m.put("es", "Mayor (Jónico) ▾");
        m.put("zh", "自然大调 (Ionian) ▾");
        m.put("ja", "メジャー (アイオニアン) ▾");
        m.put("ko", "메이저 (이오니안) ▾");
        m.put("pt", "Maior (Jônio) ▾");
        m.put("ru", "Мажор (Ионийский) ▾");
        m.put("ar", "كبير (إيوني) ▾");
        m.put("hi", "मेजर (आयोनियन) ▾");
        m.put("bn", "মেজর (আয়োনিয়ান) ▾");
        m.put("in", "Mayor (Ionian) ▾");
        m.put("tr", "Majör (İyonya) ▾");
        m.put("vi", "Trưởng (Ionian) ▾");
        m.put("pl", "Dur (Jońska) ▾");
        m.put("nl", "Groot (Ionisch) ▾");
        m.put("th", "เมเจอร์ (ไอโอเนียน) ▾");
        m.put("uk", "Мажор (Іонійський) ▾");
        EXACT.put("Major (Ionian) ▾", m);
    }
    private static void putExact_45() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Natural Minor (Aeolian) ▾");
        m.put("fr", "Mineur Naturel (Éolien) ▾");
        m.put("it", "Minore Naturale (Eolio) ▾");
        m.put("de", "Natürliches Moll (Äolisch) ▾");
        m.put("es", "Menor Natural (Eólico) ▾");
        m.put("zh", "自然小调 (Aeolian) ▾");
        m.put("ja", "ナチュラル・マイナー ▾");
        m.put("ko", "내추럴 마이너 (에올리안) ▾");
        m.put("pt", "Menor Natural (Eólio) ▾");
        m.put("ru", "Натуральный минор ▾");
        m.put("ar", "صغير طبيعي ▾");
        m.put("hi", "नेचुरल माइनर ▾");
        m.put("bn", "ন্যাচারাল মাইনর ▾");
        m.put("in", "Minor Alami ▾");
        m.put("tr", "Doğal Minör ▾");
        m.put("vi", "Thứ Tự Nhiên ▾");
        m.put("pl", "Mol naturalny ▾");
        m.put("nl", "Natuurlijk mineur ▾");
        m.put("th", "เนเชอรัลไมเนอร์ ▾");
        m.put("uk", "Натуральний мінор ▾");
        EXACT.put("Natural Minor (Aeolian) ▾", m);
    }
    private static void putExact_46() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Major Pentatonic ▾");
        m.put("fr", "Pentatonique Majeure ▾");
        m.put("it", "Pentatonica Maggiore ▾");
        m.put("de", "Dur-Pentatonik ▾");
        m.put("es", "Pentatónica Mayor ▾");
        m.put("zh", "大调五声音阶 ▾");
        m.put("ja", "メジャー・ペンタトニック ▾");
        m.put("ko", "메이저 펜타토닉 ▾");
        m.put("pt", "Pentatônica Maior ▾");
        m.put("ru", "Мажорная пентатоника ▾");
        m.put("ar", "خماسي كبير ▾");
        m.put("hi", "मेजर पेंटाटोनिक ▾");
        m.put("bn", "মেজর পেন্টাটোনিক ▾");
        m.put("in", "Pentatonik Mayor ▾");
        m.put("tr", "Majör Pentatonik ▾");
        m.put("vi", "Ngũ cung Trưởng ▾");
        m.put("pl", "Pentatonika durowa ▾");
        m.put("nl", "Groot pentatonisch ▾");
        m.put("th", "เมเจอร์เพนทาโทนิก ▾");
        m.put("uk", "Мажорна пентатоніка ▾");
        EXACT.put("Major Pentatonic ▾", m);
    }
    private static void putExact_47() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "PENTATONIC");
        m.put("fr", "PENTATONIQUE");
        m.put("it", "PENTATONICA");
        m.put("de", "PENTATONIK");
        m.put("es", "PENTATÓNICA");
        m.put("zh", "五声音阶");
        m.put("ja", "ペンタトニック");
        m.put("ko", "펜타토닉");
        m.put("pt", "PENTATÔNICA");
        m.put("ru", "ПЕНТАТОНИКА");
        m.put("ar", "خماسي");
        m.put("hi", "पेंटाटोनिक");
        m.put("bn", "পেন্টাটোনিক");
        m.put("in", "PENTATONIK");
        m.put("tr", "PENTATONİK");
        m.put("vi", "NGŨ CUNG");
        m.put("pl", "PENTATONIKA");
        m.put("nl", "PENTATONISCH");
        m.put("th", "เพนทาโทนิก");
        m.put("uk", "ПЕНТАТОНІКА");
        EXACT.put("PENTATONIC", m);
    }
    private static void putExact_48() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "LEGATO/ARP");
        m.put("fr", "LEGATO/ARP");
        m.put("it", "LEGATO/ARP");
        m.put("de", "LEGATO/ARP");
        m.put("es", "LEGATO/ARP");
        m.put("zh", "连音/琶音");
        m.put("ja", "レガート/ARP");
        m.put("ko", "레가토/아르페지오");
        m.put("pt", "LEGATO/ARP");
        m.put("ru", "ЛЕГАТО/АРП");
        m.put("ar", "ليغاتو/أربيج");
        m.put("hi", "लेगाटो/अर्पेजियो");
        m.put("bn", "লেগাটো/আরপেজিও");
        m.put("in", "LEGATO/ARP");
        m.put("tr", "LEGATO/ARP");
        m.put("vi", "LEGATO/RẢI");
        m.put("pl", "LEGATO/ARP");
        m.put("nl", "LEGATO/ARP");
        m.put("th", "เลกาโต/อาร์เพจจิโอ");
        m.put("uk", "ЛЕГАТО/АРП");
        EXACT.put("LEGATO/ARP", m);
    }
    private static void putExact_49() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "ALT PICK");
        m.put("fr", "ALLER-RETOUR");
        m.put("it", "PENNATA ALT.");
        m.put("de", "WECHSELSCHLAG");
        m.put("es", "PÚA ALTERNA");
        m.put("zh", "交替拨弦");
        m.put("ja", "オルタネイト");
        m.put("ko", "얼터네이트 피킹");
        m.put("pt", "PALHETADA ALT");
        m.put("ru", "ПЕРЕМЕН. ШТРИХ");
        m.put("ar", "ريشة متبادلة");
        m.put("hi", "अल्टरनेट पिकिंग");
        m.put("bn", "অল্টারনেট পিকিং");
        m.put("in", "PETIKAN ALT");
        m.put("tr", "ALT PENA");
        m.put("vi", "PHÍM LUÂN PHIÊN");
        m.put("pl", "KOSTkowanie");
        m.put("nl", "WISSELSLAG");
        m.put("th", "ดีดสลับขึ้นลง");
        m.put("uk", "ЗМІННИЙ ШТРИХ");
        EXACT.put("ALT PICK", m);
    }
    private static void putExact_50() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "BLUES/ROCK");
        m.put("fr", "BLUES/ROCK");
        m.put("it", "BLUES/ROCK");
        m.put("de", "BLUES/ROCK");
        m.put("es", "BLUES/ROCK");
        m.put("zh", "布鲁斯/摇滚");
        m.put("ja", "ブルース/ロック");
        m.put("ko", "블루스/록");
        m.put("pt", "BLUES/ROCK");
        m.put("ru", "БЛЮЗ/РОК");
        m.put("ar", "بلوز/روك");
        m.put("hi", "ब्लूज़/रॉक");
        m.put("bn", "ব্লুজ/রক");
        m.put("in", "BLUES/ROCK");
        m.put("tr", "BLUES/ROCK");
        m.put("vi", "BLUES/ROCK");
        m.put("pl", "BLUES/ROCK");
        m.put("nl", "BLUES/ROCK");
        m.put("th", "บลูส์/ร็อก");
        m.put("uk", "БЛЮЗ/РОК");
        EXACT.put("BLUES/ROCK", m);
    }
    private static void putExact_51() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SPIDER");
        m.put("fr", "ARAIGNÉE");
        m.put("it", "RAGNO");
        m.put("de", "SPINNE");
        m.put("es", "ARAÑA");
        m.put("zh", "爬格子练习");
        m.put("ja", "スパイダー");
        m.put("ko", "스파이더 크로매틱");
        m.put("pt", "ARANHA");
        m.put("ru", "ПАУК");
        m.put("ar", "عنكبوت");
        m.put("hi", "स्पाइडर");
        m.put("bn", "স্পাইডার");
        m.put("in", "LABA-LABA");
        m.put("tr", "ÖRÜMCEK");
        m.put("vi", "LUYỆN NGÓN");
        m.put("pl", "PAJĄK");
        m.put("nl", "SPIN");
        m.put("th", "ฝึกนิ้วสไปเดอร์");
        m.put("uk", "ПАВУК");
        EXACT.put("SPIDER", m);
    }
    private static void putExact_52() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "JAZZ 7THS");
        m.put("fr", "JAZZ 7ÈMES");
        m.put("it", "JAZZ 7ª");
        m.put("de", "JAZZ-SEPT.");
        m.put("es", "JAZZ 7ª");
        m.put("zh", "爵士七和弦");
        m.put("ja", "ジャズ 7th");
        m.put("ko", "재즈 7화음");
        m.put("pt", "JAZZ 7ªs");
        m.put("ru", "ДЖАЗ СЕПТ.");
        m.put("ar", "جاز سباعيات");
        m.put("hi", "जैज़ 7वें");
        m.put("bn", "জ্যাজ ৭ম");
        m.put("in", "JAZZ 7TH");
        m.put("tr", "CAZ 7'Lİ");
        m.put("vi", "HỢP ÂM 7 JAZZ");
        m.put("pl", "JAZZ 7-KI");
        m.put("nl", "JAZZ 7e");
        m.put("th", "แจ๊สคอร์ด 7");
        m.put("uk", "ДЖАЗ СЕПТ.");
        EXACT.put("JAZZ 7THS", m);
    }
    private static void putExact_53() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "LADDER: STATIC");
        m.put("fr", "ÉCHELLE: FIXE");
        m.put("it", "SCALA: FISSA");
        m.put("de", "STUFE: KONSTANT");
        m.put("es", "ESCALA: FIJA");
        m.put("zh", "阶梯加速: 固定");
        m.put("ja", "ラダー: 固定");
        m.put("ko", "래더: 고정");
        m.put("pt", "ESCADA: FIXA");
        m.put("ru", "ЛЕСТНИЦА: ФИКС");
        m.put("ar", "سلم السرعة: ثابت");
        m.put("hi", "लैडर: स्थिर");
        m.put("bn", "ল্যাডার: স্থির");
        m.put("in", "TANGGA: TETAP");
        m.put("tr", "BASAMAK: SABİT");
        m.put("vi", "TĂNG TỐC: CỐ ĐỊNH");
        m.put("pl", "DRABINA: STAŁA");
        m.put("nl", "LADDER: VAST");
        m.put("th", "ขั้นความเร็ว: คงที่");
        m.put("uk", "СХОДИ: ФІКС");
        EXACT.put("LADDER: STATIC", m);
    }
    private static void putExact_54() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "LOOP: FULL BAR");
        m.put("fr", "BOUCLE: MESURE");
        m.put("it", "LOOP: BATTUTA");
        m.put("de", "LOOP: GANZER TAKT");
        m.put("es", "BUCLE: COMPÁS");
        m.put("zh", "循环: 整小节");
        m.put("ja", "ループ: 全小節");
        m.put("ko", "루프: 전체 마디");
        m.put("pt", "LOOP: COMPASSO");
        m.put("ru", "ЦИКЛ: ВЕСЬ ТАКТ");
        m.put("ar", "تكرار: مازورة كاملة");
        m.put("hi", "लूप: पूरा बार");
        m.put("bn", "লুপ: পুরো বার");
        m.put("in", "ULANG: 1 BAR");
        m.put("tr", "DÖNGÜ: TAM ÖLÇÜ");
        m.put("vi", "LẶP: CẢ Ô NHỊP");
        m.put("pl", "PĘTLA: CAŁY TAKT");
        m.put("nl", "LUS: HELE MAAT");
        m.put("th", "ลูป: เต็มห้อง");
        m.put("uk", "ЦИКЛ: ВЕСЬ ТАКТ");
        EXACT.put("LOOP: FULL BAR", m);
    }
    private static void putExact_55() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "LOOP: BEAT 1 ONLY");
        m.put("fr", "BOUCLE: TEMPS 1");
        m.put("it", "LOOP: SOLO MOV. 1");
        m.put("de", "LOOP: NUR SCHLAG 1");
        m.put("es", "BUCLE: PULSO 1");
        m.put("zh", "循环: 仅第1拍");
        m.put("ja", "ループ: 1拍目のみ");
        m.put("ko", "루프: 1박만");
        m.put("pt", "LOOP: SÓ TEMPO 1");
        m.put("ru", "ЦИКЛ: 1-Я ДОЛЯ");
        m.put("ar", "تكرار: النبضة 1");
        m.put("hi", "लूप: केवल बीट 1");
        m.put("bn", "লুপ: শুধু বিট ১");
        m.put("in", "ULANG: KETUKAN 1");
        m.put("tr", "DÖNGÜ: SADECE 1");
        m.put("vi", "LẶP: NHỊP 1");
        m.put("pl", "PĘTLA: 1. MIARA");
        m.put("nl", "LUS: ALLEEN TEL 1");
        m.put("th", "ลูป: จังหวะที่ 1");
        m.put("uk", "ЦИКЛ: 1-ША ДОЛЯ");
        EXACT.put("LOOP: BEAT 1 ONLY", m);
    }
    private static void putExact_56() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "1: INDEX");
        m.put("fr", "1: INDEX");
        m.put("it", "1: INDICE");
        m.put("de", "1: ZEIGEF.");
        m.put("es", "1: ÍNDICE");
        m.put("zh", "1: 食指");
        m.put("ja", "1: 人差指");
        m.put("ko", "1: 검지");
        m.put("pt", "1: INDICADOR");
        m.put("ru", "1: УКАЗАТ.");
        m.put("ar", "1: سبابة");
        m.put("hi", "1: तर्जनी");
        m.put("bn", "1: তর্জনী");
        m.put("in", "1: TELUNJUK");
        m.put("tr", "1: İŞARET");
        m.put("vi", "1: NGÓN TRỎ");
        m.put("pl", "1: WSKAZ.");
        m.put("nl", "1: WIJSV.");
        m.put("th", "1: นิ้วชี้");
        m.put("uk", "1: ВКАЗІВ.");
        EXACT.put("1: INDEX", m);
    }
    private static void putExact_57() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "2: MIDDLE");
        m.put("fr", "2: MAJEUR");
        m.put("it", "2: MEDIO");
        m.put("de", "2: MITTELF.");
        m.put("es", "2: MEDIO");
        m.put("zh", "2: 中指");
        m.put("ja", "2: 中指");
        m.put("ko", "2: 중지");
        m.put("pt", "2: MÉDIO");
        m.put("ru", "2: СРЕДНИЙ");
        m.put("ar", "2: وسطى");
        m.put("hi", "2: मध्यमा");
        m.put("bn", "2: মধ্যমা");
        m.put("in", "2: TENGAH");
        m.put("tr", "2: ORTA");
        m.put("vi", "2: NGÓN GIỮA");
        m.put("pl", "2: ŚRODK.");
        m.put("nl", "2: MIDDELV.");
        m.put("th", "2: นิ้วกลาง");
        m.put("uk", "2: СЕРЕДНІЙ");
        EXACT.put("2: MIDDLE", m);
    }
    private static void putExact_58() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "3: RING");
        m.put("fr", "3: ANNULAIRE");
        m.put("it", "3: ANULARE");
        m.put("de", "3: RINGF.");
        m.put("es", "3: ANULAR");
        m.put("zh", "3: 无名指");
        m.put("ja", "3: 薬指");
        m.put("ko", "3: 약지");
        m.put("pt", "3: ANELAR");
        m.put("ru", "3: БЕЗЫМ.");
        m.put("ar", "3: بنصر");
        m.put("hi", "3: अनामिका");
        m.put("bn", "3: অনামিকা");
        m.put("in", "3: MANIS");
        m.put("tr", "3: YÜZÜK");
        m.put("vi", "3: ÁP ÚT");
        m.put("pl", "3: SERDECZ.");
        m.put("nl", "3: RINGV.");
        m.put("th", "3: นิ้วนาง");
        m.put("uk", "3: БЕЗІМ.");
        EXACT.put("3: RING", m);
    }
    private static void putExact_59() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "4: PINKY");
        m.put("fr", "4: AURIC.");
        m.put("it", "4: MIGNOLO");
        m.put("de", "4: KLEINER F.");
        m.put("es", "4: MEÑIQUE");
        m.put("zh", "4: 小指");
        m.put("ja", "4: 小指");
        m.put("ko", "4: 소지");
        m.put("pt", "4: MÍNIMO");
        m.put("ru", "4: МИЗИНЕЦ");
        m.put("ar", "4: خنصر");
        m.put("hi", "4: कनिष्ठा");
        m.put("bn", "4: কনিষ্ঠা");
        m.put("in", "4: KELINGKING");
        m.put("tr", "4: SERÇE");
        m.put("vi", "4: NGÓN ÚT");
        m.put("pl", "4: MAŁY");
        m.put("nl", "4: PINK");
        m.put("th", "4: นิ้วก้อย");
        m.put("uk", "4: МІЗИНЕЦЬ");
        EXACT.put("4: PINKY", m);
    }
    private static void putExact_60() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "1: INDEX");
        m.put("fr", "1: INDEX");
        m.put("it", "1: INDICE");
        m.put("de", "1: ZEIGEF.");
        m.put("es", "1: ÍNDICE");
        m.put("zh", "1: 食指");
        m.put("ja", "1: 人差指");
        m.put("ko", "1: 검지");
        m.put("pt", "1: INDICADOR");
        m.put("ru", "1: УКАЗАТ.");
        m.put("ar", "1: سبابة");
        m.put("hi", "1: तर्जनी");
        m.put("bn", "1: তর্জনী");
        m.put("in", "1: TELUNJUK");
        m.put("tr", "1: İŞARET");
        m.put("vi", "1: NGÓN TRỎ");
        m.put("pl", "1: WSKAZ.");
        m.put("nl", "1: WIJSV.");
        m.put("th", "1: นิ้วชี้");
        m.put("uk", "1: ВКАЗІВ.");
        EXACT.put("1:Index", m);
    }
    private static void putExact_61() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "2: MIDDLE");
        m.put("fr", "2: MAJEUR");
        m.put("it", "2: MEDIO");
        m.put("de", "2: MITTELF.");
        m.put("es", "2: MEDIO");
        m.put("zh", "2: 中指");
        m.put("ja", "2: 中指");
        m.put("ko", "2: 중지");
        m.put("pt", "2: MÉDIO");
        m.put("ru", "2: СРЕДНИЙ");
        m.put("ar", "2: وسطى");
        m.put("hi", "2: मध्यमा");
        m.put("bn", "2: মধ্যমা");
        m.put("in", "2: TENGAH");
        m.put("tr", "2: ORTA");
        m.put("vi", "2: NGÓN GIỮA");
        m.put("pl", "2: ŚRODK.");
        m.put("nl", "2: MIDDELV.");
        m.put("th", "2: นิ้วกลาง");
        m.put("uk", "2: СЕРЕДНІЙ");
        EXACT.put("2:Middle", m);
    }
    private static void putExact_62() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "3: RING");
        m.put("fr", "3: ANNULAIRE");
        m.put("it", "3: ANULARE");
        m.put("de", "3: RINGF.");
        m.put("es", "3: ANULAR");
        m.put("zh", "3: 无名指");
        m.put("ja", "3: 薬指");
        m.put("ko", "3: 약지");
        m.put("pt", "3: ANELAR");
        m.put("ru", "3: БЕЗЫМ.");
        m.put("ar", "3: بنصر");
        m.put("hi", "3: अनामिका");
        m.put("bn", "3: অনামিকা");
        m.put("in", "3: MANIS");
        m.put("tr", "3: YÜZÜK");
        m.put("vi", "3: ÁP ÚT");
        m.put("pl", "3: SERDECZ.");
        m.put("nl", "3: RINGV.");
        m.put("th", "3: นิ้วนาง");
        m.put("uk", "3: БЕЗІМ.");
        EXACT.put("3:Ring", m);
    }
    private static void putExact_63() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "4: PINKY");
        m.put("fr", "4: AURIC.");
        m.put("it", "4: MIGNOLO");
        m.put("de", "4: KLEINER F.");
        m.put("es", "4: MEÑIQUE");
        m.put("zh", "4: 小指");
        m.put("ja", "4: 小指");
        m.put("ko", "4: 소지");
        m.put("pt", "4: MÍNIMO");
        m.put("ru", "4: МИЗИНЕЦ");
        m.put("ar", "4: خنصر");
        m.put("hi", "4: कनिष्ठा");
        m.put("bn", "4: কনিষ্ঠা");
        m.put("in", "4: KELINGKING");
        m.put("tr", "4: SERÇE");
        m.put("vi", "4: NGÓN ÚT");
        m.put("pl", "4: MAŁY");
        m.put("nl", "4: PINK");
        m.put("th", "4: นิ้วก้อย");
        m.put("uk", "4: МІЗИНЕЦЬ");
        EXACT.put("4:Pinky", m);
    }
    private static void putExact_64() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "TAB+BCK");
        m.put("fr", "TAB+ACC");
        m.put("it", "TAB+BASE");
        m.put("de", "TAB+BAND");
        m.put("es", "TAB+PISTA");
        m.put("zh", "六线谱+伴奏");
        m.put("ja", "TAB+伴奏");
        m.put("ko", "탭+반주");
        m.put("pt", "TAB+BASE");
        m.put("ru", "ТАБ+ФОН");
        m.put("ar", "تاب+مرافقة");
        m.put("hi", "टैब+बैक");
        m.put("bn", "ট্যাব+ব্যাক");
        m.put("in", "TAB+IRING");
        m.put("tr", "TAB+ALTYAPI");
        m.put("vi", "TAB+NHẠC");
        m.put("pl", "TAB+PODKŁ");
        m.put("nl", "TAB+BAND");
        m.put("th", "แท็บ+แบ็กกิ้ง");
        m.put("uk", "ТАБ+ФОН");
        EXACT.put("TAB+BCK", m);
    }
    private static void putExact_65() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "#01 GILBERT 4-NOTE SEQ  •  Paul Gilbert Box 1 Pentatonic Burner");
        m.put("fr", "#01 SÉQ 4 NOTES GILBERT  •  Pentatonique Box 1 Paul Gilbert");
        m.put("it", "#01 SEQ 4 NOTE GILBERT  •  Pentatonica Box 1 Paul Gilbert");
        m.put("de", "#01 GILBERT 4-NOTEN-SEQ  •  Paul Gilbert Box 1 Pentatonik");
        m.put("es", "#01 SEC 4 NOTAS GILBERT  •  Pentatónica Caja 1 Paul Gilbert");
        m.put("zh", "#01 吉尔伯特四音模进  •  Paul Gilbert 第1把位五声速弹");
        m.put("ja", "#01 ギルバート4音シーケンス  •  Paul Gilbert Box 1 ペンタ速弾き");
        m.put("ko", "#01 길버트 4음 시퀀스  •  폴 길버트 박스 1 펜타토닉 속주");
        m.put("pt", "#01 SEQ 4 NOTAS GILBERT  •  Pentatônica Box 1 Paul Gilbert");
        m.put("ru", "#01 СЕКВЕНЦИЯ ГИЛБЕРТА  •  Пентатоника Бокс 1 Пола Гилберта");
        m.put("ar", "#01 تسلسل غيلبرت الرباعي  •  خماسي بول غيلبرت");
        m.put("hi", "#01 गिलबर्ट 4-नोट सीक्वेंस  •  पॉल गिलबर्ट पेंटाटोनिक");
        m.put("bn", "#01 গিলবার্ট ৪-নোট সিকোয়েন্স  •  পল গিলবার্ট পেন্টাটোনিক");
        m.put("in", "#01 SEKUENS 4-NADA GILBERT  •  Pentatonik Box 1 Paul Gilbert");
        m.put("tr", "#01 GILBERT 4 NOTA SEKANS  •  Paul Gilbert Box 1 Pentatonik");
        m.put("vi", "#01 CHUỖI 4 NỐT GILBERT  •  Ngũ cung Thế tay 1 Paul Gilbert");
        m.put("pl", "#01 SEKWENCJA 4 NUT GILBERTA  •  Pentatonika Box 1");
        m.put("nl", "#01 GILBERT 4-NOOT SEQ  •  Paul Gilbert Box 1 Pentatonisch");
        m.put("th", "#01 กิลเบิร์ต 4 โน้ต  •  พอล กิลเบิร์ต บ็อกซ์ 1 เพนทาโทนิก");
        m.put("uk", "#01 СЕКВЕНЦІЯ ГІЛБЕРТА  •  Пентатоніка Бокс 1 Пола Гілберта");
        EXACT.put("#01 GILBERT 4-NOTE SEQ  •  Paul Gilbert Box 1 Pentatonic Burner", m);
    }
    private static void putExact_66() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Band");
        m.put("fr", "Groupe");
        m.put("it", "Band");
        m.put("de", "Band");
        m.put("es", "Banda");
        m.put("zh", "伴奏乐队");
        m.put("ja", "バンド");
        m.put("ko", "밴드");
        m.put("pt", "Banda");
        m.put("ru", "Бэнд");
        m.put("ar", "فرقة");
        m.put("hi", "बैंड");
        m.put("bn", "ব্যান্ড");
        m.put("in", "Band");
        m.put("tr", "Grup");
        m.put("vi", "Ban nhạc");
        m.put("pl", "Zespół");
        m.put("nl", "Band");
        m.put("th", "วงดนตรี");
        m.put("uk", "Бенд");
        EXACT.put("Band", m);
    }
    private static void putExact_67() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "PLAY");
        m.put("fr", "JOUER");
        m.put("it", "AVVIA");
        m.put("de", "START");
        m.put("es", "TOCAR");
        m.put("zh", "播放");
        m.put("ja", "再生");
        m.put("ko", "재생");
        m.put("pt", "TOCAR");
        m.put("ru", "ПУСК");
        m.put("ar", "تشغيل");
        m.put("hi", "चलाएं");
        m.put("bn", "চালান");
        m.put("in", "MULAI");
        m.put("tr", "OYNAT");
        m.put("vi", "PHÁT");
        m.put("pl", "GRAJ");
        m.put("nl", "SPEEL");
        m.put("th", "เล่น");
        m.put("uk", "ПУСК");
        EXACT.put("PLAY", m);
    }
    private static void putExact_68() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "STOP");
        m.put("fr", "STOP");
        m.put("it", "STOP");
        m.put("de", "STOPP");
        m.put("es", "PARAR");
        m.put("zh", "停止");
        m.put("ja", "停止");
        m.put("ko", "정지");
        m.put("pt", "PARAR");
        m.put("ru", "СТОП");
        m.put("ar", "إيقاف");
        m.put("hi", "रोकें");
        m.put("bn", "থামুন");
        m.put("in", "BERHENTI");
        m.put("tr", "DUR");
        m.put("vi", "DỪNG");
        m.put("pl", "STOP");
        m.put("nl", "STOP");
        m.put("th", "หยุด");
        m.put("uk", "СТОП");
        EXACT.put("STOP", m);
    }
    private static void putExact_69() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "PAUSE");
        m.put("fr", "PAUSE");
        m.put("it", "PAUSA");
        m.put("de", "PAUSE");
        m.put("es", "PAUSA");
        m.put("zh", "暂停");
        m.put("ja", "一時停止");
        m.put("ko", "일시정지");
        m.put("pt", "PAUSA");
        m.put("ru", "ПАУЗА");
        m.put("ar", "إيقاف مؤقت");
        m.put("hi", "रोकें");
        m.put("bn", "বিরতি");
        m.put("in", "JEDA");
        m.put("tr", "DURAKLAT");
        m.put("vi", "TẠM DỪNG");
        m.put("pl", "PAUZA");
        m.put("nl", "PAUZE");
        m.put("th", "หยุดชั่วคราว");
        m.put("uk", "ПАУЗА");
        EXACT.put("PAUSE", m);
    }
    private static void putExact_70() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "FILL");
        m.put("fr", "FILL");
        m.put("it", "FILL");
        m.put("de", "FILL");
        m.put("es", "REDOBLE");
        m.put("zh", "加花");
        m.put("ja", "フィル");
        m.put("ko", "필인");
        m.put("pt", "VIRADA");
        m.put("ru", "СБИВКА");
        m.put("ar", "فيل");
        m.put("hi", "फ़िल");
        m.put("bn", "ফিল");
        m.put("in", "FILL");
        m.put("tr", "ATAK");
        m.put("vi", "BÁO TRỐNG");
        m.put("pl", "PRZEJŚCIE");
        m.put("nl", "FILL");
        m.put("th", "ลูกส่ง");
        m.put("uk", "ЗБИВКА");
        EXACT.put("FILL", m);
    }
    private static void putExact_71() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "AUTO:ON");
        m.put("fr", "AUTO:OUI");
        m.put("it", "AUTO:ON");
        m.put("de", "AUTO:AN");
        m.put("es", "AUTO:SÍ");
        m.put("zh", "自动:开");
        m.put("ja", "自動:入");
        m.put("ko", "자동:켜짐");
        m.put("pt", "AUTO:SIM");
        m.put("ru", "АВТО:ВКЛ");
        m.put("ar", "تلقائي:مفعل");
        m.put("hi", "ऑटो:चालू");
        m.put("bn", "অটো:চালু");
        m.put("in", "AUTO:NYALA");
        m.put("tr", "OTO:AÇIK");
        m.put("vi", "TỰ ĐỘNG:BẬT");
        m.put("pl", "AUTO:WŁ");
        m.put("nl", "AUTO:AAN");
        m.put("th", "ออโต้:เปิด");
        m.put("uk", "АВТО:УВІМК");
        EXACT.put("AUTO:ON", m);
    }
    private static void putExact_72() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "AUTO:OFF");
        m.put("fr", "AUTO:NON");
        m.put("it", "AUTO:OFF");
        m.put("de", "AUTO:AUS");
        m.put("es", "AUTO:NO");
        m.put("zh", "自动:关");
        m.put("ja", "自動:切");
        m.put("ko", "자동:꺼짐");
        m.put("pt", "AUTO:NÃO");
        m.put("ru", "АВТО:ВЫКЛ");
        m.put("ar", "تلقائي:معطل");
        m.put("hi", "ऑटो:बंद");
        m.put("bn", "অটো:বন্ধ");
        m.put("in", "AUTO:MATI");
        m.put("tr", "OTO:KAPALI");
        m.put("vi", "TỰ ĐỘNG:TẮT");
        m.put("pl", "AUTO:WYŁ");
        m.put("nl", "AUTO:UIT");
        m.put("th", "ออโต้:ปิด");
        m.put("uk", "АВТО:ВИМК");
        EXACT.put("AUTO:OFF", m);
    }
    private static void putExact_73() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "RST");
        m.put("fr", "INIT");
        m.put("it", "RST");
        m.put("de", "RST");
        m.put("es", "INI");
        m.put("zh", "重置");
        m.put("ja", "初期化");
        m.put("ko", "초기화");
        m.put("pt", "RST");
        m.put("ru", "СБР");
        m.put("ar", "إعادة");
        m.put("hi", "रीसेट");
        m.put("bn", "রিসেট");
        m.put("in", "RST");
        m.put("tr", "SFR");
        m.put("vi", "ĐẶT LẠI");
        m.put("pl", "RST");
        m.put("nl", "RST");
        m.put("th", "รีเซ็ต");
        m.put("uk", "СКИД");
        EXACT.put("RST", m);
    }
    private static void putExact_74() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "KEY");
        m.put("fr", "TONALITÉ");
        m.put("it", "TONALITÀ");
        m.put("de", "TONART");
        m.put("es", "TONALIDAD");
        m.put("zh", "调式 (KEY)");
        m.put("ja", "キー");
        m.put("ko", "조성 (KEY)");
        m.put("pt", "TOM");
        m.put("ru", "ТОНАЛЬНОСТЬ");
        m.put("ar", "المفتاح");
        m.put("hi", "स्केल / की");
        m.put("bn", "কী (KEY)");
        m.put("in", "NADA DASAR");
        m.put("tr", "TON");
        m.put("vi", "GIỌNG");
        m.put("pl", "TONACJA");
        m.put("nl", "TOONSOORT");
        m.put("th", "คีย์");
        m.put("uk", "ТОНАЛЬНІСТЬ");
        EXACT.put("KEY", m);
    }
    private static void putExact_75() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "TEMPO");
        m.put("fr", "TEMPO");
        m.put("it", "TEMPO");
        m.put("de", "TEMPO");
        m.put("es", "TEMPO");
        m.put("zh", "速度 (TEMPO)");
        m.put("ja", "テンポ");
        m.put("ko", "템포");
        m.put("pt", "TEMPO");
        m.put("ru", "ТЕМП");
        m.put("ar", "السرعة");
        m.put("hi", "टेम्पो");
        m.put("bn", "টেম্পো");
        m.put("in", "TEMPO");
        m.put("tr", "TEMPO");
        m.put("vi", "NHỊP ĐỘ");
        m.put("pl", "TEMPO");
        m.put("nl", "TEMPO");
        m.put("th", "ความเร็ว");
        m.put("uk", "ТЕМП");
        EXACT.put("TEMPO", m);
    }
    private static void putExact_76() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "DRUMS");
        m.put("fr", "BATTERIE");
        m.put("it", "BATTERIA");
        m.put("de", "SCHLAGZ.");
        m.put("es", "BATERÍA");
        m.put("zh", "鼓组");
        m.put("ja", "ドラム");
        m.put("ko", "드럼");
        m.put("pt", "BATERIA");
        m.put("ru", "УДАРНЫЕ");
        m.put("ar", "طبول");
        m.put("hi", "ड्रम");
        m.put("bn", "ড্রামস");
        m.put("in", "DRUM");
        m.put("tr", "DAVUL");
        m.put("vi", "TRỐNG");
        m.put("pl", "PERKUSJA");
        m.put("nl", "DRUMS");
        m.put("th", "กลอง");
        m.put("uk", "УДАРНІ");
        EXACT.put("DRUMS", m);
    }
    private static void putExact_77() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "BASS");
        m.put("fr", "BASSE");
        m.put("it", "BASSO");
        m.put("de", "BASS");
        m.put("es", "BAJO");
        m.put("zh", "贝斯");
        m.put("ja", "ベース");
        m.put("ko", "베이스");
        m.put("pt", "BAIXO");
        m.put("ru", "БАС");
        m.put("ar", "باص");
        m.put("hi", "बेस");
        m.put("bn", "বেস");
        m.put("in", "BASS");
        m.put("tr", "BAS");
        m.put("vi", "BASS");
        m.put("pl", "BAS");
        m.put("nl", "BAS");
        m.put("th", "เบส");
        m.put("uk", "БАС");
        EXACT.put("BASS", m);
    }
    private static void putExact_78() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "CHORDS");
        m.put("fr", "ACCORDS");
        m.put("it", "ACCORDI");
        m.put("de", "AKKORDE");
        m.put("es", "ACORDES");
        m.put("zh", "和弦");
        m.put("ja", "コード");
        m.put("ko", "코드");
        m.put("pt", "ACORDES");
        m.put("ru", "АККОРДЫ");
        m.put("ar", "أوتار");
        m.put("hi", "कॉर्ड्स");
        m.put("bn", "কর্ড");
        m.put("in", "AKOR");
        m.put("tr", "AKORLAR");
        m.put("vi", "HỢP ÂM");
        m.put("pl", "AKORDY");
        m.put("nl", "AKKOORDEN");
        m.put("th", "คอร์ด");
        m.put("uk", "АКОРДИ");
        EXACT.put("CHORDS", m);
    }
    private static void putExact_79() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "ON");
        m.put("fr", "ACTIF");
        m.put("it", "ATTIVO");
        m.put("de", "EIN");
        m.put("es", "ACTIVO");
        m.put("zh", "开启");
        m.put("ja", "ON");
        m.put("ko", "켜짐");
        m.put("pt", "ATIVO");
        m.put("ru", "ВКЛ");
        m.put("ar", "مفعل");
        m.put("hi", "चालू");
        m.put("bn", "চালু");
        m.put("in", "AKTIF");
        m.put("tr", "AÇIK");
        m.put("vi", "BẬT");
        m.put("pl", "WŁ.");
        m.put("nl", "AAN");
        m.put("th", "เปิด");
        m.put("uk", "УВІМК");
        EXACT.put("ON", m);
    }
    private static void putExact_80() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "MUTE");
        m.put("fr", "MUET");
        m.put("it", "MUTO");
        m.put("de", "STUMM");
        m.put("es", "MUDO");
        m.put("zh", "静音");
        m.put("ja", "ミュート");
        m.put("ko", "음소거");
        m.put("pt", "MUDO");
        m.put("ru", "ВЫКЛ");
        m.put("ar", "كتم");
        m.put("hi", "म्यूट");
        m.put("bn", "মিউট");
        m.put("in", "BISU");
        m.put("tr", "SESSİZ");
        m.put("vi", "TẮT");
        m.put("pl", "WYCISZ");
        m.put("nl", "DEMP");
        m.put("th", "ปิดเสียง");
        m.put("uk", "ВИМК");
        EXACT.put("MUTE", m);
    }
    private static void putExact_81() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "CHORD PROGRESSION GRID (TAP BAR TO AUDITION)");
        m.put("fr", "GRILLE D'ACCORDS (TOUCHER UNE MESURE POUR ÉCOUTER)");
        m.put("it", "GRIGLIA ACCORDI (TOCCA UNA BATTUTA PER ASCOLTARE)");
        m.put("de", "AKKORDFOLGE-RASTER (TAKT ANTIPPEN ZUM ANHÖREN)");
        m.put("es", "CUADRÍCULA DE ACORDES (TOCA UN COMPÁS PARA OÍR)");
        m.put("zh", "和弦进行网格（点击小节试听并切换和弦）");
        m.put("ja", "コード進行グリッド（小節をタップして試聴・変更）");
        m.put("ko", "코드 진행 그리드 (마디를 탭하여 미리 듣기 및 변경)");
        m.put("pt", "GRADE DE ACORDES (TOQUE NO COMPASSO PARA OUVIR)");
        m.put("ru", "СЕТКА АККОРДОВ (НАЖМИТЕ НА ТАКТ ДЛЯ ПРОСЛУШИВАНИЯ)");
        m.put("ar", "شبكة تسلسل الأوتار (انقر على المازورة للاستماع)");
        m.put("hi", "कॉर्ड प्रोग्रेशन ग्रिड (सुनने के लिए बार पर टैप करें)");
        m.put("bn", "কর্ড প্রোগ্রেশন গ্রিড (শুনতে বারে ট্যাপ করুন)");
        m.put("in", "GRID PROGRESI AKOR (KETUK BAR UNTUK MENDENGAR)");
        m.put("tr", "AKOR DİZİLİMİ (DİNLEMEK İÇİN ÖLÇÜYE DOKUNUN)");
        m.put("vi", "LƯỚI HỢP ÂM (CHẠM Ô NHỊP ĐỂ NGHE THỬ)");
        m.put("pl", "SIATKA AKORDÓW (DOTKNIJ TAKTU, ABY ODSŁUCHAĆ)");
        m.put("nl", "AKKOORDSCHEMA (TIK OP EEN MAAT OM TE BELUISTEREN)");
        m.put("th", "ตารางทางเดินคอร์ด (แตะห้องเพลงเพื่อฟังเสียง)");
        m.put("uk", "СІТКА АКОРДІВ (ТОРКНІТЬСЯ ТАКТУ ДЛЯ ПРОСЛУХОВУВАННЯ)");
        EXACT.put("CHORD PROGRESSION GRID (TAP BAR TO AUDITION)", m);
    }
    private static void putExact_82() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "STEP SEQ");
        m.put("fr", "SÉQUENCEUR");
        m.put("it", "SEQUENCER");
        m.put("de", "STEP-SEQ");
        m.put("es", "SECUENC.");
        m.put("zh", "步进音序器");
        m.put("ja", "シーケンサー");
        m.put("ko", "스텝 시퀀서");
        m.put("pt", "SEQUENC.");
        m.put("ru", "СЕКВЕНСОР");
        m.put("ar", "مرتب خطوات");
        m.put("hi", "सीक्वेंसर");
        m.put("bn", "সিকোয়েন্সার");
        m.put("in", "SEKUENSER");
        m.put("tr", "SEKANS");
        m.put("vi", "BỘ ĐẾM BƯỚC");
        m.put("pl", "SEKWENCER");
        m.put("nl", "SEQUENCER");
        m.put("th", "ซีเควนเซอร์");
        m.put("uk", "СЕКВЕНСОР");
        EXACT.put("STEP SEQ", m);
    }
    private static void putExact_83() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Blues Shuffle 4/4 ▾");
        m.put("fr", "Blues Shuffle 4/4 ▾");
        m.put("it", "Blues Shuffle 4/4 ▾");
        m.put("de", "Blues Shuffle 4/4 ▾");
        m.put("es", "Blues Shuffle 4/4 ▾");
        m.put("zh", "布鲁斯摇摆 4/4 ▾");
        m.put("ja", "ブルース・シャッフル 4/4 ▾");
        m.put("ko", "블루스 셔플 4/4 ▾");
        m.put("pt", "Blues Shuffle 4/4 ▾");
        m.put("ru", "Блюз-шаффл 4/4 ▾");
        m.put("ar", "بلوز شافل 4/4 ▾");
        m.put("hi", "ब्लूज़ शफ़ल 4/4 ▾");
        m.put("bn", "ব্লুজ শাফেল 4/4 ▾");
        m.put("in", "Blues Shuffle 4/4 ▾");
        m.put("tr", "Blues Shuffle 4/4 ▾");
        m.put("vi", "Blues Shuffle 4/4 ▾");
        m.put("pl", "Blues Shuffle 4/4 ▾");
        m.put("nl", "Blues Shuffle 4/4 ▾");
        m.put("th", "บลูส์ชัฟเฟิล 4/4 ▾");
        m.put("uk", "Блюз-шаффл 4/4 ▾");
        EXACT.put("Blues Shuffle 4/4 ▾", m);
    }
    private static void putExact_84() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "12-Bar Blues ▾");
        m.put("fr", "Blues 12 Mesures ▾");
        m.put("it", "Blues 12 Battute ▾");
        m.put("de", "12-Takt-Blues ▾");
        m.put("es", "Blues de 12 Compases ▾");
        m.put("zh", "12小节布鲁斯 ▾");
        m.put("ja", "12小節ブルース ▾");
        m.put("ko", "12마디 블루스 ▾");
        m.put("pt", "Blues de 12 Compassos ▾");
        m.put("ru", "12-тактовый блюз ▾");
        m.put("ar", "بلوز 12 مازورة ▾");
        m.put("hi", "12-बार ब्लूज़ ▾");
        m.put("bn", "১২-বার ব্লুজ ▾");
        m.put("in", "Blues 12-Bar ▾");
        m.put("tr", "12 Ölçülük Blues ▾");
        m.put("vi", "Blues 12 Ô Nhịp ▾");
        m.put("pl", "12-taktowy Blues ▾");
        m.put("nl", "12-Maten Blues ▾");
        m.put("th", "บลูส์ 12 ห้อง ▾");
        m.put("uk", "12-тактовий блюз ▾");
        EXACT.put("12-Bar Blues ▾", m);
    }
    private static void putExact_85() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "GOLD=Key  GRN=Chord  PINK=Blue  CYN=Scale");
        m.put("fr", "OR=Ton  VERT=Accord  ROSE=Blue  CYAN=Gamme");
        m.put("it", "ORO=Ton.  VERDE=Acc.  ROSA=Blue  CIANO=Scala");
        m.put("de", "GOLD=Tonart  GRÜN=Akk.  ROSA=Blue  CYAN=Skala");
        m.put("es", "ORO=Tono  VERDE=Acorde  ROSA=Blue  CIAN=Escala");
        m.put("zh", "金=主音  绿=和弦根音  粉=蓝调音  青=音阶");
        m.put("ja", "金=キー  緑=コード  桃=ブルー  水=スケール");
        m.put("ko", "금=으뜸음  녹=코드근음  분홍=블루노트  청=스케일");
        m.put("pt", "OURO=Tom  VERDE=Acorde  ROSA=Blue  CIANO=Escala");
        m.put("ru", "ЗОЛ=Тон  ЗЕЛ=Аккорд  РОЗ=Блюз  ГОЛ=Гамма");
        m.put("ar", "ذهبي=مفتاح  أخضر=وتر  وردي=بلوز  سماوي=سلم");
        m.put("hi", "स्वर्ण=की  हरा=कॉर्ड  गुलाबी=ब्लू  सियान=स्केल");
        m.put("bn", "সোনা=কী  সবুজ=কর্ড  গোলাপী=ব্লু  সায়ান=স্কেল");
        m.put("in", "EMAS=Nada  HIJAU=Akor  PINK=Blue  SIAN=Skala");
        m.put("tr", "ALTIN=Ton  YEŞİL=Akor  PEMBE=Blue  CAM=Gam");
        m.put("vi", "VÀNG=Chủ  LỤC=Hợp âm  HỒNG=Blue  LAM=Âm giai");
        m.put("pl", "ZŁOTY=Ton  ZIEL=Akord  RÓŻ=Blue  BŁĘK=Skala");
        m.put("nl", "GOUD=Toon  GROEN=Akk.  ROZE=Blue  CYAAN=Ladder");
        m.put("th", "ทอง=คีย์  เขียว=คอร์ด  ชมพู=บลูโน้ต  ฟ้า=สเกล");
        m.put("uk", "ЗОЛ=Тон  ЗЕЛ=Акорд  РОЖ=Блюз  БЛАК=Гама");
        EXACT.put("GOLD=Key  GRN=Chord  PINK=Blue  CYN=Scale", m);
    }
    private static void putExact_86() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "KICK");
        m.put("fr", "GROSSE C.");
        m.put("it", "CASSA");
        m.put("de", "BASSDRUM");
        m.put("es", "BOMBO");
        m.put("zh", "底鼓");
        m.put("ja", "キック");
        m.put("ko", "킥드럼");
        m.put("pt", "BUMBO");
        m.put("ru", "БОЧКА");
        m.put("ar", "كيك");
        m.put("hi", "किक");
        m.put("bn", "কিক");
        m.put("in", "KICK");
        m.put("tr", "KICK");
        m.put("vi", "TRỐNG CÁI");
        m.put("pl", "STOPA");
        m.put("nl", "KICK");
        m.put("th", "กระเดื่อง");
        m.put("uk", "БОЧКА");
        EXACT.put("KICK", m);
    }
    private static void putExact_87() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SNARE");
        m.put("fr", "CAISSE CL.");
        m.put("it", "RULLANTE");
        m.put("de", "SNARE");
        m.put("es", "CAJA");
        m.put("zh", "军鼓");
        m.put("ja", "スネア");
        m.put("ko", "스네어");
        m.put("pt", "CAIXA");
        m.put("ru", "МАЛЫЙ");
        m.put("ar", "سنير");
        m.put("hi", "स्नेयर");
        m.put("bn", "স্নেয়ার");
        m.put("in", "SNARE");
        m.put("tr", "TRAMPET");
        m.put("vi", "TRỐNG LẮC");
        m.put("pl", "WERBEL");
        m.put("nl", "SNARE");
        m.put("th", "สแนร์");
        m.put("uk", "МАЛИЙ");
        EXACT.put("SNARE", m);
    }
    private static void putExact_88() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "CL HAT");
        m.put("fr", "CHARL. F.");
        m.put("it", "HH CHIUSO");
        m.put("de", "HH ZU");
        m.put("es", "HH CERR.");
        m.put("zh", "闭镲");
        m.put("ja", "閉ハイハット");
        m.put("ko", "클로즈드햇");
        m.put("pt", "CHIMB. F.");
        m.put("ru", "ЗАКР. ХЭТ");
        m.put("ar", "هايهات م");
        m.put("hi", "क्लोज़्ड हैट");
        m.put("bn", "ক্লোজড হ্যাট");
        m.put("in", "HH TUTUP");
        m.put("tr", "KAPALI HH");
        m.put("vi", "HI-HAT ĐÓNG");
        m.put("pl", "HH ZAMK.");
        m.put("nl", "HH DICHT");
        m.put("th", "ไฮแฮตปิด");
        m.put("uk", "ЗАКР. ХЕТ");
        EXACT.put("CL HAT", m);
    }
    private static void putExact_89() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "OP HAT");
        m.put("fr", "CHARL. O.");
        m.put("it", "HH APERTO");
        m.put("de", "HH OFFEN");
        m.put("es", "HH ABIER.");
        m.put("zh", "开镲");
        m.put("ja", "開ハイハット");
        m.put("ko", "오픈드햇");
        m.put("pt", "CHIMB. A.");
        m.put("ru", "ОТКР. ХЭТ");
        m.put("ar", "هايهات ف");
        m.put("hi", "ओपन हैट");
        m.put("bn", "ওপেন হ্যাট");
        m.put("in", "HH BUKA");
        m.put("tr", "AÇIK HH");
        m.put("vi", "HI-HAT MỞ");
        m.put("pl", "HH OTW.");
        m.put("nl", "HH OPEN");
        m.put("th", "ไฮแฮตเปิด");
        m.put("uk", "ВІДКР. ХЕТ");
        EXACT.put("OP HAT", m);
    }
    private static void putExact_90() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "INTERVALS");
        m.put("fr", "INTERVALLES");
        m.put("it", "INTERVALLI");
        m.put("de", "INTERVALLE");
        m.put("es", "INTERVALOS");
        m.put("zh", "音程");
        m.put("ja", "音程 (インターバル)");
        m.put("ko", "음정");
        m.put("pt", "INTERVALOS");
        m.put("ru", "ИНТЕРВАЛЫ");
        m.put("ar", "المسافات");
        m.put("hi", "अंतराल");
        m.put("bn", "ব্যবধান");
        m.put("in", "INTERVAL");
        m.put("tr", "ARALIKLAR");
        m.put("vi", "QUÃNG");
        m.put("pl", "INTERWAŁY");
        m.put("nl", "INTERVALLEN");
        m.put("th", "ขั้นคู่เสียง");
        m.put("uk", "ІНТЕРВАЛИ");
        EXACT.put("INTERVALS", m);
    }
    private static void putExact_91() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SOLFEGE");
        m.put("fr", "SOLFÈGE");
        m.put("it", "SOLFEGGIO");
        m.put("de", "SOLMISATION");
        m.put("es", "SOLFEO");
        m.put("zh", "唱名/级数");
        m.put("ja", "ソルフェージュ");
        m.put("ko", "계명 (솔페지)");
        m.put("pt", "SOLFEJO");
        m.put("ru", "СОЛЬФЕДЖИО");
        m.put("ar", "صولفيج");
        m.put("hi", "सोलफ़ेज");
        m.put("bn", "সলফেজ");
        m.put("in", "SOLFEGIO");
        m.put("tr", "SOLFEJ");
        m.put("vi", "XƯỚNG ÂM");
        m.put("pl", "SOLFEŻ");
        m.put("nl", "SOLFÈGE");
        m.put("th", "ซอลเฟจ");
        m.put("uk", "СОЛЬФЕДЖІО");
        EXACT.put("SOLFEGE", m);
    }
    private static void putExact_92() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "MELODIC");
        m.put("fr", "MÉLODIQUE");
        m.put("it", "MELODICO");
        m.put("de", "MELODISCH");
        m.put("es", "MELÓDICO");
        m.put("zh", "旋律听写");
        m.put("ja", "メロディ");
        m.put("ko", "선율 (멜로디)");
        m.put("pt", "MELÓDICO");
        m.put("ru", "МЕЛОДИКА");
        m.put("ar", "لحني");
        m.put("hi", "मेलोडिक");
        m.put("bn", "মেলোডিক");
        m.put("in", "MELODIS");
        m.put("tr", "MELODİK");
        m.put("vi", "GIAI ĐIỆU");
        m.put("pl", "MELODYCZNE");
        m.put("nl", "MELODISCH");
        m.put("th", "เมโลดี้");
        m.put("uk", "МЕЛОДИКА");
        EXACT.put("MELODIC", m);
    }
    private static void putExact_93() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "EASY");
        m.put("fr", "FACILE");
        m.put("it", "FACILE");
        m.put("de", "LEICHT");
        m.put("es", "FÁCIL");
        m.put("zh", "初级");
        m.put("ja", "初級");
        m.put("ko", "쉬움");
        m.put("pt", "FÁCIL");
        m.put("ru", "ЛЕГКО");
        m.put("ar", "سهل");
        m.put("hi", "आसान");
        m.put("bn", "সহজ");
        m.put("in", "MUDAH");
        m.put("tr", "KOLAY");
        m.put("vi", "DỄ");
        m.put("pl", "ŁATWY");
        m.put("nl", "MAKKELIJK");
        m.put("th", "ง่าย");
        m.put("uk", "ЛЕГКО");
        EXACT.put("EASY", m);
    }
    private static void putExact_94() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "MEDIUM");
        m.put("fr", "MOYEN");
        m.put("it", "MEDIO");
        m.put("de", "MITTEL");
        m.put("es", "MEDIO");
        m.put("zh", "中级");
        m.put("ja", "中級");
        m.put("ko", "보통");
        m.put("pt", "MÉDIO");
        m.put("ru", "СРЕДНЕ");
        m.put("ar", "متوسط");
        m.put("hi", "मध्यम");
        m.put("bn", "মাঝারি");
        m.put("in", "SEDANG");
        m.put("tr", "ORTA");
        m.put("vi", "TRUNG BÌNH");
        m.put("pl", "ŚREDNI");
        m.put("nl", "GEMIDDELD");
        m.put("th", "ปานกลาง");
        m.put("uk", "СЕРЕДНЬО");
        EXACT.put("MEDIUM", m);
    }
    private static void putExact_95() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "ALL 12");
        m.put("fr", "LES 12");
        m.put("it", "TUTTI 12");
        m.put("de", "ALLE 12");
        m.put("es", "LOS 12");
        m.put("zh", "全部12音");
        m.put("ja", "全12音");
        m.put("ko", "전체 12음");
        m.put("pt", "TODOS 12");
        m.put("ru", "ВСЕ 12");
        m.put("ar", "كل 12");
        m.put("hi", "सभी 12");
        m.put("bn", "সব ১২টি");
        m.put("in", "SEMUA 12");
        m.put("tr", "TÜM 12");
        m.put("vi", "CẢ 12");
        m.put("pl", "WSZYSTKIE 12");
        m.put("nl", "ALLE 12");
        m.put("th", "ครบทั้ง 12");
        m.put("uk", "УСІ 12");
        EXACT.put("ALL 12", m);
    }
    private static void putExact_96() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "REPLAY");
        m.put("fr", "RÉÉCOUTER");
        m.put("it", "RIASCOLTA");
        m.put("de", "NOCHMAL");
        m.put("es", "REPETIR");
        m.put("zh", "重播");
        m.put("ja", "もう一度");
        m.put("ko", "다시 듣기");
        m.put("pt", "REPETIR");
        m.put("ru", "ПОВТОР");
        m.put("ar", "إعادة");
        m.put("hi", "फिर सुनें");
        m.put("bn", "পুনরায়");
        m.put("in", "ULANGI");
        m.put("tr", "TEKRAR");
        m.put("vi", "NGHE LẠI");
        m.put("pl", "POWTÓRZ");
        m.put("nl", "HERHAAL");
        m.put("th", "ฟังซ้ำ");
        m.put("uk", "ПОВТОР");
        EXACT.put("REPLAY", m);
    }
    private static void putExact_97() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "ROOT NOTE");
        m.put("fr", "NOTE FOND.");
        m.put("it", "NOTA BASE");
        m.put("de", "GRUNDTON");
        m.put("es", "NOTA RAÍZ");
        m.put("zh", "根音参考");
        m.put("ja", "ルート音");
        m.put("ko", "근음 듣기");
        m.put("pt", "NOTA RAIZ");
        m.put("ru", "ТОНИКА");
        m.put("ar", "نغمة الأساس");
        m.put("hi", "मूल स्वर");
        m.put("bn", "মূল নোট");
        m.put("in", "NADA DASAR");
        m.put("tr", "KÖK NOTA");
        m.put("vi", "NỐT GỐC");
        m.put("pl", "PRYMA");
        m.put("nl", "GRONDTOON");
        m.put("th", "โน้ตหลัก");
        m.put("uk", "ТОНІКА");
        EXACT.put("ROOT NOTE", m);
    }
    private static void putExact_98() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "STYLE");
        m.put("fr", "STYLE");
        m.put("it", "STILE");
        m.put("de", "STIL");
        m.put("es", "ESTILO");
        m.put("zh", "奏法");
        m.put("ja", "スタイル");
        m.put("ko", "주법");
        m.put("pt", "ESTILO");
        m.put("ru", "СТИЛЬ");
        m.put("ar", "أسلوب");
        m.put("hi", "शैली");
        m.put("bn", "স্টাইল");
        m.put("in", "GAYA");
        m.put("tr", "STİL");
        m.put("vi", "KIỂU");
        m.put("pl", "STYL");
        m.put("nl", "STIJL");
        m.put("th", "สไตล์");
        m.put("uk", "СТИЛЬ");
        EXACT.put("STYLE", m);
    }
    private static void putExact_99() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "HINT (-2)");
        m.put("fr", "INDICE (-2)");
        m.put("it", "AIUTO (-2)");
        m.put("de", "TIPP (-2)");
        m.put("es", "PISTA (-2)");
        m.put("zh", "提示 (-2)");
        m.put("ja", "ヒント (-2)");
        m.put("ko", "힌트 (-2)");
        m.put("pt", "DICA (-2)");
        m.put("ru", "ПОДСКАЗКА");
        m.put("ar", "تلميح (-2)");
        m.put("hi", "संकेत (-2)");
        m.put("bn", "ইঙ্গিত (-2)");
        m.put("in", "PETUNJUK");
        m.put("tr", "İPUCU (-2)");
        m.put("vi", "GỢI Ý (-2)");
        m.put("pl", "PODPOWIEDŹ");
        m.put("nl", "HINT (-2)");
        m.put("th", "คำใบ้ (-2)");
        m.put("uk", "ПІДКАЗКА");
        EXACT.put("HINT (-2)", m);
    }
    private static void putExact_100() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "NEXT >>");
        m.put("fr", "SUIVANT >>");
        m.put("it", "AVANTI >>");
        m.put("de", "WEITER >>");
        m.put("es", "SIGUIENTE >>");
        m.put("zh", "下一题 >>");
        m.put("ja", "次へ >>");
        m.put("ko", "다음 >>");
        m.put("pt", "PRÓXIMO >>");
        m.put("ru", "ДАЛЕЕ >>");
        m.put("ar", "التالي >>");
        m.put("hi", "अगला >>");
        m.put("bn", "পরবর্তী >>");
        m.put("in", "LANJUT >>");
        m.put("tr", "SONRAKİ >>");
        m.put("vi", "TIẾP >>");
        m.put("pl", "DALEJ >>");
        m.put("nl", "VOLGENDE >>");
        m.put("th", "ถัดไป >>");
        m.put("uk", "ДАЛІ >>");
        EXACT.put("NEXT >>", m);
    }
    private static void putExact_101() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SKIP >>");
        m.put("fr", "PASSER >>");
        m.put("it", "SALTA >>");
        m.put("de", "ÜBERSPR. >>");
        m.put("es", "OMITIR >>");
        m.put("zh", "跳过 >>");
        m.put("ja", "スキップ >>");
        m.put("ko", "건너뛰기 >>");
        m.put("pt", "PULAR >>");
        m.put("ru", "ПРОПУСК >>");
        m.put("ar", "تخطي >>");
        m.put("hi", "छोड़ें >>");
        m.put("bn", "এড়িয়ে যান >>");
        m.put("in", "LEWATI >>");
        m.put("tr", "ATLA >>");
        m.put("vi", "BỎ QUA >>");
        m.put("pl", "POMIŃ >>");
        m.put("nl", "OVERSLAAN >>");
        m.put("th", "ข้าม >>");
        m.put("uk", "ПРОПУСК >>");
        EXACT.put("SKIP >>", m);
    }
    private static void putExact_102() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "ASC");
        m.put("fr", "ASC");
        m.put("it", "ASC");
        m.put("de", "AUFST.");
        m.put("es", "ASC");
        m.put("zh", "上行");
        m.put("ja", "上行");
        m.put("ko", "상행");
        m.put("pt", "ASC");
        m.put("ru", "ВОСХ.");
        m.put("ar", "صاعد");
        m.put("hi", "आरोही");
        m.put("bn", "আরোহী");
        m.put("in", "NAIK");
        m.put("tr", "ÇIKIŞ");
        m.put("vi", "ĐI LÊN");
        m.put("pl", "W GÓRĘ");
        m.put("nl", "OPL.");
        m.put("th", "ขาขึ้น");
        m.put("uk", "ВИСХ.");
        EXACT.put("ASC", m);
    }
    private static void putExact_103() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "DESC");
        m.put("fr", "DESC");
        m.put("it", "DISC");
        m.put("de", "ABST.");
        m.put("es", "DESC");
        m.put("zh", "下行");
        m.put("ja", "下行");
        m.put("ko", "하행");
        m.put("pt", "DESC");
        m.put("ru", "НИСХ.");
        m.put("ar", "هابط");
        m.put("hi", "अवरोही");
        m.put("bn", "অবরোহী");
        m.put("in", "TURUN");
        m.put("tr", "İNİŞ");
        m.put("vi", "ĐI XUỐNG");
        m.put("pl", "W DÓŁ");
        m.put("nl", "AFL.");
        m.put("th", "ขาลง");
        m.put("uk", "НИЗХ.");
        EXACT.put("DESC", m);
    }
    private static void putExact_104() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "HARM");
        m.put("fr", "HARM");
        m.put("it", "ARM");
        m.put("de", "HARM.");
        m.put("es", "ARM");
        m.put("zh", "和声音程");
        m.put("ja", "和音");
        m.put("ko", "화성");
        m.put("pt", "HARM");
        m.put("ru", "ГАРМ.");
        m.put("ar", "توافقي");
        m.put("hi", "हार्मोनिक");
        m.put("bn", "হারমোনিক");
        m.put("in", "HARM");
        m.put("tr", "ARM.");
        m.put("vi", "HÒA ÂM");
        m.put("pl", "HARM.");
        m.put("nl", "HARM.");
        m.put("th", "ประสาน");
        m.put("uk", "ГАРМ.");
        EXACT.put("HARM", m);
    }
    private static void putExact_105() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "PRECISION METRONOME");
        m.put("fr", "MÉTRONOME DE PRÉCISION");
        m.put("it", "METRONOMO DI PRECISIONE");
        m.put("de", "PRÄZISIONS-METRONOM");
        m.put("es", "METRÓNOMO DE PRECISIÓN");
        m.put("zh", "高精度专业节拍器");
        m.put("ja", "高精度メトロノーム");
        m.put("ko", "정밀 메트로놈");
        m.put("pt", "METRÔNOMO DE PRECISÃO");
        m.put("ru", "ТОЧНЫЙ МЕТРОНОМ");
        m.put("ar", "ميترونوم دقيق");
        m.put("hi", "सटीक मेट्रोनोम");
        m.put("bn", "নিখুঁত মেট্রোনোম");
        m.put("in", "METRONOM PRESISI");
        m.put("tr", "HASSAS METRONOM");
        m.put("vi", "MÁY ĐẾM NHỊP CHÍNH XÁC");
        m.put("pl", "METRONOM PRECYZYJNY");
        m.put("nl", "PRECISIEMETRONOOM");
        m.put("th", "เมโทรนอมความแม่นยำสูง");
        m.put("uk", "ТОЧНИЙ МЕТРОНОМ");
        EXACT.put("PRECISION METRONOME", m);
    }
    private static void putExact_106() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "TAP TEMPO");
        m.put("fr", "TAP TEMPO");
        m.put("it", "TAP TEMPO");
        m.put("de", "TAP-TEMPO");
        m.put("es", "TAP TEMPO");
        m.put("zh", "点按测速");
        m.put("ja", "タップテンポ");
        m.put("ko", "탭 템포");
        m.put("pt", "TAP TEMPO");
        m.put("ru", "НАСТУЧАТЬ");
        m.put("ar", "نقر السرعة");
        m.put("hi", "टैप टेम्पो");
        m.put("bn", "ট্যাপ টেম্পো");
        m.put("in", "KETUK TEMPO");
        m.put("tr", "TEMPO TUT");
        m.put("vi", "CHẠM NHỊP");
        m.put("pl", "NABIJ TEMPO");
        m.put("nl", "TIKTEMPO");
        m.put("th", "แตะวัดจังหวะ");
        m.put("uk", "НАСТУКАТИ");
        EXACT.put("TAP TEMPO", m);
    }
    private static void putExact_107() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "TIME SIGNATURE");
        m.put("fr", "SIGNATURE RYTHMIQUE");
        m.put("it", "INDICAZIONE DI TEMPO");
        m.put("de", "TAKTART");
        m.put("es", "COMPÁS");
        m.put("zh", "拍号 (TIME SIG)");
        m.put("ja", "拍子記号");
        m.put("ko", "박자표");
        m.put("pt", "FÓRMULA DE COMPASSO");
        m.put("ru", "РАЗМЕР ТАКТА");
        m.put("ar", "الميزان الإيقاعي");
        m.put("hi", "टाइम सिग्नेचर");
        m.put("bn", "তাল / মাত্রা");
        m.put("in", "BIRAMA");
        m.put("tr", "ÖLÇÜ BİRİMİ");
        m.put("vi", "SỐ CHỈ NHỊP");
        m.put("pl", "METRUM");
        m.put("nl", "MAATSOORT");
        m.put("th", "เครื่องหมายกำหนดจังหวะ");
        m.put("uk", "РОЗМІР ТАКТУ");
        EXACT.put("TIME SIGNATURE", m);
    }
    private static void putExact_108() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SUBDIVISION");
        m.put("fr", "SUBDIVISION");
        m.put("it", "SUDDIVISIONE");
        m.put("de", "UNTERTEILUNG");
        m.put("es", "SUBDIVISIÓN");
        m.put("zh", "节奏细分");
        m.put("ja", "サブディビジョン");
        m.put("ko", "세분박 (서브디비전)");
        m.put("pt", "SUBDIVISÃO");
        m.put("ru", "ПУЛЬСАЦИЯ");
        m.put("ar", "التقسيم الإيقاعي");
        m.put("hi", "सबडिवीज़न");
        m.put("bn", "সাবডিভিশন");
        m.put("in", "SUBDIVISI");
        m.put("tr", "ALT BÖLÜM");
        m.put("vi", "CHIA NHỎ NHỊP");
        m.put("pl", "PODZIAŁ RYTMU");
        m.put("nl", "ONDERVERDELING");
        m.put("th", "การแบ่งจังหวะย่อย");
        m.put("uk", "ПУЛЬСАЦІЯ");
        EXACT.put("SUBDIVISION", m);
    }
    private static void putExact_109() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "STEALTH DROP %");
        m.put("fr", "SILENCE ALÉATOIRE %");
        m.put("it", "DROP SILENZIOSO %");
        m.put("de", "STUMM-QUOTE %");
        m.put("es", "SILENCIO OCULTO %");
        m.put("zh", "随机漏拍率 %");
        m.put("ja", "ステルス消音 %");
        m.put("ko", "무작위 묵음 %");
        m.put("pt", "CORTE OCULTO %");
        m.put("ru", "СКРЫТЫЙ ПРОПУСК %");
        m.put("ar", "نسبة الصمت الخفي %");
        m.put("hi", "स्टेल्थ ड्रॉप %");
        m.put("bn", "নীরব বিট %");
        m.put("in", "PERSEN SENYAP %");
        m.put("tr", "GİZLİ SUS %");
        m.put("vi", "TỶ LỆ ẨN NHỊP %");
        m.put("pl", "UKRYTE WYCIECIA %");
        m.put("nl", "STILTE-KANS %");
        m.put("th", "เปอร์เซ็นต์เงียบสุ่ม %");
        m.put("uk", "ПРИХОВАНИЙ ПРОПУСК %");
        EXACT.put("STEALTH DROP %", m);
    }
    private static void putExact_110() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SPEED TRAINER");
        m.put("fr", "ACCÉLÉRATEUR");
        m.put("it", "ALLENATORE VELOCITÀ");
        m.put("de", "TEMPO-TRAINER");
        m.put("es", "ENTRENADOR VELOCIDAD");
        m.put("zh", "速度递增训练");
        m.put("ja", "スピードトレーナー");
        m.put("ko", "스피드 트레이너");
        m.put("pt", "TREINADOR DE VELOC.");
        m.put("ru", "УСКОРИТЕЛЬ ТЕМПА");
        m.put("ar", "مدرب السرعة");
        m.put("hi", "स्पीड ट्रेनर");
        m.put("bn", "স্পিড ট্রেইনার");
        m.put("in", "PELATIH KECEPATAN");
        m.put("tr", "HIZ EĞİTMENİ");
        m.put("vi", "LUYỆN TĂNG TỐC");
        m.put("pl", "TRENER TEMPA");
        m.put("nl", "SNELHEIDSTRAINER");
        m.put("th", "ฝึกเร่งความเร็ว");
        m.put("uk", "ПРИСКОРЮВАЧ ТЕМПУ");
        EXACT.put("SPEED TRAINER", m);
    }
    private static void putExact_111() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "▶   START METRONOME   (SPACE)");
        m.put("fr", "▶   DÉMARRER MÉTRONOME");
        m.put("it", "▶   AVVIA METRONOMO");
        m.put("de", "▶   METRONOM STARTEN");
        m.put("es", "▶   INICIAR METRÓNOMO");
        m.put("zh", "▶   启动节拍器");
        m.put("ja", "▶   メトロノーム開始");
        m.put("ko", "▶   메트로놈 시작");
        m.put("pt", "▶   INICIAR METRÔNOMO");
        m.put("ru", "▶   ЗАПУСТИТЬ МЕТРОНОМ");
        m.put("ar", "▶   بدء الميترونوم");
        m.put("hi", "▶   मेट्रोनोम शुरू करें");
        m.put("bn", "▶   মেট্রোনোম চালু করুন");
        m.put("in", "▶   MULAI METRONOM");
        m.put("tr", "▶   METRONOMU BAŞLAT");
        m.put("vi", "▶   BẮT ĐẦU ĐẾM NHỊP");
        m.put("pl", "▶   URUCHOM METRONOM");
        m.put("nl", "▶   START METRONOOM");
        m.put("th", "▶   เริ่มเมโทรนอม");
        m.put("uk", "▶   ЗАПУСТИТИ МЕТРОНОМ");
        EXACT.put("▶   START METRONOME   (SPACE)", m);
    }
    private static void putExact_112() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "■   STOP METRONOME   (SPACE)");
        m.put("fr", "■   ARRÊTER MÉTRONOME");
        m.put("it", "■   FERMA METRONOMO");
        m.put("de", "■   METRONOM STOPPEN");
        m.put("es", "■   DETENER METRÓNOMO");
        m.put("zh", "■   停止节拍器");
        m.put("ja", "■   メトロノーム停止");
        m.put("ko", "■   메트로놈 정지");
        m.put("pt", "■   PARAR METRÔNOMO");
        m.put("ru", "■   ОСТАНОВИТЬ МЕТРОНОМ");
        m.put("ar", "■   إيقاف الميترونوم");
        m.put("hi", "■   मेट्रोनोम रोकें");
        m.put("bn", "■   মেট্রোনোম বন্ধ করুন");
        m.put("in", "■   HENTIKAN METRONOM");
        m.put("tr", "■   METRONOMU DURDUR");
        m.put("vi", "■   DỪNG ĐẾM NHỊP");
        m.put("pl", "■   ZATRZYMAJ METRONOM");
        m.put("nl", "■   STOP METRONOOM");
        m.put("th", "■   หยุดเมโทรนอม");
        m.put("uk", "■   ЗУПИНИТИ МЕТРОНОМ");
        EXACT.put("■   STOP METRONOME   (SPACE)", m);
    }
    private static void putExact_113() {
        Map<String, String> m = new HashMap<>();
        m.put("en", " STEADY CLICK");
        m.put("fr", " CLIC RÉGULIER");
        m.put("it", " CLICK COSTANTE");
        m.put("de", " KONSTANTER KLICK");
        m.put("es", " CLIC CONSTANTE");
        m.put("zh", " 稳定节拍");
        m.put("ja", " 通常クリック");
        m.put("ko", " 정박 클릭");
        m.put("pt", " CLIQUE FIRME");
        m.put("ru", " РОВНЫЙ КЛИК");
        m.put("ar", " نقر ثابت");
        m.put("hi", " स्थिर क्लिक");
        m.put("bn", " স্থির ক্লিক");
        m.put("in", " KLIK STABIL");
        m.put("tr", " SABİT TIK");
        m.put("vi", " NHỊP ĐỀU");
        m.put("pl", " STAŁY KLIK");
        m.put("nl", " VASTE KLIK");
        m.put("th", " จังหวะคงที่");
        m.put("uk", " РІВНИЙ КЛІК");
        EXACT.put(" STEADY CLICK", m);
    }
    private static void putExact_114() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "OFF");
        m.put("fr", "ARRÊT");
        m.put("it", "OFF");
        m.put("de", "AUS");
        m.put("es", "NO");
        m.put("zh", "关闭");
        m.put("ja", "オフ");
        m.put("ko", "끔");
        m.put("pt", "DESL.");
        m.put("ru", "ВЫКЛ");
        m.put("ar", "إيقاف");
        m.put("hi", "बंद");
        m.put("bn", "বন্ধ");
        m.put("in", "MATI");
        m.put("tr", "KAPALI");
        m.put("vi", "TẮT");
        m.put("pl", "WYŁ.");
        m.put("nl", "UIT");
        m.put("th", "ปิด");
        m.put("uk", "ВИМК");
        EXACT.put("OFF", m);
    }
    private static void putExact_115() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "4/4 (Common Time)");
        m.put("fr", "4/4 (ordinaire)");
        m.put("it", "4/4 (Tempo comune)");
        m.put("de", "4/4 (Vierviertel)");
        m.put("es", "4/4 (Compás común)");
        m.put("zh", "4/4 (四四拍)");
        m.put("ja", "4/4 (4分の4拍子)");
        m.put("ko", "4/4 (4분의 4박자)");
        m.put("pt", "4/4 (Quaternário)");
        m.put("ru", "4/4 (Четыре четв.)");
        m.put("ar", "4/4 (ميزان رباعي)");
        m.put("hi", "4/4 (सामान्य ताल)");
        m.put("bn", "4/4 (সাধারণ তাল)");
        m.put("in", "4/4 (Birama Umum)");
        m.put("tr", "4/4 (Dört Dörtlük)");
        m.put("vi", "4/4 (Nhịp 4/4)");
        m.put("pl", "4/4 (Metrum 4/4)");
        m.put("nl", "4/4 (Vierkwarts)");
        m.put("th", "4/4 (จังหวะสี่สี่)");
        m.put("uk", "4/4 (Чотири чверті)");
        EXACT.put("4/4 (Common Time)", m);
    }
    private static void putExact_116() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "2x 8TH • Eighth (2x)");
        m.put("fr", "2x Croches (1/8)");
        m.put("it", "2x Crome (1/8)");
        m.put("de", "2x Achtel (1/8)");
        m.put("es", "2x Corcheas (1/8)");
        m.put("zh", "2x 八分音符");
        m.put("ja", "2x 8分音符");
        m.put("ko", "2x 8분음표");
        m.put("pt", "2x Colcheias (1/8)");
        m.put("ru", "2x Восьмые (1/8)");
        m.put("ar", "2x ثنائية (1/8)");
        m.put("hi", "2x आठवां (1/8)");
        m.put("bn", "2x অষ্টম (1/8)");
        m.put("in", "2x Seperdelapan");
        m.put("tr", "2x Sekizlik (1/8)");
        m.put("vi", "2x Móc đơn (1/8)");
        m.put("pl", "2x Ósemki (1/8)");
        m.put("nl", "2x Achtste (1/8)");
        m.put("th", "2x เขบ็ด 1 ชั้น");
        m.put("uk", "2x Восьмі (1/8)");
        EXACT.put("2x 8TH • Eighth (2x)", m);
    }
    private static void putExact_117() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "0% (OFF)");
        m.put("fr", "0% (ARRÊT)");
        m.put("it", "0% (OFF)");
        m.put("de", "0% (AUS)");
        m.put("es", "0% (NO)");
        m.put("zh", "0% (关闭)");
        m.put("ja", "0% (オフ)");
        m.put("ko", "0% (꺼짐)");
        m.put("pt", "0% (DESL.)");
        m.put("ru", "0% (ВЫКЛ)");
        m.put("ar", "0% (إيقاف)");
        m.put("hi", "0% (बंद)");
        m.put("bn", "0% (বন্ধ)");
        m.put("in", "0% (MATI)");
        m.put("tr", "0% (KAPALI)");
        m.put("vi", "0% (TẮT)");
        m.put("pl", "0% (WYŁ.)");
        m.put("nl", "0% (UIT)");
        m.put("th", "0% (ปิด)");
        m.put("uk", "0% (ВИМК)");
        EXACT.put("0% (OFF)", m);
    }
    private static void putExact_118() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Fast, bright & lively");
        m.put("fr", "Rapide, brillant et vif");
        m.put("it", "Veloce, brillante e vivace");
        m.put("de", "Schnell, hell & lebhaft");
        m.put("es", "Rápido, brillante y vivo");
        m.put("zh", "快速、明亮、充满活力");
        m.put("ja", "速く、明るく生き生きと");
        m.put("ko", "빠르고 밝으며 경쾌하게");
        m.put("pt", "Rápido, brilhante e vivo");
        m.put("ru", "Быстро, ярко и живо");
        m.put("ar", "سريع ومشرق وحيوي");
        m.put("hi", "तेज़, उज्ज्वल और जीवंत");
        m.put("bn", "দ্রুত, উজ্জ্বল ও প্রাণবন্ত");
        m.put("in", "Cepat, cerah & hidup");
        m.put("tr", "Hızlı, parlak ve canlı");
        m.put("vi", "Nhanh, sáng và sôi nổi");
        m.put("pl", "Szybko, jasno i żywo");
        m.put("nl", "Snel, helder & levendig");
        m.put("th", "เร็ว สดใส และมีชีวิตชีวา");
        m.put("uk", "Швидко, яскраво та жваво");
        EXACT.put("Fast, bright & lively", m);
    }
    private static void putExact_119() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "WORKING: DO NOT DISTURB");
        m.put("fr", "CONCENTRATION : NE PAS DÉRANGER");
        m.put("it", "LAVORO: NON DISTURBARE");
        m.put("de", "ARBEITSPHASE: BITTE NICHT STÖREN");
        m.put("es", "TRABAJANDO: NO MOLESTAR");
        m.put("zh", "专注工作中：请勿打扰");
        m.put("ja", "集中作業中：応答不可");
        m.put("ko", "집중 세션 진행 중: 방해 금지");
        m.put("pt", "FOCADO: NÃO PERTURBE");
        m.put("ru", "РАБОТА: НЕ БЕСПОКОИТЬ");
        m.put("ar", "جلسة تركيز: الرجاء عدم الإزعاج");
        m.put("hi", "कार्य प्रगति पर: कृपया व्यवधान न डालें");
        m.put("bn", "কাজ চলছে: বিরক্ত করবেন না");
        m.put("in", "FOKUS KERJA: JANGAN GANGGU");
        m.put("tr", "ÇALIŞMA: RAHATSIZ ETMEYİN");
        m.put("vi", "ĐANG TẬP TRUNG: ĐỪNG LÀM PHIỀN");
        m.put("pl", "PRACA: NIE PRZESZKADZAĆ");
        m.put("nl", "FOCUS: NIET STOREN");
        m.put("th", "กำลังโฟกัส: ห้ามรบกวน");
        m.put("uk", "РОБОТА: НЕ ТУРБУВАТИ");
        EXACT.put("WORKING: DO NOT DISTURB", m);
    }
    private static void putExact_120() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SHORT BREAK: RELAX & RECHARGE");
        m.put("fr", "PAUSE COURTE : DÉTENTE");
        m.put("it", "PAUSA BREVE: RILASSATI");
        m.put("de", "KURZE PAUSE: ENTSPANNEN");
        m.put("es", "PAUSA CORTA: DESCANSA");
        m.put("zh", "短休息：放松充电");
        m.put("ja", "小休憩：リラックス");
        m.put("ko", "짧은 휴식: 재충전 시간");
        m.put("pt", "PAUSA CURTA: RELAXE");
        m.put("ru", "КОРОТКИЙ ПЕРЕРЫВ: ОТДЫХ");
        m.put("ar", "استراحة قصيرة: استرخِ");
        m.put("hi", "छोटा ब्रेक: आराम करें");
        m.put("bn", "ছোট বিরতি: বিশ্রাম নিন");
        m.put("in", "ISTIRAHAT PENDEK: RILEKS");
        m.put("tr", "KISA MOLA: DİNLENİN");
        m.put("vi", "NGHỈ NGẮN: THƯ GIÃN");
        m.put("pl", "KRÓTKA PRZERWA: ODPOCZNIJ");
        m.put("nl", "KORTE PAUZE: ONTSPAN");
        m.put("th", "พักสั้น: ผ่อนคลาย");
        m.put("uk", "КОРОТКА ПЕРЕРВА: ВІДПОЧИНОК");
        EXACT.put("SHORT BREAK: RELAX & RECHARGE", m);
    }
    private static void putExact_121() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "LONG BREAK: DEEP REST");
        m.put("fr", "PAUSE LONGUE : REPOS PROFOND");
        m.put("it", "PAUSA LUNGA: RIPOSO");
        m.put("de", "LANGE PAUSE: ERHOLUNG");
        m.put("es", "PAUSA LARGA: DESCANSO");
        m.put("zh", "长休息：深度恢复");
        m.put("ja", "長休憩：しっかり休息");
        m.put("ko", "긴 휴식: 깊은 휴식");
        m.put("pt", "PAUSA LONGA: DESCANSO");
        m.put("ru", "ДЛИННЫЙ ПЕРЕРЫВ: ОТДЫХ");
        m.put("ar", "استراحة طويلة: راحة عميقة");
        m.put("hi", "लंबा ब्रेक: गहरा विश्राम");
        m.put("bn", "দীর্ঘ বিরতি: পূর্ণ বিশ্রাম");
        m.put("in", "ISTIRAHAT PANJANG: PULIHKAN");
        m.put("tr", "UZUN MOLA: DERİN DİNLENME");
        m.put("vi", "NGHỈ DÀI: PHỤC HỒI NĂNG LƯỢNG");
        m.put("pl", "DŁUGA PRZERWA: REGENERACJA");
        m.put("nl", "LANGE PAUZE: DIEPE RUST");
        m.put("th", "พักยาว: พักผ่อนเต็มที่");
        m.put("uk", "ДОВГА ПЕРЕРВА: ВІДНОВЛЕННЯ");
        EXACT.put("LONG BREAK: DEEP REST", m);
    }
    private static void putExact_122() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "PAUSED: TIMER STOPPED");
        m.put("fr", "EN PAUSE : MINUTEUR ARRÊTÉ");
        m.put("it", "IN PAUSA: TIMER FERMO");
        m.put("de", "PAUSIERT: TIMER ANGEHALTEN");
        m.put("es", "PAUSADO: TEMPORIZADOR DETENIDO");
        m.put("zh", "已暂停：计时停止");
        m.put("ja", "一時停止中");
        m.put("ko", "일시정지됨: 타이머 멈춤");
        m.put("pt", "PAUSADO: TIMER PARADO");
        m.put("ru", "ПАУЗА: ТАЙМЕР ОСТАНОВЛЕН");
        m.put("ar", "متوقف مؤقتاً");
        m.put("hi", "रुका हुआ: टाइमर बंद");
        m.put("bn", "বিরতি: টাইমার থামানো হয়েছে");
        m.put("in", "DIJEDA: TIMER BERHENTI");
        m.put("tr", "DURAKLATILDI");
        m.put("vi", "TẠM DỪNG: ĐỒNG HỒ DỪNG");
        m.put("pl", "WSTRZYMANO: LICZNIK STOP");
        m.put("nl", "GEPAUZEERD: TIMER GESTOPT");
        m.put("th", "หยุดชั่วคราว");
        m.put("uk", "ПАУЗА: ТАЙМЕР ЗУПИНЕНО");
        EXACT.put("PAUSED: TIMER STOPPED", m);
    }
    private static void putExact_123() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "READY TO FOCUS");
        m.put("fr", "PRÊT À SE CONCENTRER");
        m.put("it", "PRONTO PER CONCENTRARTI");
        m.put("de", "BEREIT FÜR FOKUS");
        m.put("es", "LISTO PARA ENFOCARSE");
        m.put("zh", "准备开始专注");
        m.put("ja", "集中セッション準備完了");
        m.put("ko", "집중 준비 완료");
        m.put("pt", "PRONTO PARA FOCAR");
        m.put("ru", "ГОТОВ К ФОКУСУ");
        m.put("ar", "جاهز للتركيز");
        m.put("hi", "फ़ोकस के लिए तैयार");
        m.put("bn", "মনোযোগের জন্য প্রস্তুত");
        m.put("in", "SIAP UNTUK FOKUS");
        m.put("tr", "ODAKLANMAYA HAZIR");
        m.put("vi", "SẴN SÀNG TẬP TRUNG");
        m.put("pl", "GOTOWY DO PRACY");
        m.put("nl", "KLAAR VOOR FOCUS");
        m.put("th", "พร้อมโฟกัส");
        m.put("uk", "ГОТОВИЙ ДО ФОКУСУ");
        EXACT.put("READY TO FOCUS", m);
    }
    private static void putExact_124() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "RESET");
        m.put("fr", "RÉINIT.");
        m.put("it", "AZZERA");
        m.put("de", "RESET");
        m.put("es", "REINICIAR");
        m.put("zh", "重置");
        m.put("ja", "リセット");
        m.put("ko", "초기화");
        m.put("pt", "ZERAR");
        m.put("ru", "СБРОС");
        m.put("ar", "إعادة");
        m.put("hi", "रीसेट");
        m.put("bn", "রিসেট");
        m.put("in", "RESET");
        m.put("tr", "SIFIRLA");
        m.put("vi", "ĐẶT LẠI");
        m.put("pl", "RESET");
        m.put("nl", "RESET");
        m.put("th", "รีเซ็ต");
        m.put("uk", "СКИДАННЯ");
        EXACT.put("RESET", m);
    }
    private static void putExact_125() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "START FOCUS");
        m.put("fr", "DÉMARRER");
        m.put("it", "AVVIA FOCUS");
        m.put("de", "FOKUS STARTEN");
        m.put("es", "INICIAR FOCO");
        m.put("zh", "开始专注");
        m.put("ja", "集中スタート");
        m.put("ko", "집중 시작");
        m.put("pt", "INICIAR FOCO");
        m.put("ru", "НАЧАТЬ ФОКУС");
        m.put("ar", "ابدأ التركيز");
        m.put("hi", "फ़ोकस शुरू करें");
        m.put("bn", "ফোকাস শুরু");
        m.put("in", "MULAI FOKUS");
        m.put("tr", "ODAK BAŞLAT");
        m.put("vi", "BẮT ĐẦU");
        m.put("pl", "START PRACY");
        m.put("nl", "START FOCUS");
        m.put("th", "เริ่มโฟกัส");
        m.put("uk", "ПОЧАТИ ФОКУС");
        EXACT.put("START FOCUS", m);
    }
    private static void putExact_126() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "RESUME");
        m.put("fr", "REPRENDRE");
        m.put("it", "RIPRENDI");
        m.put("de", "FORTSETZEN");
        m.put("es", "REANUDAR");
        m.put("zh", "继续");
        m.put("ja", "再開");
        m.put("ko", "계속하기");
        m.put("pt", "RETOMAR");
        m.put("ru", "ПРОДОЛЖИТЬ");
        m.put("ar", "استئناف");
        m.put("hi", "जारी रखें");
        m.put("bn", "চালিয়ে যান");
        m.put("in", "LANJUTKAN");
        m.put("tr", "DEVAM ET");
        m.put("vi", "TIẾP TỤC");
        m.put("pl", "WZNÓW");
        m.put("nl", "HERVATTEN");
        m.put("th", "ทำต่อ");
        m.put("uk", "ПРОДОВЖИТИ");
        EXACT.put("RESUME", m);
    }
    private static void putExact_127() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Background sound");
        m.put("fr", "Son d'ambiance");
        m.put("it", "Suono di sottofondo");
        m.put("de", "Hintergrundklang");
        m.put("es", "Sonido de fondo");
        m.put("zh", "背景白噪音 / 音效");
        m.put("ja", "環境サウンド (BGM)");
        m.put("ko", "배경 사운드");
        m.put("pt", "Som de fundo");
        m.put("ru", "Фоновый звук");
        m.put("ar", "صوت الخلفية");
        m.put("hi", "पृष्ठभूमि ध्वनि");
        m.put("bn", "পটভূমির শব্দ");
        m.put("in", "Suara latar");
        m.put("tr", "Arka plan sesi");
        m.put("vi", "Âm thanh nền");
        m.put("pl", "Dźwięk w tle");
        m.put("nl", "Achtergrondgeluid");
        m.put("th", "เสียงบรรยากาศพื้นหลัง");
        m.put("uk", "Фоновий звук");
        EXACT.put("Background sound", m);
    }
    private static void putExact_128() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SOFT TICK");
        m.put("fr", "TIC-TAC DOUX");
        m.put("it", "TICCHETTIO");
        m.put("de", "SANFTES TICKEN");
        m.put("es", "TIC SUAVE");
        m.put("zh", "轻柔滴答");
        m.put("ja", "ソフトティック");
        m.put("ko", "부드러운 초침");
        m.put("pt", "TIQUE SUAVE");
        m.put("ru", "МЯГКИЙ ТИК");
        m.put("ar", "دقات ناعمة");
        m.put("hi", "हल्की टिक");
        m.put("bn", "মৃদু টিক");
        m.put("in", "DETAK HALUS");
        m.put("tr", "YUMUŞAK TIK");
        m.put("vi", "TÍCH TẮC NHẸ");
        m.put("pl", "CICHY TIK");
        m.put("nl", "ZACHTE TIK");
        m.put("th", "เสียงเข็มเดินเบาๆ");
        m.put("uk", "М'ЯКИЙ ТІК");
        EXACT.put("SOFT TICK", m);
    }
    private static void putExact_129() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "RAIN");
        m.put("fr", "PLUIE");
        m.put("it", "PIOGGIA");
        m.put("de", "REGEN");
        m.put("es", "LLUVIA");
        m.put("zh", "细雨声");
        m.put("ja", "雨音");
        m.put("ko", "빗소리");
        m.put("pt", "CHUVA");
        m.put("ru", "ДОЖДЬ");
        m.put("ar", "مطر");
        m.put("hi", "बारिश");
        m.put("bn", "বৃষ্টি");
        m.put("in", "HUJAN");
        m.put("tr", "YAĞMUR");
        m.put("vi", "TIẾNG MƯA");
        m.put("pl", "DESZCZ");
        m.put("nl", "REGEN");
        m.put("th", "เสียงฝน");
        m.put("uk", "ДОЩ");
        EXACT.put("RAIN", m);
    }
    private static void putExact_130() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "BROWN NOISE");
        m.put("fr", "BRUIT BRUN");
        m.put("it", "RUMORE MARRONE");
        m.put("de", "BRAUNES RAUSCHEN");
        m.put("es", "RUIDO MARRÓN");
        m.put("zh", "布朗噪音");
        m.put("ja", "ブラウンノイズ");
        m.put("ko", "브라운 노이즈");
        m.put("pt", "RUÍDO MARROM");
        m.put("ru", "БРОУН. ШУМ");
        m.put("ar", "ضوضاء بنية");
        m.put("hi", "ब्राउन नॉइज़");
        m.put("bn", "ব্রাউন নয়েজ");
        m.put("in", "DERAU COKELAT");
        m.put("tr", "KAHVE GÜRÜLTÜ");
        m.put("vi", "NHIỄU NÂU");
        m.put("pl", "SZUM BRUNATNY");
        m.put("nl", "BRUINE RUIS");
        m.put("th", "บราวน์นอยส์");
        m.put("uk", "БРОУН. ШУМ");
        EXACT.put("BROWN NOISE", m);
    }
    private static void putExact_131() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "DURATION PRESETS (WORK / SHORT / LONG)");
        m.put("fr", "PRÉRÉGLAGES DE DURÉE (TRAVAIL / COURTE / LONGUE)");
        m.put("it", "PRESET DURATA (LAVORO / BREVE / LUNGA)");
        m.put("de", "DAUER-PRESETS (FOKUS / KURZ / LANG)");
        m.put("es", "PREAJUSTES DE DURACIÓN (FOCO / CORTA / LARGA)");
        m.put("zh", "时长预设（专注 / 短休 / 长休）");
        m.put("ja", "時間プリセット（集中 / 短休憩 / 長休憩）");
        m.put("ko", "시간 프리셋 (집중 / 짧은 휴식 / 긴 휴식)");
        m.put("pt", "PREDEFINIÇÕES (FOCO / CURTA / LONGA)");
        m.put("ru", "ПРЕСЕТЫ ВРЕМЕНИ (РАБОТА / КОРОТКИЙ / ДЛИННЫЙ)");
        m.put("ar", "قوالب المدة (تركيز / قصيرة / طويلة)");
        m.put("hi", "अवधि प्रीसेट (कार्य / छोटा / लंबा)");
        m.put("bn", "সময় প্রিসেট (কাজ / ছোট / দীর্ঘ)");
        m.put("in", "PRESET DURASI (KERJA / PENDEK / PANJANG)");
        m.put("tr", "SÜRE AYARLARI (ODAK / KISA / UZUN)");
        m.put("vi", "MẪU THỜI LƯỢNG (LÀM VIỆC / NGHỈ NGẮN / DÀI)");
        m.put("pl", "PRESETY CZASU (PRACA / KRÓTKA / DŁUGA)");
        m.put("nl", "TIJDSDUUR PRESETS (FOCUS / KORT / LANG)");
        m.put("th", "พรีเซ็ตเวลา (โฟกัส / พักสั้น / พักยาว)");
        m.put("uk", "ПРЕСЕТИ ЧАСУ (РОБОТА / КОРОТКА / ДОВГА)");
        EXACT.put("DURATION PRESETS (WORK / SHORT / LONG)", m);
    }
    private static void putExact_132() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "ALL-TIME TOTAL");
        m.put("fr", "TOTAL CUMULÉ");
        m.put("it", "TOTALE GENERALE");
        m.put("de", "GESAMTSUMME");
        m.put("es", "TOTAL HISTÓRICO");
        m.put("zh", "累计总时长");
        m.put("ja", "累計トータル");
        m.put("ko", "전체 누적 기록");
        m.put("pt", "TOTAL GERAL");
        m.put("ru", "ВСЕГО ЗА ВСЁ ВРЕМЯ");
        m.put("ar", "الإجمالي الكلي");
        m.put("hi", "कुल समय");
        m.put("bn", "সর্বমোট সময়");
        m.put("in", "TOTAL KESELURUHAN");
        m.put("tr", "TOPLAM SÜRE");
        m.put("vi", "TỔNG TÍCH LŨY");
        m.put("pl", "SUMA CAŁKOWITA");
        m.put("nl", "TOTAAL ALLERTIJDEN");
        m.put("th", "รวมทั้งหมด");
        m.put("uk", "УСЬОГО ЗА ВЕСЬ ЧАС");
        EXACT.put("ALL-TIME TOTAL", m);
    }
    private static void putExact_133() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "CURRENT SESSION");
        m.put("fr", "SESSION ACTUELLE");
        m.put("it", "SESSIONE ATTUALE");
        m.put("de", "AKTUELLE SITZUNG");
        m.put("es", "SESIÓN ACTUAL");
        m.put("zh", "当前阶段");
        m.put("ja", "現在のセッション");
        m.put("ko", "현재 세션");
        m.put("pt", "SESSÃO ATUAL");
        m.put("ru", "ТЕКУЩАЯ СЕССИЯ");
        m.put("ar", "الجلسة الحالية");
        m.put("hi", "वर्तमान सत्र");
        m.put("bn", "বর্তমান সেশন");
        m.put("in", "SESI SAAT INI");
        m.put("tr", "MEVCUT OTURUM");
        m.put("vi", "PHIÊN HIỆN TẠI");
        m.put("pl", "BIEŻĄCA SESJA");
        m.put("nl", "HUIDIGE SESSIE");
        m.put("th", "เซสชันปัจจุบัน");
        m.put("uk", "ПОТОЧНА СЕСІЯ");
        EXACT.put("CURRENT SESSION", m);
    }
    private static void putExact_134() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "FOCUS SESSION");
        m.put("fr", "SESSION FOCUS");
        m.put("it", "SESSIONE FOCUS");
        m.put("de", "FOKUS-SITZUNG");
        m.put("es", "SESIÓN DE FOCO");
        m.put("zh", "专注时段");
        m.put("ja", "集中セッション");
        m.put("ko", "집중 세션");
        m.put("pt", "SESSÃO DE FOCO");
        m.put("ru", "ФОКУС-СЕССИЯ");
        m.put("ar", "جلسة تركيز");
        m.put("hi", "फ़ोकस सत्र");
        m.put("bn", "ফোকাস সেশন");
        m.put("in", "SESI FOKUS");
        m.put("tr", "ODAK OTURUMU");
        m.put("vi", "PHIÊN TẬP TRUNG");
        m.put("pl", "SESJA PRACY");
        m.put("nl", "FOCUSSESSIE");
        m.put("th", "ช่วงโฟกัส");
        m.put("uk", "ФОКУС-СЕСІЯ");
        EXACT.put("FOCUS SESSION", m);
    }
    private static void putExact_135() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SIGHTREAD");
        m.put("fr", "LECTURE À VUE");
        m.put("it", "PRIMA VISTA");
        m.put("de", "BLATTSPIEL");
        m.put("es", "LECTURA MUSICAL");
        m.put("zh", "五线谱视奏");
        m.put("ja", "初見視奏");
        m.put("ko", "악보 초견");
        m.put("pt", "LEITURA MUSICAL");
        m.put("ru", "ЧТЕНИЕ С ЛИСТА");
        m.put("ar", "قراءة النوتة");
        m.put("hi", "साइटरीड");
        m.put("bn", "সাইটরিড");
        m.put("in", "BACA NOT");
        m.put("tr", "DEŞİFRE");
        m.put("vi", "THỊ TẤU");
        m.put("pl", "A VISTA");
        m.put("nl", "BLADLEZEN");
        m.put("th", "อ่านโน้ต");
        m.put("uk", "ЧИТАННЯ З АРКУША");
        EXACT.put("SIGHTREAD", m);
    }
    private static void putExact_136() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "SOUND");
        m.put("fr", "SON");
        m.put("it", "SUONO");
        m.put("de", "TON AN");
        m.put("es", "SONIDO");
        m.put("zh", "声音");
        m.put("ja", "音声ON");
        m.put("ko", "소리 켜짐");
        m.put("pt", "SOM");
        m.put("ru", "ЗВУК");
        m.put("ar", "صوت");
        m.put("hi", "ध्वनि");
        m.put("bn", "শব্দ");
        m.put("in", "SUARA");
        m.put("tr", "SES AÇIK");
        m.put("vi", "ÂM THANH");
        m.put("pl", "DŹWIĘK");
        m.put("nl", "GELUID");
        m.put("th", "เปิดเสียง");
        m.put("uk", "ЗВУК");
        EXACT.put("SOUND", m);
    }
    private static void putExact_137() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "MUTED");
        m.put("fr", "MUET");
        m.put("it", "MUTO");
        m.put("de", "STUMM");
        m.put("es", "MUDO");
        m.put("zh", "静音");
        m.put("ja", "消音");
        m.put("ko", "음소거");
        m.put("pt", "MUDO");
        m.put("ru", "ВЫКЛ");
        m.put("ar", "مكتوم");
        m.put("hi", "म्यूट");
        m.put("bn", "মিউট");
        m.put("in", "BISU");
        m.put("tr", "SESSİZ");
        m.put("vi", "TẮT TIẾNG");
        m.put("pl", "WYCISZ.");
        m.put("nl", "GEDEMPT");
        m.put("th", "ปิดเสียง");
        m.put("uk", "ВИМК");
        EXACT.put("MUTED", m);
    }
    private static void putExact_138() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "NEW PIECE");
        m.put("fr", "NOUVEAU");
        m.put("it", "NUOVO BRANO");
        m.put("de", "NEUES STÜCK");
        m.put("es", "NUEVA PIEZA");
        m.put("zh", "换一首练习");
        m.put("ja", "新しい課題");
        m.put("ko", "새 연습곡");
        m.put("pt", "NOVA PEÇA");
        m.put("ru", "НОВЫЙ ЭТЮД");
        m.put("ar", "مقطوعة جديدة");
        m.put("hi", "नया अभ्यास");
        m.put("bn", "নতুন অনুশীলন");
        m.put("in", "LAGU BARU");
        m.put("tr", "YENİ ETÜT");
        m.put("vi", "BÀI MỚI");
        m.put("pl", "NOWY UTWÓR");
        m.put("nl", "NIEUW STUK");
        m.put("th", "แบบฝึกหัดใหม่");
        m.put("uk", "НОВИЙ ЕТЮД");
        EXACT.put("NEW PIECE", m);
    }
    private static void putExact_139() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "HINT: ON");
        m.put("fr", "INDICE: OUI");
        m.put("it", "AIUTO: ON");
        m.put("de", "HILFE: AN");
        m.put("es", "PISTA: SÍ");
        m.put("zh", "提示: 开");
        m.put("ja", "ヒント: 入");
        m.put("ko", "힌트: 켜짐");
        m.put("pt", "DICA: SIM");
        m.put("ru", "ПОДСК.: ВКЛ");
        m.put("ar", "تلميح: مفعل");
        m.put("hi", "संकेत: चालू");
        m.put("bn", "ইঙ্গিত: চালু");
        m.put("in", "PETUNJUK: ON");
        m.put("tr", "İPUCU: AÇIK");
        m.put("vi", "GỢI Ý: BẬT");
        m.put("pl", "PODP.: WŁ.");
        m.put("nl", "HINT: AAN");
        m.put("th", "คำใบ้: เปิด");
        m.put("uk", "ПІДКАЗ.: УВІМК");
        EXACT.put("HINT: ON", m);
    }
    private static void putExact_140() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "HINT: OFF");
        m.put("fr", "INDICE: NON");
        m.put("it", "AIUTO: OFF");
        m.put("de", "HILFE: AUS");
        m.put("es", "PISTA: NO");
        m.put("zh", "提示: 关");
        m.put("ja", "ヒント: 切");
        m.put("ko", "힌트: 꺼짐");
        m.put("pt", "DICA: NÃO");
        m.put("ru", "ПОДСК.: ВЫКЛ");
        m.put("ar", "تلميح: معطل");
        m.put("hi", "संकेत: बंद");
        m.put("bn", "ইঙ্গিত: বন্ধ");
        m.put("in", "PETUNJUK: OFF");
        m.put("tr", "İPUCU: KAPALI");
        m.put("vi", "GỢI Ý: TẮT");
        m.put("pl", "PODP.: WYŁ.");
        m.put("nl", "HINT: UIT");
        m.put("th", "คำใบ้: ปิด");
        m.put("uk", "ПІДКАЗ.: ВИМК");
        EXACT.put("HINT: OFF", m);
    }
    private static void putExact_141() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "MODE: ETUDE");
        m.put("fr", "MODE: ÉTUDE");
        m.put("it", "MODO: STUDIO");
        m.put("de", "MODUS: ETÜDE");
        m.put("es", "MODO: ESTUDIO");
        m.put("zh", "模式: 练习曲");
        m.put("ja", "モード: エチュード");
        m.put("ko", "모드: 에튀드");
        m.put("pt", "MODO: ESTUDO");
        m.put("ru", "РЕЖИМ: ЭТЮД");
        m.put("ar", "الوضع: دراسة");
        m.put("hi", "मोड: एट्यूड");
        m.put("bn", "মোড: এটিউড");
        m.put("in", "MODE: ETUDE");
        m.put("tr", "MOD: ETÜT");
        m.put("vi", "CHẾ ĐỘ: ETUDE");
        m.put("pl", "TRYB: ETIUDA");
        m.put("nl", "MODUS: ETUDE");
        m.put("th", "โหมด: เอチュด์");
        m.put("uk", "РЕЖИМ: ЕТЮД");
        EXACT.put("MODE: ETUDE", m);
    }
    private static void putExact_142() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "MODE: QUIZ");
        m.put("fr", "MODE: QUIZ");
        m.put("it", "MODO: QUIZ");
        m.put("de", "MODUS: QUIZ");
        m.put("es", "MODO: TEST");
        m.put("zh", "模式: 测验");
        m.put("ja", "モード: クイズ");
        m.put("ko", "모드: 퀴즈");
        m.put("pt", "MODO: QUIZ");
        m.put("ru", "РЕЖИМ: ТЕСТ");
        m.put("ar", "الوضع: اختبار");
        m.put("hi", "मोड: क्विज़");
        m.put("bn", "মোড: কুইজ");
        m.put("in", "MODE: KUIS");
        m.put("tr", "MOD: TEST");
        m.put("vi", "CHẾ ĐỘ: ĐỐ");
        m.put("pl", "TRYB: QUIZ");
        m.put("nl", "MODUS: QUIZ");
        m.put("th", "โหมด: ทดสอบ");
        m.put("uk", "РЕЖИМ: ТЕСТ");
        EXACT.put("MODE: QUIZ", m);
    }
    private static void putExact_143() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "MODE: FLOW");
        m.put("fr", "MODE: CONTINU");
        m.put("it", "MODO: FLUSSO");
        m.put("de", "MODUS: FLUSS");
        m.put("es", "MODO: FLUJO");
        m.put("zh", "模式: 连续视奏");
        m.put("ja", "モード: フロー");
        m.put("ko", "모드: 연속 연주");
        m.put("pt", "MODO: FLUXO");
        m.put("ru", "РЕЖИМ: ПОТОК");
        m.put("ar", "الوضع: متواصل");
        m.put("hi", "मोड: फ़्लो");
        m.put("bn", "মোড: ফ্লো");
        m.put("in", "MODE: ALIR");
        m.put("tr", "MOD: AKIŞ");
        m.put("vi", "CHẾ ĐỘ: LIÊN TỤC");
        m.put("pl", "TRYB: PŁYNNY");
        m.put("nl", "MODUS: FLOW");
        m.put("th", "โหมด: ต่อเนื่อง");
        m.put("uk", "РЕЖИМ: ПОТІК");
        EXACT.put("MODE: FLOW", m);
    }
    private static void putExact_144() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "GOT IT");
        m.put("fr", "COMPRIS");
        m.put("it", "HO CAPITO");
        m.put("de", "VERSTANDEN");
        m.put("es", "ENTENDIDO");
        m.put("zh", "明白了");
        m.put("ja", "了解");
        m.put("ko", "확인");
        m.put("pt", "ENTENDI");
        m.put("ru", "ПОНЯТНО");
        m.put("ar", "حسناً");
        m.put("hi", "समझ गया");
        m.put("bn", "বুঝেছি");
        m.put("in", "MENGERTI");
        m.put("tr", "ANLADIM");
        m.put("vi", "ĐÃ HIỂU");
        m.put("pl", "ROZUMIEM");
        m.put("nl", "BEGREPEN");
        m.put("th", "เข้าใจแล้ว");
        m.put("uk", "ЗРОЗУМІЛО");
        EXACT.put("GOT IT", m);
    }
    private static void putExact_145() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "OPEN (0-4fr)");
        m.put("fr", "OUVERT (0-4fr)");
        m.put("it", "APERTO (0-4fr)");
        m.put("de", "OFFEN (0-4B)");
        m.put("es", "ABIERTO (0-4tr)");
        m.put("zh", "开放把位 (0-4品)");
        m.put("ja", "開放 (0-4F)");
        m.put("ko", "오픈 (0-4프렛)");
        m.put("pt", "ABERTO (0-4tr)");
        m.put("ru", "ОТКР. (0-4л)");
        m.put("ar", "مفتوح (0-4)");
        m.put("hi", "ओपन (0-4fr)");
        m.put("bn", "ওপেন (0-4fr)");
        m.put("in", "TERBUKA (0-4)");
        m.put("tr", "AÇIK (0-4p)");
        m.put("vi", "MỞ (0-4ph)");
        m.put("pl", "OTW. (0-4pr)");
        m.put("nl", "OPEN (0-4fr)");
        m.put("th", "เปิด (0-4เฟรต)");
        m.put("uk", "ВІДКР. (0-4л)");
        EXACT.put("OPEN (0-4fr)", m);
    }
    private static void putExact_146() {
        Map<String, String> m = new HashMap<>();
        m.put("en", "Ex 1: Open Stepwise ▾");
        m.put("fr", "Ex 1: Degrés Conjoints ▾");
        m.put("it", "Es 1: Gradi Congiunti ▾");
        m.put("de", "Üb 1: Stufenweise ▾");
        m.put("es", "Ej 1: Grados Conjuntos ▾");
        m.put("zh", "练习 1: 开放把位级进 ▾");
        m.put("ja", "練習 1: 開放順次進行 ▾");
        m.put("ko", "연습 1: 개방현 순차진행 ▾");
        m.put("pt", "Ex 1: Graus Conjuntos ▾");
        m.put("ru", "Упр 1: Поступенное ▾");
        m.put("ar", "تمرين 1: تدرج مفتوح ▾");
        m.put("hi", "अभ्यास 1: स्टेपवाइज़ ▾");
        m.put("bn", "অনুশীলন ১: ধাপে ধাপে ▾");
        m.put("in", "Lat 1: Melangkah ▾");
        m.put("tr", "Etüt 1: Yanaşık ▾");
        m.put("vi", "Bài 1: Liền bậc ▾");
        m.put("pl", "Ćw 1: Krokami ▾");
        m.put("nl", "Oef 1: Stapsgewijs ▾");
        m.put("th", "แบบฝึก 1: ไล่ขั้น ▾");
        m.put("uk", "Впр 1: Поступовий ▾");
        EXACT.put("Ex 1: Open Stepwise ▾", m);
    }

    private AppI18n() {}

    public static void initFromIntent(Context context, Intent intent) {
        String lang = null;
        if (intent != null && intent.hasExtra("demo_lang")) {
            lang = intent.getStringExtra("demo_lang");
        }
        if (lang == null || lang.trim().isEmpty()) {
            Locale loc = Locale.getDefault();
            lang = (loc != null) ? loc.getLanguage() : "en";
        }
        setLanguage(lang);
    }

    public static void setLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            sLang = "en";
            return;
        }
        String norm = lang.trim().toLowerCase(Locale.US);
        int dash = norm.indexOf('-');
        if (dash > 0) norm = norm.substring(0, dash);
        int under = norm.indexOf('_');
        if (under > 0) norm = norm.substring(0, under);
        if ("id".equals(norm)) norm = "in";
        sLang = norm;
    }

    private static volatile String sCachedResLang = "";
    private static volatile android.content.res.Resources sCachedRes = null;

    public static String str(Context context, int resId) {
        if (context == null) return "";
        try {
            String lang = sLang;
            if (lang == null || lang.isEmpty()) lang = "en";
            android.content.res.Resources res = sCachedRes;
            if (res == null || !lang.equals(sCachedResLang)) {
                android.content.res.Configuration cfg =
                        new android.content.res.Configuration(context.getResources().getConfiguration());
                Locale loc;
                if ("zh".equals(lang)) {
                    loc = Locale.SIMPLIFIED_CHINESE;
                } else if ("pt".equals(lang)) {
                    loc = new Locale("pt", "BR");
                } else if ("in".equals(lang) || "id".equals(lang)) {
                    loc = new Locale("in");
                } else {
                    loc = new Locale(lang);
                }
                cfg.setLocale(loc);
                res = context.createConfigurationContext(cfg).getResources();
                sCachedRes = res;
                sCachedResLang = lang;
            }
            return res.getString(resId);
        } catch (Exception e) {
            return context.getResources().getString(resId);
        }
    }

    public static java.util.List<String> wrapText(String text, Paint paint, float maxWidth) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            out.add("");
            return out;
        }
        float safeMax = Math.max(40f, maxWidth);
        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            String rem = para.trim();
            if (rem.isEmpty()) {
                out.add("");
                continue;
            }
            while (!rem.isEmpty()) {
                if (paint.measureText(rem) <= safeMax) {
                    out.add(rem);
                    break;
                }
                int count = paint.breakText(rem, true, safeMax, null);
                if (count <= 0) count = 1;
                if (count < rem.length()) {
                    int lastSpace = rem.lastIndexOf(' ', count);
                    if (lastSpace > count / 3) {
                        out.add(rem.substring(0, lastSpace).trim());
                        rem = rem.substring(lastSpace + 1).trim();
                        continue;
                    }
                }
                out.add(rem.substring(0, count).trim());
                rem = rem.substring(count).trim();
            }
        }
        return out;
    }

    public static float drawScrollableHelpBlock(
            Canvas canvas,
            android.graphics.RectF viewport,
            String[] titles,
            String[] bodies,
            int[] titleColors,
            int bodyColor,
            int cardFillColor,
            int cardBorderColor,
            int trackColor,
            int thumbColor,
            Paint textPaint,
            Paint fillPaint,
            Paint strokePaint,
            float titleSize,
            float bodySize,
            float dp,
            float scrollY) {
        float sbWidth = 6.5f * dp;
        float sbMargin = 7.0f * dp;
        float cardPadX = 12.0f * dp;
        float cardPadY = 10.0f * dp;
        float secGap = 8.5f * dp;
        float cardLeft = viewport.left;
        float cardRight = viewport.right - sbWidth - sbMargin;
        float textMaxW = Math.max(48f, (cardRight - cardLeft) - cardPadX * 2.0f);

        float titleLineH = titleSize * 1.30f;
        float bodyLineH = bodySize * 1.38f;

        int n = Math.min(titles.length, bodies.length);
        java.util.List<java.util.List<String>> wrappedTitles = new java.util.ArrayList<>(n);
        java.util.List<java.util.List<String>> wrappedBodies = new java.util.ArrayList<>(n);
        float[] cardHeights = new float[n];
        float totalH = 0f;

        boolean origBold = textPaint.isFakeBoldText();
        float origSize = textPaint.getTextSize();
        Paint.Align origAlign = textPaint.getTextAlign();

        for (int i = 0; i < n; i++) {
            textPaint.setFakeBoldText(true);
            textPaint.setTextSize(titleSize);
            java.util.List<String> tLines = wrapText(titles[i], textPaint, textMaxW);
            wrappedTitles.add(tLines);

            textPaint.setFakeBoldText(false);
            textPaint.setTextSize(bodySize);
            java.util.List<String> bLines = wrapText(bodies[i], textPaint, textMaxW);
            wrappedBodies.add(bLines);

            float ch = cardPadY * 2.0f + tLines.size() * titleLineH + 5.0f * dp + bLines.size() * bodyLineH;
            cardHeights[i] = ch;
            totalH += ch;
            if (i < n - 1) totalH += secGap;
        }
        totalH += 4.0f * dp;

        float viewH = Math.max(1f, viewport.height());
        float maxScroll = Math.max(0f, totalH - viewH);
        float curScroll = Math.max(0f, Math.min(scrollY, maxScroll));

        int save = canvas.save();
        canvas.clipRect(viewport);

        float curY = viewport.top - curScroll;
        android.graphics.RectF cardRect = new android.graphics.RectF();
        for (int i = 0; i < n; i++) {
            float ch = cardHeights[i];
            if (curY + ch >= viewport.top - 4f && curY <= viewport.bottom + 4f) {
                cardRect.set(cardLeft, curY, cardRight, curY + ch);
                fillPaint.setStyle(Paint.Style.FILL);
                fillPaint.setColor(cardFillColor);
                canvas.drawRoundRect(cardRect, 8.0f * dp, 8.0f * dp, fillPaint);

                strokePaint.setStyle(Paint.Style.STROKE);
                strokePaint.setStrokeWidth(1.2f * dp);
                strokePaint.setColor(cardBorderColor);
                canvas.drawRoundRect(cardRect, 8.0f * dp, 8.0f * dp, strokePaint);

                float ty = curY + cardPadY + titleSize * 0.86f;
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setFakeBoldText(true);
                textPaint.setTextSize(titleSize);
                textPaint.setColor(titleColors[i % titleColors.length]);
                for (String tl : wrappedTitles.get(i)) {
                    canvas.drawText(tl, cardLeft + cardPadX, ty, textPaint);
                    ty += titleLineH;
                }

                ty += 5.0f * dp - (titleLineH - bodyLineH) * 0.25f;
                textPaint.setFakeBoldText(false);
                textPaint.setTextSize(bodySize);
                textPaint.setColor(bodyColor);
                for (String bl : wrappedBodies.get(i)) {
                    canvas.drawText(bl, cardLeft + cardPadX, ty, textPaint);
                    ty += bodyLineH;
                }
            }
            curY += ch + secGap;
        }
        canvas.restoreToCount(save);

        /* Draw Vertical Scrollbar Track & Thumb */
        android.graphics.RectF trackRect = new android.graphics.RectF(
                viewport.right - sbWidth, viewport.top, viewport.right, viewport.bottom);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(trackColor);
        canvas.drawRoundRect(trackRect, sbWidth * 0.5f, sbWidth * 0.5f, fillPaint);

        float ratio = (totalH > viewH) ? (viewH / totalH) : 0.78f;
        float thumbH = Math.max(28.0f * dp, Math.min(viewH, viewH * ratio));
        float availTrack = Math.max(0f, viewH - thumbH);
        float scrollFrac = (maxScroll > 0f) ? (curScroll / maxScroll) : 0f;
        float thumbTop = viewport.top + availTrack * scrollFrac;
        android.graphics.RectF thumbRect = new android.graphics.RectF(
                viewport.right - sbWidth, thumbTop, viewport.right, thumbTop + thumbH);
        fillPaint.setColor(thumbColor);
        canvas.drawRoundRect(thumbRect, sbWidth * 0.5f, sbWidth * 0.5f, fillPaint);

        textPaint.setFakeBoldText(origBold);
        textPaint.setTextSize(origSize);
        textPaint.setTextAlign(origAlign);
        return maxScroll;
    }

    public static float drawScrollableHelpBlock(
            Canvas canvas,
            android.graphics.RectF viewport,
            String[] titles,
            String[] bodies,
            int[] titleColors,
            int bodyColor,
            int cardFillColor,
            int cardBorderColor,
            int thumbColor,
            float titleSize,
            float bodySize,
            float scrollY,
            Paint textPaint,
            Paint fillPaint) {
        float dp = Math.max(1.0f, bodySize / 11.2f);
        int trackColor = (cardBorderColor & 0x00FFFFFF) | 0x66000000;
        return drawScrollableHelpBlock(
                canvas, viewport, titles, bodies, titleColors,
                bodyColor, cardFillColor, cardBorderColor, trackColor, thumbColor,
                textPaint, fillPaint, fillPaint, titleSize, bodySize, dp, scrollY);
    }

    public static String getLanguage() {
        return sLang;
    }

    public static void drawText(Canvas canvas, String text, float x, float y, Paint paint) {
        canvas.drawText(tr(text), x, y, paint);
    }

    public static void drawTextFit(Canvas canvas, String text, float x, float y, Paint paint, float maxWidth) {
        String trText = tr(text);
        float origSize = paint.getTextSize();
        float tw = paint.measureText(trText);
        if (tw > maxWidth && tw > 0f && maxWidth > 4f) {
            paint.setTextSize(origSize * (maxWidth / tw));
        }
        canvas.drawText(trText, x, y, paint);
        paint.setTextSize(origSize);
    }

    public static String tr(String text) {
        if (text == null || text.isEmpty() || "en".equals(sLang)) {
            return text;
        }
        Map<String, String> exactMap = EXACT.get(text);
        if (exactMap != null) {
            String hit = exactMap.get(sLang);
            if (hit != null) return hit;
        }
        return translateDynamic(text, sLang);
    }

    private static String translateDynamic(String t, String lang) {
        if (t.startsWith("KEY: ")) {
            return keyPrefixColon(lang) + t.substring(5);
        }
        if (t.startsWith("Key: ")) {
            return keyPrefixColon(lang) + t.substring(5);
        }
        if (t.startsWith("KEY ")) {
            return keyPrefixSpace(lang) + t.substring(4);
        }
        if (t.startsWith("STEP ")) {
            return stepPrefix(lang) + t.substring(5);
        }
        if (t.startsWith("BEAT ")) {
            return beatPrefix(lang) + t.substring(5);
        }
        if (t.contains("  BAR ") && t.contains("  BEAT ")) {
            return t.replace("  BAR ", "  " + barPrefix(lang))
                    .replace("  BEAT ", "  " + beatPrefix(lang));
        }
        if (t.startsWith("SOLO SCALE: ")) {
            String rest = t.substring(12).replace("Minor Pentatonic + Blues", minPentBlues(lang));
            return soloScalePrefix(lang) + rest;
        }
        if (t.startsWith("NOTE: ")) {
            return notePrefix(lang) + t.substring(6);
        }
        if (t.startsWith("FINGER: ")) {
            String val = t.substring(8);
            return fingerPrefix(lang) + tr(val);
        }
        if (t.startsWith("PICK: ")) {
            String val = t.substring(6);
            if (val.startsWith("DOWN")) val = pickDown(lang) + val.substring(4);
            else if (val.startsWith("UP")) val = pickUp(lang) + val.substring(2);
            return pickPrefix(lang) + val;
        }
        if (t.startsWith("STRK ") && t.contains(" (BEST ")) {
            String rest = t.substring(5);
            int idx = rest.indexOf(" (BEST ");
            String sNum = rest.substring(0, idx);
            String bNum = rest.substring(idx + 7);
            return streakPrefix(lang) + sNum + " (" + bestPrefix(lang) + bNum;
        }
        if (t.startsWith("Root: ") && t.contains("  |  Identify the ")) {
            int idx = t.indexOf("  |  Identify the ");
            String rootPart = t.substring(6, idx);
            String kind = t.substring(idx + 18);
            return rootWord(lang) + ": " + rootPart + "  |  " + identifyPhrase(lang, kind);
        }
        if (t.startsWith("Tap any answer pad below (")) {
            int idx = t.indexOf(" tries left)");
            String n = (idx > 26) ? t.substring(26, idx) : "3";
            return tapPadTriesPhrase(lang, n);
        }
        if (t.startsWith("TODAY (") && t.endsWith(")")) {
            String mid = t.substring(7, t.length() - 1);
            return todayDatePhrase(lang, mid);
        }
        if (t.endsWith(" hrs")) {
            return t.substring(0, t.length() - 4) + " " + hrsWord(lang);
        }
        if (t.endsWith(" Pomodoros")) {
            String num = t.substring(0, t.length() - 10);
            return num + " " + pomodorosWord(lang);
        }
        if (t.startsWith("Classic ")) return classicWord(lang) + t.substring(7);
        if (t.startsWith("Deep Work ")) return deepWorkWord(lang) + t.substring(9);
        if (t.startsWith("Studio ")) return studioWord(lang) + t.substring(6);
        if (t.startsWith("Sprint ")) return sprintWord(lang) + t.substring(6);
        if (t.startsWith("FOCUS (")) {
            return focusTabWord(lang) + " (" + t.substring(7);
        }
        if (t.startsWith("SHORT (")) {
            return shortTabWord(lang) + " (" + t.substring(7);
        }
        if (t.startsWith("LONG (")) {
            return longTabWord(lang) + " (" + t.substring(6);
        }
        if (t.startsWith("Target: ")) {
            String rest = t.substring(8);
            rest = rest.replace("Stepwise Quarters", stepwiseQuarters(lang));
            return targetWord(lang) + ": " + rest;
        }
        if (t.startsWith("Score: ") && t.contains("Streak: ")) {
            return t.replace("Score: ", scoreWord(lang) + ": ")
                    .replace("Streak: ", streakPrefix(lang).trim() + ": ");
        }
        if (t.startsWith("TREBLE CLEF")) {
            return t.replace("TREBLE CLEF", trebleClefWord(lang))
                    .replace("OPEN", tr("OPEN"));
        }
        if (t.contains(" • Formula: ") && t.contains(" • Notes: ")) {
            return t.replace("Dorian Mode", dorianName(lang))
                    .replace("Formula: ", formulaWord(lang) + ": ")
                    .replace("Notes: ", notesWord(lang) + ": ");
        }
        if (t.startsWith("Skeleton Chord: ")) {
            return t.replace("Skeleton Chord: ", skeletonChordWord(lang) + ": ")
                    .replace("Position: ALL NECK", posWord(lang) + ": " + tr("ALL NECK"))
                    .replace("Tap any fret to audition note", tapFretAudition(lang));
        }
        return t;
    }

    private static String keyPrefixColon(String l) {
        switch (l) {
            case "fr": return "TON: ";
            case "it": return "TON.: ";
            case "de": return "TONART: ";
            case "es": return "TONO: ";
            case "zh": return "调式: ";
            case "ja": return "キー: ";
            case "ko": return "조성: ";
            default: return "KEY: ";
        }
    }

    private static String keyPrefixSpace(String l) {
        switch (l) {
            case "fr": return "TON ";
            case "it": return "TON. ";
            case "de": return "TONART ";
            case "es": return "TONO ";
            case "zh": return "调式 ";
            case "ja": return "キー ";
            case "ko": return "조성 ";
            default: return "KEY ";
        }
    }

    private static String stepPrefix(String l) {
        switch (l) {
            case "fr": return "PAS ";
            case "it": return "PASSO ";
            case "de": return "SCHRITT ";
            case "es": return "PASO ";
            case "zh": return "步进 ";
            case "ja": return "ステップ ";
            case "ko": return "스텝 ";
            default: return "STEP ";
        }
    }

    private static String barPrefix(String l) {
        switch (l) {
            case "fr": return "MES. ";
            case "it": return "BATT. ";
            case "de": return "TAKT ";
            case "es": return "COMPÁS ";
            case "zh": return "小节 ";
            case "ja": return "小節 ";
            case "ko": return "마디 ";
            default: return "BAR ";
        }
    }

    private static String beatPrefix(String l) {
        switch (l) {
            case "fr": return "TEMPS ";
            case "it": return "MOV. ";
            case "de": return "SCHLAG ";
            case "es": return "PULSO ";
            case "zh": return "拍 ";
            case "ja": return "拍 ";
            case "ko": return "박 ";
            default: return "BEAT ";
        }
    }

    private static String soloScalePrefix(String l) {
        switch (l) {
            case "fr": return "GAMME SOLO: ";
            case "it": return "SCALA SOLO: ";
            case "de": return "SOLO-SKALA: ";
            case "es": return "ESCALA SOLO: ";
            case "zh": return "独奏音阶: ";
            case "ja": return "ソロスケール: ";
            case "ko": return "솔로 스케일: ";
            default: return "SOLO SCALE: ";
        }
    }

    private static String minPentBlues(String l) {
        switch (l) {
            case "fr": return "Pentatonique Min + Blues";
            case "it": return "Pentatonica Min + Blues";
            case "de": return "Moll-Pentatonik + Blues";
            case "es": return "Pentatónica Men + Blues";
            case "zh": return "小调五声 + 布鲁斯音阶";
            case "ja": return "マイナーペンタ + ブルース";
            case "ko": return "마이너 펜타토닉 + 블루스";
            default: return "Minor Pentatonic + Blues";
        }
    }

    private static String notePrefix(String l) {
        switch (l) {
            case "fr": return "NOTE: ";
            case "it": return "NOTA: ";
            case "de": return "NOTE: ";
            case "es": return "NOTA: ";
            case "zh": return "音符: ";
            case "ja": return "音名: ";
            case "ko": return "음표: ";
            default: return "NOTE: ";
        }
    }

    private static String fingerPrefix(String l) {
        switch (l) {
            case "fr": return "DOIGT: ";
            case "it": return "DITO: ";
            case "de": return "FINGER: ";
            case "es": return "DEDO: ";
            case "zh": return "指法: ";
            case "ja": return "運指: ";
            case "ko": return "운지: ";
            default: return "FINGER: ";
        }
    }

    private static String pickPrefix(String l) {
        switch (l) {
            case "fr": return "MÉD.: ";
            case "it": return "PENN.: ";
            case "de": return "ANSCHL.: ";
            case "es": return "PÚA: ";
            case "zh": return "拨弦: ";
            case "ja": return "ピッキング: ";
            case "ko": return "피킹: ";
            default: return "PICK: ";
        }
    }

    private static String pickDown(String l) {
        switch (l) {
            case "fr": return "BAS";
            case "it": return "GIÙ";
            case "de": return "AB";
            case "es": return "ABAJO";
            case "zh": return "下拨";
            case "ja": return "ダウン";
            case "ko": return "다운";
            default: return "DOWN";
        }
    }

    private static String pickUp(String l) {
        switch (l) {
            case "fr": return "HAUT";
            case "it": return "SU";
            case "de": return "AUF";
            case "es": return "ARRIBA";
            case "zh": return "上挑";
            case "ja": return "アップ";
            case "ko": return "업";
            default: return "UP";
        }
    }

    private static String streakPrefix(String l) {
        switch (l) {
            case "fr": return "SÉRIE ";
            case "it": return "SERIE ";
            case "de": return "SERIE ";
            case "es": return "RACHA ";
            case "zh": return "连对 ";
            case "ja": return "連続 ";
            case "ko": return "연속 ";
            default: return "STRK ";
        }
    }

    private static String bestPrefix(String l) {
        switch (l) {
            case "fr": return "MAX ";
            case "it": return "MAX ";
            case "de": return "BEST ";
            case "es": return "MÁX ";
            case "zh": return "最高 ";
            case "ja": return "最高 ";
            case "ko": return "최고 ";
            default: return "BEST ";
        }
    }

    private static String rootWord(String l) {
        switch (l) {
            case "fr": return "Fondamentale";
            case "it": return "Fondamentale";
            case "de": return "Grundton";
            case "es": return "Tónica";
            case "zh": return "根音";
            case "ja": return "ルート音";
            case "ko": return "근음";
            default: return "Root";
        }
    }

    private static String identifyPhrase(String l, String kind) {
        switch (l) {
            case "fr": return "Identifiez l'intervalle (m2..P8)";
            case "it": return "Identifica l'intervallo (m2..P8)";
            case "de": return "Intervall bestimmen (m2..P8)";
            case "es": return "Identifica el intervalo (m2..P8)";
            case "zh": return "请听辨当前音程 (m2..P8)";
            case "ja": return "音程を聴き分けよう (m2..P8)";
            case "ko": return "들리는 음정을 찾으세요 (m2..P8)";
            default: return "Identify the " + kind;
        }
    }

    private static String tapPadTriesPhrase(String l, String n) {
        switch (l) {
            case "fr": return "Touchez un pad ci-dessous (" + n + " essais restants) ou le piano/manche";
            case "it": return "Tocca un pad qui sotto (" + n + " tentativi rimasti) o piano/tastiera";
            case "de": return "Tippe unten auf ein Pad (" + n + " Versuche übrig) oder Klavier/Griffbrett";
            case "es": return "Toca un botón abajo (" + n + " intentos) o explora el piano/diapasón";
            case "zh": return "点击下方按钮作答（剩余 " + n + " 次机会），或点击键盘/指板试听";
            case "ja": return "下のパッドで回答（残り " + n + " 回）、または鍵盤・指板をタップして試聴";
            case "ko": return "아래 패드를 탭해 정답 선택 (남은 기회 " + n + "회) 또는 건반/지판 탐색";
            default: return "Tap any answer pad below (" + n + " tries left) or tap Piano/Fretboard to explore";
        }
    }

    private static String todayDatePhrase(String l, String mid) {
        switch (l) {
            case "fr": return "AUJOURD'HUI (" + mid + ")";
            case "it": return "OGGI (" + mid + ")";
            case "de": return "HEUTE (" + mid + ")";
            case "es": return "HOY (" + mid + ")";
            case "zh": return "今日专注 (" + mid + ")";
            case "ja": return "今日の実績 (" + mid + ")";
            case "ko": return "오늘의 기록 (" + mid + ")";
            default: return "TODAY (" + mid + ")";
        }
    }

    private static String hrsWord(String l) {
        switch (l) {
            case "fr": return "h";
            case "it": return "ore";
            case "de": return "Std.";
            case "es": return "h";
            case "zh": return "小时";
            case "ja": return "時間";
            case "ko": return "시간";
            default: return "hrs";
        }
    }

    private static String pomodorosWord(String l) {
        switch (l) {
            case "zh": return "个番茄钟";
            case "ja": return "ポモドーロ";
            case "ko": return "포모도로";
            default: return "Pomodoros";
        }
    }

    private static String classicWord(String l) {
        switch (l) {
            case "fr": return "Classique ";
            case "it": return "Classico ";
            case "de": return "Klassisch ";
            case "es": return "Clásico ";
            case "zh": return "经典 ";
            case "ja": return "標準 ";
            case "ko": return "클래식 ";
            default: return "Classic ";
        }
    }

    private static String deepWorkWord(String l) {
        switch (l) {
            case "fr": return "Profond ";
            case "it": return "Profondo ";
            case "de": return "Deep Work ";
            case "es": return "Profundo ";
            case "zh": return "深度 ";
            case "ja": return "ディープ ";
            case "ko": return "딥워크 ";
            default: return "Deep Work ";
        }
    }

    private static String studioWord(String l) {
        switch (l) {
            case "zh": return "工作室 ";
            case "ja": return "スタジオ ";
            case "ko": return "스튜디오 ";
            default: return "Studio ";
        }
    }

    private static String sprintWord(String l) {
        switch (l) {
            case "zh": return "冲刺 ";
            case "ja": return "スプリント ";
            case "ko": return "스프린트 ";
            default: return "Sprint ";
        }
    }

    private static String focusTabWord(String l) {
        switch (l) {
            case "fr": return "FOCUS";
            case "it": return "FOCUS";
            case "de": return "FOKUS";
            case "es": return "FOCO";
            case "zh": return "专注";
            case "ja": return "集中";
            case "ko": return "집중";
            default: return "FOCUS";
        }
    }

    private static String shortTabWord(String l) {
        switch (l) {
            case "fr": return "COURTE";
            case "it": return "BREVE";
            case "de": return "KURZ";
            case "es": return "CORTA";
            case "zh": return "短休";
            case "ja": return "短休憩";
            case "ko": return "짧은휴식";
            default: return "SHORT";
        }
    }

    private static String longTabWord(String l) {
        switch (l) {
            case "fr": return "LONGUE";
            case "it": return "LUNGA";
            case "de": return "LANG";
            case "es": return "LARGA";
            case "zh": return "长休";
            case "ja": return "長休憩";
            case "ko": return "긴휴식";
            default: return "LONG";
        }
    }

    private static String targetWord(String l) {
        switch (l) {
            case "fr": return "Cible";
            case "it": return "Nota";
            case "de": return "Ziel";
            case "es": return "Objetivo";
            case "zh": return "目标音";
            case "ja": return "ターゲット";
            case "ko": return "목표 음";
            default: return "Target";
        }
    }

    private static String stepwiseQuarters(String l) {
        switch (l) {
            case "fr": return "Noires conjointes";
            case "it": return "Semiminime congiunte";
            case "de": return "Stufenweise Viertel";
            case "es": return "Negras por grados";
            case "zh": return "四分音符级进";
            case "ja": return "四分音符の順次進行";
            case "ko": return "4분음표 순차진행";
            default: return "Stepwise Quarters";
        }
    }

    private static String trebleClefWord(String l) {
        switch (l) {
            case "fr": return "CLÉ DE SOL";
            case "it": return "CHIAVE DI VIOLINO";
            case "de": return "VIOLINSCHLÜSSEL";
            case "es": return "CLAVE DE SOL";
            case "zh": return "高音谱号";
            case "ja": return "ト音記号";
            case "ko": return "높은음자리표";
            default: return "TREBLE CLEF";
        }
    }

    private static String scoreWord(String l) {
        switch (l) {
            case "fr": return "Score";
            case "it": return "Punti";
            case "de": return "Punkte";
            case "es": return "Puntos";
            case "zh": return "得分";
            case "ja": return "スコア";
            case "ko": return "점수";
            default: return "Score";
        }
    }

    private static String posWord(String l) {
        switch (l) {
            case "de": return "Lage";
            case "zh": return "把位";
            case "ja": return "ポジション";
            case "ko": return "포지션";
            default: return "Position";
        }
    }

    private static String notesWord(String l) {
        switch (l) {
            case "fr": return "Notes";
            case "it": return "Note";
            case "de": return "Noten";
            case "es": return "Notas";
            case "zh": return "音符";
            case "ja": return "構成音";
            case "ko": return "구성음";
            default: return "Notes";
        }
    }

    private static String formulaWord(String l) {
        switch (l) {
            case "fr": return "Formule";
            case "it": return "Formula";
            case "de": return "Formel";
            case "es": return "Fórmula";
            case "zh": return "音程公式";
            case "ja": return "度数構成";
            case "ko": return "음정 공식";
            default: return "Formula";
        }
    }

    private static String dorianName(String l) {
        switch (l) {
            case "fr": return "Mode Dorien";
            case "it": return "Modo Dorico";
            case "de": return "Dorischer Modus";
            case "es": return "Modo Dórico";
            case "zh": return "多利亚调式";
            case "ja": return "ドリアン・モード";
            case "ko": return "도리안 모드";
            default: return "Dorian Mode";
        }
    }

    private static String skeletonChordWord(String l) {
        switch (l) {
            case "fr": return "Accord squelette";
            case "it": return "Accordo guida";
            case "de": return "Basis-Akkord";
            case "es": return "Acorde base";
            case "zh": return "骨干和弦";
            case "ja": return "構成コード";
            case "ko": return "골격 코드";
            default: return "Skeleton Chord";
        }
    }

    private static String tapFretAudition(String l) {
        switch (l) {
            case "fr": return "Touchez une frette pour écouter";
            case "it": return "Tocca un tasto per ascoltare";
            case "de": return "Bund antippen zum Anhören";
            case "es": return "Toca un traste para escuchar";
            case "zh": return "点击任意品格试听音高";
            case "ja": return "フレットをタップして試聴";
            case "ko": return "프렛을 탭하여 음 듣기";
            default: return "Tap any fret to audition note";
        }
    }
}
