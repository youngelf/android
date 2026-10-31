# Google Play Console — "Import Translations with AI" File (Pomodoro Timer / `pomodoro` — All 20 Languages)

Upload this file (**[`PLAY_STORE_AI_IMPORT.md`](PLAY_STORE_AI_IMPORT.md)**), the CSV version (**[`PLAY_STORE_AI_IMPORT.csv`](PLAY_STORE_AI_IMPORT.csv)**), or the plain-text JSON version (**[`PLAY_STORE_AI_IMPORT.txt`](PLAY_STORE_AI_IMPORT.txt)**) directly into the Google Play Console **Store Listing → Import translations with AI** dialog. It automatically populates **App name (`appName`, ≤ 30 chars)**, **Short description (`shortDescription`, ≤ 80 chars)**, and **Full description (`fullDescription`, ≤ 4000 chars)** across all **20 supported languages**.

## Summary Table of All 20 Languages

| `#` | Play Console `languageCode` | Language | `appName` (`<= 30` chars) | `shortDescription` (`<= 80` chars) | `fullDescription` (`<= 4000` chars) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | **`en-US`** | English (United States) | [`Pomodoro: Focus & White Noise`](playstore/metadata/android/en-US/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/en-US/short_description.txt) (76/80) | [`full_description.txt`](playstore/metadata/android/en-US/full_description.txt) (3279/4000) |
| 2 | **`fr-FR`** | French (France) / Français | [`Pomodoro : Focus & Son Ambiant`](playstore/metadata/android/fr-FR/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/fr-FR/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/fr-FR/full_description.txt) (1425/4000) |
| 3 | **`it-IT`** | Italian (Italy) / Italiano | [`Pomodoro: Timer Focus e Suoni`](playstore/metadata/android/it-IT/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/it-IT/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/it-IT/full_description.txt) (1311/4000) |
| 4 | **`de-DE`** | German (Germany) / Deutsch | [`Pomodoro: Fokus-Timer & Klang`](playstore/metadata/android/de-DE/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/de-DE/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/de-DE/full_description.txt) (1318/4000) |
| 5 | **`es-ES`** | Spanish (Spain) / Español | [`Pomodoro: Temporizador y Ruido`](playstore/metadata/android/es-ES/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/es-ES/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/es-ES/full_description.txt) (1301/4000) |
| 6 | **`zh-CN`** | Chinese (Simplified) / 中文（简体） | [`番茄专注时钟 (Pomodoro Timer)`](playstore/metadata/android/zh-CN/title.txt) (23/30) | [`short_description.txt`](playstore/metadata/android/zh-CN/short_description.txt) (41/80) | [`full_description.txt`](playstore/metadata/android/zh-CN/full_description.txt) (695/4000) |
| 7 | **`ja-JP`** | Japanese / 日本語 | [`ポモドーロ集中タイマー (Pomodoro)`](playstore/metadata/android/ja-JP/title.txt) (22/30) | [`short_description.txt`](playstore/metadata/android/ja-JP/short_description.txt) (44/80) | [`full_description.txt`](playstore/metadata/android/ja-JP/full_description.txt) (674/4000) |
| 8 | **`ko-KR`** | Korean / 한국어 | [`포모도로 집중 타이머 (Pomodoro)`](playstore/metadata/android/ko-KR/title.txt) (22/30) | [`short_description.txt`](playstore/metadata/android/ko-KR/short_description.txt) (49/80) | [`full_description.txt`](playstore/metadata/android/ko-KR/full_description.txt) (810/4000) |
| 9 | **`pt-BR`** | Portuguese (Brazil) / Português | [`Pomodoro: Foco e Som Ambiente`](playstore/metadata/android/pt-BR/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/pt-BR/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/pt-BR/full_description.txt) (699/4000) |
| 10 | **`ru-RU`** | Russian / Русский | [`Pomodoro: Фокус и Фоновый Звук`](playstore/metadata/android/ru-RU/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/ru-RU/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/ru-RU/full_description.txt) (681/4000) |
| 11 | **`ar`** | Arabic / العربية | [`Pomodoro: مؤقت تركيز وضوضاء`](playstore/metadata/android/ar/title.txt) (27/30) | [`short_description.txt`](playstore/metadata/android/ar/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/ar/full_description.txt) (595/4000) |
| 12 | **`hi-IN`** | Hindi / हिन्दी | [`Pomodoro: फ़ोकस टाइमर व साउंड`](playstore/metadata/android/hi-IN/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/hi-IN/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/hi-IN/full_description.txt) (563/4000) |
| 13 | **`bn-BD`** | Bangla (Bengali) / বাংলা | [`Pomodoro: ফোকাস ও সাউন্ড`](playstore/metadata/android/bn-BD/title.txt) (24/30) | [`short_description.txt`](playstore/metadata/android/bn-BD/short_description.txt) (71/80) | [`full_description.txt`](playstore/metadata/android/bn-BD/full_description.txt) (484/4000) |
| 14 | **`id`** | Indonesian / Bahasa Indonesia | [`Pomodoro: Timer Fokus & Suara`](playstore/metadata/android/id/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/id/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/id/full_description.txt) (630/4000) |
| 15 | **`tr-TR`** | Turkish / Türkçe | [`Pomodoro: Odak Zamanlayıcısı`](playstore/metadata/android/tr-TR/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/tr-TR/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/tr-TR/full_description.txt) (377/4000) |
| 16 | **`vi`** | Vietnamese / Tiếng Việt | [`Pomodoro: Đồng Hồ Tập Trung`](playstore/metadata/android/vi/title.txt) (27/30) | [`short_description.txt`](playstore/metadata/android/vi/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/vi/full_description.txt) (383/4000) |
| 17 | **`pl-PL`** | Polish / Polski | [`Pomodoro: Timer Skupienia`](playstore/metadata/android/pl-PL/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/pl-PL/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/pl-PL/full_description.txt) (346/4000) |
| 18 | **`nl-NL`** | Dutch / Nederlands | [`Pomodoro: Focustimer & Geluid`](playstore/metadata/android/nl-NL/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/nl-NL/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/nl-NL/full_description.txt) (349/4000) |
| 19 | **`th`** | Thai / ไทย | [`Pomodoro: นาฬิกาจับเวลาโฟกัส`](playstore/metadata/android/th/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/th/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/th/full_description.txt) (315/4000) |
| 20 | **`uk`** | Ukrainian / Українська | [`Pomodoro: Таймер Фокусу`](playstore/metadata/android/uk/title.txt) (23/30) | [`short_description.txt`](playstore/metadata/android/uk/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/uk/full_description.txt) (361/4000) |

---

## Language: `en-US` — English (United States)

- **`languageCode`**: `en-US`
- **`appName`** (29 / 30 characters): Pomodoro: Focus & White Noise
- **`shortDescription`** (76 / 80 characters): Stay focused with a clean Pomodoro timer, ambient soundscapes, and work log.
- **`fullDescription`** (3279 / 4000 characters):

```text
Reclaim your deep concentration for music practice, songwriting, studying, and software engineering with Pomodoro — Focus Timer & Ambient Soundscapes. Designed with a calm, high-contrast studio interface and a built-in real-time procedural audio synthesizer, Pomodoro eliminates visual clutter and notification fatigue so you can enter a state of effortless flow.

Unlike bloated productivity apps that require accounts, subscriptions, cloud syncing, or endless task-list management, Pomodoro launches instantaneously into a distraction-free countdown ring paired with soothing background soundscapes and automatic local work-hour tracking.

HIGH-VISIBILITY FOCUS RING & DO-NOT-DISTURB BANNER
• Clear At-a-Glance Status Header: Displays a bold, unmistakable banner along the top of the screen—WORKING: DO NOT DISTURB during active focus sessions, BREAK: RECHARGE during rest intervals, and READY TO FOCUS when paused—so both you and anyone around your desk or studio know when you are in a deep-focus block.
• Precision Circular Countdown Dial: A clean, distraction-free progress ring surrounds the large monospace countdown readout, accompanied by a standard || Pause / Play transport button, quick -5m, -1m, +1m, and +5m duration adjusters, and a single-tap speaker mute/unmute icon tucked neatly into the lower-right corner.
• Seamless Phase Transitions: Automatically alternates between Focus Work intervals, Short Breaks, and Long Breaks with gentle synthesized singing-bowl chimes at the end of each phase, or lets you skip ahead to the next phase at any time.

REAL-TIME PROCEDURAL BACKGROUND SOUND GENERATOR
Mask distracting room noise, office chatter, or street traffic using three real-time 44.1 kHz synthesized background soundscapes—generated mathematically on the fly with zero looping audio seams and zero network streaming:
• SOFT TICK: A warm, subtle mechanical watch escapement pulse that gently anchors your sense of passing time.
• RAIN: Richly textured pink-noise rainfall with organic droplet modulation for calm, sustained concentration.
• BROWN NOISE: Deep, low-frequency waterfall warmth that masks external distractions and reduces auditory fatigue during long coding, reading, or instrument practice sessions.
• One-Tap Mute Toggle: Use the corner speaker button (green when active, crossed-out when muted) to silence or restore sound instantaneously.

4 WORKFLOW PRESETS + FLEXIBLE DURATION TUNING
Switch between four proven focus cadences with a single tap, and fine-tune any timer on the fly:
• Classic (25m Work / 5m Short Break / 15m Long Break)
• Deep Work (50m Work / 10m Short Break / 20m Long Break)
• Studio Practice (45m Work / 15m Short Break / 30m Long Break)
• Quick Sprint (15m Work / 3m Short Break / 10m Long Break)

LOCAL WORK LOG & PRIVACY-FIRST ARCHITECTURE
• Automatic Focus Telemetry: Tracks your completed sessions and total focused hours for Today and All-Time directly on your device so you can measure consistent daily progress.
• Adaptive Portrait & Landscape Layouts: Looks sharp on phones and tablets held vertically or placed horizontally on a desk stand or music stand.
• 100% Offline & Private: Requires zero permissions, collects zero personal data, shows zero ads, and keeps your screen awake while the timer is active.
```

---

## Language: `fr-FR` — French (France) / Français

- **`languageCode`**: `fr-FR`
- **`appName`** (30 / 30 characters): Pomodoro : Focus & Son Ambiant
- **`shortDescription`** (79 / 80 characters): Minuteur Pomodoro épuré avec sons d'ambiance synthétisés et journal de travail.
- **`fullDescription`** (1425 / 4000 characters):

```text
Retrouvez une concentration profonde pour la pratique musicale, la composition, les études et le développement logiciel avec Pomodoro : Minuteur Focus & Sons d'Ambiance. Doté d'une interface studio calme à fort contraste et d'un synthétiseur audio procédural en temps réel, Pomodoro élimine toute distraction visuelle.

ANNEAU DE COMPTE À REBOURS ET BANNIÈRE NE PAS DÉRanger
• Bannière d'état claire : Affiche CONCENTRATION : NE PAS DÉRANGER pendant le travail, PAUSE : RECHARGEZ pendant le repos, et PRÊT À SE CONCENTRER en pause.
• Anneau circulaire précis avec boutons -5m, -1m, +1m, +5m et bouton haut-parleur discret dans le coin inférieur droit.
• Transitions fluides entre travail, pause courte et pause longue avec carillon synthétisé.

GÉNÉRATEUR DE SONS D'AMBIANCE PROCÉDURAUX EN TEMPS RÉEL (44,1 kHz)
Masquez les bruits ambiants sans aucune boucle répétitive ni connexion Internet :
• TIC-TAC DOUX : Pulsation horlogère subtile et chaleureuse.
• PLUIE : Pluie apaisante modulée de manière organique.
• BRUIT BRUN : Chaleur basse fréquence profonde idéale pour les longues sessions de code ou d'instrument.

4 PRÉRÉGLAGES DE DURÉE ET JOURNAL D'HEURES LOCAL
• Classique (25/5/15), Travail Profond (50/10/20), Studio (45/15/30) et Sprint (15/3/10).
• Suivi automatique des heures concentrées Aujourd'hui et au Total sur votre appareil.
• Modes Portrait et Paysage, 100 % hors ligne, zéro permission et sans publicité.
```

---

## Language: `it-IT` — Italian (Italy) / Italiano

- **`languageCode`**: `it-IT`
- **`appName`** (29 / 30 characters): Pomodoro: Timer Focus e Suoni
- **`shortDescription`** (80 / 80 characters): Resta concentrato con un timer Pomodoro pulito, suoni ambientali e registro ore.
- **`fullDescription`** (1311 / 4000 characters):

```text
Ritrova la massima concentrazione per lo studio musicale, la scrittura, lo studio universitario e la programmazione con Pomodoro: Timer Focus e Suoni Ambientali. Progettato con un'interfaccia ad alto contrasto e un sintetizzatore audio procedurale in tempo reale, Pomodoro elimina ogni distrazione.

ANELLO DI CONTO ALLA ROVESCIA E BANNER NON DISTURBARE
• Banner di Stato Chiaro: Mostra LAVORO: NON DISTURBARE durante le sessioni di focus, PAUSA: RICARICA durante il riposo e PRONTO PER IL FOCUS in pausa.
• Quadrante circolare con regolazioni rapide -5m, -1m, +1m, +5m e pulsante altoparlante nell'angolo inferiore destro.

GENERATORE PROCEDURALE DI SUONI DI SOTTOFONDO A 44,1 kHz
Isolati dai rumori esterni con tre paesaggi sonori sintetizzati matematicamente in tempo reale:
• TICCHETTIO: Un battito meccanico morbido e discreto.
• PIOGGIA: Pioggia rilassante con modulazione organica.
• RUMORE MARRONE: Calore a bassa frequenza ideale per lunghe sessioni di programmazione o studio strumentale.

4 PRESET DI DURATA E REGISTRO LAVORO LOCALE
• Classico (25/5/15), Lavoro Profondo (50/10/20), Studio (45/15/30) e Sprint (15/3/10).
• Registra automaticamente le ore di concentrazione di Oggi e Totali direttamente sul dispositivo.
• Layout Verticale e Orizzontale, 100% offline, zero permessi e zero pubblicità.
```

---

## Language: `de-DE` — German (Germany) / Deutsch

- **`languageCode`**: `de-DE`
- **`appName`** (29 / 30 characters): Pomodoro: Fokus-Timer & Klang
- **`shortDescription`** (79 / 80 characters): Fokussiert arbeiten mit klarem Pomodoro-Timer, Hintergrundklang und Arbeitslog.
- **`fullDescription`** (1318 / 4000 characters):

```text
Gewinnen Sie tiefe Konzentration für Musikpraxis, Songwriting, Studium und Softwareentwicklung zurück mit Pomodoro: Fokus-Timer & Hintergrundklänge. Mit einer ruhigen, kontrastreichen Oberfläche und einem Echtzeit-Klangsynthesizer beseitigt Pomodoro visuelle Ablenkungen sofort.

FOKUS-COUNTDOWN-RING & BITTE-NICHT-STÖREN-BANNER
• Klare Statusanzeige: Zeigt ARBEITSPHASE: BITTE NICHT STÖREN während aktiver Fokusblöcke, PAUSE: AUFLADEN in Erholungsphasen und BEREIT FÜR FOKUS im Pausenmodus.
• Präziser Fortschrittsring mit -5m, -1m, +1m und +5m Zeitanpassung sowie praktischer Lautsprecher-Stummschaltung unten rechts.

PROZEDURALER HINTERGRUNDKLANG-GENERATOR (44,1 kHz)
Blenden Sie störende Umgebungsgeräusche mit drei mathematisch in Echtzeit erzeugten Klanglandschaften aus:
• SANFTES TICKEN: Dezenter mechanischer Uhrwerk-Impuls.
• REGEN: Organisch moduliertes Regenrauschen für entspannte Konzentration.
• BRAUNES RAUSCHEN: Tieffrequentes warmes Rauschen gegen Hörermüdung bei langen Coding- oder Übesessions.

4 ZEIT-PRESETS & LOKALES ARBEITSPROTOKOLL
• Klassisch (25/5/15), Deep Work (50/10/20), Studio (45/15/30) und Sprint (15/3/10).
• Erfasst abgeschlossene Pomodoros und Fokusstunden für Heute und Gesamt lokal auf Ihrem Gerät.
• Hoch- und Querformat, 100 % offline, keine Berechtigungen und keine Werbung.
```

---

## Language: `es-ES` — Spanish (Spain) / Español

- **`languageCode`**: `es-ES`
- **`appName`** (30 / 30 characters): Pomodoro: Temporizador y Ruido
- **`shortDescription`** (80 / 80 characters): Mantén la concentración con un temporizador Pomodoro, sonido de fondo y registro
- **`fullDescription`** (1301 / 4000 characters):

```text
Recupera tu concentración profunda para practicar música, estudiar y programar con Pomodoro: Temporizador de Enfoque y Sonidos de Fondo. Diseñado con una interfaz de estudio limpia de alto contraste y un sintetizador de audio procedimental en tiempo real, Pomodoro elimina las distracciones visuales.

ANILLO DE CUENTA ATRÁS Y BANNER DE NO MOLESTAR
• Encabezado de Estado Claro: Muestra TRABAJANDO: NO MOLESTAR durante las sesiones de enfoque, DESCANSO: RECARGAR en las pausas y LISTO PARA ENFOCAR en espera.
• Dial circular con ajustes rápidos de -5m, -1m, +1m y +5m e icono de altavoz en la esquina inferior derecha.

GENERADOR DE SONIDO DE FONDO EN TIEMPO REAL (44,1 kHz)
Bloquea el ruido ambiental con tres paisajes sonoros generados matemáticamente al instante sin conexión:
• TIC SUAVE: Pulso mecánico cálido y sutil.
• LLUVIA: Lluvia relajante con modulación orgánica de gotas.
• RUIDO MARRÓN: Calidez de baja frecuencia ideal para largas sesiones de programación, lectura o estudio musical.

4 PREAJUSTES DE DURACIÓN Y REGISTRO DE HORAS LOCAL
• Clásico (25/5/15), Profundo (50/10/20), Estudio (45/15/30) y Sprint (15/3/10).
• Registra automáticamente tus sesiones y horas enfocadas de Hoy y Totales en tu dispositivo.
• Diseño Vertical y Horizontal, 100 % offline, sin permisos y sin anuncios.
```

---

## Language: `zh-CN` — Chinese (Simplified) / 中文（简体）

- **`languageCode`**: `zh-CN`
- **`appName`** (23 / 30 characters): 番茄专注时钟 (Pomodoro Timer)
- **`shortDescription`** (41 / 80 characters): 极简无干扰番茄工作法专注计时器，内置实时合成雨声/布朗噪音背景音效与专注时长统计。
- **`fullDescription`** (695 / 4000 characters):

```text
使用《番茄专注时钟 (Pomodoro — Focus Timer & Ambient Soundscapes)》找回练琴、作曲、学习与编程时的深度专注力。应用采用高对比度极简工作台界面，并内置 44.1 kHz 实时程序化背景白噪音合成器，彻底告别繁琐的账号登录、订阅弹窗与社交干扰，助您一秒进入心流状态。

醒目专注倒计时圆环与“请勿打扰”状态横幅
• 一目了然的顶部状态横幅：专注期间显示“专注工作中：请勿打扰”，休息期间显示“休息一下：补充能量”，置于桌面或谱架上时可清晰提醒周围人不要打断您的专注时段。
• 配备大字号等宽倒计时、-5m / -1m / +1m / +5m 快速时长微调按钮，以及位于右下角的一键静音扬声器图标。
• 阶段结束时播放温润的合成颂钵提示音，自动在专注、短休与长休之间平滑流转。

44.1 kHz 实时程序化背景音效生成器
无需联网流媒体，通过数学算法实时合成无接缝的自然背景声景：
• 轻柔滴答 (SOFT TICK)：温暖细腻的机械擒纵脉冲，温和锚定时间感。
• 细雨声 (RAIN)：富有层次的粉红噪音声景与雨滴调制，带来宁静的沉浸感。
• 布朗噪音 (BROWN NOISE)：深沉浑厚的低频瀑布声，有效屏蔽环境杂音并减轻长时间用耳疲劳。

4 组经典时长预设与本地专注时长统计
• 一键切换经典 (25/5/15)、深度工作 (50/10/20)、练琴室 (45/15/30) 与冲刺 (15/3/10) 四种节奏。
• 本地自动统计“今日专注”与“累计总时长”及完成番茄数。
• 横竖屏自适应布局，100% 离线运行，零权限要求，无广告。
```

---

## Language: `ja-JP` — Japanese / 日本語

- **`languageCode`**: `ja-JP`
- **`appName`** (22 / 30 characters): ポモドーロ集中タイマー (Pomodoro)
- **`shortDescription`** (44 / 80 characters): シンプルなポモドーロタイマー、リアルタイム環境音（雨音・ブラウンノイズ）、作業時間記録。
- **`fullDescription`** (674 / 4000 characters):

```text
『Pomodoro — 集中タイマー＆環境サウンド』で、楽器練習、作曲、勉強、プログラミングの深い集中力（ディープワーク）を取り戻しましょう。視認性の高いスタジオUIと44.1 kHzリアルタイム環境音シンセサイザーを搭載し、アカウント登録や通知の煩わしさなしで瞬時にフロー状態へ入れます。

高視認性カウントダウンリング ＆「応答不可」ステータスバナー
• ひと目でわかる上部バナー：集中時は「集中作業中: 応答不可」、休憩時は「休憩タイム: リフレッシュ」を表示し、周囲にも集中中であることをスマートに伝えます。
• 見やすい円形プログレスリング、-5m / -1m / +1m / +5m の時間調整ボタン、右下に配置されたワンタップ消音スピーカーボタン。

リアルタイム生成の環境サウンド（BGM）シンセサイザー
ループの継ぎ目が一切ない、リアルタイム演算による3種類の環境サウンドを内蔵：
• ソフトティック：心地よい機械式時計の秒針パルス。
• 雨音（RAIN）：穏やかな雨粒の揺らぎを持つピンクノイズ・レイン。
• ブラウンノイズ：周囲の雑音を遮断し、長時間のコーディングや練習でも聴き疲れしない深い低域ノイズ。

4種類の時間プリセット ＆ ローカル作業時間ログ
• 標準 (25/5/15)、ディープ (50/10/20)、スタジオ (45/15/30)、スプリント (15/3/10) をワンタップ切替。
• 今日の集中時間と累計トータル時間を端末内に自動記録。
• 縦・横画面対応、権限不要・広告なし・100%オフライン動作。
```

---

## Language: `ko-KR` — Korean / 한국어

- **`languageCode`**: `ko-KR`
- **`appName`** (22 / 30 characters): 포모도로 집중 타이머 (Pomodoro)
- **`shortDescription`** (49 / 80 characters): 깔끔한 포모도로 타이머, 실시간 배경 사운드(빗소리·브라운 노이즈) 및 집중 시간 기록.
- **`fullDescription`** (810 / 4000 characters):

```text
『Pomodoro — 집중 타이머 & 앰비언트 사운드』로 악기 연습, 작곡, 독서, 프로그래밍을 위한 깊은 몰입(Deep Work)을 경험하세요. 고대비 스튜디오 인터페이스와 실시간 절차적 오디오 합성 엔진을 결합하여, 복잡한 회원가입이나 광고 없이 즉시 집중 타이머와 배경 사운드를 실행할 수 있습니다.

직관적인 카운트다운 링 & '방해 금지' 상태 배너
• 명확한 상단 상태 헤더: 집중 시간에는 '집중 세션 진행 중: 방해 금지', 휴식 시간에는 '휴식 시간: 재충전'을 표시하여 주변 사람들에게도 집중 상태를 자연스럽게 알립니다.
• 깔끔한 원형 진행 링, -5m / -1m / +1m / +5m 빠른 시간 조절 버튼, 우측 하단에 배치된 원터치 스피커 음소거 아이콘.

44.1 kHz 실시간 배경 사운드 생성기
스트리밍이나 루프 이음매 없이 실시간 수학적 합성으로 생성되는 3가지 배경음:
• 부드러운 초침 (SOFT TICK): 시간의 흐름을 은은하게 잡아주는 아날로그 시계 초침 소리.
• 빗소리 (RAIN): 자연스러운 빗방울 모듈레이션이 더해진 편안한 빗소리.
• 브라운 노이즈 (BROWN NOISE): 주변 소음을 차단하고 장시간 코딩이나 악기 연습 시 청각 피로를 줄여주는 깊은 저음역 노이즈.

4가지 워크플로우 프리셋 & 로컬 집중 기록 통계
• 클래식 (25/5/15), 딥워크 (50/10/20), 스튜디오 (45/15/30), 스프린트 (15/3/10) 원터치 전환.
• 오늘의 집중 시간과 전체 누적 기록을 기기에 자동 저장.
• 세로 및 가로 화면 지원, 권한 요청 없음, 광고 없음, 100% 오프라인 작동.
```

---

## Language: `pt-BR` — Portuguese (Brazil) / Português

- **`languageCode`**: `pt-BR`
- **`appName`** (29 / 30 characters): Pomodoro: Foco e Som Ambiente
- **`shortDescription`** (80 / 80 characters): Mantenha o foco com um timer Pomodoro limpo, sons ambientes e registro de horas.
- **`fullDescription`** (699 / 4000 characters):

```text
Recupere sua concentração profunda para estudo musical, leitura e programação com Pomodoro: Timer de Foco e Sons Ambientes. Interface limpa de alto contraste com sintetizador de som ambiente em tempo real a 44,1 kHz.

DESTAQUES
• Banner de status claro (TRABALHANDO: NÃO PERTURBE / PAUSA: RECARREGAR), anel de contagem regressiva, ajustes rápidos (-5m, -1m, +1m, +5m) e botão de mudo no canto inferior direito.
• 3 Sons de Fundo Procedurais sem loops: Tique Suave, Chuva e Ruído Marrom.
• 4 Predefinições (Clássico 25/5/15, Trabalho Profundo 50/10/20, Estúdio 45/15/30 e Sprint 15/3/10) e registro automático de horas Hoje e Total.
• Retrato e Paisagem, 100% offline, zero permissões e sem anúncios.
```

---

## Language: `ru-RU` — Russian / Русский

- **`languageCode`**: `ru-RU`
- **`appName`** (30 / 30 characters): Pomodoro: Фокус и Фоновый Звук
- **`shortDescription`** (77 / 80 characters): Минималистичный таймер Помодоро с фоновыми звуками дождя/шума и учетом часов.
- **`fullDescription`** (681 / 4000 characters):

```text
Верните глубокую концентрацию для занятий музыкой, учебы и программирования с Pomodoro: Таймер Фокуса и Фоновые Звуки. Чистый контрастный интерфейс со встроенным процедурным синтезатором фоновых звуков 44,1 кГц.

ОСНОВНЫЕ ВОЗМОЖНОСТИ
• Заметный баннер статуса (РАБОТА: НЕ БЕСПОКОИТЬ / ПЕРЕРЫВ), круговая шкала времени, быстрые кнопки (-5м, -1м, +1м, +5м) и кнопка отключения звука в правом нижнем углу.
• 3 процедурных фоновых звука в реальном времени: Мягкий тик, Дождь и Коричневый шум.
• 4 пресета длительности (Классика 25/5/15, Глубокая работа 50/10/20, Студия 45/15/30, Спринт 15/3/10) и локальный журнал часов за Сегодня и Все время.
• 100% офлайн, без рекламы и разрешений.
```

---

## Language: `ar` — Arabic / العربية

- **`languageCode`**: `ar`
- **`appName`** (27 / 30 characters): Pomodoro: مؤقت تركيز وضوضاء
- **`shortDescription`** (74 / 80 characters): حافظ على تركيزك مع مؤقت بومودورو أنيق، أصوات خلفية هادئة وسجل ساعات العمل.
- **`fullDescription`** (595 / 4000 characters):

```text
استعد تركيزك العميق للتدريب الموسيقي والدراسة والبرمجة مع Pomodoro: مؤقت التركيز والأصوات المحيطة. واجهة عالية التباين خالية من المشتتات مع مولد أصوات خلفية فوري بتردد 44.1 كيلوهرتز.

المميزات الرئيسية
• لافتة حالة واضحة (جلسة عمل: الرجاء عدم الإزعاج)، حلقة عد تنازلي مع أزرار تعديل سريع (-5د، -1د، +1د، +5د) وزر كتم الصوت في الزاوية اليمنى السفلى.
• 3 أصوات خلفية مولدة لحظياً بدون إنترنت: دقات ناعمة، مطر هادئ، وضوضاء بنية.
• 4 إعدادات جاهزة (كلاسيكي 25/5/15، عمل عميق 50/10/20، استوديو 45/15/30، سريع 15/3/10) وسجل تلقائي لساعات التركيز اليوم والإجمالي.
• يعمل 100% بدون إنترنت وبدون إعلانات.
```

---

## Language: `hi-IN` — Hindi / हिन्दी

- **`languageCode`**: `hi-IN`
- **`appName`** (29 / 30 characters): Pomodoro: फ़ोकस टाइमर व साउंड
- **`shortDescription`** (79 / 80 characters): स्वच्छ पोमोडोरो टाइमर, शांत बैकग्राउंड साउंड और कार्य लॉग के साथ केंद्रित रहें।
- **`fullDescription`** (563 / 4000 characters):

```text
Pomodoro: फ़ोकस टाइमर और एम्बिएंट साउंडस्केप के साथ संगीत अभ्यास, पढ़ाई और कोडिंग के लिए अपनी गहरी एकाग्रता वापस पाएं।

मुख्य विशेषताएं
• स्पष्ट स्टेटस बैनर (कार्य चालू: कृपया डिस्टर्ब न करें), सर्कुलर काउंटडाउन रिंग, त्वरित समय समायोजन (-5m, -1m, +1m, +5m) और नीचे दाईं ओर स्पीकर म्यूट बटन।
• 3 रियल-टाइम बैकग्राउंड साउंड्स (44.1 kHz): सॉफ्ट टिक, बारिश (Rain) और ब्राउन नॉइज़।
• 4 वर्कफ़्लो प्रीसेट (क्लासिक 25/5/15, डीप वर्क 50/10/20, स्टूडियो 45/15/30, स्प्रिंट 15/3/10) और आज व कुल घंटों का ऑटोमैटिक रिकॉर्ड।
• 100% ऑफ़लाइन, शून्य अनुमतियां और विज्ञापन-मुक्त।
```

---

## Language: `bn-BD` — Bangla (Bengali) / বাংলা

- **`languageCode`**: `bn-BD`
- **`appName`** (24 / 30 characters): Pomodoro: ফোকাস ও সাউন্ড
- **`shortDescription`** (71 / 80 characters): পোমোডোরো টাইমার, ব্যাকগ্রাউন্ড সাউন্ড এবং কাজের হিসাব সহ মনোযোগী থাকুন।
- **`fullDescription`** (484 / 4000 characters):

```text
Pomodoro: ফোকাস টাইমার ও অ্যাম্বিয়েন্ট সাউন্ড অ্যাপের মাধ্যমে সঙ্গীত অনুশীলন, পড়াশোনা এবং প্রোগ্রামিংয়ে গভীর মনোযোগ বজায় রাখুন।

মূল বৈশিষ্ট্যসমূহ
• স্পষ্ট স্ট্যাটাস ব্যানার (কাজ চলছে: বিরক্ত করবেন না), বৃত্তাকার টাইমার রিং, -5m/-1m/+1m/+5m বাটন এবং নিচে ডানদিকে স্পিকার মিউট আইকন।
• ৩টি রিয়েল-টাইম ব্যাকগ্রাউন্ড সাউন্ড: সফট টিক, বৃষ্টির শব্দ এবং ব্রাউন নয়েজ।
• ৪টি প্রিসেট (২৫/৫/১৫, ৫০/১০/২০, ৪৫/১৫/৩০, ১৫/৩/১০) এবং আজকের ও মোট কাজের ঘণ্টার লগ।
• ১০০% অফলাইন এবং বিজ্ঞাপনমুক্ত।
```

---

## Language: `id` — Indonesian / Bahasa Indonesia

- **`languageCode`**: `id`
- **`appName`** (29 / 30 characters): Pomodoro: Timer Fokus & Suara
- **`shortDescription`** (78 / 80 characters): Tetap fokus dengan timer Pomodoro bersih, suara latar tenang, dan catatan jam.
- **`fullDescription`** (630 / 4000 characters):

```text
Kembalikan konsentrasi mendalam Anda untuk latihan musik, belajar, dan pemrograman dengan Pomodoro: Timer Fokus & Suara Latar. Dilengkapi antarmuka kontras tinggi dan synthesizer suara latar prosedural 44,1 kHz.

FITUR UTAMA
• Banner status jelas (SEDANG FOKUS: JANGAN GANGGU), cincin hitung mundur, tombol -5m/-1m/+1m/+5m, dan ikon speaker di kanan bawah.
• 3 Suara Latar Real-Time tanpa internet: Detik Halus, Hujan, dan Derau Cokelat (Brown Noise).
• 4 Preset Durasi (Klasik 25/5/15, Fokus Dalam 50/10/20, Studio 45/15/30, Sprint 15/3/10) serta catatan jam fokus Hari Ini dan Total.
• 100% offline, tanpa izin, dan tanpa iklan.
```

---

## Language: `tr-TR` — Turkish / Türkçe

- **`languageCode`**: `tr-TR`
- **`appName`** (28 / 30 characters): Pomodoro: Odak Zamanlayıcısı
- **`shortDescription`** (80 / 80 characters): Temiz bir Pomodoro zamanlayıcısı, arka plan sesleri ve çalışma kaydı ile odaklan
- **`fullDescription`** (377 / 4000 characters):

```text
Pomodoro: Odak Zamanlayıcısı ve Ortam Sesleri ile müzik pratiği, ders çalışma ve yazılım geliştirme için derin odaklanmanızı geri kazanın. Rahatsız Etmeyin durum başlığı, dairesel geri sayım halkası, gerçek zamanlı sentezlenen 3 arka plan sesi (Yumuşak Tik, Yağmur, Kahverengi Gürültü), 4 süre ön ayarı ve yerel çalışma saati istatistikleri sunar. %100 çevrimdışı ve reklamsız.
```

---

## Language: `vi` — Vietnamese / Tiếng Việt

- **`languageCode`**: `vi`
- **`appName`** (27 / 30 characters): Pomodoro: Đồng Hồ Tập Trung
- **`shortDescription`** (77 / 80 characters): Giữ tập trung với đồng hồ Pomodoro tối giản, âm thanh nền và nhật ký giờ làm.
- **`fullDescription`** (383 / 4000 characters):

```text
Lấy lại sự tập trung sâu cho việc luyện nhạc, học tập và lập trình với Pomodoro: Đồng Hồ Tập Trung & Âm Thanh Nền. Tích hợp biểu ngữ trạng thái KHÔNG LÀM PHIỀN, vòng đếm ngược trực quan, 3 âm thanh nền tổng hợp thời gian thực 44,1 kHz (Tiếng tích tắc nhẹ, Tiếng mưa, Tiếng ồn nâu), 4 cấu hình thời gian và thống kê giờ làm việc Hôm nay / Tổng cộng. 100% ngoại tuyến, không quảng cáo.
```

---

## Language: `pl-PL` — Polish / Polski

- **`languageCode`**: `pl-PL`
- **`appName`** (25 / 30 characters): Pomodoro: Timer Skupienia
- **`shortDescription`** (80 / 80 characters): Zachowaj skupienie dzięki przejrzystemu timerowi Pomodoro, dźwiękom tła i logom.
- **`fullDescription`** (346 / 4000 characters):

```text
Odzyskaj głęboką koncentrację podczas ćwiczenia muzyki, nauki i programowania dzięki Pomodoro: Timer Skupienia i Dźwięki Tła. Wyraźny baner statusu (PRACA: NIE PRZESZKADZAĆ), pierścień odliczania, 3 proceduralne dźwięki tła 44,1 kHz (Cichy tyk, Deszcz, Brązowy szum), 4 presety czasu oraz lokalny dziennik godzin pracy. 100% offline i bez reklam.
```

---

## Language: `nl-NL` — Dutch / Nederlands

- **`languageCode`**: `nl-NL`
- **`appName`** (29 / 30 characters): Pomodoro: Focustimer & Geluid
- **`shortDescription`** (80 / 80 characters): Blijf gefocust met een strakke Pomodoro-timer, achtergrondgeluid en werklogboek.
- **`fullDescription`** (349 / 4000 characters):

```text
Herwin diepe concentratie voor muziekstudie, studeren en programmeren met Pomodoro: Focustimer & Achtergrondgeluiden. Voorzien van een duidelijke NIET STOREN-statusbalk, aftelring, 3 realtime gesynthetiseerde achtergrondgeluiden (Zachte tik, Regen, Bruine ruis), 4 tijdpresets en lokale registratie van je gewerkte uren. 100% offline en reclamevrij.
```

---

## Language: `th` — Thai / ไทย

- **`languageCode`**: `th`
- **`appName`** (28 / 30 characters): Pomodoro: นาฬิกาจับเวลาโฟกัส
- **`shortDescription`** (78 / 80 characters): โฟกัสกับการซ้อมดนตรีและทำงานด้วยนาฬิกา Pomodoro เสียงบรรยากาศ และบันทึกชั่วโมง
- **`fullDescription`** (315 / 4000 characters):

```text
สร้างสมาธิเชิงลึกสำหรับการซ้อมดนตรี การเรียน และการเขียนโปรแกรมด้วย Pomodoro: นาฬิกาจับเวลาโฟกัสและเสียงบรรยากาศ มาพร้อมป้ายสถานะห้ามรบกวน วงแหวนนับถอยหลัง เสียงพื้นหลังสังเคราะห์สด 3 แบบ (เสียงเข็มนาฬิกา เสียงฝน และ Brown Noise) พรีเซ็ตเวลา 4 แบบ และระบบบันทึกชั่วโมงโฟกัสรายวันและสะสม ทำงานออฟไลน์ 100% ไม่มีโฆษณา
```

---

## Language: `uk` — Ukrainian / Українська

- **`languageCode`**: `uk`
- **`appName`** (23 / 30 characters): Pomodoro: Таймер Фокусу
- **`shortDescription`** (78 / 80 characters): Зберігайте концентрацію з чистим таймером Помодоро, фоновими звуками та логом.
- **`fullDescription`** (361 / 4000 characters):

```text
Поверніть глибоку концентрацію для занять музикою, навчання та програмування з Pomodoro: Таймер Фокусу та Фонові Звуки. Чіткий банер статусу (РОБОТА: НЕ ТУРБУВАТИ), кільце зворотного відліку, 3 процедурні фонові звуки 44,1 кГц (М'який цокіт, Дощ, Коричневий шум), 4 пресети тривалості та локальний облік годин за Сьогодні та Весь час. 100% офлайн і без реклами.
```

---
