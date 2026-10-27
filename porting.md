# PowKiddy V90 (C / SDL1.2) to Android Porting Guide

This document records the exact step-by-step methodology used to translate the PowKiddy V90 C/SDL1.2 **Circle of Fifths** application (`~/xkdr/a/powkiddy-v90/chordwheel/chordwheel.c`) into a modern, Play-Store-ready Android application ([`chordwheel/`](chordwheel/)).

It serves both as an architectural record of the `chordwheel` port and as a reusable engineering playbook for porting other PowKiddy V90 music and utility apps (such as `licktrainer`, `pocketband`, `sightread`, and `metronome`) to Android.

---

## Step 1: Project Scaffolding, Build System & Play Store Signing

Using `~/xkdr/a/sound-sleep` as the canonical template, each Android app is structured as a zero-dependency Gradle project targeting modern Google Play Store requirements while remaining compatible with older devices:

1. **Gradle & SDK Configuration** ([`chordwheel/build.gradle`](chordwheel/build.gradle#L1-L63)):
   - **Android Gradle Plugin**: `8.9.0` with **Gradle `8.14`** ([`chordwheel/gradle/wrapper/gradle-wrapper.properties`](chordwheel/gradle/wrapper/gradle-wrapper.properties)).
   - **API Levels**:
     - `minSdk = 19` (Android 4.4 KitKat — runs on virtually 100% of active Android devices).
     - `compileSdk = 36` and `targetSdk = 36` (Android 15/16 — satisfies current Google Play Console target API enforcement).
   - **Source Sets**: Flat directory layout (`src/` for Java sources, `res/` for Android resources, `test/` for JUnit 4 unit tests, and root `AndroidManifest.xml`).
2. **Automated Release Signing** ([`chordwheel/build.gradle` lines 5–9 & 35–42](chordwheel/build.gradle#L5-L42)):
   - Reads keystore path, alias, and passwords from [`chordwheel/ant.properties`](chordwheel/ant.properties) (`release-keystore.key`).
   - Running `./gradlew assembleRelease bundleRelease` produces both a signed `.apk` (`build/outputs/apk/release/`) and a signed Play Store Android App Bundle `.aab` (`build/outputs/bundle/release/`).
3. **Zero-Permission Non-Fullscreen Manifest & Selective Screen Wake Policy** ([`chordwheel/AndroidManifest.xml`](chordwheel/AndroidManifest.xml#L1-L20)):
   - Declares **zero `<uses-permission>` tags** (no network, storage, microphone, or advertising ID permissions).
   - Uses `@android:style/Theme.DeviceDefault.NoActionBar` (not `.Fullscreen`) so the top Android status bar (clock, battery, notifications) remains visible at all times, wrapped in a `FrameLayout` that applies `WindowInsets.Type.systemBars()` padding on Android 11+ (`API 30+`) so edge-to-edge mode (`targetSdk = 36`) never draws the custom `Canvas` behind the status bar.
   - **Selective Screen Timeout (`FLAG_KEEP_SCREEN_ON`)**: Only continuous hands-free playback apps (**`metronome`** and **`pocketband`**) set `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`. All other apps (**`chordwheel`**, **`fretboard`**, **`licktrainer`**, **`eartrainer`**, **`pomodoro`**, and **`sightread`**) omit `FLAG_KEEP_SCREEN_ON` and respect the user's normal device screen timeout.
   - Sets `android:configChanges="orientation|screenSize|keyboardHidden|keyboard|navigation"` on the Activity so rotating the device between Portrait and Landscape dynamically reflows the custom `View` on the fly without destroying the Activity or interrupting real-time audio synthesis.

---

## Step 2: Decomposing Monolithic C/SDL1.2 into Modular Java

PowKiddy V90 apps are typically written as a single self-contained `.c` file combining domain logic, `SDL_Surface` software pixel rendering, `SDL_Event` button polling, and an `SDL_OpenAudio` PCM callback.

For Android, split the monolithic C file into four clean layers (illustrated by [`chordwheel/src/com/eggwall/chordwheel/`](chordwheel/src/com/eggwall/chordwheel/)):

| PowKiddy V90 C Layer (`chordwheel.c`) | Android Java Equivalent | Key Translation Changes |
| :--- | :--- | :--- |
| **`const` structs & pure helper functions** (`WHEEL[]`, `OPEN_MAJ[][]`, `resolve_chord_at()`, `build_guitar_voicing()`) | **[`MusicTheory.java`](chordwheel/src/com/eggwall/chordwheel/MusicTheory.java#L20-L465)** | Pure, stateless Java class with zero Android UI dependencies so 100% of the music theory and voicing math can be unit-tested on a host JVM via JUnit 4. |
| **`SDL_OpenAudio` callback** (`22050 Hz` 16-bit mono PCM callback mixing Karplus-Strong delay lines) | **[`GuitarSynthEngine.java`](chordwheel/src/com/eggwall/chordwheel/GuitarSynthEngine.java#L26-L261)** | Dedicated high-priority background thread (`Thread.MAX_PRIORITY - 1`) streaming **44.1 kHz** 16-bit mono PCM into an Android `AudioTrack` (`MODE_STREAM`, 256-frame low-latency chunks). Added individual string plucking ([`pluckSingleString()`](chordwheel/src/com/eggwall/chordwheel/GuitarSynthEngine.java#L101-L106)) alongside full-chord strumming ([`triggerStrum()`](chordwheel/src/com/eggwall/chordwheel/GuitarSynthEngine.java#L89-L110)). |
| **Fixed `320×240` `RGB565` pixel rasterizer & `SDL_PollEvent` loop** | **[`ChordWheelView.java`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L37-L1411)** | Hardware-accelerated `android.view.View` subclass drawing anti-aliased vector `Path`s, rounded cards/pills, and crisp text scaled by a virtual coordinate unit `u`, plus `onTouchEvent()` and `onKeyDown()`. |
| **`main()` + `/mnt/FunKey/.chordwheel.cfg` file persistence** | **[`ChordWheelActivity.java`](chordwheel/src/com/eggwall/chordwheel/ChordWheelActivity.java#L18-L120)** + **`SharedPreferences`** | Manages Android lifecycle (`onCreate`, `onResume`, `onPause`), visible status-bar window insets (`wrapWithStatusBarInsets`), and private sandboxed state persistence ([`loadState()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L184-L201) / [`saveState()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L203-L218)). |

---

## Step 3: Translating Handheld Hardware Controls to Touch & Gestures

On the PowKiddy V90, all interaction happens via physical buttons (`DPAD`, `A`, `B`, `X`, `Y`, `L1`, `R1`, `SELECT`, `START`). On Android, every hardware button is translated into a direct, discoverable touch affordance in [`ChordWheelView.onTouchEvent()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L1100-L1271) while preserving the original keybindings in [`ChordWheelView.onKeyDown()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L1317-L1410) for Bluetooth/USB controllers and Chromebooks:

1. **Wheel Navigation (`DPAD Left/Right/Up/Down`) → Direct Polar Touch & Drag**:
   - In [`handleWheelTouch()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L1288-L1311), touch coordinates `(x, y)` relative to `(mWheelCx, mWheelCy)` are converted to polar radius `r = Math.hypot(dx, dy)` and angle `deg = Math.toDegrees(Math.atan2(dy, dx)) + 90.0 + 15.0`.
   - Tapping or dragging smoothly around the wheel selects the corresponding 30° sector (`0..11`) and concentric ring (`0 = Outer Major`, `1 = Inner Minor`, `2 = Center vii° Hub`).
2. **Chord Strum (`A` Button) → Multi-Modal Fretboard Touch + Speaker Icon**:
   - **Finger-Drag Across Strings**: Dragging horizontally across the fretboard grid (`mFretGridRect`) detects each crossed string via [`findNearestString(x)`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L1273-L1286) and plucks that individual string in real time ([`pluckSingleString()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L279-L289)), illuminating the vibrating string in gold for 220 ms.
   - **Single Tap on Strings or Speaker Button**: Tapping the fretboard or the dedicated green vector **Speaker button** ([`drawSpeakerButton()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L827-L857)) triggers a 22 ms pick-staggered full-chord strum.
3. **Voicing Cycle (`L1 / R1` Shoulder Buttons) → Header Tap or Horizontal Swipe**:
   - Tapping the `< 1/3 Open/CAGED >` header bar (`mVoicingBarRect`) cycles forward; swiping left/right across the fretboard card cycles forward or backward between `Open/CAGED`, `E-Barre (6str)`, and `A-Barre (5str)`.
4. **Mode Toggles (`B`, `SELECT`, `X`, `Y`) → Dedicated On-Screen Pill Buttons**:
   - **`B` (Triads / 7ths)** → Tappable **`[TRIADS]` / `[7THS]`** pill in the Chord Info card (`mSeventhPillRect`).
   - **`SELECT` (Key Lock)** → Tappable **`[LOCK]`** pill in the top-right corner of the wheel pane (`mLockModePillRect`).
   - **`X` & `Y` (Progression Looper & Presets)** → Bottom progression bar with **`[> LOOP]` / `[|| PAUSE]`**, **`[+PROG]`**, 4 directly tappable chord slots (`mProgSlotRects[0..3]`), and **`[PRESETS]`**.

---

## Step 4: Iterative UI, Layout & Music-Theory Refinements for Mobile Screens

Unlike the V90's fixed `320×240` (4:3 landscape) LCD, Android phones and tablets have wide aspect ratios (~20:9) and are used in both **Portrait** and **Landscape**. We iterated on the UI using live screenshots from an Android emulator:

1. **Responsive Dual-Orientation Layout** ([`drawLandscapeLayout()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L421-L447) & [`drawPortraitLayout()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L449-L479)):
   - **Portrait Mode**: Full-width Circle of Fifths wheel on top, stacked Chord Info + Fretboard cards in the middle, and full-width 4-Bar Progression Looper bar at the bottom.
   - **Landscape Mode**: Placed the 4-Bar Progression Looper bar inside the bottom of the **right pane** (below the Chord Info and Fretboard cards) rather than across the full screen width, allowing the Circle of Fifths wheel on the **left pane** to expand to 100% of the screen height.
2. **Maximized Full-Width Wheel & Corner Controls** ([`drawWheelHeaderAndLegend()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L481-L530)):
   - Removed the `"CIRCLE OF FIFTHS"` title banner so it neither looked like a button nor consumed vertical space above the wheel.
   - Tucked the clickable **`[?]`** help pill into the top-left corner and the **`[LOCK]`** pill into the top-right corner of the wheel pane (sitting in the empty corners outside the circle's perimeter), allowing the wheel radius (`maxRadius = Math.min(paneW, wheelAvailH) * 0.495f`) to span edge-to-edge across the full width.
3. **Removed Redundant Diatonic Strip & Added Compact Speaker Button**:
   - Removed the 7-column `DIATONIC (C MAJ)` strip that was needed on the non-touch V90 (since users can tap chords directly on the touch wheel), giving that space to the wheel and fretboard.
   - Moved the strum trigger to a compact green vector **Speaker icon** on the right side of the fretboard diagram.
4. **Added In-App `"?"` How-It-Works Help Modal** ([`drawGuideModal()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L986-L1055)):
   - Tapping the top-left **`[?]`** pill opens a 5-card modal guide explaining the wheel rings, `ROTATE` vs. `LOCK`, scale-degree numbers, guitar voicings, and the 4-bar looper.
5. **Added Active-Key Scale-Degree Chord Composition (`1 - 3 - 5`, `4 - 6 - 8`)** ([`MusicTheory.formatScaleDegreeTone()`](chordwheel/src/com/eggwall/chordwheel/MusicTheory.java#L249-L257)):
   - Enhanced the Chord Info card to display each chord's scale-degree composition relative to the **active key** alongside the spelled note names:
     - In Key of C, `C Major` (`I`) displays `C - E - G (1 - 3 - 5)`.
     - In Key of C, `F Major` (`IV`) displays `F - A - C (4 - 6 - 8)` (not `1 - 3 - 5`).
     - In Key of C, `G7` (`V7`) displays `G - B - D - F (5 - 7 - 9 - 11)`.
     - Non-diatonic chords display exact chromatic alterations relative to the active key (e.g., `D Major` `V/V` = `2 - #4 - 6`, borrowed `Fm` `iv` = `4 - b6 - 8`).

---

## Step 5: Emulator Verification on a Cloudtop (ACID Remote Emulator)

Because Google Cloudtops do not expose nested `/dev/kvm` hardware virtualization for local QEMU emulators, use the **ACID remote emulator** workflow for instant visual inspection and screenshot capture:

1. **Lease an Android Emulator**:
   ```bash
   acid lease PIXEL_8:35
   ```
   This provisions a remote hardware-accelerated Android 15 (`API 35`) Pixel 8 device and forwards `adb` to `localhost:<port>`.
2. **Add Programmatic Demo-State Intent Hooks** ([`ChordWheelActivity.handleIntentExtras()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelActivity.java#L47-L68) & [`ChordWheelView.configureDemoState()`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L221-L242)):
   - Allow `adb shell am start` extras (`demo_state`, `key_sector`, `cur_sector`, `cur_ring`, `key_locked`, `seventh_mode`, `voicing_idx`, `loop_playing`, `show_help`, `show_guide`) to jump directly to any state for deterministic screenshot capture:
   ```bash
   ~/Android/Sdk/platform-tools/adb -s localhost:26507 shell am start \
     -n com.eggwall.chordwheel/.ChordWheelActivity \
     --ez demo_state true --ei key_sector 0 --ei cur_sector 11 --ez key_locked true
   ~/Android/Sdk/platform-tools/adb -s localhost:26507 exec-out screencap -p > screenshot.png
   ```

---

## Step 6: Android Adaptive, Circular & Play Store Icon Suite

Modern Android launchers (Android 8.0+ Adaptive Icons) and Google Play Store guidelines require icons that work cleanly inside circular, squircle, and rounded-square masks:

1. **Adaptive Icon XMLs (`API 26+`)**:
   - [`chordwheel/res/drawable-anydpi-v26/ic_launcher.xml`](chordwheel/res/drawable-anydpi-v26/ic_launcher.xml) and [`ic_launcher_round.xml`](chordwheel/res/drawable-anydpi-v26/ic_launcher_round.xml) combine `@color/ic_launcher_background` (`#0B101B` in [`chordwheel/res/values/colors.xml`](chordwheel/res/values/colors.xml)) with `@drawable/ic_launcher_foreground`.
   - The foreground Circle of Fifths graphic (`108×108 dp`, rendered at `108, 162, 216, 324, 432 px`) is centered inside the inner **66dp circular safe zone** so no launcher mask ever clips the outer ring.
2. **Legacy & Round Multi-Density Icons (`mdpi`–`xxxhdpi`)**:
   - Generated `ic_launcher.png` and `ic_launcher_round.png` at `48, 72, 96, 144, 192 px` across `res/drawable-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/`.
3. **Google Play Store High-Res 512×512 Icon**:
   - Saved at [`chordwheel/playstore-icon-512.png`](chordwheel/playstore-icon-512.png).

---

## Step 7: Automated JUnit 4 Unit Tests

Adding a host-side JUnit 4 suite ([`chordwheel/test/com/eggwall/chordwheel/MusicTheoryTest.java`](chordwheel/test/com/eggwall/chordwheel/MusicTheoryTest.java#L18-L229), executed via `./gradlew testDebugUnitTest`) verified the entire music-theory engine in ~1 second and caught a subtle voicing bug inherited from the C port:

1. **What We Tested**:
   - Clockwise perfect-fifth intervals (`+7` semitones), relative minor (`+9`), and leading-tone diminished (`+11`) across all 12 sectors ([`testCircleOfFifthsSectorIntervals()`](chordwheel/test/com/eggwall/chordwheel/MusicTheoryTest.java#L21-L39)).
   - Active-key scale-degree numbers for all 7 diatonic triads (`1 - 3 - 5`, `2 - 4 - 6`, `3 - 5 - 7`, `4 - 6 - 8`, `5 - 7 - 9`, `6 - 8 - 10`, `7 - 9 - 11`), 7th chords (`1 - 3 - 5 - 7`, `5 - 7 - 9 - 11`, `7 - 9 - 11 - 13`), key transpositions, and chromatic chords ([`testScaleDegreeNumbersInKeyOfC()`](chordwheel/test/com/eggwall/chordwheel/MusicTheoryTest.java#L42-L96), [`testSeventhChordScaleDegreeNumbersAndTransposition()`](chordwheel/test/com/eggwall/chordwheel/MusicTheoryTest.java#L99-L147)).
   - **Exhaustive verification of all 252 guitar voicings** (`12 roots × 7 qualities × 3 voicings`) ensuring every unmuted string produces a valid chord pitch class ([`testAll252GuitarVoicingsProduceValidChordTones()`](chordwheel/test/com/eggwall/chordwheel/MusicTheoryTest.java#L150-L190)).
   - Validation of all 5 progression presets across all 12 keys ([`testProgressionPresetsAreValid()`](chordwheel/test/com/eggwall/chordwheel/MusicTheoryTest.java#L193-L212)).
2. **Bug Caught & Fixed by the Unit Test**:
   - `testAll252GuitarVoicingsProduceValidChordTones()` caught that the open/barre shapes for `QUAL_DIM` and `QUAL_M7B5` had an unadjusted B-string fret offset that sounded the `6th` (`bb7`) instead of `b3` or `b7`. Fixing those fret offsets in [`MusicTheory.buildGuitarVoicing()`](chordwheel/src/com/eggwall/chordwheel/MusicTheory.java#L289-L365) brought all 252 voicings to 100% chord-tone accuracy.

---

## Step 8: Documentation Suite with Cropped Visual Segments & Privacy Policy

Finally, every ported Android app should include four Markdown documents in its project directory with cropped screenshot segments (`docs/images/seg_*.png`) and relative source-code links:

1. **Musician's User Guide** ([`chordwheel/USER_GUIDE.md`](chordwheel/USER_GUIDE.md)): Visual guide to every screen control paired with structured musicianship workouts.
2. **Engineering & Customization Guide** ([`chordwheel/DEVELOPER_GUIDE.md`](chordwheel/DEVELOPER_GUIDE.md)): Visual map of UI segments to Java methods/line numbers and step-by-step recipes for modifying presets, tunings, chord qualities, synth parameters, and layouts.
3. **Privacy Policy** ([`chordwheel/PRIVACY_POLICY.md`](chordwheel/PRIVACY_POLICY.md)): Play-Store-ready zero-data-collection policy with direct relative links to [`AndroidManifest.xml`](chordwheel/AndroidManifest.xml), [`build.gradle`](chordwheel/build.gradle), [`ChordWheelView.java`](chordwheel/src/com/eggwall/chordwheel/ChordWheelView.java#L184-L218), and [`GuitarSynthEngine.java`](chordwheel/src/com/eggwall/chordwheel/GuitarSynthEngine.java#L174-L260).
4. **Project README** ([`chordwheel/README.md`](chordwheel/README.md)): Entry point linking the guides, feature summary, and `./gradlew` build/test/sign commands.
