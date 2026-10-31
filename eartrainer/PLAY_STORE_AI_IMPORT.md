# Google Play Console — "Import Translations with AI" File (EarTrainer / `eartrainer` — All 20 Languages)

Upload this file (**[`PLAY_STORE_AI_IMPORT.md`](PLAY_STORE_AI_IMPORT.md)**), the CSV version (**[`PLAY_STORE_AI_IMPORT.csv`](PLAY_STORE_AI_IMPORT.csv)**), or the plain-text JSON version (**[`PLAY_STORE_AI_IMPORT.txt`](PLAY_STORE_AI_IMPORT.txt)**) directly into the Google Play Console **Store Listing → Import translations with AI** dialog. It automatically populates **App name (`appName`, ≤ 30 chars)**, **Short description (`shortDescription`, ≤ 80 chars)**, and **Full description (`fullDescription`, ≤ 4000 chars)** across all **20 supported languages**.

## Summary Table of All 20 Languages

| `#` | Play Console `languageCode` | Language | `appName` (`<= 30` chars) | `shortDescription` (`<= 80` chars) | `fullDescription` (`<= 4000` chars) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | **`en-US`** | English (United States) | [`EarTrainer: Pitch, Chords, Key`](playstore/metadata/android/en-US/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/en-US/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/en-US/full_description.txt) (3231/4000) |
| 2 | **`fr-FR`** | French (France) / Français | [`EarTrainer : Oreille Musicale`](playstore/metadata/android/fr-FR/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/fr-FR/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/fr-FR/full_description.txt) (1494/4000) |
| 3 | **`it-IT`** | Italian (Italy) / Italiano | [`EarTrainer: Ear Training Piano`](playstore/metadata/android/it-IT/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/it-IT/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/it-IT/full_description.txt) (1433/4000) |
| 4 | **`de-DE`** | German (Germany) / Deutsch | [`EarTrainer: Gehörbildung Piano`](playstore/metadata/android/de-DE/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/de-DE/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/de-DE/full_description.txt) (1420/4000) |
| 5 | **`es-ES`** | Spanish (Spain) / Español | [`EarTrainer: Entrenar el Oído`](playstore/metadata/android/es-ES/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/es-ES/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/es-ES/full_description.txt) (1388/4000) |
| 6 | **`zh-CN`** | Chinese (Simplified) / 中文（简体） | [`视唱练耳教练 (EarTrainer)`](playstore/metadata/android/zh-CN/title.txt) (19/30) | [`short_description.txt`](playstore/metadata/android/zh-CN/short_description.txt) (38/80) | [`full_description.txt`](playstore/metadata/android/zh-CN/full_description.txt) (725/4000) |
| 7 | **`ja-JP`** | Japanese / 日本語 | [`EarTrainer: 相対音感＆耳コピ練習`](playstore/metadata/android/ja-JP/title.txt) (22/30) | [`short_description.txt`](playstore/metadata/android/ja-JP/short_description.txt) (41/80) | [`full_description.txt`](playstore/metadata/android/ja-JP/full_description.txt) (779/4000) |
| 8 | **`ko-KR`** | Korean / 한국어 | [`EarTrainer: 청음 & 상대음감 훈련`](playstore/metadata/android/ko-KR/title.txt) (24/30) | [`short_description.txt`](playstore/metadata/android/ko-KR/short_description.txt) (43/80) | [`full_description.txt`](playstore/metadata/android/ko-KR/full_description.txt) (828/4000) |
| 9 | **`pt-BR`** | Portuguese (Brazil) / Português | [`EarTrainer: Treino Auditivo`](playstore/metadata/android/pt-BR/title.txt) (27/30) | [`short_description.txt`](playstore/metadata/android/pt-BR/short_description.txt) (75/80) | [`full_description.txt`](playstore/metadata/android/pt-BR/full_description.txt) (953/4000) |
| 10 | **`ru-RU`** | Russian / Русский | [`EarTrainer: Музыкальный Слух`](playstore/metadata/android/ru-RU/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/ru-RU/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/ru-RU/full_description.txt) (786/4000) |
| 11 | **`ar`** | Arabic / العربية | [`EarTrainer: تدريب الأذن`](playstore/metadata/android/ar/title.txt) (23/30) | [`short_description.txt`](playstore/metadata/android/ar/short_description.txt) (66/80) | [`full_description.txt`](playstore/metadata/android/ar/full_description.txt) (562/4000) |
| 12 | **`hi-IN`** | Hindi / हिन्दी | [`EarTrainer: ईयर ट्रेनिंग`](playstore/metadata/android/hi-IN/title.txt) (24/30) | [`short_description.txt`](playstore/metadata/android/hi-IN/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/hi-IN/full_description.txt) (580/4000) |
| 13 | **`bn-BD`** | Bangla (Bengali) / বাংলা | [`EarTrainer: ইয়ার ট্রেনিং`](playstore/metadata/android/bn-BD/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/bn-BD/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/bn-BD/full_description.txt) (529/4000) |
| 14 | **`id`** | Indonesian / Bahasa Indonesia | [`EarTrainer: Latih Telinga`](playstore/metadata/android/id/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/id/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/id/full_description.txt) (659/4000) |
| 15 | **`tr-TR`** | Turkish / Türkçe | [`EarTrainer: Kulak Eğitimi`](playstore/metadata/android/tr-TR/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/tr-TR/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/tr-TR/full_description.txt) (365/4000) |
| 16 | **`vi`** | Vietnamese / Tiếng Việt | [`EarTrainer: Luyện Tai Âm Nhạc`](playstore/metadata/android/vi/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/vi/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/vi/full_description.txt) (350/4000) |
| 17 | **`pl-PL`** | Polish / Polski | [`EarTrainer: Kształcenie Słuchu`](playstore/metadata/android/pl-PL/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/pl-PL/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/pl-PL/full_description.txt) (320/4000) |
| 18 | **`nl-NL`** | Dutch / Nederlands | [`EarTrainer: Gehoorvorming`](playstore/metadata/android/nl-NL/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/nl-NL/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/nl-NL/full_description.txt) (334/4000) |
| 19 | **`th`** | Thai / ไทย | [`EarTrainer: ฝึกโสตทักษะดนตรี`](playstore/metadata/android/th/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/th/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/th/full_description.txt) (303/4000) |
| 20 | **`uk`** | Ukrainian / Українська | [`EarTrainer: Музичний Слух`](playstore/metadata/android/uk/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/uk/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/uk/full_description.txt) (314/4000) |

---

## Language: `en-US` — English (United States)

- **`languageCode`**: `en-US`
- **`appName`** (30 / 30 characters): EarTrainer: Pitch, Chords, Key
- **`shortDescription`** (74 / 80 characters): Train your ear on intervals, chords, and scales with an interactive piano.
- **`fullDescription`** (3231 / 4000 characters):

```text
Develop razor-sharp relative pitch, harmonic recognition, and musical memory with EarTrainer — Interval, Chord & Scale Ear Gym. Built for guitarists, pianists, vocalists, producers, and music theory students, EarTrainer combines four progressive ear-training disciplines with an interactive 2-octave keyboard, three synthesized instrument timbres, and real-time accuracy telemetry.

Great musicians don't guess which fret or key to play next—they hear the interval, chord quality, and scale degree in their head before their fingers touch the instrument. EarTrainer gives you a structured, distraction-free daily workout to connect what you hear directly to music theory and your instrument.

FOUR COMPREHENSIVE EAR-TRAINING MODULES
1. INTERVAL RECOGNITION
• All 13 Chromatic Intervals: From Minor 2nd (m2) and Major 2nd (M2) through Tritone (TT), Perfect 5th (P5), Minor/Major 6ths and 7ths, up to the Perfect Octave (P8).
• 3 Playback Directions: Practice intervals Ascending (melodic up), Descending (melodic down), or Harmonic (both notes struck simultaneously) to master both melodic soloing and chord voicing analysis.

2. CHORD QUALITY IDENTIFICATION
• 9 Essential Triad & 7th Qualities: Hear the distinct emotional and harmonic color of Major, Minor, Diminished, Augmented, Sus2, Sus4, Dominant 7, Major 7, and Minor 7 chords.
• Arpeggiated + Block Voicing Playback: Listen to the chord both rolled note-by-note and played together so you can isolate internal intervals (3rds, 5ths, and 7ths) within the harmony.

3. SCALES & MODES EAR GYM
• 8 Core Scales & Church Modes: Train your ear to distinguish Major (Ionian), Natural Minor (Aeolian), Harmonic Minor, Melodic Minor, Dorian, Mixolydian, Lydian, and Phrygian by their characteristic color tones.

4. MELODIC DICTATION (PLAY BY EAR)
• Transcribe 3-Note to 5-Note Melodies: Listen to a short melodic phrase anchored to a tonic reference and play it back note-for-note to build real-world transcription and improvisation reflexes.

PROGRESSIVE DIFFICULTY TIERS & INTERACTIVE 2-OCTAVE PIANO
• 3 Structured Skill Levels: Start with Level 1 (Foundational Consonances & Basic Triads), advance to Level 2 (Full Diatonic Palette & 7th Chords), and master Level 3 (All 13 Chromatic Intervals, Modal Scales & Extended Harmonies).
• Playable 2-Octave Keyboard (C4–C6): Freely audition notes, compare intervals, and test melodies on the responsive on-screen piano before or after locking in your answer.
• Visual Answer Breakdown: Whenever you answer a question, EarTrainer highlights the exact root and target keys on the piano keyboard and displays the interval distance in semitones so every mistake turns into an instant learning moment.

THREE REAL-TIME INSTRUMENT TIMBRES
• Train your ear across multiple acoustic textures using our 44.1 kHz polyphonic synthesis engine: switch at any time between Grand Piano (harmonic-rich hammer attack), Acoustic Guitar Pluck (Karplus-Strong string model), and Warm Studio Organ (sustained drawbar tones).

PRIVACY-FIRST, OFFLINE & DISTRACTION-FREE
• Tracks your current streak, best streak, and session accuracy locally on your device.
• Responsive Portrait and Landscape layouts, zero permissions, zero ads, and 100% offline operation.
```

---

## Language: `fr-FR` — French (France) / Français

- **`languageCode`**: `fr-FR`
- **`appName`** (29 / 30 characters): EarTrainer : Oreille Musicale
- **`shortDescription`** (80 / 80 characters): Entraînez votre oreille aux intervalles, accords et gammes avec piano interactif
- **`fullDescription`** (1494 / 4000 characters):

```text
Développez une oreille relative précise, la reconnaissance harmonique et votre mémoire musicale avec EarTrainer : Entraînement de l'Oreille Musicale. Conçu pour les guitaristes, pianistes, chanteurs et étudiants en musique, EarTrainer réunit 4 modules progressifs, un clavier interactif de 2 octaves, 3 timbres d'instruments synthétisés et un suivi de précision en temps réel.

QUATRE MODULES COMPLETS D'ENTRAÎNEMENT AUDITIF
1. RECONNAISSANCE DES INTERVALLES
• Les 13 intervalles chromatiques : De la seconde mineure (m2) à l'octave juste (P8) en passant par le triton (TT) et la quinte juste (P5).
• 3 modes d'écoute : Ascendant, Descendant ou Harmonique (notes jouées simultanément).

2. IDENTIFICATION DES ACCORDS
• 9 qualités d'accords essentiels : Majeur, Mineur, Diminué, Augmenté, Sus2, Sus4, 7e de dominante, Majeur 7 et Mineur 7.

3. GAMMES ET MODES
• 8 gammes et modes : Majeur (Ionien), Mineur naturel, Mineur harmonique, Mineur mélodique, Dorien, Mixolydien, Lydien et Phrygien.

4. DICTÉE MÉLODIQUE
• Écoutez de courtes phrases mélodiques de 3 à 5 notes et rejouez-les note par note pour développer vos réflexes de transcription.

PIANO INTERACTIF 2 OCTAVES ET 3 TIMBRES D'INSTRUMENTS
• Clavier jouable (C4–C6) avec affichage visuel des intervalles en demi-tons après chaque réponse.
• 3 timbres synthétisés en temps réel à 44,1 kHz : Piano à queue, Guitare acoustique (Karplus-Strong) et Orgue de studio.
• Portrait et Paysage, 100 % hors ligne, zéro permission et sans publicité.
```

---

## Language: `it-IT` — Italian (Italy) / Italiano

- **`languageCode`**: `it-IT`
- **`appName`** (30 / 30 characters): EarTrainer: Ear Training Piano
- **`shortDescription`** (80 / 80 characters): Allena l'orecchio su intervalli, accordi, scale e dettato con piano interattivo.
- **`fullDescription`** (1433 / 4000 characters):

```text
Sviluppa un orecchio relativo infallibile, il riconoscimento armonico e la memoria musicale con EarTrainer: Palestra per l'Orecchio Musicale. Creato per chitarristi, pianisti, cantanti e studenti di musica, EarTrainer unisce quattro moduli progressivi di ear training con una tastiera interattiva a 2 ottave, tre timbri strumentali sintetizzati e statistiche di precisione in tempo reale.

QUATTRO MODULI COMPLETI DI EAR TRAINING
1. RICONOSCIMENTO DEGLI INTERVALLI
• Tutti i 13 intervalli cromatici: Dalla seconda minore (m2) fino all'ottava giusta (P8), inclusi tritono (TT) e quinta giusta (P5).
• 3 direzioni di ascolto: Ascendente, Discendente o Armonico (note simultanee).

2. IDENTIFICAZIONE DELLA QUALITÀ DEGLI ACCORDI
• 9 qualità di triadi e accordi di settima: Maggiore, Minore, Diminuito, Aumentato, Sus2, Sus4, Settima di Dominante, Maj7 e m7.

3. SCALE E MODI
• 8 scale e modi ecclesiastici: Maggiore (Ionico), Minore Naturale, Minore Armonica, Minore Melodica, Dorico, Misolidio, Lidio e Frigio.

4. DETTATO MELODICO
• Ascolta brevi frasi melodiche da 3 a 5 note e riproducile nota per nota.

PIANOFORTE INTERATTIVO A 2 OTTAVE E 3 TIMBRI REAL-TIME
• Tastiera suonabile (C4–C6) con evidenziazione visiva della tonica e dell'intervallo in semitoni.
• 3 timbri a 44,1 kHz: Pianoforte a Coda, Chitarra Acustica (Karplus-Strong) e Organo Caldo.
• Layout Verticale e Orizzontale, 100% offline, zero permessi e zero pubblicità.
```

---

## Language: `de-DE` — German (Germany) / Deutsch

- **`languageCode`**: `de-DE`
- **`appName`** (30 / 30 characters): EarTrainer: Gehörbildung Piano
- **`shortDescription`** (77 / 80 characters): Trainieren Sie Intervalle, Akkorde und Skalen mit einem interaktiven Klavier.
- **`fullDescription`** (1420 / 4000 characters):

```text
Entwickeln Sie ein präzises relatives Gehör, harmonisches Erkennen und musikalisches Gedächtnis mit EarTrainer: Intervall-, Akkord- & Skalen-Gehörbildung. Entwickelt für Gitarristen, Pianisten, Sänger und Musikstudenten, kombiniert EarTrainer vier progressive Gehörbildungsmodule mit einer interaktiven 2-Oktaven-Klaviatur, drei Instrumentenklangfarben und Echtzeit-Trefferstatistiken.

VIER UMFASSENDE GEHÖRBILDUNGSMODULE
1. INTERVALLERKENNUNG
• Alle 13 chromatischen Intervalle: Von der kleinen Sekunde (m2) über den Tritonus (TT) und die reine Quinte (P5) bis zur reinen Oktave (P8).
• 3 Wiedergaberichtungen: Aufsteigend, Absteigend oder Harmonisch (gleichzeitig).

2. AKKORDQUALITÄTEN ERKENNEN
• 9 Dreiklangs- & Septakkord-Qualitäten: Dur, Moll, Vermindert, Übermäßig, Sus2, Sus4, Dominant-7, Maj7 und m7.

3. TONLEITERN & KIRCHENTONARTEN (MODES)
• 8 Skalen & Modi: Dur (Ionisch), Natürlich Moll, Harmonisch Moll, Melodisch Moll, Dorisch, Mixolydisch, Lydisch und Phrygisch.

4. MELODIEDIKTAT (NACH GEHÖR SPIELEN)
• Hören Sie kurze Melodiephrasen aus 3 bis 5 Tönen und spielen Sie diese Ton für Ton nach.

INTERAKTives 2-OKTAVEN-KLAVIER & 3 INSTRUMENTENKLÄNGE
• Spielbare Klaviatur (C4–C6) mit visueller Auflösung der Halbtonabstände.
• 3 Echtzeit-Klangfarben (44,1 kHz): Konzertflügel, Akustikgitarre (Karplus-Strong) und Studio-Orgel.
• Hoch- und Querformat, 100 % offline, keine Berechtigungen und keine Werbung.
```

---

## Language: `es-ES` — Spanish (Spain) / Español

- **`languageCode`**: `es-ES`
- **`appName`** (28 / 30 characters): EarTrainer: Entrenar el Oído
- **`shortDescription`** (74 / 80 characters): Entrena tu oído en intervalos, acordes y escalas con un piano interactivo.
- **`fullDescription`** (1388 / 4000 characters):

```text
Desarrolla un oído relativo preciso, reconocimiento armónico y memoria musical con EarTrainer: Gimnasio Auditivo de Intervalos, Acordes y Escalas. Diseñado para guitarristas, pianistas, cantantes y estudiantes de música, EarTrainer combina cuatro módulos progresivos de entrenamiento auditivo con un teclado interactivo de 2 octavas, tres timbres de instrumentos sintetizados y estadísticas de precisión.

CUATRO MÓDULOS DE ENTRENAMIENTO AUDITIVO
1. RECONOCIMIENTO DE INTERVALOS
• Los 13 intervalos cromáticos: Desde la 2.ª menor (m2) hasta la Octava justa (P8), pasando por el Tritono (TT) y la 5.ª justa (P5).
• 3 direcciones de reproducción: Ascendente, Descendente o Armónico (simultáneo).

2. IDENTIFICACIÓN DE ACORDES
• 9 cualidades de tríadas y séptimas: Mayor, Menor, Disminuido, Aumentado, Sus2, Sus4, 7.ª Dominante, Maj7 y m7.

3. ESCALAS Y MODOS
• 8 escalas y modos: Mayor (Jónico), Menor Natural, Menor Armónica, Menor Melódica, Dórico, Mixolidio, Lidio y Frigio.

4. DICTADO MELÓDICO
• Escucha frases melódicas de 3 a 5 notas y tócalas nota por nota.

PIANO INTERACTIVO DE 2 OCTAVAS Y 3 TIMBRES EN TIEMPO REAL
• Teclado interactivo (C4–C6) con desglose visual de la respuesta en semitonos.
• 3 timbres sintetizados a 44,1 kHz: Piano de Cola, Guitarra Acústica (Karplus-Strong) y Órgano Cálido.
• Diseño Vertical y Horizontal, 100 % sin conexión, cero permisos y sin anuncios.
```

---

## Language: `zh-CN` — Chinese (Simplified) / 中文（简体）

- **`languageCode`**: `zh-CN`
- **`appName`** (19 / 30 characters): 视唱练耳教练 (EarTrainer)
- **`shortDescription`** (38 / 80 characters): 通过交互式钢琴键盘训练音程、和弦色彩、调式音阶与旋律听写，全面提升相对音感。
- **`fullDescription`** (725 / 4000 characters):

```text
使用《视唱练耳教练 (EarTrainer)》培养敏锐的相对音感、和声辨识力与音乐记忆。专为吉他手、钢琴手、声乐学习者、编曲人与音乐专业学生打造，EarTrainer 将四大渐进式练耳模块与 2 个八度交互式钢琴键盘、三种实时合成乐器音色以及准确率连胜统计融为一体。

四大核心练耳训练模块
1. 音程听辨 (INTERVALS)
• 涵盖全部 13 个半音音程：从小二度 (m2)、大二度 (M2)、三全音 (TT)、纯五度 (P5) 直至纯八度 (P8)。
• 3 种发声方式：上行旋律音程、下行旋律音程与和声音程（双音同时发声）。

2. 和弦色彩听辨 (CHORDS)
• 9 种核心三和弦与七和弦：大三、小三、减三、增三、Sus2、Sus4、属七 (Dom7)、大七 (Maj7) 与小七 (Min7) 和弦，支持分解与柱式和弦发声。

3. 音阶与教会调式听辨 (SCALES & MODES)
• 8 种常用音阶与调式：自然大调、自然小调、和声小调、旋律小调、多利亚 (Dorian)、混合利底亚 (Mixolydian)、利底亚 (Lydian) 与弗里吉亚 (Phrygian)。

4. 旋律听写 (MELODIC DICTATION)
• 聆听 3 至 5 个音符组成的短旋律乐句并逐音还原，快速提升扒谱与即兴反应能力。

交互式双八度钢琴键盘与 3 种实时乐器音色
• 可自由弹奏的 C4–C6 钢琴键盘，答题后直观高亮根音与目标音并显示半音距离。
• 内置 44.1 kHz 三角钢琴、原声木吉他拨弦（Karplus-Strong）与温暖管风琴三种合成音色。
• 支持竖屏与横屏布局，100% 离线运行，零权限要求，无广告。
```

---

## Language: `ja-JP` — Japanese / 日本語

- **`languageCode`**: `ja-JP`
- **`appName`** (22 / 30 characters): EarTrainer: 相対音感＆耳コピ練習
- **`shortDescription`** (41 / 80 characters): インタラクティブなピアノ鍵盤でインターバル、コード、スケール、聴音をトレーニング。
- **`fullDescription`** (779 / 4000 characters):

```text
『EarTrainer — インターバル・コード・スケール耳コピジム』で、確かな相対音感、ハーモニー認識力、音楽的記憶力を鍛えましょう。ギタリスト、ピアニスト、ボーカリスト、DTMクリエイター、音楽学生のために設計された EarTrainer は、4つの体系的なイヤートレーニング・モジュール、2オクターブのインタラクティブ鍵盤、3種類の楽器音色、リアルタイムの正答率トラッキングを備えています。

4つの本格イヤートレーニング・モジュール
1. インターバル（音程）認識
• 全13種類のクロマチック音程：短2度（m2）から三全音（TT）、完全5度（P5）、完全8度（P8）まで網羅。
• 3つの再生モード：上行（Ascending）、下行（Descending）、ハーモニック（同時和音）。

2. コードクオリティ判別
• 9種類の主要トライアド＆7thコード：メジャー、マイナー、ディミニッシュ、オーギュメント、Sus2、Sus4、ドミナント7th、メジャー7th、マイナー7th。

3. スケール＆教会旋法（モード）
• 8種類のスケール：メジャー、ナチュラルマイナー、ハーモニックマイナー、メロディックマイナー、ドリアン、ミクソリディアン、リディアン、フリジアン。

4. メロディック・ディクテーション（聴音・耳コピ）
• 3〜5音の短いメロディフレーズを聴き取り、1音ずつ再現する実践的な耳コピ訓練。

2オクターブ鍵盤 ＆ 3種類のリアルタイム楽器音色
• 自由に試奏できる C4〜C6 ピアノ鍵盤と、回答後のルート音・ターゲット音の視覚ハイライト。
• グランドピアノ、アコースティックギター（Karplus-Strong）、ウォームオルガンの3音色を切り替え可能。
• 縦画面・横画面対応、権限不要・広告なし・100%オフライン動作。
```

---

## Language: `ko-KR` — Korean / 한국어

- **`languageCode`**: `ko-KR`
- **`appName`** (24 / 30 characters): EarTrainer: 청음 & 상대음감 훈련
- **`shortDescription`** (43 / 80 characters): 인터랙티브 피아노 건반으로 음정, 코드, 스케일 및 멜로디 청음을 훈련하세요.
- **`fullDescription`** (828 / 4000 characters):

```text
『EarTrainer — 음정, 코드 & 스케일 청음 체육관』으로 정확한 상대음감, 화성 인식 능력, 음악적 기억력을 키우세요. 기타리스트, 피아니스트, 보컬리스트, 작곡가 및 실용음악 학생을 위해 설계된 EarTrainer는 4가지 체계적인 청음 훈련 모듈과 2옥타브 인터랙티브 피아노 건반, 3가지 악기 음색, 실시간 정답률 통계를 제공합니다.

4가지 핵심 청음 훈련 모듈
1. 음정(INTERVAL) 인식
• 13가지 모든 반음계 음정: 단2도(m2)부터 증4도/감5도(TT), 완전5도(P5), 완전8도(P8)까지 완벽 지원.
• 3가지 재생 방향: 상행(멜로디 상승), 하행(멜로디 하강), 화성(두 음 동시 재생).

2. 코드 퀄리티(CHORD) 판별
• 9가지 필수 3화음 및 7화음: 메이저, 마이너, 디미니쉬, 오그멘티드, Sus2, Sus4, 도미넌트 7, 메이저 7, 마이너 7.

3. 스케일 & 모드(SCALES & MODES)
• 8가지 주요 스케일 및 선법: 메이저, 내추럴 마이너, 하모닉 마이너, 멜로딕 마이너, 도리안, 믹솔리디안, 리디안, 프리지안.

4. 멜로디 청음(MELODIC DICTATION)
• 3~5음으로 이루어진 짧은 멜로디 프레이즈를 듣고 한 음씩 그대로 받아적으며 카피 능력을 기릅니다.

인터랙티브 2옥타브 피아노 & 3가지 악기 음색
• 자유롭게 눌러보고 음정을 비교할 수 있는 C4–C6 피아노 건반 및 정답 반음 간격 시각화.
• 그랜드 피아노, 어쿠스틱 기타(Karplus-Strong), 웜 스튜디오 오르간 음색 실시간 전환.
• 세로 및 가로 화면 지원, 권한 요청 없음, 광고 없음, 100% 오프라인 작동.
```

---

## Language: `pt-BR` — Portuguese (Brazil) / Português

- **`languageCode`**: `pt-BR`
- **`appName`** (27 / 30 characters): EarTrainer: Treino Auditivo
- **`shortDescription`** (75 / 80 characters): Treine seu ouvido em intervalos, acordes e escalas com um piano interativo.
- **`fullDescription`** (953 / 4000 characters):

```text
Desenvolva ouvido relativo preciso, reconhecimento harmônico e memória musical com EarTrainer: Academia de Perceção Musical. Criado para guitarristas, pianistas, cantores e estudantes de música, o EarTrainer reúne 4 módulos progressivos, um teclado interativo de 2 oitavas (C4–C6), 3 timbres sintetizados em tempo real e estatísticas de precisão.

QUATRO MÓDULOS DE PERCEPÇÃO MUSICAL
1. Reconhecimento de Intervalos: Todos os 13 intervalos cromáticos (de 2ª menor a Oitava justa) nos modos Ascendente, Descendente e Harmônico.
2. Identificação de Acordes: 9 qualidades de tríades e tétrades (Maior, Menor, Diminuto, Aumentado, Sus2, Sus4, Dom7, Maj7, m7).
3. Escalas e Modos: 8 escalas e modos gregorianos.
4. Ditado Melódico: Ouça frases de 3 a 5 notas e reproduza nota por nota no teclado.

PIANO DE 2 OITAVAS E 3 TIMBRES EM TEMPO REAL (44,1 kHz)
• Alterne entre Piano de Cauda, Violão (Karplus-Strong) e Órgão de Estúdio. 100% offline e sem anúncios.
```

---

## Language: `ru-RU` — Russian / Русский

- **`languageCode`**: `ru-RU`
- **`appName`** (28 / 30 characters): EarTrainer: Музыкальный Слух
- **`shortDescription`** (78 / 80 characters): Тренировка слуха: интервалы, аккорды, лады и диктанты с интерактивным пианино.
- **`fullDescription`** (786 / 4000 characters):

```text
Развивайте относительный слух, гармоническое восприятие и музыкальную память с EarTrainer: Тренажер Музыкального Слуха. Приложение объединяет 4 прогрессивных модуля сольфеджио, интерактивную 2-октавную клавиатуру пианино (C4–C6), 3 синтезированных тембра инструментов и статистику ответов.

ЧЕТЫРЕ МОДУЛЯ РАЗВИТИЯ СЛУХА
1. Интервалы: Все 13 хроматических интервалов (восходящие, нисходящие и гармонические).
2. Аккорды: 9 типов трезвучий и септаккордов (мажор, минор, уменьшенный, увеличенный, Sus2, Sus4, Dom7, Maj7, m7).
3. Гаммы и лады: 8 основных гамм и церковных ладов.
4. Мелодический диктант: Слушайте фразы из 3–5 нот и повторяйте их на клавиатуре.

2-ОКТАВНОЕ ПИАНИНО И 3 ТЕМБРА (44,1 кГц)
• Рояль, Акустическая гитара (Karplus-Strong) и Теплый орган. 100% офлайн, без рекламы.
```

---

## Language: `ar` — Arabic / العربية

- **`languageCode`**: `ar`
- **`appName`** (23 / 30 characters): EarTrainer: تدريب الأذن
- **`shortDescription`** (66 / 80 characters): درّب أذنك على المسافات الصوتية والكوردات والسلالم مع بيانو تفاعلي.
- **`fullDescription`** (562 / 4000 characters):

```text
طوّر الأذن النسبية والتمييز التناغمي والذاكرة الموسيقية مع EarTrainer: تدريب الأذن الموسيقية. يجمع التطبيق 4 وحدات تدريبية متدرجة مع لوحة مفاتيح بيانو تفاعلية من أوكتافين (C4–C6) وثلاثة أصوات آلات موسيقية.

أربع وحدات تدريبية شاملة
1. تمييز المسافات الصوتية (13 مسافة صاعدة أو هابطة أو توافقية).
2. تحديد نوع الكورد (9 أنواع من الكوردات الثلاثية والسباعية).
3. السلالم والمقامات (8 سلالم ومقامات).
4. الإملاء اللحني (عزف جمل من 3 إلى 5 نغمات بالسمع).

بيانو تفاعلي وثلاثة أصوات (بيانو كبير، جيتار، أورغن) بتردد 44.1 كيلوهرتز، يعمل 100% بدون إنترنت وبدون إعلانات.
```

---

## Language: `hi-IN` — Hindi / हिन्दी

- **`languageCode`**: `hi-IN`
- **`appName`** (24 / 30 characters): EarTrainer: ईयर ट्रेनिंग
- **`shortDescription`** (78 / 80 characters): इंटरैक्टिव पियानो के साथ अंतराल, कॉर्ड्स और स्केल पर अपने कान प्रशिक्षित करें।
- **`fullDescription`** (580 / 4000 characters):

```text
EarTrainer: इंटरवल, कॉर्ड और स्केल ईयर जिम के साथ अपनी सापेक्ष पिच (Relative Pitch) और संगीत स्मृति विकसित करें। इसमें 4 प्रगतिशील ईयर-ट्रेनिंग मॉड्यूल, 2-ऑक्टेव इंटरैक्टिव पियानो कीबोर्ड (C4–C6) और 3 इंस्ट्रूमेंट साउंड्स शामिल हैं।

चार ईयर-ट्रेनिंग मॉड्यूल
1. अंतराल पहचान (Intervals): सभी 13 क्रोमैटिक अंतराल (आरोही, अवरोही और हार्मोनिक)।
2. कॉर्ड पहचान: 9 ट्रायड और सेवंथ कॉर्ड क्वालिटी।
3. स्केल और मोड्स: 8 प्रमुख स्केल और मोड्स।
4. मेलोडिक डिक्टेशन: 3 से 5 स्वरों की धुन सुनकर कीबोर्ड पर बजाएं।

ग्रैंड पियानो, गिटार प्लक और ऑर्गन टोन के साथ 100% ऑफ़लाइन और विज्ञापन-मुक्त।
```

---

## Language: `bn-BD` — Bangla (Bengali) / বাংলা

- **`languageCode`**: `bn-BD`
- **`appName`** (25 / 30 characters): EarTrainer: ইয়ার ট্রেনিং
- **`shortDescription`** (80 / 80 characters): ইন্টারেক্টিভ পিয়ানোর সাথে ইন্টারভ্যাল, কর্ড ও স্কেলে আপনার কান প্রশিক্ষিত করুন।
- **`fullDescription`** (529 / 4000 characters):

```text
EarTrainer অ্যাপের মাধ্যমে আপনার রিলেটিভ পিচ, হারমনি চেনা এবং মিউজিক্যাল মেমোরি উন্নত করুন। এতে ৪টি ইয়ার-ট্রেনিং মডিউল, ২-অক্টেভ ইন্টারেক্টিভ পিয়ানো কিবোর্ড (C4–C6) এবং ৩টি যন্ত্রের সাউন্ড রয়েছে।

চারটি ইয়ার-ট্রেনিং মডিউল
১. ইন্টারভ্যাল চেনা (১৩টি ক্রোম্যাটিক ইন্টারভ্যাল — আরোহী, অবরোহী ও হারমোনিক)।
২. কর্ড কোয়ালিটি শনাক্তকরণ (৯ ধরনের ট্রায়াড ও সেভেন্থ কর্ড)।
৩. স্কেল ও মোড (৮টি স্কেল ও মোড)।
৪. মেলোডিক ডিক্টেশন (৩–৫ নোটের মেলোডি শুনে বাজানো)।

গ্র্যান্ড পিয়ানো, গিটার ও অর্গান সাউন্ড সহ ১০০% অফলাইন এবং বিজ্ঞাপনমুক্ত।
```

---

## Language: `id` — Indonesian / Bahasa Indonesia

- **`languageCode`**: `id`
- **`appName`** (25 / 30 characters): EarTrainer: Latih Telinga
- **`shortDescription`** (79 / 80 characters): Latih pendengaran musik pada interval, akor, dan skala dengan piano interaktif.
- **`fullDescription`** (659 / 4000 characters):

```text
Kembangkan kepekaan nada relatif (relative pitch), pengenalan harmoni, dan memori musik dengan EarTrainer: Latihan Telinga Interval, Akor & Skala. Menggabungkan 4 modul pelatihan pendengaran progresif dengan piano interaktif 2 oktaf (C4–C6) dan 3 warna suara instrumen.

EMPAT MODUL PELATIHAN PENDENGARAN
1. Pengenalan Interval: 13 interval kromatis (Naik, Turun, dan Harmonis).
2. Identifikasi Kualitas Akor: 9 kualitas triad & akor seventh.
3. Skala & Mode: 8 skala dan mode gereja.
4. Dikte Melodi: Dengarkan frasa 3–5 nada dan mainkan kembali di piano.

Dilengkapi suara Grand Piano, Petikan Gitar, dan Organ Studio 44,1 kHz. 100% offline dan tanpa iklan.
```

---

## Language: `tr-TR` — Turkish / Türkçe

- **`languageCode`**: `tr-TR`
- **`appName`** (25 / 30 characters): EarTrainer: Kulak Eğitimi
- **`shortDescription`** (80 / 80 characters): Etkileşimli piyano ile aralıklar, akorlar ve diziler üzerinde kulağınızı eğitin.
- **`fullDescription`** (365 / 4000 characters):

```text
EarTrainer: Aralık, Akor ve Dizi Kulak Eğitimi ile göreceli perde algınızı (relative pitch) ve müzikal hafızanızı geliştirin. 4 eğitim modülünü (Aralıklar, Akorlar, Diziler/Modlar ve Melodik Dikte), 2 oktavlık etkileşimli piyano klavyesini (C4–C6) ve 3 gerçek zamanlı enstrüman tınısını (Kuyruklu Piyano, Gitar, Org) bir araya getirir. %100 çevrimdışı ve reklamsız.
```

---

## Language: `vi` — Vietnamese / Tiếng Việt

- **`languageCode`**: `vi`
- **`appName`** (29 / 30 characters): EarTrainer: Luyện Tai Âm Nhạc
- **`shortDescription`** (79 / 80 characters): Luyện tai cảm âm quãng, hợp âm, âm giai và ký xướng âm với đàn piano tương tác.
- **`fullDescription`** (350 / 4000 characters):

```text
Phát triển khả năng cảm âm tương đối, nhận diện hòa âm và trí nhớ âm nhạc với EarTrainer: Phòng Tập Cảm Âm Quãng, Hợp Âm & Âm Giai. Kết hợp 4 học phần (Quãng, Hợp âm, Âm giai/Điệu thức và Nghe chép giai điệu) cùng bàn phím piano 2 quãng tám (C4–C6) và 3 âm sắc nhạc cụ (Đại dương cầm, Guitar, Đàn Organ) 44,1 kHz. 100% ngoại tuyến và không quảng cáo.
```

---

## Language: `pl-PL` — Polish / Polski

- **`languageCode`**: `pl-PL`
- **`appName`** (30 / 30 characters): EarTrainer: Kształcenie Słuchu
- **`shortDescription`** (74 / 80 characters): Trenuj słuch muzyczny: interwały, akordy i skale z interaktywnym pianinem.
- **`fullDescription`** (320 / 4000 characters):

```text
Rozwijaj słuch relatywny, rozpoznawanie harmonii i pamięć muzyczną dzięki EarTrainer: Trening Słuchu Muzycznego. Łączy 4 moduły (Interwały, Akordy, Skale/Modusy i Dyktando melodyczne) z interaktywną 2-oktawową klawiaturą pianina (C4–C6) oraz 3 brzmieniami (Fortepian, Gitara, Organy) 44,1 kHz. 100% offline i bez reklam.
```

---

## Language: `nl-NL` — Dutch / Nederlands

- **`languageCode`**: `nl-NL`
- **`appName`** (25 / 30 characters): EarTrainer: Gehoorvorming
- **`shortDescription`** (80 / 80 characters): Train je muzikale gehoor op intervallen, akkoorden en toonladders met een piano.
- **`fullDescription`** (334 / 4000 characters):

```text
Ontwikkel een scherp relatief gehoor en muzikaal geheugen met EarTrainer: Intervallen, Akkoorden & Toonladders. Combineert 4 modules (Intervallen, Akkoorden, Toonladders/Modi en Melodisch Dictee) met een speelbaar 2-octaafs pianoklavier (C4–C6) en 3 instrumentklanken (Vleugel, Gitaar, Orgel) op 44,1 kHz. 100% offline en reclamevrij.
```

---

## Language: `th` — Thai / ไทย

- **`languageCode`**: `th`
- **`appName`** (28 / 30 characters): EarTrainer: ฝึกโสตทักษะดนตรี
- **`shortDescription`** (77 / 80 characters): ฝึกฟังขั้นคู่เสียง คอร์ด สเกล และแกะเมโลดี้ด้วยเปียโนจำลองแบบโต้ตอบ 2 อ็อกเทฟ
- **`fullDescription`** (303 / 4000 characters):

```text
พัฒนาโสตทักษะ (Ear Training) และการจำแนกเสียงดนตรีด้วย EarTrainer รวม 4 โมดูลการฝึก (ขั้นคู่เสียง 13 ขั้น, คุณภาพคอร์ด 9 แบบ, สเกลและโหมด 8 แบบ และการฟังแกะเมโลดี้) พร้อมคีย์บอร์ดเปียโน 2 อ็อกเทฟ (C4–C6) และเสียงเครื่องดนตรีสังเคราะห์ 3 ชนิด (แกรนด์เปียโน กีตาร์ และออร์แกน) ทำงานออฟไลน์ 100% ไม่มีโฆษณา
```

---

## Language: `uk` — Ukrainian / Українська

- **`languageCode`**: `uk`
- **`appName`** (25 / 30 characters): EarTrainer: Музичний Слух
- **`shortDescription`** (78 / 80 characters): Тренуйте музичний слух на інтервалах, акордах і гамах з інтерактивним піаніно.
- **`fullDescription`** (314 / 4000 characters):

```text
Розвивайте відносний слух, гармонічне сприйняття та музичну пам'ять із EarTrainer: Тренажер Музичного Слуху. Поєднує 4 модулі (Інтервали, Акорди, Гами/Лади та Мелодичний диктант), інтерактивну 2-октавну клавіатуру піаніно (C4–C6) та 3 тембри інструментів (Рояль, Гітара, Орган) 44,1 кГц. 100% офлайн і без реклами.
```

---
