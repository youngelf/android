# Google Play Console — "Import Translations with AI" File (ChordWheel / `chordwheel` — All 20 Languages)

Upload this file (**[`PLAY_STORE_AI_IMPORT.md`](PLAY_STORE_AI_IMPORT.md)**), the CSV version (**[`PLAY_STORE_AI_IMPORT.csv`](PLAY_STORE_AI_IMPORT.csv)**), or the plain-text JSON version (**[`PLAY_STORE_AI_IMPORT.txt`](PLAY_STORE_AI_IMPORT.txt)**) directly into the Google Play Console **Store Listing → Import translations with AI** dialog. It automatically populates **App name (`appName`, ≤ 30 chars)**, **Short description (`shortDescription`, ≤ 80 chars)**, and **Full description (`fullDescription`, ≤ 4000 chars)** across all **20 supported languages**.

## Summary Table of All 20 Languages

| `#` | Play Console `languageCode` | Language | `appName` (`<= 30` chars) | `shortDescription` (`<= 80` chars) | `fullDescription` (`<= 4000` chars) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | **`en-US`** | English (United States) | [`ChordWheel: Circle of Fifths`](playstore/metadata/android/en-US/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/en-US/short_description.txt) (76/80) | [`full_description.txt`](playstore/metadata/android/en-US/full_description.txt) (3755/4000) |
| 2 | **`fr-FR`** | French (France) / Français | [`ChordWheel : Cycle des Quintes`](playstore/metadata/android/fr-FR/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/fr-FR/short_description.txt) (76/80) | [`full_description.txt`](playstore/metadata/android/fr-FR/full_description.txt) (3006/4000) |
| 3 | **`it-IT`** | Italian (Italy) / Italiano | [`ChordWheel: Circolo Quinte`](playstore/metadata/android/it-IT/title.txt) (26/30) | [`short_description.txt`](playstore/metadata/android/it-IT/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/it-IT/full_description.txt) (2969/4000) |
| 4 | **`de-DE`** | German (Germany) / Deutsch | [`ChordWheel: Quintenzirkel`](playstore/metadata/android/de-DE/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/de-DE/short_description.txt) (74/80) | [`full_description.txt`](playstore/metadata/android/de-DE/full_description.txt) (3057/4000) |
| 5 | **`es-ES`** | Spanish (Spain) / Español | [`ChordWheel: Círculo de Quintas`](playstore/metadata/android/es-ES/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/es-ES/short_description.txt) (80/80) | [`full_description.txt`](playstore/metadata/android/es-ES/full_description.txt) (3052/4000) |
| 6 | **`zh-CN`** | Chinese (Simplified) / 中文（简体） | [`五度圈和弦轮盘 (ChordWheel)`](playstore/metadata/android/zh-CN/title.txt) (20/30) | [`short_description.txt`](playstore/metadata/android/zh-CN/short_description.txt) (33/80) | [`full_description.txt`](playstore/metadata/android/zh-CN/full_description.txt) (1214/4000) |
| 7 | **`ja-JP`** | Japanese / 日本語 | [`ChordWheel: 五度圏＆ギターコード`](playstore/metadata/android/ja-JP/title.txt) (22/30) | [`short_description.txt`](playstore/metadata/android/ja-JP/short_description.txt) (40/80) | [`full_description.txt`](playstore/metadata/android/ja-JP/full_description.txt) (1414/4000) |
| 8 | **`ko-KR`** | Korean / 한국어 | [`ChordWheel: 5도권 & 기타 코드 루퍼`](playstore/metadata/android/ko-KR/title.txt) (26/30) | [`short_description.txt`](playstore/metadata/android/ko-KR/short_description.txt) (45/80) | [`full_description.txt`](playstore/metadata/android/ko-KR/full_description.txt) (1633/4000) |
| 9 | **`pt-BR`** | Portuguese (Brazil) / Português | [`ChordWheel: Círculo de Quintas`](playstore/metadata/android/pt-BR/title.txt) (30/30) | [`short_description.txt`](playstore/metadata/android/pt-BR/short_description.txt) (73/80) | [`full_description.txt`](playstore/metadata/android/pt-BR/full_description.txt) (2220/4000) |
| 10 | **`ru-RU`** | Russian / Русский | [`ChordWheel: Кварто-квинтовый`](playstore/metadata/android/ru-RU/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/ru-RU/short_description.txt) (79/80) | [`full_description.txt`](playstore/metadata/android/ru-RU/full_description.txt) (1985/4000) |
| 11 | **`ar`** | Arabic / العربية | [`ChordWheel: دائرة الخامسات`](playstore/metadata/android/ar/title.txt) (26/30) | [`short_description.txt`](playstore/metadata/android/ar/short_description.txt) (69/80) | [`full_description.txt`](playstore/metadata/android/ar/full_description.txt) (1499/4000) |
| 12 | **`hi-IN`** | Hindi / हिन्दी | [`ChordWheel: सर्कल ऑफ़ फ़िफ़्थ`](playstore/metadata/android/hi-IN/title.txt) (29/30) | [`short_description.txt`](playstore/metadata/android/hi-IN/short_description.txt) (72/80) | [`full_description.txt`](playstore/metadata/android/hi-IN/full_description.txt) (1565/4000) |
| 13 | **`bn-BD`** | Bangla (Bengali) / বাংলা | [`ChordWheel: সার্কেল অফ ফিফথস`](playstore/metadata/android/bn-BD/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/bn-BD/short_description.txt) (69/80) | [`full_description.txt`](playstore/metadata/android/bn-BD/full_description.txt) (1431/4000) |
| 14 | **`id`** | Indonesian / Bahasa Indonesia | [`ChordWheel: Lingkaran Kuint`](playstore/metadata/android/id/title.txt) (27/30) | [`short_description.txt`](playstore/metadata/android/id/short_description.txt) (71/80) | [`full_description.txt`](playstore/metadata/android/id/full_description.txt) (1773/4000) |
| 15 | **`tr-TR`** | Turkish / Türkçe | [`ChordWheel: Beşliler Çemberi`](playstore/metadata/android/tr-TR/title.txt) (28/30) | [`short_description.txt`](playstore/metadata/android/tr-TR/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/tr-TR/full_description.txt) (976/4000) |
| 16 | **`vi`** | Vietnamese / Tiếng Việt | [`ChordWheel: Vòng Bậc Năm`](playstore/metadata/android/vi/title.txt) (24/30) | [`short_description.txt`](playstore/metadata/android/vi/short_description.txt) (78/80) | [`full_description.txt`](playstore/metadata/android/vi/full_description.txt) (793/4000) |
| 17 | **`pl-PL`** | Polish / Polski | [`ChordWheel: Koło Kwintowe`](playstore/metadata/android/pl-PL/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/pl-PL/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/pl-PL/full_description.txt) (798/4000) |
| 18 | **`nl-NL`** | Dutch / Nederlands | [`ChordWheel: Kwintencirkel`](playstore/metadata/android/nl-NL/title.txt) (25/30) | [`short_description.txt`](playstore/metadata/android/nl-NL/short_description.txt) (75/80) | [`full_description.txt`](playstore/metadata/android/nl-NL/full_description.txt) (763/4000) |
| 19 | **`th`** | Thai / ไทย | [`ChordWheel: วงกลมคู่ห้า`](playstore/metadata/android/th/title.txt) (23/30) | [`short_description.txt`](playstore/metadata/android/th/short_description.txt) (67/80) | [`full_description.txt`](playstore/metadata/android/th/full_description.txt) (620/4000) |
| 20 | **`uk`** | Ukrainian / Українська | [`ChordWheel: Кварто-квінтове`](playstore/metadata/android/uk/title.txt) (27/30) | [`short_description.txt`](playstore/metadata/android/uk/short_description.txt) (77/80) | [`full_description.txt`](playstore/metadata/android/uk/full_description.txt) (736/4000) |

---

## Language: `en-US` — English (United States)

- **`languageCode`**: `en-US`
- **`appName`** (28 / 30 characters): ChordWheel: Circle of Fifths
- **`shortDescription`** (76 / 80 characters): Interactive Circle of Fifths, CAGED guitar voicings, and 4-bar chord looper.
- **`fullDescription`** (3755 / 4000 characters):

```text
Master music theory, songwriting harmony, and guitar chord voicings on a single interactive screen with ChordWheel — Circle of Fifths & Guitar Looper. Built specifically for guitarists, songwriters, music students, and educators, ChordWheel transforms the classic Circle of Fifths from a static wall poster into a playable, real-time musical instrument and harmonic workstation.

Whether you are figuring out which chords belong to a key, transposing a song on the fly, exploring CAGED guitar shapes up the neck, or jamming over a custom 4-bar chord progression, ChordWheel gives you instant visual and audio feedback with zero clutter, zero ads, and zero internet dependency.

CONCENTRIC CIRCLE OF FIFTHS HARMONIC WHEEL
• Outer Major Ring: Displays all 12 Major keys arranged in fifths (C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F) and highlights the primary diatonic major chords (I, IV, and V) in the active key.
• Inner Relative Minor Ring: Aligns each key with its relative minor and highlights the diatonic minor chords (ii, iii, and vi) inside the harmonic family wedge.
• Diminished Center Wedge & Roman Numerals: Clearly marks the leading-tone diminished chord (vii°) and labels every diatonic chord with its Roman numeral function (I, ii, iii, IV, V, vi, vii°) so you immediately understand harmonic movement.
• Dual Wheel Modes (ROTATE vs. LOCK): Spin the physical wheel smoothly to bring any key Signature to the top 12 o'clock anchor in ROTATE mode, or switch to LOCK mode to keep C anchored at 12 o'clock while tapping any key or chord around the perimeter.

SCALE-DEGREE FORMULAS & CHORD QUALITY EXTENSIONS
• Real-Time Scale-Degree Readout: Displays the exact scale degrees of the selected chord relative to the active key signature (for example, I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) alongside individual pitch names and interval formulas.
• Instant Chord Quality Modifiers: Expand any triad into a Seventh (maj7, m7, dom7, m7b5), Sus2, Sus4, or Add9 voicing with a single tap to hear rich contemporary, jazz, pop, and neo-soul colors.

MOVABLE CAGED GUITAR FRETBOARD VOICINGS
• Three Playable Neck Positions Per Chord: Switch seamlessly between Open / CAGED shapes, 6th-string E-Shape Barre voicings, and 5th-string A-Shape Barre voicings.
• Fingerings, Barres & String Mutes: Every fretboard diagram displays recommended finger numbers (1–4), curved barre arcs, open-string circles (O), muted-string markers (X), and fret position numbers so you can play the voicing immediately on your guitar.
• Tap-to-Strum Karplus-Strong Guitar Synthesis: Tap any chord wedge on the wheel or tap the guitar neck diagram to hear an expressive, physically modeled 6-string acoustic guitar strum synthesized in real time at 44.1 kHz.

4-BAR SONGWRITING LOOPER & GENRE PRESETS
• Programmable 4-Bar Chord Sequencer: Assign any chord and voicing to Bars 1 through 4 to sketch verses, choruses, and bridges, or create a backing loop for solo improvisation.
• Curated Hit Progression Presets: Load classic harmonic templates instantly—including Pop I–V–vi–IV, 50s Doo-Wop I–vi–IV–V, Jazz ii–V–I–VI, Andalusian Cadence, Royal Road (IV–V–iii–vi), and Blues turnarounds—and transpose them into any of the 12 keys with one tap.
• Adjustable Tempo & Strum Groove: Dial in your exact practice BPM and loop continuously with sample-accurate timing.

DESIGNED FOR MUSICIANS — PRIVACY FIRST & 100% OFFLINE
• Adaptive Portrait & Landscape Layouts: Optimized vector interface that adapts cleanly to phones and tablets in both vertical and horizontal orientations.
• Complete Privacy: Requires zero runtime permissions, collects zero personal data or analytics, displays no advertisements or paywalls, and runs 100% offline anywhere your music takes you.
```

---

## Language: `fr-FR` — French (France) / Français

- **`languageCode`**: `fr-FR`
- **`appName`** (30 / 30 characters): ChordWheel : Cycle des Quintes
- **`shortDescription`** (76 / 80 characters): Cycle des quintes interactif, positions guitare CAGED et boucleur 4 mesures.
- **`fullDescription`** (3006 / 4000 characters):

```text
Maîtrisez l'harmonie musicale, la composition et les positions d'accords à la guitare sur un seul écran interactif avec ChordWheel : Cycle des Quintes & Boucleur Guitare. Conçu pour les guitaristes, auteurs-compositeurs, étudiants en musique et professeurs, ChordWheel transforme le classique cycle des quintes en un véritable instrument interactif et un atelier harmonique en temps réel.

Que vous cherchiez les accords d'une tonalité, transposiez un morceau à la volée, exploriez les formes CAGED sur le manche ou improvisiez sur une grille de 4 mesures, ChordWheel offre un retour visuel et sonore immédiat, sans publicité et 100 % hors ligne.

ROUE HARMONIQUE CONCENTRIQUE DU CYCLE DES QUINTES
• Anneau majeur extérieur : Affiche les 12 tonalités majeures ordonnées par quintes (C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F) et met en évidence les degrés majeurs diatoniques (I, IV et V).
• Anneau mineur relatif intérieur : Aligne chaque tonalité avec sa relative mineure et souligne les accords mineurs diatoniques (ii, iii et vi).
• Secteur diminué central et chiffres romains : Identifie clairement l'accord diminué (vii°) et affiche la fonction harmonique en chiffres romains (I, ii, iii, IV, V, vi, vii°).
• Modes ROTATION et VERROUILLAGE (LOCK) : Faites tourner la roue pour placer n'importe quelle tonalité à 12 heures, ou verrouillez Do (C) en haut tout en explorant librement les accords.

FORMULES DE DEGRÉS ET EXTENSIONS D'ACCORDS
• Formules de degrés en temps réel : Affiche les degrés exacts de l'accord sélectionné par rapport à la tonalité active (ex. I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) ainsi que les notes individuelles.
• Extensions de 7e : Basculez instantanément entre triades et accords de 7e (maj7, m7, dom7, m7b5) pour enrichir vos grilles pop, jazz ou neo-soul.

DIAGRAMMES GUITARE CAGED ET SYNTHÈSE KARPLUS-STRONG
• Trois positions jouables par accord : Alternez entre positions ouvertes/CAGED, barrés forme de Mi (corde 6) et barrés forme de La (corde 5).
• Doigtés, barrés et cordes étouffées : Affiche les numéros de doigts (1–4), les barrés, les cordes à vide (O), les cordes étouffées (X) et les numéros de cases.
• Synthèse acoustique 44,1 kHz : Touchez un accord ou glissez sur le diagramme du manche pour entendre un arpège ou un gratté de guitare modélisé physiquement (Karplus-Strong) en temps réel.

BOUCLEUR DE COMPOSITIONS 4 MESURES ET PRÉRÉGLAGES
• Séquenceur d'accords 4 mesures : Assignez n'importe quel accord aux mesures 1 à 4 pour esquisser couplets et refrains ou créer un accompagnement d'improvisation.
• Grilles classiques intégrées : Chargez instantanément les progressions Pop I–V–vi–IV, Jazz ii–V–I–VI, Cadence andalouse, Doo-Wop ou Blues et transposez-les dans les 12 tonalités.

CONÇU POUR LES MUSICIENS — CONFIDENTIALITÉ TOTALE ET 100 % HORS LIGNE
• Interfaces Portrait et Paysage adaptatives pour téléphones et tablettes.
• Zéro permission requise, aucune collecte de données, aucune publicité et fonctionnement 100 % hors ligne.
```

---

## Language: `it-IT` — Italian (Italy) / Italiano

- **`languageCode`**: `it-IT`
- **`appName`** (26 / 30 characters): ChordWheel: Circolo Quinte
- **`shortDescription`** (78 / 80 characters): Circolo delle quinte interattivo, accordi chitarra CAGED e looper a 4 battute.
- **`fullDescription`** (2969 / 4000 characters):

```text
Padroneggia la teoria musicale, l'armonia e i voicing per chitarra su un unico schermo interattivo con ChordWheel: Circolo delle Quinte e Looper per Chitarra. Creato per chitarristi, cantautori, studenti di musica e insegnanti, ChordWheel trasforma il classico Circolo delle Quinte in uno strumento musicale suonabile in tempo reale.

Che tu stia cercando gli accordi di una tonalità, trasponendo un brano al volo, esplorando le forme CAGED lungo la tastiera o improvvisando su un giro armonico di 4 battute, ChordWheel ti offre un feedback visivo e sonoro immediato, senza pubblicità e completamente offline.

RUOTA ARMONICA CONCENTRICA DEL CIRCOLO DELLE QUINTE
• Anello Maggiore Esterno: Mostra tutte le 12 tonalità maggiori ordinate per quinte (C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F) ed evidenzia gli accordi maggiori diatonici principali (I, IV e V).
• Anello Minore Relativo Interno: Allinea ogni tonalità con la sua relativa minore ed evidenzia gli accordi minori diatonici (ii, iii e vi).
• Settore Diminuito Centrale e Numeri Romani: Indica chiaramente l'accordo diminuito sul settimo grado (vii°) ed etichetta ogni accordo con la sua funzione armonica (I, ii, iii, IV, V, vi, vii°).
• Modalità RUOTA e BLOCCA (LOCK): Ruota la ghiera per portare qualsiasi tonalità a ore 12, oppure blocca il Do (C) in alto toccando liberamente gli accordi lungo il perimetro.

FORMULE DEI GRADI DELLA SCALA ED ESTENSIONI DI SETTIMA
• Lettura in Tempo Reale dei Gradi: Visualizza i gradi esatti dell'accordo selezionato rispetto alla tonalità attiva (ad es. I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) insieme ai nomi delle singole note.
• Accordi di Settima Istantanei: Espandi qualsiasi triade in un accordo di settima (maj7, m7, dom7, m7b5) con un solo tocco.

VOICING CHITARRA CAGED E SINTESI KARPLUS-STRONG
• Tre Posizioni sul Manico per Accordo: Passa istantaneamente tra forme Aperte/CAGED, barrè in forma di Mi (6ª corda) e barrè in forma di La (5ª corda).
• Diteggiature, Barrè e Corde Stoppate: Ogni diagramma mostra i numeri delle dita (1–4), gli archi del barrè, le corde a vuoto (O), le corde stoppate (X) e il numero del tasto.
• Sintesi Chitarra Acustica a 44,1 kHz: Tocca uno spicchio della ruota o scorri sul diagramma del manico per ascoltare una pennata di chitarra a 6 corde modellata fisicamente (Karplus-Strong) in tempo reale.

LOOPER A 4 BATTUTE E PROGRESSIONI PRESET
• Sequencer Armonico a 4 Battute: Assegna qualsiasi accordo alle battute 1–4 per comporre strofe e ritornelli o creare una base per l'improvvisazione.
• Progressioni Classiche Integrate: Carica all'istante giri Pop I–V–vi–IV, Jazz ii–V–I–VI, Cadenza Andalusa, Doo-Wop e Blues, trasponendoli in tutte le 12 tonalità.

PROGETTATO PER I MUSICISTI — PRIVACY TOTALE E 100% OFFLINE
• Layout Verticale (Portrait) e Orizzontale (Landscape) ottimizzati per smartphone e tablet.
• Zero autorizzazioni richieste, nessuna raccolta dati, zero pubblicità e funzionamento 100% offline.
```

---

## Language: `de-DE` — German (Germany) / Deutsch

- **`languageCode`**: `de-DE`
- **`appName`** (25 / 30 characters): ChordWheel: Quintenzirkel
- **`shortDescription`** (74 / 80 characters): Interaktiver Quintenzirkel, CAGED-Gitarrengriffe und 4-Takt-Akkord-Looper.
- **`fullDescription`** (3057 / 4000 characters):

```text
Meistern Sie Harmonielehre, Songwriting und Gitarren-Voicings auf einem einzigen interaktiven Bildschirm mit ChordWheel: Quintenzirkel & Gitarren-Looper. Entwickelt für Gitarristen, Songwriter, Musikstudenten und Lehrer, verwandelt ChordWheel den klassischen Quintenzirkel in ein spielbares Echtzeit-Musikinstrument und Harmoniewerkzeug.

Egal, ob Sie herausfinden möchten, welche Akkorde zu einer Tonart gehören, einen Song spontan transponieren, CAGED-Griffbilder auf dem Griffbrett erkunden oder über eine eigene 4-Takt-Akkordfolge jammen: ChordWheel liefert sofortiges visuelles und akustisches Feedback ohne Werbung und 100 % offline.

KONZENTRISCHER QUINTENZIRKEL
• Äußerer Dur-Ring: Zeigt alle 12 Dur-Tonarten im Quintabstand (C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F) und hebt die diatonischen Hauptdreiklänge (I, IV und V) der aktiven Tonart hervor.
• Innerer Parallel-Moll-Ring: Ordnet jeder Dur-Tonart ihre parallele Moll-Tonart zu und markiert die diatonischen Moll-Akkorde (ii, iii und vi).
• Verminderter Zentrumskeil & Stufenbezeichnungen: Kennzeichnet den verminderten Leittonakkord (vii°) und beschriftet jeden diatonischen Akkord mit seiner römischen Stufenfunktion (I, ii, iii, IV, V, vi, vii°).
• Dreh- und Sperrmodus (ROTATE vs. LOCK): Drehen Sie das Rad stufenlos, um jede Tonart auf die 12-Uhr-Position zu bringen, oder fixieren Sie C im LOCK-Modus oben.

TONLEITERSTUFEN-FORMELN & SEPTAKKORD-ERWEITERUNGEN
• Echtzeit-Stufenformeln: Zeigt die exakten Tonleiterstufen des gewählten Akkords relativ zur aktiven Tonart (z. B. I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) sowie alle Einzelnoten.
• Septakkorde auf Knopfdruck: Erweitern Sie jeden Dreiklang sofort zu einem Septakkord (maj7, m7, dom7, m7b5) für reichhaltige Jazz-, Pop- und Soul-Klänge.

VERSCHIEBBARE CAGED-GITARRENGRIFFE & KARPLUS-STRONG-SYNTHESE
• Drei spielbare Griffbrett-Lagen pro Akkord: Wechseln Sie nahtlos zwischen offenen/CAGED-Griffen, E-Typ-Barrégrifften (6. Saite) und A-Typ-Barrégrifften (5. Saite).
• Fingersätze, Barrés & gedämpfte Saiten: Jedes Griffbild zeigt empfohlene Fingerzahlen (1–4), Barré-Bögen, Leersaiten (O), gedämpfte Saiten (X) und Bundzahlen.
• Echtzeit-Gitarrensynthese mit 44,1 kHz: Tippen Sie auf einen Akkordsektor oder streichen Sie über das Griffbrettdiagramm, um einen physikalisch modellierten 6-Saiten-Gitarrenanschlag (Karplus-Strong) zu hören.

4-TAKT-SONGWRITING-LOOPER & GENRE-PRESETS
• Programmierbarer 4-Takt-Akkordsequenzer: Weisen Sie den Takten 1 bis 4 beliebige Akkorde zu, um Songideen zu skizzieren oder eine Begleitschleife zum Solieren zu erstellen.
• Integrierte Hit-Progressionen: Laden Sie Klassiker wie Pop I–V–vi–IV, Jazz ii–V–I–VI, Andalusische Kadenz, Doo-Wop oder Blues-Turnarounds und transponieren Sie diese sofort in alle 12 Tonarten.

FÜR MUSIKER ENTWICKELT — DATENSCHUTZ & 100 % OFFLINE
• Optimierte Hochformat- (Portrait) und Querformat-Layouts (Landscape) für Smartphones und Tablets.
• Keine Berechtigungen erforderlich, keine Datenerfassung, keine Werbung und vollständig offline nutzbar.
```

---

## Language: `es-ES` — Spanish (Spain) / Español

- **`languageCode`**: `es-ES`
- **`appName`** (30 / 30 characters): ChordWheel: Círculo de Quintas
- **`shortDescription`** (80 / 80 characters): Círculo de quintas interactivo, acordes CAGED de guitarra y looper de 4 compases
- **`fullDescription`** (3052 / 4000 characters):

```text
Domina la teoría musical, la armonía de composición y los acordes de guitarra en una sola pantalla interactiva con ChordWheel: Círculo de Quintas y Looper de Guitarra. Diseñado para guitarristas, compositores, estudiantes de música y profesores, ChordWheel transforma el clásico Círculo de Quintas en un instrumento musical interactivo en tiempo real.

Tanto si buscas qué acordes pertenecen a una tonalidad, como si transportas una canción al instante, exploras posiciones CAGED por el mástil o improvisas sobre una progresión de 4 compases, ChordWheel te ofrece respuesta visual y sonora inmediata, sin anuncios y 100 % sin conexión.

RUEDA ARMÓNICA CONCÉNTRICA DEL CÍRCULO DE QUINTAS
• Anillo Mayor Exterior: Muestra las 12 tonalidades mayores ordenadas por quintas (C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F) y resalta los acordes mayores diatónicos principales (I, IV y V).
• Anillo Menor Relativo Interior: Alinea cada tonalidad con su relativo menor y destaca los acordes menores diatónicos (ii, iii y vi).
• Sector Disminuido Central y Números Romanos: Marca claramente el acorde disminuido (vii°) y etiqueta cada acorde diatónico con su función en números romanos (I, ii, iii, IV, V, vi, vii°).
• Modos GIRAR y BLOQUEAR (LOCK): Gira la rueda suavemente para llevar cualquier tonalidad a las 12 en punto, o bloquea Do (C) arriba mientras tocas cualquier acorde del círculo.

FÓRMULAS DE GRADOS DE LA ESCALA Y ACORDES DE SÉPTIMA
• Lectura de Grados en Tiempo Real: Muestra los grados exactos del acorde seleccionado respecto a la tonalidad activa (por ejemplo, I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) junto con los nombres de las notas.
• Acordes de Séptima al Instante: Expande cualquier tríada a un acorde de séptima (maj7, m7, dom7, m7b5) con un solo toque.

POSICIONES CAGED DE GUITARRA Y SÍNTESIS KARPLUS-STRONG
• Tres Posiciones por Acorde en el Mástil: Alterna al instante entre formas Abiertas/CAGED, cejillas con forma de Mi (6.ª cuerda) y cejillas con forma de La (5.ª cuerda).
• Digitaciones, Cejillas y Cuerdas Muteadas: Cada diagrama muestra los dedos recomendados (1–4), arcos de cejilla, cuerdas al aire (O), cuerdas silenciadas (X) y número de traste.
• Síntesis de Guitarra en Tiempo Real a 44,1 kHz: Toca cualquier sector de la rueda o desliza el dedo por el diagrama del diapasón para escuchar un rasgueo de guitarra acústica de 6 cuerdas modelado físicamente (Karplus-Strong).

LOOPER DE 4 COMPASES Y PROGRESIONES PREDEFINIDAS
• Secuenciador de Acordes de 4 Compases: Asigna cualquier acorde a los compases 1 a 4 para componer estrofas y estribillos o crear una base de improvisación.
• Plantillas de Progresiones Clásicas: Carga al instante progresiones Pop I–V–vi–IV, Jazz ii–V–I–VI, Cadencia Andaluza, Doo-Wop y Blues en cualquiera de las 12 tonalidades.

DISEÑADO PARA MÚSICOS — PRIVACIDAD TOTAL Y 100 % OFFLINE
• Diseños adaptativos en Vertical (Portrait) y Horizontal (Landscape) para móviles y tablets.
• Cero permisos requeridos, sin recopilación de datos, sin anuncios y funcionamiento 100 % offline.
```

---

## Language: `zh-CN` — Chinese (Simplified) / 中文（简体）

- **`languageCode`**: `zh-CN`
- **`appName`** (20 / 30 characters): 五度圈和弦轮盘 (ChordWheel)
- **`shortDescription`** (33 / 80 characters): 交互式五度圈音乐理论、CAGED吉他指型图与4小节和弦循环伴奏器。
- **`fullDescription`** (1214 / 4000 characters):

```text
使用《五度圈和弦轮盘 (ChordWheel)》在单个交互式屏幕上全面掌握音乐理论、作曲和声与吉他和弦指型。专为吉他手、唱作人、音乐专业学生与教师打造，ChordWheel 将传统的静态五度圈挂图升级为可实时弹奏的交互式乐器与和声工作站。

无论您是在查找某个调式内的顺阶和弦、即时移调歌曲、探索指板上的 CAGED 把位指型，还是在自定义的 4 小节和弦进行上即兴伴奏，ChordWheel 都能提供零延迟的视觉与原声吉他音频反馈，无广告且 100% 离线运行。

同心五度圈和声轮盘
• 外圈大调环：按五度循环展示全部 12 个大调（C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F），并高亮显示当前调式的正三和弦（I、IV、V 级）。
• 内圈关系小调环：将每个大调与其关系小调精准对齐，并突出显示顺阶小三和弦（ii、iii、vi 级）。
• 中心减和弦扇区与罗马数字级数：清晰标示导音减三和弦（vii°）及所有顺阶和弦的罗马数字功能标记（I, ii, iii, IV, V, vi, vii°），让您一眼看懂和声走向。
• 旋转与锁定双模式（ROTATE / LOCK）：可平滑旋转轮盘将任意调式置于 12 点钟主音位置，或锁定 C 调在顶部并自由点按圆周上的任意和弦。

音阶度数公式与七和弦扩展
• 实时音阶度数解析：实时显示所选和弦相对于当前调式的音阶度数构成（如 I = 1 - 3 - 5，IV = 4 - 6 - 8，V7 = 5 - 7 - 9 - 11）以及具体音名。
• 一键切换七和弦：一键将三和弦扩展为七和弦（maj7、m7、dom7、m7b5），轻松聆听流行、爵士与 Neo-Soul 的丰富色彩。

CAGED 吉他指板把位与 KARPLUS-STRONG 物理建模合成
• 每个和弦三种实用把位：在开放/CAGED 指型、6 弦根音 E 型大横按与 5 弦根音 A 型大横按之间自由切换。
• 完整指法与横按标记：清晰标注左手按弦指法编号（1–4）、横按弧线、空弦（O）、闷音弦（X）与品格位置。
• 44.1 kHz 实时吉他扫弦合成：点击和弦扇区或在指板图上滑动，即可听到由 Karplus-Strong 物理建模实时合成的逼真六弦原声吉他扫弦与分解音色。

4 小节写歌循环器与经典和弦进行预设
• 可编程 4 小节序列器：为第 1 至第 4 小节自由分配和弦，快速构思歌曲主歌/副歌或生成即兴独奏伴奏循环。
• 内置经典热门进行：一键加载流行 I–V–vi–IV、爵士 ii–V–I–VI、安达卢西亚进行、复古 Doo-Wop 与布鲁斯和声模板，并支持 12 调即时移调。

专为音乐人设计 — 隐私优先且 100% 离线
• 自适应竖屏与横屏双布局，完美适配手机与平板。
• 零权限要求、不收集任何个人数据、无广告，随时随地离线练琴与创作。
```

---

## Language: `ja-JP` — Japanese / 日本語

- **`languageCode`**: `ja-JP`
- **`appName`** (22 / 30 characters): ChordWheel: 五度圏＆ギターコード
- **`shortDescription`** (40 / 80 characters): インタラクティブな五度圏、CAGEDギターコードフォーム、4小節コードルーパー。
- **`fullDescription`** (1414 / 4000 characters):

```text
『ChordWheel — 五度圏＆ギターコードルーパー』で、音楽理論・作曲ハーモニー・ギターのコードボイシングを1つの画面で自在にマスターしましょう。ギタリスト、ソングライター、音楽学生、講師のために設計された ChordWheel は、定番の五度圏（Circle of Fifths）をリアルタイムに演奏できる楽器＆ハーモニー・ワークステーションへと進化させます。

キーに含まれるダイアトニックコードの確認、瞬時の移調（トランスポーズ）、CAGEDフォームによる指板上のボイシング確認、4小節のコード進行ループを使ったジャム練習まで、広告なし・完全オフラインで直感的に行えます。

同心円状の五度圏ハーモニック・ホイール
• 外側メジャーリング：全12メジャーキーを五度圏順に配置し、選択キーの主要メジャーコード（I、IV、V）をハイライト表示します。
• 内側平行マイナーリング：各メジャーキーに対応する平行マイナーキーとダイアトニックマイナーコード（ii、iii、vi）を分かりやすく表示します。
• 中央ディミニッシュ＆ローマ数字度数：導音ディミニッシュ（vii°）を含むすべてのダイアトニックコードにローマ数字の度数機能（I, ii, iii, IV, V, vi, vii°）を表示します。
• 回転モードと固定（LOCK）モード：ホイールを回して任意のキーを12時の位置に合わせることも、Cを12時に固定したまま周囲のコードをタップすることも可能です。

スケールドグリー構成音＆セブンス拡張
• リアルタイム構成音表示：現在のキーに対するコードの構成度数（例：I = 1 - 3 - 5、IV = 4 - 6 - 8、V7 = 5 - 7 - 9 - 11）と構成音名を即座に表示します。
• ワンタップ7thコード切り替え：トライアド（三和音）からセブンスコード（maj7, m7, dom7, m7b5）へワンタップで切り替え、ジャズやポップスの豊かな響きを確認できます。

CAGEDギターボイシング＆物理モデリング音源
• 1コードにつき3種類のポジション：オープン／CAGEDフォーム、6弦ルートEフォーム・バレー、5弦ルートAフォーム・バレーを瞬時に切り替えられます。
• 指番号・セーハ・ミュート表示：押弦する指番号（1〜4）、セーハ（バレー）記号、開放弦（O）、ミュート弦（X）、フレット番号を明快に表示します。
• 44.1 kHz Karplus-Strong ギターシンセサイザー：ホイール上のコードや指板図をタップ／スワイプすると、物理モデリングによるリアルな6弦アコースティックギターのストローク音が鳴り響きます。

4小節コード進行ルーパー＆定番プリセット
• 4小節シーケンサー：1〜4小節に好きなコードを割り当て、作曲のスケッチやアドリブ練習用のバッキングループを作成できます。
• 定番コード進行プリセット：王道ポップ進行（I–V–vi–IV）、ジャズ（ii–V–I–VI）、アンダルシア進行、ブルースなどをワンタップで読み込み、全12キーへ自在に移調できます。

ミュージシャンファースト設計 — 完全オフライン＆プライバシー保護
• スマートフォンやタブレットの縦画面・横画面の両方に最適化。
• 権限要求ゼロ、データ収集ゼロ、広告ゼロ、100%オフラインで動作します。
```

---

## Language: `ko-KR` — Korean / 한국어

- **`languageCode`**: `ko-KR`
- **`appName`** (26 / 30 characters): ChordWheel: 5도권 & 기타 코드 루퍼
- **`shortDescription`** (45 / 80 characters): 인터랙티브 5도권 화성학 휠, CAGED 기타 코드 보이싱 및 4마디 코드 루퍼.
- **`fullDescription`** (1633 / 4000 characters):

```text
『ChordWheel — 5도권 & 기타 코드 루퍼』로 음악 이론, 작곡 화성학, 기타 코드 보이싱을 하나의 인터랙티브 화면에서 마스터하세요. 기타리스트, 작곡가, 실용음악 학생 및 교육자를 위해 설계된 ChordWheel은 정적인 5도권(Circle of Fifths) 표를 실시간으로 연주하고 소리를 들을 수 있는 악기이자 화성 워크스테이션으로 바꿔 줍니다.

특정 조성(Key)에 속하는 다이어토닉 코드를 찾거나, 곡을 즉시 조옮김(Transpose)하고, 지판 위의 CAGED 코드 폼을 탐색하거나 4마디 코드 진행 루프 위에서 즉흥 연주를 할 때 광고 없이 100% 오프라인으로 즉각적인 시각·청각 피드백을 제공합니다.

동심원 구조의 5도권 화성 휠
• 바깥쪽 메이저 링: 5도 간격으로 배열된 12개의 메이저 키(C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F)를 표시하고 현재 키의 주요 장3화음(I, IV, V)을 강조합니다.
• 안쪽 나란한조 마이너 링: 각 메이저 키와 나란한 마이너 키를 정렬하고 다이어토닉 단3화음(ii, iii, vi)을 한눈에 보여줍니다.
• 중앙 디미니쉬 웨지 및 로마 숫자 도수 표시: 이음 감3화음(vii°)과 모든 다이어토닉 코드의 화성 기능(I, ii, iii, IV, V, vi, vii°)을 명확하게 표시합니다.
• 회전 및 잠금(LOCK) 모드: 휠을 부드럽게 돌려 원하는 키를 12시 기준점에 놓거나, LOCK 모드로 C키를 상단에 고정한 채 모든 코드를 탐색할 수 있습니다.

스케일 도수 공식 및 7화음(7THS) 확장
• 실시간 스케일 도수 공식: 선택한 코드가 현재 키에서 가지는 스케일 도수(예: I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11)와 구성음 이름을 즉시 표시합니다.
• 원터치 7화음 전환: 3화음을 세븐스 코드(maj7, m7, dom7, m7b5)로 즉시 확장하여 팝, 재즈, 네오소울의 풍부한 화성 색채를 들어볼 수 있습니다.

CAGED 기타 지판 보이싱 및 KARPLUS-STRONG 사운드 합성
• 코드당 3가지 지판 포지션: 오픈/CAGED 폼, 6번줄 루트 E-바레 폼, 5번줄 루트 A-바레 폼을 즉시 전환할 수 있습니다.
• 운지 번호, 바레 및 뮤트 표시: 손가락 번호(1–4), 바레 곡선, 개방현(O), 뮤트현(X), 프렛 번호를 정확히 시각화합니다.
• 44.1 kHz 실시간 기타 스트럼 사운드: 휠의 코드 섹터나 지판 다이어그램을 탭하거나 스와이프하면 물리 모델링(Karplus-Strong) 기반의 생생한 6현 어쿠스틱 기타 사운드가 재생됩니다.

4마디 작곡 루퍼 및 장르별 진행 프리셋
• 4마디 코드 시퀀서: 1~4마디에 원하는 코드를 배치하여 곡의 벌스/코러스를 스케치하거나 솔로 연습용 반주 루프를 만드세요.
• 클래식 코드 진행 프리셋: 팝(I–V–vi–IV), 재즈(ii–V–I–VI), 안달루시아 카덴차, 블루스 등의 명곡 진행을 불러와 12키로 즉시 조옮김할 수 있습니다.

뮤지션을 위한 설계 — 완벽한 개인정보 보호 및 100% 오프라인
• 스마트폰과 태블릿의 세로(Portrait) 및 가로(Landscape) 화면 완벽 지원.
• 권한 요청 없음, 데이터 수집 없음, 광고 없음, 100% 오프라인 작동.
```

---

## Language: `pt-BR` — Portuguese (Brazil) / Português

- **`languageCode`**: `pt-BR`
- **`appName`** (30 / 30 characters): ChordWheel: Círculo de Quintas
- **`shortDescription`** (73 / 80 characters): Círculo de quintas interativo, acordes CAGED de violão/guitarra e looper.
- **`fullDescription`** (2220 / 4000 characters):

```text
Domine a teoria musical, a harmonia de composição e os acordes de guitarra/violão em uma única tela interativa com ChordWheel: Círculo de Quintas e Looper de Guitarra. Projetado para guitarristas, violonistas, compositores, estudantes e professores de música, o ChordWheel transforma o clássico Círculo de Quintas em um instrumento musical interativo em tempo real.

RODA HARMÔNICA CONCÊNTRICA DO CÍRCULO DE QUINTAS
• Anel Maior Externo: Exibe todas as 12 tonalidades maiores ordenadas por quintas (C, G, D, A, E, B/Cb, F#/Gb, Db, Ab, Eb, Bb, F) e destaca os acordes maiores diatônicos principais (I, IV e V).
• Anel Menor Relativo Interno: Alinha cada tom com seu relativo menor e destaca os acordes menores diatônicos (ii, iii e vi).
• Setor Diminuto Central e Algarismos Romanos: Marca claramente o acorde diminuto (vii°) e identifica a função harmônica (I, ii, iii, IV, V, vi, vii°).
• Modos GIRAR e TRAVAR (LOCK): Gire a roda para colocar qualquer tonalidade no topo (12 horas) ou trave Dó (C) no topo enquanto explora qualquer acorde.

FÓRMULAS DE GRAUS DA ESCALA E EXTENSÕES DE SÉTIMA
• Leitura de Graus em Tempo Real: Mostra os graus exatos do acorde selecionado em relação à tonalidade ativa (ex.: I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) junto com os nomes das notas.
• Acordes com Sétima num Toque: Expanda qualquer tríade para um acorde com sétima (maj7, m7, dom7, m7b5) instantaneamente.

VOICINGS CAGED NO BRAÇO E SÍNTESE KARPLUS-STRONG
• Três Posições por Acorde: Alterne entre formas Abertas/CAGED, pestanas na forma de Mi (6ª corda) e pestanas na forma de Lá (5ª corda).
• Digitações, Pestanas e Cordas Abafadas: Indica os dedos (1–4), pestanas, cordas soltas (O), cordas abafadas (X) e casas.
• Síntese de Violão em Tempo Real a 44,1 kHz: Toque em qualquer setor ou deslize pelo diagrama do braço para ouvir uma batida de violão de 6 cordas modelada fisicamente (Karplus-Strong).

LOOPER DE 4 COMPASSOS E PROGRESSÕES PRONTAS
• Sequenciador de 4 Compassos: Programe os compassos 1 a 4 e carregue progressões clássicas de Pop (I–V–vi–IV), Jazz (ii–V–I–VI), Cadência Andaluza, Doo-Wop e Blues em qualquer um dos 12 tons.
• Modos Retrato e Paisagem, zero permissões, sem anúncios e 100% offline.
```

---

## Language: `ru-RU` — Russian / Русский

- **`languageCode`**: `ru-RU`
- **`appName`** (28 / 30 characters): ChordWheel: Кварто-квинтовый
- **`shortDescription`** (79 / 80 characters): Интерактивный кварто-квинтовый круг, аккорды CAGED для гитары и лупер 4 тактов.
- **`fullDescription`** (1985 / 4000 characters):

```text
Освойте теорию музыки, гармонию и аппликатуры гитарных аккордов на одном интерактивном экране с ChordWheel: Кварто-квинтовый круг и гитарный лупер. Созданное для гитаристов, сонграйтеров, студентов и преподавателей музыки, приложение превращает классический кварто-квинтовый круг в живой музыкальный инструмент и гармоническую рабочую станцию.

КОНЦЕНТРИЧЕСКИЙ КВАРТО-КВИНТОВЫЙ КРУГ
• Внешнее мажорное кольцо: Отображает все 12 мажорных тональностей по квинтам и выделяет главные диатонические трезвучия (I, IV и V).
• Внутреннее кольцо параллельного минора: Сопоставляет каждую тональность с параллельным минором и подсвечивает минорные аккорды (ii, iii и vi).
• Центральный уменьшенный сектор и римские цифры: Показывает вводный уменьшенный аккорд (vii°) и гармоническую функцию каждого аккорда (I, ii, iii, IV, V, vi, vii°).
• Режимы ВРАЩЕНИЯ и ФИКСАЦИИ (LOCK): Вращайте колесо для выбора тональности на отметке 12 часов или зафиксируйте До (C) наверху.

ФОРМУЛЫ СТУПЕНЕЙ И СЕПТАККОРДЫ
• Формулы ступеней в реальном времени: Отображает точные ступени выбранного аккорда относительно активной тональности (например, I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) и названия нот.
• Переключение в септаккорды (7THS): Превращайте любое трезвучие в септаккорд (maj7, m7, dom7, m7b5) одним нажатием.

АППЛИКАТУРЫ CAGED НА ГРИФЕ И СИНТЕЗ KARPLUS-STRONG
• Три позиции на грифе для каждого аккорда: Открытые формы/CAGED, баррэ от 6-й струны (форма E) и баррэ от 5-й струны (форма A).
• Пальцовка, баррэ и глушение струн: Номера пальцев (1–4), дуги баррэ, открытые (O) и приглушенные (X) струны.
• Физический синтез гитары 44,1 кГц (Karplus-Strong) при нажатии на сектор или проведении по схеме грифа.

4-ТАКТОВЫЙ ЛУПЕР И ГОТОВЫЕ ПРОГРЕССИИ
• Программируйте 4-тактовые последовательности или загружайте классические поп-, джаз-, блюз- и андалузские прогрессии с мгновенным транспонированием во все 12 тональностей.
• Портретный и альбомный режимы, 100% офлайн, без рекламы и разрешений.
```

---

## Language: `ar` — Arabic / العربية

- **`languageCode`**: `ar`
- **`appName`** (26 / 30 characters): ChordWheel: دائرة الخامسات
- **`shortDescription`** (69 / 80 characters): دائرة الخامسات التفاعلية، أشكال كوردات الجيتار CAGED، ومكرر 4 موازير.
- **`fullDescription`** (1499 / 4000 characters):

```text
أتقن نظرية الموسيقى، وتناغم كتابة الأغاني، وأشكال كوردات الجيتار على شاشة تفاعلية واحدة مع تطبيق ChordWheel: دائرة الخامسات ومكرر الجيتار. صُمم التطبيق لعازفي الجيتار والملحنين وطلاب الموسيقى، حيث يحول دائرة الخامسات الكلاسيكية إلى آلة موسيقية تفاعلية في الوقت الفعلي.

عجلة دائرة الخامسات المتمركزة
• الحلقة الكبرى الخارجية: تعرض جميع المقامات الكبرى الـ 12 مرتبة بالخامسات وتبرز الكوردات الأساسية (I و IV و V).
• الحلقة الصغرى النسبية الداخلية: تعرض المقام الصغير النسبي والكوردات الصغرى (ii و iii و vi).
• الكورد الناقص والأرقام الرومانية: يوضح الكورد الناقص (vii°) والوظيفة التناغمية لكل كورد (I, ii, iii, IV, V, vi, vii°).
• وضعا التدوير والقفل (LOCK): أدر العجلة لوضع أي مقام في الأعلى أو اقفل مقام C عند الساعة 12.

صيغ درجات السلم وامتدادات الكورد السابع
• عرض فوري لدرجات السلم الموسيقي للكورد المحدد (مثل I = 1 - 3 - 5، IV = 4 - 6 - 8) مع أسماء النغمات.
• التبديل الفوري إلى الكوردات السباعية (maj7, m7, dom7, m7b5) بلمسة واحدة.

مواضع الجيتار CAGED وتوليد صوت الجيتار الواقعي
• ثلاث وضعيات لكل كورد على الرقبة: الأشكال المفتوحة/CAGED، وباريه الوتر السادس (شكل E)، وباريه الوتر الخامس (شكل A) مع أرقام الأصابع (1–4).
• محرك صوتي فيزيائي بتردد 44.1 كيلوهرتز (Karplus-Strong) يعزف الكورد عند لمس العجلة أو تمرير الإصبع على مخطط الرقبة.

مكرر كوردات من 4 موازير وقوالب جاهزة
• أنشئ تتابعات كوردات من 4 موازير أو اختر من القوالب الكلاسيكية (بوب، جاز، بلوز، كادينزا أندلسية) مع تغيير الطبقة إلى أي من الـ 12 مقاماً.
• يدعم الوضعين العمودي والأفقي، يعمل 100% بدون إنترنت، بدون إعلانات أو أذونات.
```

---

## Language: `hi-IN` — Hindi / हिन्दी

- **`languageCode`**: `hi-IN`
- **`appName`** (29 / 30 characters): ChordWheel: सर्कल ऑफ़ फ़िफ़्थ
- **`shortDescription`** (72 / 80 characters): इंटरैक्टिव सर्कल ऑफ़ फ़िफ़्थ्स, CAGED गिटार कॉर्ड्स और 4-बार कॉर्ड लूपर।
- **`fullDescription`** (1565 / 4000 characters):

```text
ChordWheel: सर्कल ऑफ़ फ़िफ़्थ्स और गिटार लूपर के साथ एक ही इंटरैक्टिव स्क्रीन पर म्यूज़िक थ्योरी, सॉन्गराइटिंग हार्मनी और गिटार कॉर्ड वॉइसिंग में महारत हासिल करें। गिटारवादकों, गीतकारों, संगीत छात्रों और शिक्षकों के लिए डिज़ाइन किया गया यह ऐप क्लासिक सर्कल ऑफ़ फ़िफ़्थ्स को एक रियल-टाइम वाद्ययंत्र और हार्मोनिक वर्कस्टेशन में बदल देता है।

सर्कल ऑफ़ फ़िफ़्थ्स हार्मोनिक व्हील
• बाहरी मेजर रिंग: सभी 12 मेजर कीज़ (C, G, D, A, E, B, F#, Db, Ab, Eb, Bb, F) और मुख्य डायटोनिक मेजर कॉर्ड्स (I, IV, V) दिखाता है।
• आंतरिक रिलेटिव माइनर रिंग: प्रत्येक की (Key) के सापेक्ष माइनर कॉर्ड्स (ii, iii, vi) को हाइलाइट करता है।
• डिमिनिश्ड केंद्र और रोमन अंक: vii° कॉर्ड और सभी कॉर्ड्स के रोमन फ़ंक्शन (I, ii, iii, IV, V, vi, vii°) स्पष्ट रूप से दर्शाता है।
• रोटेट और लॉक (LOCK) मोड: किसी भी स्केल को 12 बजे के स्थान पर घुमाएं या C को ऊपर लॉक रखें।

स्केल-डिग्री फ़ॉर्मूला और सेवंथ कॉर्ड्स (7THS)
• चयनित कॉर्ड की सटीक स्केल डिग्री (जैसे I = 1 - 3 - 5, IV = 4 - 6 - 8, V7 = 5 - 7 - 9 - 11) और स्वर नाम तुरंत देखें।
• एक टैप में ट्रायड को सेवंथ कॉर्ड्स (maj7, m7, dom7, m7b5) में बदलें।

CAGED गिटार फ़्रेटबोर्ड डायग्राम और रियल-टाइम ऑडियो
• प्रत्येक कॉर्ड के लिए 3 नेक पोज़िशन: ओपन/CAGED, E-शेप बार कॉर्ड और A-शेप बार कॉर्ड, उंगलियों के नंबर (1–4) के साथ।
• 44.1 kHz Karplus-Strong फ़िज़िकल मॉडलिंग गिटार साउंड इंजन के साथ तुरंत स्ट्रम सुनें।

4-बार कॉर्ड लूपर और प्रीसेट प्रोग्रेशन
• 4-बार कॉर्ड सीक्वेंसर में पॉप, जैज़, ब्लूज़ और आंदालुसियन प्रोग्रेशन लोड करें और सभी 12 कीज़ में ट्रांसपोज़ करें।
• पोर्ट्रेट और लैंडस्केप लेआउट, 100% ऑफ़लाइन, शून्य अनुमतियां और विज्ञापन-मुक्त।
```

---

## Language: `bn-BD` — Bangla (Bengali) / বাংলা

- **`languageCode`**: `bn-BD`
- **`appName`** (28 / 30 characters): ChordWheel: সার্কেল অফ ফিফথস
- **`shortDescription`** (69 / 80 characters): ইন্টারেক্টিভ সার্কেল অফ ফিফথস, CAGED গিটার কর্ড এবং ৪-বার কর্ড লুপার।
- **`fullDescription`** (1431 / 4000 characters):

```text
ChordWheel: সার্কেল অফ ফিফথস ও গিটার লুপার অ্যাপের মাধ্যমে একটিমাত্র ইন্টারেক্টিভ স্ক্রিনে মিউজিক থিওরি, গান লেখার হারমনি এবং গিটার কর্ড ভয়েসিং আয়ত্ত করুন। গিটারিস্ট, গীতিকার, সঙ্গীত শিক্ষার্থী এবং শিক্ষকদের জন্য তৈরি এই অ্যাপটি ক্লাসিক সার্কেল অফ ফিফথসকে একটি রিয়েল-টাইম বাদ্যযন্ত্রে রূপান্তরিত করে।

কনসেন্ট্রিক সার্কেল অফ ফিফথস চাকা
• বাইরের মেজর রিং: ১২টি মেজর কী (C থেকে F) এবং প্রধান ডায়াটনিক মেজর কর্ড (I, IV, V) প্রদর্শন করে।
• ভেতরের রিলেটিভ মাইনর রিং: প্রতিটি মেজর কী-এর সাথে সম্পর্কিত মাইনর কর্ড (ii, iii, vi) তুলে ধরে।
• ডিমিনিশড কেন্দ্র ও রোমান সংখ্যা: vii° কর্ড এবং প্রতিটি কর্ডের রোমান ফাংশন (I, ii, iii, IV, V, vi, vii°) স্পষ্টভাবে দেখায়।
• রোটেট এবং লক (LOCK) মোড: চাকা ঘুরিয়ে যেকোনো কী উপরে আনুন অথবা C-কে উপরে লক করে রাখুন।

স্কেল-ডিগ্রি ফর্মুলা ও সেভেন্থ কর্ড (7THS)
• নির্বাচিত কর্ডের স্কেল ডিগ্রি (যেমন I = 1 - 3 - 5, IV = 4 - 6 - 8) এবং নোটের নাম তাৎক্ষণিক দেখুন।
• এক ট্যাপে যেকোনো ট্রায়াডকে সেভেন্থ কর্ডে (maj7, m7, dom7, m7b5) রূপান্তর করুন।

CAGED গিটার ফ্রেটবোর্ড ও রিয়েল-টাইম গিটার সাউন্ড
• প্রতিটি কর্ডের জন্য ৩টি পজিশন: ওপেন/CAGED, ৬ষ্ঠ তারের E-ব্যারে এবং ৫ম তারের A-ব্যারে (আঙুলের নম্বর ১–৪ সহ)।
• ৪৪.১ kHz Karplus-Strong গিটার সিন্থেসিস ইঞ্জিনের মাধ্যমে বাস্তবসম্মত গিটার স্ট্রামিং শুনুন।

৪-বার কর্ড লুপার ও প্রিসেট প্রগ্রেশন
• ৪-বার সিকোয়েন্সারে পপ, জ্যাজ, ব্লুজ প্রগ্রেশন লোড করুন এবং ১২টি কী-তে ট্রান্সপোজ করুন।
• পোর্ট্রেট ও ল্যান্ডস্কেপ মোড, ১০০% অফলাইন, কোনো অনুমতি বা বিজ্ঞাপন নেই।
```

---

## Language: `id` — Indonesian / Bahasa Indonesia

- **`languageCode`**: `id`
- **`appName`** (27 / 30 characters): ChordWheel: Lingkaran Kuint
- **`shortDescription`** (71 / 80 characters): Lingkaran Kuint interaktif, akor gitar CAGED, dan looper akor 4 birama.
- **`fullDescription`** (1773 / 4000 characters):

```text
Kuasai teori musik, harmoni penulisan lagu, dan posisi akor gitar dalam satu layar interaktif dengan ChordWheel: Lingkaran Kuint & Looper Gitar. Dirancang untuk gitaris, penulis lagu, pelajar musik, dan pengajar, ChordWheel mengubah Lingkaran Kuint (Circle of Fifths) klasik menjadi instrumen musik interaktif secara real-time.

RODA HARMONI LINGKARAN KUINT KONSENTRIS
• Cincin Mayor Luar: Menampilkan 12 tangga nada Mayor yang disusun berdasarkan kuint (C, G, D, A, E, B, F#, Db, Ab, Eb, Bb, F) serta menyoroti akor mayor diatonis utama (I, IV, dan V).
• Cincin Minor Relatif Dalam: Menyelaraskan setiap nada dasar dengan minor relatifnya dan menyoroti akor minor diatonis (ii, iii, dan vi).
• Sektor Diminished Tengah & Angka Romawi: Menandai akor diminished (vii°) dan fungsi angka Romawi (I, ii, iii, IV, V, vi, vii°).
• Mode PUTAR dan KUNCI (LOCK): Putar roda untuk menempatkan nada dasar apa pun di posisi jam 12, atau kunci C di bagian atas.

FORMULA DERAJAT SKALA & EKSTENSI AKOR TUJUH (7THS)
• Menampilkan derajat skala akor terhadap nada dasar aktif (mis. I = 1 - 3 - 5, IV = 4 - 6 - 8) beserta nama nada.
• Ubah triad menjadi akor seventh (maj7, m7, dom7, m7b5) hanya dengan satu ketukan.

POSISI GITAR CAGED & SINTESIS KARPLUS-STRONG
• Tiga Posisi Fretboard per Akor: Beralih antara bentuk Open/CAGED, akor gantung bentuk E (senar 6), dan bentuk A (senar 5) lengkap dengan nomor jari (1–4).
• Sintesis Gitar Akustik 44,1 kHz: Ketuk akor atau usap diagram fretboard untuk mendengar genjrengan gitar 6 senar secara real-time.

LOOPER 4 BIRAMA & PRESET PROGRESI LAGU
• Susun progresi 4 birama atau muat preset Pop I–V–vi–IV, Jazz ii–V–I–VI, Andalusia, dan Blues di semua 12 nada dasar.
• Layar Potret & Lanskap, 100% offline, tanpa izin perangkat, dan tanpa iklan.
```

---

## Language: `tr-TR` — Turkish / Türkçe

- **`languageCode`**: `tr-TR`
- **`appName`** (28 / 30 characters): ChordWheel: Beşliler Çemberi
- **`shortDescription`** (77 / 80 characters): Etkileşimli Beşliler Çemberi, CAGED gitar akorları ve 4 ölçülük akor döngüsü.
- **`fullDescription`** (976 / 4000 characters):

```text
ChordWheel: Beşliler Çemberi ve Gitar Looper ile müzik teorisini, şarkı yazımı armonisini ve gitar akor pozisyonlarını tek bir etkileşimli ekranda öğrenin. Gitaristler, besteciler ve müzik öğrencileri için tasarlanan ChordWheel, klasik Beşliler Çemberini gerçek zamanlı çalınabilir bir enstrümana dönüştürür.

ÖNE ÇIKAN ÖZELLİKLER
• Eşmerkezli Beşliler Çemberi: Dış halkada 12 Majör ton (I, IV, V), iç halkada ilgili minör akorlar (ii, iii, vi) ve merkezde eksik akor (vii°) ile Roma rakamı fonksiyonları.
• Döndürme ve Kilitleme (LOCK) modları, gerçek zamanlı dizi derecesi formülleri (1 - 3 - 5, 4 - 6 - 8) ve tek dokunuşla 7'li akorlar (maj7, m7, dom7, m7b5).
• Her akor için 3 CAGED gitar klavye pozisyonu (Açık/CAGED, E-tipi Bare, A-tipi Bare), parmak numaraları (1–4) ve 44,1 kHz Karplus-Strong gerçekçi gitar sesi.
• 4 ölçülük akor döngüleyici (Looper) ve Pop, Caz, Blues hazır akor yürüyüşleri.
• Dikey ve Yatay ekran desteği, %100 çevrimdışı, sıfır izin ve reklamsız.
```

---

## Language: `vi` — Vietnamese / Tiếng Việt

- **`languageCode`**: `vi`
- **`appName`** (24 / 30 characters): ChordWheel: Vòng Bậc Năm
- **`shortDescription`** (78 / 80 characters): Vòng bậc năm tương tác, thế bấm hợp âm guitar CAGED và bộ lặp hợp âm 4 ô nhịp.
- **`fullDescription`** (793 / 4000 characters):

```text
Làm chủ nhạc lý, hòa âm sáng tác và thế bấm hợp âm guitar trên một màn hình tương tác duy nhất với ChordWheel: Vòng Bậc Năm & Guitar Looper.

TÍNH NĂNG NỔI BẬT
• Vòng Bậc Năm Đồng Tâm: Vòng ngoài hiển thị 12 giọng Trưởng (I, IV, V), vòng trong hiển thị giọng Thứ tương đương (ii, iii, vi), tâm hiển thị hợp âm giảm (vii°) và ký hiệu bậc La Mã.
• Công thức bậc âm giai thời gian thực (1 - 3 - 5, 4 - 6 - 8) và mở rộng hợp âm bậc 7 (maj7, m7, dom7, m7b5) chỉ với một chạm.
• 3 thế bấm CAGED trên cần đàn cho mỗi hợp âm (Mở/CAGED, chặn thế Mi dây 6, chặn thế La dây 5) cùng âm thanh guitar Karplus-Strong 44,1 kHz chân thực.
• Bộ lặp 4 ô nhịp (Looper) và các vòng hòa thanh Pop, Jazz, Blues cổ điển chuyển giọng tức thì.
• Hỗ trợ màn hình Dọc & Ngang, hoạt động 100% ngoại tuyến, không quảng cáo.
```

---

## Language: `pl-PL` — Polish / Polski

- **`languageCode`**: `pl-PL`
- **`appName`** (25 / 30 characters): ChordWheel: Koło Kwintowe
- **`shortDescription`** (77 / 80 characters): Interaktywne koło kwintowe, chwyty gitarowe CAGED i 4-taktowy looper akordów.
- **`fullDescription`** (798 / 4000 characters):

```text
Opanuj teorię muzyki, harmonię i chwyty gitarowe na jednym interaktywnym ekranie dzięki ChordWheel: Koło Kwintowe i Looper Gitarowy.

GŁÓWNE FUNKCJE
• Koncentryczne Koło Kwintowe: Zewnętrzny pierścień 12 tonacji durowych (I, IV, V), wewnętrzny pierścień równoległych tonacji molowych (ii, iii, vi), środkowy sektor zmniejszony (vii°) i oznaczenia rzymskie.
• Formuły stopni skali na żywo (1 - 3 - 5, 4 - 6 - 8) oraz przełącznik akordów septymowych (maj7, m7, dom7, m7b5).
• 3 pozycje CAGED na gryfie dla każdego akordu (Otwarte/CAGED, barré typu E i barré typu A) z numeracją palców (1–4) i syntezą gitary Karplus-Strong 44,1 kHz.
• 4-taktowy looper akordów z gotowymi progresjami Pop, Jazz, Blues i transpozycją do 12 tonacji.
• Układ pionowy i poziomy, 100% offline, zero uprawnień i brak reklam.
```

---

## Language: `nl-NL` — Dutch / Nederlands

- **`languageCode`**: `nl-NL`
- **`appName`** (25 / 30 characters): ChordWheel: Kwintencirkel
- **`shortDescription`** (75 / 80 characters): Interactieve kwintencirkel, CAGED-gitaarakkoorden en 4-maats akkoordlooper.
- **`fullDescription`** (763 / 4000 characters):

```text
Beheers muziektheorie, harmonieleer en gitaarakkoorden op één interactief scherm met ChordWheel: Kwintencirkel & Gitaarlooper.

BELANGRIJKSTE KENMERKEN
• Concentrische Kwintencirkel: Buitenste ring voor alle 12 majeurtoonsoorten (I, IV, V), binnenste ring voor parallelle mineurakkoorden (ii, iii, vi), verminderd middensegment (vii°) en Romeinse cijfers.
• Realtime toonladderformules (1 - 3 - 5, 4 - 6 - 8) en septiemakkoorden (maj7, m7, dom7, m7b5) met één tik.
• 3 CAGED-posities per akkoord (Open/CAGED, E-barré en A-barré) met vingerzettingen (1–4) en 44,1 kHz Karplus-Strong gitaarsynthese.
• 4-maats akkoordsequencer met Pop-, Jazz- en Blues-presets in alle 12 toonsoorten.
• Portret- en landschapsmodus, 100% offline, geen rechten vereist en reclamevrij.
```

---

## Language: `th` — Thai / ไทย

- **`languageCode`**: `th`
- **`appName`** (23 / 30 characters): ChordWheel: วงกลมคู่ห้า
- **`shortDescription`** (67 / 80 characters): วงกลมคู่ห้าแบบโต้ตอบ ฟอร์มคอร์ดกีตาร์ CAGED และลูปเปอร์คอร์ด 4 ห้อง
- **`fullDescription`** (620 / 4000 characters):

```text
เชี่ยวชาญทฤษฎีทางดนตรี การแต่งเพลง และฟอร์มคอร์ดกีตาร์ในหน้าจอเดียวด้วย ChordWheel: วงกลมคู่ห้า (Circle of Fifths) & กีตาร์ลูปเปอร์

คุณสมบัติหลัก
• วงกลมคู่ห้าแบบซ้อนวง: วงนอกแสดง 12 คีย์เมเจอร์ (I, IV, V) วงในแสดงคีย์ไมเนอร์สัมพันธ์ (ii, iii, vi) และตรงกลางแสดงคอร์ดดิมินิช (vii°) พร้อมตัวเลขโรมัน
• แสดงสูตรลำดับขั้นสเกล (1 - 3 - 5, 4 - 6 - 8) และสลับคอร์ดเซเว่น (maj7, m7, dom7, m7b5) ได้ทันที
• ฟอร์มคอร์ดกีตาร์ CAGED 3 ตำแหน่งต่อคอร์ด พร้อมหมายเลขนิ้ว (1–4) และเสียงกีตาร์สังเคราะห์ Karplus-Strong 44.1 kHz
• ลูปเปอร์คอร์ด 4 ห้องพร้อมพรีเซ็ต Pop, Jazz, Blues เปลี่ยนคีย์ได้ทั้ง 12 คีย์ ทำงานออฟไลน์ 100% ไม่มีโฆษณา
```

---

## Language: `uk` — Ukrainian / Українська

- **`languageCode`**: `uk`
- **`appName`** (27 / 30 characters): ChordWheel: Кварто-квінтове
- **`shortDescription`** (77 / 80 characters): Інтерактивне кварто-квінтове коло, акорди CAGED для гітари та лупер 4 тактів.
- **`fullDescription`** (736 / 4000 characters):

```text
Опануйте теорію музики, гармонію та аплікатури гітарних акордів на одному інтерактивному екрані з ChordWheel: Кварто-квінтове коло та гітарний лупер.

ОСНОВНІ МОЖЛИВОСТІ
• Концентричне кварто-квінтове коло: Зовнішнє мажорне кільце (I, IV, V), внутрішнє кільце паралельного мінору (ii, iii, vi), зменшений сектор (vii°) та римські цифри.
• Формули ступенів гами в реальному часі (1 - 3 - 5, 4 - 6 - 8) та перемикання на септакорди (maj7, m7, dom7, m7b5).
• 3 позиції CAGED на грифі для кожного акорду з пальцівкою (1–4) та фізичним синтезом гітари Karplus-Strong 44,1 кГц.
• 4-тактовий лупер акордів із класичними прогресіями Pop, Jazz, Blues у всіх 12 тональностях.
• Портретний і альбомний режими, 100% офлайн, без реклами та дозволів.
```

---
