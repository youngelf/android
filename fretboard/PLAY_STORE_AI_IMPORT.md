# Google Play Console — "Import Translations with AI" File (Fretboard Explorer / `fretboard` — All 20 Languages)

Upload this file (**[`PLAY_STORE_AI_IMPORT.md`](PLAY_STORE_AI_IMPORT.md)**), the CSV version (**[`PLAY_STORE_AI_IMPORT.csv`](PLAY_STORE_AI_IMPORT.csv)**), or the plain-text JSON version (**[`PLAY_STORE_AI_IMPORT.txt`](PLAY_STORE_AI_IMPORT.txt)**) directly into the Google Play Console **Store Listing → Import translations with AI** dialog. It automatically populates **App name (`appName`, ≤ 30 chars)**, **Short description (`shortDescription`, ≤ 80 chars)**, and **Full description (`fullDescription`, ≤ 4000 chars)** across all **20 supported languages**.

## Summary Table of All 20 Languages

| `#` | Play Console `languageCode` | Language | `appName` (`<= 30` chars) | `shortDescription` (`<= 80` chars) | `fullDescription` (`<= 4000` chars) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | **`en-US`** | English (United States) | [`Fretboard Explorer: Scales`](playstore/metadata/android/en-US/title.txt) (26/30) | [`short_description.txt`](playstore/metadata/android/en-US/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/en-US/full_description.txt) (3376/4000) |
| 2 | **`fr-FR`** | French (France) / Français | [`Fretboard : Gammes & Accords`](playstore/metadata/android/fr-FR/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/fr-FR/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/fr-FR/full_description.txt) (2025/4000) |
| 3 | **`it-IT`** | Italian (Italy) / Italiano | [`Fretboard: Scale e Accordi`](playstore/metadata/android/it-IT/title.txt) (26/30) | [`short_description.txt`](playstore/metadata/android/it-IT/short_description.txt) (71/80) | [`full_description.txt`](playstore/metadata/android/it-IT/full_description.txt) (1623/4000) |
| 4 | **`de-DE`** | German (Germany) / Deutsch | [`Fretboard: Gitarren-Skalen`](playstore/metadata/android/de-DE/title.txt) (26/30) | [`short_description.txt`](playstore/metadata/android/de-DE/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/de-DE/full_description.txt) (1576/4000) |
| 5 | **`es-ES`** | Spanish (Spain) / Español | [`Fretboard: Escalas y Acordes`](playstore/metadata/android/es-ES/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/es-ES/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/es-ES/full_description.txt) (1565/4000) |
| 6 | **`zh-CN`** | Chinese (Simplified) / 中文（简体） | [`吉他指板探索 (Fretboard)`](playstore/metadata/android/zh-CN/title.txt) (18/30) | [`short_description.txt`](playstore/metadata/android/zh-CN/short_description.txt) (35/80) | [`full_description.txt`](playstore/metadata/android/zh-CN/full_description.txt) (760/4000) |
| 7 | **`ja-JP`** | Japanese / 日本語 | [`ギター指板ナビ (Fretboard)`](playstore/metadata/android/ja-JP/title.txt) (19/30) | [`short_description.txt`](playstore/metadata/android/ja-JP/short_description.txt) (43/80) | [`full_description.txt`](playstore/metadata/android/ja-JP/full_description.txt) (837/4000) |
| 8 | **`ko-KR`** | Korean / 한국어 | [`기타 지판 탐색기 (Fretboard)`](playstore/metadata/android/ko-KR/title.txt) (21/30) | [`short_description.txt`](playstore/metadata/android/ko-KR/short_description.txt) (44/80) | [`full_description.txt`](playstore/metadata/android/ko-KR/full_description.txt) (842/4000) |
| 9 | **`pt-BR`** | Portuguese (Brazil) / Português | [`Fretboard: Escalas e Acordes`](playstore/metadata/android/pt-BR/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/pt-BR/short_description.txt) (75/80) | [`full_description.txt`](playstore/metadata/android/pt-BR/full_description.txt) (1149/4000) |
| 10 | **`ru-RU`** | Russian / Русский | [`Fretboard: Гаммы и Аккорды`](playstore/metadata/android/ru-RU/title.txt) (26/30) | [`short_description.txt`](playstore/metadata/android/ru-RU/short_description.txt) (75/80) | [`full_description.txt`](playstore/metadata/android/ru-RU/full_description.txt) (1016/4000) |
| 11 | **`ar`** | Arabic / العربية | [`Fretboard: سلالم وكوردات جيتار`](playstore/metadata/android/ar/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/ar/short_description.txt) (76/80) | [`full_description.txt`](playstore/metadata/android/ar/full_description.txt) (797/4000) |
| 12 | **`hi-IN`** | Hindi / हिन्दी | [`Fretboard: गिटार स्केल व कॉर्ड`](playstore/metadata/android/hi-IN/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/hi-IN/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/hi-IN/full_description.txt) (853/4000) |
| 13 | **`bn-BD`** | Bangla (Bengali) / বাংলা | [`Fretboard: গিটার স্কেল ও কর্ড`](playstore/metadata/android/bn-BD/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/bn-BD/short_description.txt) (76/80) | [`full_description.txt`](playstore/metadata/android/bn-BD/full_description.txt) (756/4000) |
| 14 | **`id`** | Indonesian / Bahasa Indonesia | [`Fretboard: Skala & Akor Gitar`](playstore/metadata/android/id/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/id/short_description.txt) (76/80) | [`full_description.txt`](playstore/metadata/android/id/full_description.txt) (973/4000) |
| 15 | **`tr-TR`** | Turkish / Türkçe | [`Fretboard: Gitar Gam & Akorlar`](playstore/metadata/android/tr-TR/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/tr-TR/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/tr-TR/full_description.txt) (616/4000) |
| 16 | **`vi`** | Vietnamese / Tiếng Việt | [`Fretboard: Âm Giai & Hợp Âm`](playstore/metadata/android/vi/title.txt) (27/30) | [`short_description.txt`](playstore/metadata/android/vi/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/vi/full_description.txt) (584/4000) |
| 17 | **`pl-PL`** | Polish / Polski | [`Fretboard: Skale i Akordy`](playstore/metadata/android/pl-PL/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/pl-PL/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/pl-PL/full_description.txt) (581/4000) |
| 18 | **`nl-NL`** | Dutch / Nederlands | [`Fretboard: Gitaar Toonladders`](playstore/metadata/android/nl-NL/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/nl-NL/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/nl-NL/full_description.txt) (622/4000) |
| 19 | **`th`** | Thai / ไทย | [`Fretboard: สเกลและคอร์ดกีตาร์`](playstore/metadata/android/th/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/th/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/th/full_description.txt) (447/4000) |
| 20 | **`uk`** | Ukrainian / Українська | [`Fretboard: Гами та Акорди`](playstore/metadata/android/uk/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/uk/short_description.txt) (75/80) | [`full_description.txt`](playstore/metadata/android/uk/full_description.txt) (576/4000) |

---

## Language: `en-US` — English (United States)

- **`languageCode`**: `en-US`
- **`appName`** (26 / 30 characters): Fretboard Explorer: Scales
- **`shortDescription`** (78 / 80 characters): Master guitar scales, CAGED boxes, chord voicings, and fretboard note quizzes.
- **`fullDescription`** (3376 / 4000 characters):

```text
Unlock the entire 6-string guitar neck with Fretboard Explorer — Guitar Scales & Chords. Designed for beginner, intermediate, and advanced guitarists, Fretboard Explorer maps scales, modes, CAGED positions, movable chord shapes, alternate tunings, and reverse chord identification onto an interactive 15-fret fingerboard with real-time Karplus-Strong guitar audio synthesis.

Stop getting trapped in a single pentatonic box at the 5th fret. Fretboard Explorer shows you how scales, modes, arpeggios, and chord voicings connect across all 15 frets and all 6 strings—in both Portrait and Landscape orientations.

THREE INTEGRATED GUITAR WORKSTATIONS
1. SCALES & MODES EXPLORER
• 14 Essential Scales & Modes: Major (Ionian), Natural Minor (Aeolian), Dorian, Phrygian, Lydian, Mixolydian, Locrian, Minor Pentatonic, Major Pentatonic, Blues Scale, Harmonic Minor, Melodic Minor, Phrygian Dominant, and Whole Tone.
• All 12 Keys via Fast Dropdown Selectors: Switch keys (C, Db, D, Eb, E, F, F#, G, Ab, A, Bb, B) and modes instantaneously using large, stage-friendly dropdown menus.
• 5 CAGED Position Boxes + Full Neck View: Step up and down the neck through CAGED Positions 1 to 5 or view the entire fretboard at once using dedicated side-rail navigation arrows. Non-box scale tones are subtly dimmed so your active position stands out with crystal clarity while preserving full neck context.

2. MOVABLE CHORD LIBRARY
• 12 Core & Extended Chord Qualities: Major, Minor, Dominant 7, Major 7, Minor 7, Minor 7b5 (Half-Diminished), Diminished 7, Sus2, Sus4, Add9, Major 6, and Minor 6.
• 5 CAGED Voicings Per Chord: Cycle through five distinct playable shapes up the fretboard for every chord, complete with finger numbers (1, 2, 3, 4), barre indicators, open/muted string symbols, and root-note highlights.
• Strummable Audio Preview: Tap the chord diagram or individual strings to audition the exact voicing with realistic plucked-string synthesis.

3. INTERACTIVE CHORD FINDER (REVERSE CHORD LOOKUP)
• Identify Any Guitar Voicing on the Spot: Tap any combination of frets across the 6 strings to place your fingers on the virtual neck.
• Instant Harmonic Analysis: Fretboard Explorer analyzes the pitch classes and bass note in real time, displaying the exact chord name (including slash chords and inversions), interval formula, and constituent notes while strumming your custom shape.

CUSTOMIZABLE TUNINGS & FRETBOARD DISPLAY
• 6 Built-In Guitar Tunings: Standard (E A D G B E), Drop D (D A D G B E), Half-Step Down (Eb Ab Db Gb Bb Eb), Full-Step Down (D G C F A D), Open G (D G D G B D), and DADGAD Celtic/ Folk tuning—every scale pattern and chord diagram recalculates automatically when you change tunings.
• Flexible Note & Interval Labels: Toggle fretboard markers between Note Names (C, D#, F), Scale Degrees / Intervals (R, b3, 4, 5, b7), or Finger Numbers.
• Traditional ~ Fret Inlay Markers: Subtle, orientation-aware fret inlays at frets 3, 5, 7, 9, and double inlays at fret 12 give you instant visual landmarks without competing with fingering dots.

BUILT FOR DAILY PRACTICE — ZERO ADS, ZERO PERMISSIONS
• Portrait & Landscape Layouts: Vertical fretboard with ergonomic thumb controls in Portrait mode; expansive horizontal 15-fret neck in Landscape mode.
• 100% Offline & Private: No internet connection required, no tracking, no ads, and zero device permissions.
```

---

## Language: `fr-FR` — French (France) / Français

- **`languageCode`**: `fr-FR`
- **`appName`** (28 / 30 characters): Fretboard : Gammes & Accords
- **`shortDescription`** (74 / 80 characters): Gammes de guitare, positions CAGED, accords, accordages et quiz du manche.
- **`fullDescription`** (2025 / 4000 characters):

```text
Débloquez l'intégralité du manche 6 cordes avec Fretboard Explorer : Gammes et Accords de Guitare. Conçu pour les guitaristes débutants, intermédiaires et avancés, Fretboard Explorer affiche les gammes, modes, positions CAGED, accords transposables, accordages alternatifs et un détecteur d'accords inversé sur un manche interactif de 15 cases avec synthèse sonore Karplus-Strong en temps réel.

Ne restez plus enfermé dans une seule position pentatonique à la 5e case. Visualisez comment les gammes, modes, arpèges et accords se connectent sur les 15 cases et les 6 cordes, en mode Portrait comme en mode Paysage.

TROIS ATELIERS GUITARE INTÉGRÉS
1. EXPLORATEUR DE GAMMES ET DE MODES
• 14 gammes et modes essentiels : Majeur (Ionien), Mineur naturel (Éolien), Dorien, Phrygien, Lydien, Mixolydien, Locrien, Pentatonique mineure, Pentatonique majeure, Gamme Blues, Mineur harmonique, Mineur mélodique, Phrygien dominant et Gamme par tons.
• Les 12 tonalités via menus déroulants rapides : Changez de tonalité (C à B) et de mode instantanément.
• 5 positions CAGED + vue Tout Manche : Parcourez les positions CAGED 1 à 5 ou affichez tout le manche d'un coup.

2. BIBLIOTHÈQUE D'ACCORDS TRANSPOSABLES
• 12 qualités d'accords : Majeur, Mineur, 7e de dominante, Majeur 7, Mineur 7, m7b5, Diminué 7, Sus2, Sus4, Add9, Majeur 6 et Mineur 6.
• 5 positions CAGED par accord avec doigtés (1–4), barrés, cordes à vide/étouffées et écoute en temps réel.

3. DÉTECTEUR D'ACCORDS INTERACTIF (CHORD FINDER)
• Touchez n'importe quelle combinaison de cases sur les 6 cordes pour identifier instantanément le nom exact de l'accord, ses renversements, sa formule d'intervalles et ses notes.

ACCORDAGES MULTIPLES, GAUCHER ET QUIZ DU MANCHE
• 6 accordages intégrés : Standard (E A D G B E), Drop D, Demi-ton plus bas (Eb), Ton entier plus bas (D), Open G et DADGAD.
• Affichage par intervalles (F, b3, 5), notes ou degrés, mode gaucher/droitier et quiz interactif des notes et intervalles.
• 100 % hors ligne, zéro permission et zéro publicité.
```

---

## Language: `it-IT` — Italian (Italy) / Italiano

- **`languageCode`**: `it-IT`
- **`appName`** (26 / 30 characters): Fretboard: Scale e Accordi
- **`shortDescription`** (71 / 80 characters): Padroneggia scale per chitarra, box CAGED, accordi, accordature e quiz.
- **`fullDescription`** (1623 / 4000 characters):

```text
Sblocca l'intera tastiera a 6 corde con Fretboard Explorer: Scale e Accordi per Chitarra. Progettato per chitarristi principianti, intermedi e avanzati, Fretboard Explorer mappa scale, modi, posizioni CAGED, accordi mobili, accordature alternative e riconoscimento inverso degli accordi su un manico interattivo a 15 tasti con sintesi sonora Karplus-Strong in tempo reale.

TRE WORKSTATION INTEGRATE PER CHITARRA
1. ESPLORATORE DI SCALE E MODI
• 14 Scale e Modi Essenziali: Maggiore (Ionico), Minore Naturale (Eolio), Dorico, Frigio, Lidio, Misolidio, Locrio, Pentatonica Minore, Pentatonica Maggiore, Scala Blues, Minore Armonica, Minore Melodica, Frigio Dominante e Esatonale.
• Tutte le 12 Tonalità tramite menu a tendina rapidi.
• 5 Box CAGED + Vista Tutta la Tastiera: Scorri tra le 5 posizioni CAGED o visualizza l'intero manico.

2. LIBRERIA DI ACCORDI MOBILI
• 12 Qualità di Accordi: Maggiore, Minore, Settima di Dominante, Maj7, m7, m7b5, dim7, Sus2, Sus4, Add9, Maj6 e m6.
• 5 Voicing CAGED per ogni accordo con diteggiature (1–4), indicatori di barrè e ascolto immediato.

3. TROVA ACCORDI INTERATTIVO (CHORD FINDER)
• Tocca qualsiasi combinazione di tasti sulle 6 corde per identificare all'istante il nome dell'accordo, i rivolti, la formula intervallare e le note.

ACCORDATURE PERSONALIZZABILI E QUIZ SULLA TASTIERA
• 6 Accordature Integrate: Standard (E A D G B E), Drop D, Eb, D Standard, Open G e DADGAD.
• Etichette per Intervalli, Note o Gradi, modalità Mancino/Destro e Quiz interattivo per memorizzare note e intervalli.
• Layout Verticale e Orizzontale, 100% offline, zero permessi e zero pubblicità.
```

---

## Language: `de-DE` — German (Germany) / Deutsch

- **`languageCode`**: `de-DE`
- **`appName`** (26 / 30 characters): Fretboard: Gitarren-Skalen
- **`shortDescription`** (74 / 80 characters): Gitarrenskalen, CAGED-Lagen, Akkordgriffe, Stimmungen und Griffbrett-Quiz.
- **`fullDescription`** (1576 / 4000 characters):

```text
Erschließen Sie das gesamte 6-saitige Gitarrengriffbrett mit Fretboard Explorer: Gitarrenskalen & Akkorde. Entwickelt für Einsteiger, Fortgeschrittene und Profis, zeigt Fretboard Explorer Tonleitern, Kirchentonarten (Modes), CAGED-Boxen, verschiebbare Akkordgriffe, alternative Stimmungen und eine umgekehrte Akkorderkennung auf einem interaktiven 15-Bund-Griffbrett mit Echtzeit-Karplus-Strong-Gitarrensynthese.

DREI INTEGRIERTE GITARREN-WERKZEUGE
1. SKALEN- & MODUS-EXPLORER
• 14 essenzielle Tonleitern & Modi: Dur (Ionisch), Natürlich Moll (Äolisch), Dorisch, Phrygisch, Lydisch, Mixolydisch, Lokrisch, Moll-Pentatonik, Dur-Pentatonik, Blues-Skala, Harmonisch Moll, Melodisch Moll, Phrygisch-Dominant und Ganztonleiter.
• Alle 12 Tonarten über schnelle Dropdown-Menüs wählbar.
• 5 CAGED-Lagen + Ganzer-Hals-Ansicht.

2. VERSCHIEBBARE AKKORDBIBLIOTHEK
• 12 Akkordqualitäten: Dur, Moll, Dominant-7, Maj7, m7, m7b5, dim7, Sus2, Sus4, Add9, Maj6 und m6.
• 5 CAGED-Voicings pro Akkord mit Fingerzahlen (1–4), Barré-Anzeige und akustischer Vorschau.

3. INTERAKTIVER AKKORD-FINDER
• Tippen Sie beliebige Bünde auf den 6 Saiten an, um sofort den exakten Akkordnamen (inklusive Slash-Chords), die Intervallformel und die Töne zu bestimmen.

6 STIMMUNGEN, LINKSHÄNDER-MODUS & GRIFFBRETT-QUIZ
• Integrierte Stimmungen: Standard E, Drop D, Eb-Standard, D-Standard, Open G und DADGAD.
• Umschaltbare Beschriftungen (Intervalle, Notennamen, Stufen), Rechtshänder/Linkshänder-Spiegelung und Griffbrett-Quiz.
• Hoch- und Querformat, 100 % offline, keine Berechtigungen und keine Werbung.
```

---

## Language: `es-ES` — Spanish (Spain) / Español

- **`languageCode`**: `es-ES`
- **`appName`** (28 / 30 characters): Fretboard: Escalas y Acordes
- **`shortDescription`** (78 / 80 characters): Domina escalas de guitarra, cajas CAGED, acordes, afinaciones y test de notas.
- **`fullDescription`** (1565 / 4000 characters):

```text
Domina todo el diapasón de 6 cuerdas con Fretboard Explorer: Escalas y Acordes de Guitarra. Diseñado para guitarristas de todos los niveles, Fretboard Explorer muestra escalas, modos, posiciones CAGED, acordes móviles, afinaciones alternativas y un buscador inverso de acordes en un mástil interactivo de 15 trastes con síntesis de guitarra Karplus-Strong en tiempo real.

TRES ESTACIONES DE TRABAJO INTEGRADAS
1. EXPLORADOR DE ESCALAS Y MODOS
• 14 Escalas y Modos Esenciales: Mayor (Jónico), Menor Natural (Eólico), Dórico, Frigio, Lidío, Mixolidio, Locrio, Pentatónica Menor, Pentatónica Mayor, Escala de Blues, Menor Armónica, Menor Melódica, Frigio Dominante y Tonos Enteros.
• Las 12 tonalidades mediante selectores desplegables rápidos.
• 5 Cajas CAGED + Vista de Todo el Mástil.

2. BIBLIOTECA DE ACORDES MÓVILES
• 12 Cualidades de Acordes: Mayor, Menor, 7.ª Dominante, Maj7, m7, m7b5, dim7, Sus2, Sus4, Add9, Maj6 y m6.
• 5 Posiciones CAGED por acorde con números de dedos (1–4), cejillas y preescucha al instante.

3. BUSCADOR INTERACTIVO DE ACORDES (CHORD FINDER)
• Toca cualquier combinación de trastes en las 6 cuerdas para identificar al instante el nombre exacto del acorde, su fórmula de intervalos y sus notas.

6 AFINACIONES, MODO ZURDO Y TEST DEL DIAPASÓN
• 6 Afinaciones Incluidas: Estándar (E A D G B E), Drop D, Medio tono abajo (Eb), Un tono abajo (D), Open G y DADGAD.
• Etiquetas de intervalos, nombres de notas o grados, modo Diestro/Zurdo y Quiz interactivo.
• Diseño Vertical y Horizontal, 100 % sin conexión, sin permisos y sin anuncios.
```

---

## Language: `zh-CN` — Chinese (Simplified) / 中文（简体）

- **`languageCode`**: `zh-CN`
- **`appName`** (18 / 30 characters): 吉他指板探索 (Fretboard)
- **`shortDescription`** (35 / 80 characters): 掌握吉他音阶、调式、CAGED把位、和弦指型、特殊调弦与指板音符测验。
- **`fullDescription`** (760 / 4000 characters):

```text
使用《吉他指板探索 (Fretboard Explorer)》彻底打通六弦吉他指板。专为初、中、高级吉他手设计，应用在 15 品交互式指板上集成了音阶与调式、CAGED 五把位系统、可移动和弦指型库、特殊调弦换算、反查和弦识别器以及实时 Karplus-Strong 物理建模吉他音源。

三大核心吉他工作站
1. 音阶与教会调式探索器
• 14 种核心音阶与调式：自然大调（伊奥尼亚）、自然小调（爱奥尼亚）、多利亚（Dorian）、弗里吉亚（Phrygian）、利底亚（Lydian）、混合利底亚（Mixolydian）、洛克里亚（Locrian）、小调五声、大调五声、布鲁斯音阶、和声小调、旋律小调、弗里吉亚属音阶与全音阶。
• 12 个调式快速下拉切换，支持 CAGED 第 1–5 把位高亮与全指板视图。

2. 可移动 CAGED 和弦指型库
• 12 种常用与扩展和弦：大三、小三、属七、大七（Maj7）、小七（m7）、半减七（m7b5）、减七（dim7）、Sus2、Sus4、Add9、大六与小六和弦。
• 每个和弦提供 5 种 CAGED 把位按法，清晰标注手指编号（1–4）、大横按、空弦与闷音，点击即可试听扫弦音色。

3. 交互式和弦反查器（查和弦）
• 在 6 根琴弦上任意点按品格组合，实时分析并显示精准的和弦名称（含转位/斜杠和弦）、音程公式与构成音。

6 种吉他调弦、左撇子模式与指板听力/认音测验
• 内置标准 E、Drop D、降半音（Eb）、降全音（D）、开放 G（Open G）与凯尔特 DADGAD 调弦，所有音阶与和弦图自动实时重算。
• 支持音程/音名/级数标签切换、左/右手型翻转以及指板音符与音程问答测验。
• 横竖屏自适应布局，100% 离线运行，零权限要求，无广告。
```

---

## Language: `ja-JP` — Japanese / 日本語

- **`languageCode`**: `ja-JP`
- **`appName`** (19 / 30 characters): ギター指板ナビ (Fretboard)
- **`shortDescription`** (43 / 80 characters): ギターのスケール、CAGEDポジション、コードフォーム、変則チューニング、指板クイズ。
- **`fullDescription`** (837 / 4000 characters):

```text
『ギター指板ナビ (Fretboard Explorer)』で6弦ギターの指板全体を自在にマスターしましょう。初心者から上級者まで、スケール、モード、CAGEDポジション、コードフォーム、変則チューニング、コード逆引き判定（コード検索）を15フレットのインタラクティブ指板上に統合し、44.1 kHz Karplus-Strong ギター音源でリアルタイムに試聴できます。

3つの統合ギター・ワークステーション
1. スケール＆モード・エクスプローラー
• 14種類の主要スケール＆モード：メジャー（アイオニアン）、ナチュラルマイナー（エオリアン）、ドリアン、フリジアン、リディアン、ミクソリディアン、ロクリアン、マイナーペンタトニック、メジャーペンタトニック、ブルース、ハーモニックマイナー、メロディックマイナー、フリジアンドミナント、ホールトーン。
• 全12キー対応の高速ドロップダウン選択、CAGED 5ポジション表示＆全指板表示。

2. CAGEDコード・ライブラリー
• 12種類のコードクオリティ（Major, Minor, 7, maj7, m7, m7b5, dim7, sus2, sus4, add9, 6, m6）×各5ポジションのCAGEDボイシングを収録。運指番号（1〜4）、セーハ、開放弦・ミュート表示とストローク試聴に対応。

3. インタラクティブ・コード検索（逆引きコードファインダー）
• 6本の弦の任意のフレットをタップして押弦すると、コード名（オンコード・転回形対応）、インターバル構成、構成音を即座に判定します。

6種類のチューニング、左利きモード＆指板クイズ
• レギュラーE、ドロップD、半音下げ（Eb）、全音下げ（D）、オープンG、DADGADに対応。
• 度数／音名ラベル切替、右利き／左利き反転、音名・インターバルを鍛える指板クイズ機能を搭載。
• 縦画面・横画面対応、100%オフライン動作、権限不要・広告なし。
```

---

## Language: `ko-KR` — Korean / 한국어

- **`languageCode`**: `ko-KR`
- **`appName`** (21 / 30 characters): 기타 지판 탐색기 (Fretboard)
- **`shortDescription`** (44 / 80 characters): 기타 스케일, CAGED 박스, 코드 보이싱, 변칙 튜닝 및 지판 음이름 퀴즈.
- **`fullDescription`** (842 / 4000 characters):

```text
『기타 지판 탐색기 (Fretboard Explorer)』로 6현 기타 지판 전체를 완벽히 마스터하세요. 입문자부터 숙련된 기타리스트까지 스케일, 교회 선법(모드), CAGED 포지션, 코드 보이싱, 변칙 튜닝, 코드 역검색(코드 찾기)을 15프렛 인터랙티브 지판 위에서 확인하고 44.1 kHz 물리 모델링 기타 사운드로 즉시 들어볼 수 있습니다.

3가지 통합 기타 워크스테이션
1. 스케일 & 모드 탐색기
• 14가지 필수 스케일 및 모드: 메이저(이오니안), 내추럴 마이너(에올리안), 도리안, 프리지안, 리디안, 믹솔리디안, 로크리안, 마이너 펜타토닉, 메이저 펜타토닉, 블루스, 하모닉 마이너, 멜로딕 마이너, 프리지안 도미넌트, 홀톤 스케일.
• 12키 빠른 드롭다운 선택 및 CAGED 1~5 포지션 박스 / 전체 지판 보기 지원.

2. CAGED 코드 라이브러리
• 12가지 코드 퀄리티(Major, Minor, 7, maj7, m7, m7b5, dim7, sus2, sus4, add9, 6, m6)별 5가지 CAGED 폼 제공. 손가락 번호(1~4), 바레 표시 및 스트럼 오디오 미리듣기 지원.

3. 인터랙티브 코드 찾기 (역검색)
• 6개 현의 원하는 프렛을 탭하여 짚으면 정확한 코드 이름(분수 코드 및 전위 포함), 음정 공식, 구성음을 실시간으로 분석합니다.

6가지 튜닝, 왼손잡이 모드 및 지판 퀴즈
• 표준 E, 드롭 D, 반음 내림(Eb), 온음 내림(D), 오픈 G, DADGAD 튜닝 자동 변환.
• 도수/음이름 표시 전환, 오른손/왼손잡이 반전 및 지판 음이름·음정 퀴즈 탑재.
• 세로 및 가로 화면 지원, 100% 오프라인 작동, 권한 요청 및 광고 없음.
```

---

## Language: `pt-BR` — Portuguese (Brazil) / Português

- **`languageCode`**: `pt-BR`
- **`appName`** (28 / 30 characters): Fretboard: Escalas e Acordes
- **`shortDescription`** (75 / 80 characters): Domine escalas de guitarra/violão, desenhos CAGED, acordes e quiz do braço.
- **`fullDescription`** (1149 / 4000 characters):

```text
Desbloqueie todo o braço de 6 cordas com Fretboard Explorer: Escalas e Acordes de Guitarra e Violão. Mapeie 14 escalas e modos, 5 regiões CAGED, 12 qualidades de acordes, 6 afinações e um identificador reverso de acordes em um braço interativo de 15 casas com síntese de áudio Karplus-Strong em tempo real.

TRÊS ESTAÇÕES DE ESTUDO INTEGRADAS
• Explorador de Escalas e Modos: 14 escalas (Maior, Menor Natural, Dórico, Frígio, Lídio, Mixolídio, Lócrio, Pentatônica Menor/Maior, Blues, Menor Harmônica/Melódica, Frígio Dominante e Tons Inteiros) nos 12 tons e 5 desenhos CAGED.
• Biblioteca de Acordes Móveis: 12 tipos de acordes com 5 formatos CAGED cada, numeração dos dedos (1–4), pestanas e áudio.
• Identificador de Acordes (Finder): Toque em qualquer combinação de casas nas 6 cordas para identificar o nome exato do acorde, inversões e intervalos.

6 AFINAÇÕES, MODO CANHOTO E QUIZ DO BRAÇO
• Afinações Padrão E, Drop D, Meio Tom Abaixo (Eb), Um Tom Abaixo (D), Open G e DADGAD.
• Alternância de rótulos (Intervalos, Notas, Graus), modo Destro/Canhoto e Quiz interativo.
• Modos Retrato e Paisagem, 100% offline, zero permissões e sem anúncios.
```

---

## Language: `ru-RU` — Russian / Русский

- **`languageCode`**: `ru-RU`
- **`appName`** (26 / 30 characters): Fretboard: Гаммы и Аккорды
- **`shortDescription`** (75 / 80 characters): Гитарные гаммы, лады, боксы CAGED, аппликатуры аккордов, строи и викторина.
- **`fullDescription`** (1016 / 4000 characters):

```text
Откройте весь 6-струнный гриф с Fretboard Explorer: Гитарные гаммы и аккорды. Приложение объединяет 14 гамм и ладов, 5 позиций CAGED, 12 типов аккордов, 6 гитарных строев и обратный определитель аккордов на интерактивном 15-ладовом грифе с физическим синтезом звука Karplus-Strong.

ТРИ ИНТЕРАКТИВНЫХ РЕЖИМА
• Гаммы и лады: 14 гамм (мажор, натуральный минор, дорийский, фригийский, лидийский, миксолидийский, локрийский, пентатоники, блюзовая гамма, гармонический и мелодический минор) во всех 12 тональностях и 5 боксах CAGED.
• Библиотека аккордов: 12 типов аккордов по 5 позиций CAGED с пальцовкой (1–4) и баррэ.
• Определитель аккордов (Finder): Нажимайте любые лады на 6 струнах для мгновенного анализа названия аккорда, обращения и интервальной формулы.

6 СТРОЕВ, РЕЖИМ ДЛЯ ЛЕВШЕЙ И ВИКТОРИНА ПО ГРИФУ
• Стандартный E, Drop D, Eb, D, Open G и DADGAD.
• Переключение меток (интервалы, ноты, ступени), режим правша/левша и проверка знания нот на грифе.
• Портретный и альбомный режимы, 100% офлайн, без рекламы.
```

---

## Language: `ar` — Arabic / العربية

- **`languageCode`**: `ar`
- **`appName`** (30 / 30 characters): Fretboard: سلالم وكوردات جيتار
- **`shortDescription`** (76 / 80 characters): أتقن سلالم الجيتار، مواضع CAGED، أشكال الكوردات، الدوزانات واختبارات الرقبة.
- **`fullDescription`** (797 / 4000 characters):

```text
استكشف رقبة الجيتار المكونة من 6 أوتار بالكامل مع Fretboard Explorer: سلالم وكوردات الجيتار. يعرض التطبيق 14 سلماً ومقاماً، و5 مواضع CAGED، و12 نوعاً من الكوردات، و6 دوزانات، وكاشف الكوردات العكسي على رقبة تفاعلية من 15 دستاناً مع توليد صوتي واقعي (Karplus-Strong).

ثلاث أدوات متكاملة للجيتار
• مستكشف السلالم والمقامات: 14 سلماً ومقاماً في جميع الطبقات الـ 12 مع 5 صناديق CAGED أو عرض الرقبة بالكامل.
• مكتبة الكوردات المتحركة: 12 نوعاً من الكوردات مع 5 أشكال CAGED لكل كورد وأرقام الأصابع (1–4).
• كاشف الكوردات التفاعلي (Finder): المس أي مجموعة دساتين على الأوتار الستة لمعرفة اسم الكورد وصيغته الفورية.

6 دوزانات ووضع الأعسر واختبار النغمات
• يدعم الدوزان القياسي E، وDrop D، وEb، وD، وOpen G، وDADGAD، مع وضع اليد اليمنى/اليسرى واختبار تفاعلي للنغمات.
• يعمل 100% بدون إنترنت وبدون إعلانات.
```

---

## Language: `hi-IN` — Hindi / हिन्दी

- **`languageCode`**: `hi-IN`
- **`appName`** (30 / 30 characters): Fretboard: गिटार स्केल व कॉर्ड
- **`shortDescription`** (78 / 80 characters): गिटार स्केल, CAGED बॉक्स, कॉर्ड वॉइसिंग, ट्यूनिंग और फ़्रेटबोर्ड क्विज़ सीखें।
- **`fullDescription`** (853 / 4000 characters):

```text
Fretboard Explorer: गिटार स्केल और कॉर्ड्स के साथ पूरे 6-स्ट्रिंग गिटार नेक को अनलॉक करें। इसमें 15-फ़्रेट इंटरैक्टिव नेक पर 14 स्केल और मोड्स, 5 CAGED पोज़िशन, 12 कॉर्ड क्वालिटी, 6 गिटार ट्यूनिंग और रिवर्स कॉर्ड फ़ाइंडर के साथ रियल-टाइम Karplus-Strong गिटार साउंड शामिल है।

तीन एकीकृत गिटार वर्कस्टेशन
• स्केल और मोड एक्सप्लोरर: सभी 12 कीज़ और 5 CAGED बॉक्स में मेजर, माइनर, डोरियन, फ्रिजियन, लिडियन, मिक्सोलिडियन, पेंटाटोनिक और ब्लूज़ सहित 14 स्केल।
• कॉर्ड लाइब्रेरी: प्रत्येक कॉर्ड के लिए 5 CAGED शेप्स और उंगलियों के नंबर (1–4)।
• इंटरैक्टिव कॉर्ड फ़ाइंडर: किसी भी कॉर्ड का नाम और स्वर जानने के लिए 6 तारों पर किसी भी फ़्रेट को टैप करें।

6 गिटार ट्यूनिंग, लेफ़्टी मोड और फ़्रेटबोर्ड क्विज़
• स्टैंडर्ड E, Drop D, Eb, D, Open G और DADGAD ट्यूनिंग, लेफ़्ट/राइट हैंड मोड और नोट क्विज़।
• पोर्ट्रेट और लैंडस्केप लेआउट, 100% ऑफ़लाइन और विज्ञापन-मुक्त।
```

---

## Language: `bn-BD` — Bangla (Bengali) / বাংলা

- **`languageCode`**: `bn-BD`
- **`appName`** (29 / 30 characters): Fretboard: গিটার স্কেল ও কর্ড
- **`shortDescription`** (76 / 80 characters): গিটার স্কেল, CAGED বক্স, কর্ড ভয়েসিং, টিউনিং এবং ফ্রেটবোর্ড নোট কুইজ শিখুন।
- **`fullDescription`** (756 / 4000 characters):

```text
Fretboard Explorer: গিটার স্কেল ও কর্ড অ্যাপের মাধ্যমে ৬-তারের গিটার ফ্রেটবোর্ড সম্পূর্ণ আয়ত্ত করুন। ১৫-ফ্রেটের ইন্টারেক্টিভ ফ্রেটবোর্ডে ১৪টি স্কেল ও মোড, ৫টি CAGED পজিশন, ১২টি কর্ড কোয়ালিটি, ৬টি টিউনিং এবং রিভার্স কর্ড ফাইন্ডারের সাথে রিয়েল-টাইম Karplus-Strong গিটার অডিও রয়েছে।

তিনটি সমন্বিত গিটার ওয়ার্কস্টেশন
• স্কেল ও মোড এক্সপ্লোরার: ১২টি কী এবং ৫টি CAGED পজিশনে মেজর, মাইনর, ডোরিয়ান, পেন্টাটনিক ও ব্লুজসহ ১৪টি স্কেল।
• কর্ড লাইব্রেরি: প্রতিটি কর্ডের জন্য ৫টি CAGED শেপ এবং আঙুলের নম্বর (১–৪)।
• কর্ড ফাইন্ডার: যেকোনো ফ্রেট সমন্বয় ট্যাপ করে কর্ডের সঠিক নাম ও নোট জানুন।

৬টি টিউনিং, বামহাতি মোড ও কুইজ
• স্ট্যান্ডার্ড E, Drop D, Eb, D, Open G এবং DADGAD টিউনিং, ডানহাতি/বামহাতি মোড ও ফ্রেটবোর্ড কুইজ।
• ১০০% অফলাইন এবং সম্পূর্ণ বিজ্ঞাপনমুক্ত।
```

---

## Language: `id` — Indonesian / Bahasa Indonesia

- **`languageCode`**: `id`
- **`appName`** (29 / 30 characters): Fretboard: Skala & Akor Gitar
- **`shortDescription`** (76 / 80 characters): Kuasai skala gitar, posisi CAGED, akor, tala alternatif, dan kuis fretboard.
- **`fullDescription`** (973 / 4000 characters):

```text
Kuasai seluruh leher gitar 6 senar dengan Fretboard Explorer: Skala & Akor Gitar. Menampilkan 14 skala dan mode, 5 posisi CAGED, 12 kualitas akor, 6 tala (tuning) gitar, dan pencari akor terbalik pada fretboard interaktif 15 fret dengan sintesis suara gitar Karplus-Strong secara real-time.

TIGA WORKSTATION GITAR TERPADU
• Eksplorer Skala & Mode: 14 skala penting (Mayor, Minor Natural, Dorian, Phrygian, Lydian, Mixolydian, Pentatonik, Blues, Minor Harmonik/Melodik) di semua 12 nada dasar dan 5 kotak CAGED.
• Pustaka Akor CAGED: 12 tipe akor dengan 5 posisi CAGED per akor, lengkap dengan nomor jari (1–4) dan tanda barre.
• Pencari Akor Interaktif (Finder): Ketuk kombinasi fret mana pun untuk mengenali nama akor, inversi, dan formula intervalnya.

6 TUNING GITAR, MODE KIDAL & KUIS FRETBOARD
• Mendukung Standard E, Drop D, Eb, D, Open G, dan DADGAD, mode Kanan/Kidal, serta kuis tebak nada & interval.
• Potret & Lanskap, 100% offline, tanpa izin, dan tanpa iklan.
```

---

## Language: `tr-TR` — Turkish / Türkçe

- **`languageCode`**: `tr-TR`
- **`appName`** (30 / 30 characters): Fretboard: Gitar Gam & Akorlar
- **`shortDescription`** (80 / 80 characters): Gitar gamlarını, CAGED kutularını, akorları, akortları ve klavye testini öğrenin
- **`fullDescription`** (616 / 4000 characters):

```text
Fretboard Explorer: Gitar Gamları ve Akorlar ile 6 telli gitar klavyesinin tamamına hakim olun. 15 perdeli etkileşimli klavye üzerinde 14 gam ve mod, 5 CAGED pozisyonu, 12 akor kalitesi, 6 gitar akordu ve tersine akor bulucu (Finder) ile gerçek zamanlı Karplus-Strong gitar ses sentezini birleştirir.

ÖNE ÇIKAN ÖZELLİKLER
• 14 Gam ve Mod, 12 ton ve 5 CAGED pozisyon kutusu.
• Her akor için 5 CAGED klavye pozisyonu ve parmak numaraları (1–4).
• Etkileşimli Akor Bulucu (Finder), 6 farklı akort (Standart E, Drop D, Eb, D, Open G, DADGAD), Sağ/Sol el modu ve Klavye Testi.
• %100 çevrimdışı, sıfır izin ve reklamsız.
```

---

## Language: `vi` — Vietnamese / Tiếng Việt

- **`languageCode`**: `vi`
- **`appName`** (27 / 30 characters): Fretboard: Âm Giai & Hợp Âm
- **`shortDescription`** (79 / 80 characters): Làm chủ âm giai guitar, thế bấm CAGED, hợp âm, dây chỉnh và đố nốt trên cần đàn
- **`fullDescription`** (584 / 4000 characters):

```text
Khám phá toàn bộ cần đàn guitar 6 dây với Fretboard Explorer: Âm Giai & Hợp Âm Guitar. Tích hợp 14 âm giai & điệu thức, 5 vị trí CAGED, 12 loại hợp âm, 6 kiểu lên dây và công cụ tìm hợp âm ngược trên cần đàn 15 ngăn tương tác với âm thanh Karplus-Strong 44,1 kHz.

TÍNH NĂNG CHÍNH
• 14 âm giai & điệu thức ở cả 12 giọng và 5 hộp CAGED.
• Thư viện 12 loại hợp âm với 5 thế bấm CAGED mỗi hợp âm và số ngón tay (1–4).
• Công cụ Tìm Hợp Âm (Finder), 6 kiểu lên dây (Standard E, Drop D, Eb, D, Open G, DADGAD), chế độ Tay Phải/Trái và Đố Nốt.
• Hoạt động 100% ngoại tuyến, không quảng cáo.
```

---

## Language: `pl-PL` — Polish / Polski

- **`languageCode`**: `pl-PL`
- **`appName`** (25 / 30 characters): Fretboard: Skale i Akordy
- **`shortDescription`** (79 / 80 characters): Opanuj skale gitarowe, pozycje CAGED, akordy, strojenia i quiz wiedzy o gryfie.
- **`fullDescription`** (581 / 4000 characters):

```text
Odkryj cały 6-strunowy gryf gitary z Fretboard Explorer: Skale i Akordy Gitarowe. Zawiera 14 skal i modusów, 5 pozycji CAGED, 12 typów akordów, 6 strojów gitarowych oraz wyszukiwarkę akordów na interaktywnym 15-progowym gryfie z syntezą dźwięku Karplus-Strong.

NAJWAŻNIEJSZE FUNKCJE
• 14 skal i modusów we wszystkich 12 tonacjach i 5 boksach CAGED.
• 12 jakości akordów po 5 pozycji CAGED z palcowaniem (1–4).
• Wyszukiwarka akordów (Finder), 6 strojów (Standard E, Drop D, Eb, D, Open G, DADGAD), tryb dla leworęcznych oraz Quiz gryfu.
• 100% offline, bez uprawnień i bez reklam.
```

---

## Language: `nl-NL` — Dutch / Nederlands

- **`languageCode`**: `nl-NL`
- **`appName`** (29 / 30 characters): Fretboard: Gitaar Toonladders
- **`shortDescription`** (77 / 80 characters): Beheers gitaartoonladders, CAGED-posities, akkoorden, stemmingen en halsquiz.
- **`fullDescription`** (622 / 4000 characters):

```text
Ontgrendel de volledige 6-snarige gitaarhals met Fretboard Explorer: Gitaartoonladders & Akkoorden. Bevat 14 toonladders en modi, 5 CAGED-posities, 12 akkoordkwaliteiten, 6 stemmingen en een omgekeerde akkoordzoeker op een interactieve 15-frets hals met Karplus-Strong gitaarsynthese.

BELANGRIJKSTE FUNCTIES
• 14 toonladders en kerktoonladders in alle 12 toonsoorten en 5 CAGED-boxen.
• 12 akkoordtypen met elk 5 CAGED-liggingen en vingerzettingen (1–4).
• Interactieve Akkoordzoeker (Finder), 6 stemmingen (Standaard E, Drop D, Eb, D, Open G, DADGAD), linkshandige modus en Fretboard Quiz.
• 100% offline en reclamevrij.
```

---

## Language: `th` — Thai / ไทย

- **`languageCode`**: `th`
- **`appName`** (29 / 30 characters): Fretboard: สเกลและคอร์ดกีตาร์
- **`shortDescription`** (74 / 80 characters): ฝึกสเกลกีตาร์ บ็อกซ์ CAGED ฟอร์มคอร์ด การตั้งสาย และแบบทดสอบโน้ตบนคอกีตาร์
- **`fullDescription`** (447 / 4000 characters):

```text
ปลดล็อกคอกีตาร์ 6 สายทั้งหมดด้วย Fretboard Explorer: สเกลและคอร์ดกีตาร์ รวม 14 สเกลและโหมด, 5 ตำแหน่ง CAGED, 12 ประเภทคอร์ด, 6 รูปแบบการตั้งสาย และตัวค้นหาคอร์ดย้อนกลับบนคอกีตาร์ 15 เฟร็ตแบบโต้ตอบพร้อมเสียงกีตาร์สังเคราะห์ Karplus-Strong

คุณสมบัติหลัก
• 14 สเกลและโหมดใน 12 คีย์ พร้อม 5 ตำแหน่ง CAGED
• คลังคอร์ด 12 ชนิด (5 รูปแบบ CAGED ต่อคอร์ด) ตัวค้นหาคอร์ด (Finder) การตั้งสาย 6 แบบ โหมดมือซ้าย/ขวา และควิซทายโน้ต ทำงานออฟไลน์ 100% ไม่มีโฆษณา
```

---

## Language: `uk` — Ukrainian / Українська

- **`languageCode`**: `uk`
- **`appName`** (25 / 30 characters): Fretboard: Гами та Акорди
- **`shortDescription`** (75 / 80 characters): Гітарні гами, бокси CAGED, аплікатури акордів, строї та вікторина по грифу.
- **`fullDescription`** (576 / 4000 characters):

```text
Відкрийте весь 6-струнний гриф гітари з Fretboard Explorer: Гітарні гами та акорди. Поєднує 14 гам і ладів, 5 позицій CAGED, 12 типів акордів, 6 гітарних строїв та зворотний визначник акордів на інтерактивному 15-ладовому грифі з синтезом звуку Karplus-Strong.

ОСНОВНІ МОЖЛИВОСТІ
• 14 гам і ладів у всіх 12 тональностях та 5 позиціях CAGED.
• 12 типів акордів по 5 аплікатур CAGED із номерами пальців (1–4).
• Визначник акордів (Finder), 6 строїв (Standard E, Drop D, Eb, D, Open G, DADGAD), режим для шульги та вікторина нот на грифі.
• 100% офлайн, без реклами та дозволів.
```

---
