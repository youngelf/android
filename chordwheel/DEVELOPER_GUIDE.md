# Circle of Fifths — Engineering & Customization Guide

This document is for software engineers who want to understand, modify, extend, or test the Android **Circle of Fifths** (`com.eggwall.chordwheel`) application.

Cropped UI segments are included throughout this guide so you can visually map every screen element to the exact class, method, hit-target `RectF`, and line range in the source code via relative links.

---

## 1. Codebase Architecture Overview

The application is zero-dependency, self-contained, and compiles in seconds. All production code lives in [`src/com/eggwall/chordwheel/`](src/com/eggwall/chordwheel/) and unit tests live in [`test/com/eggwall/chordwheel/`](test/com/eggwall/chordwheel/):

| File | Responsibility |
| :--- | :--- |
| [`AndroidManifest.xml`](AndroidManifest.xml#L1-L20) | Declares the single non-fullscreen launcher activity (`ChordWheelActivity`) with `@android:style/Theme.DeviceDefault.NoActionBar`, adaptive/round icons, and orientation handling (`configChanges="orientation\|screenSize\|..."`). Requests **zero permissions**. |
| [`build.gradle`](build.gradle#L1-L63) | Android Gradle Plugin `8.9.0` build configuration (`minSdk = 19`, `targetSdk = 36`, `compileSdk = 36`), release signing via [`ant.properties`](ant.properties), and JUnit 4 test configuration. |
| [`ChordWheelActivity.java`](src/com/eggwall/chordwheel/ChordWheelActivity.java#L18-L120) | Lifecycle host (`onCreate`, `onResume`, `onPause`), visible system status-bar window-inset wrapper ([`wrapWithStatusBarInsets()`](src/com/eggwall/chordwheel/ChordWheelActivity.java#L96-L119)), standard screen timeout (no `FLAG_KEEP_SCREEN_ON`), and `adb` intent hook ([`handleIntentExtras()`](src/com/eggwall/chordwheel/ChordWheelActivity.java#L41-L62)) for automated demo/screenshot testing. |
| [`ChordWheelView.java`](src/com/eggwall/chordwheel/ChordWheelView.java#L37-L1411) | Custom hardware-accelerated `Canvas` view rendering both Landscape ([`drawLandscapeLayout()`](src/com/eggwall/chordwheel/ChordWheelView.java#L421-L447)) and Portrait ([`drawPortraitLayout()`](src/com/eggwall/chordwheel/ChordWheelView.java#L449-L479)) orientations, touch/swipe gesture state machine ([`onTouchEvent()`](src/com/eggwall/chordwheel/ChordWheelView.java#L1100-L1271)), hardware DPAD/gamepad controls ([`onKeyDown()`](src/com/eggwall/chordwheel/ChordWheelView.java#L1317-L1410)), 4-bar looper timer, and `SharedPreferences` persistence ([`loadState()`](src/com/eggwall/chordwheel/ChordWheelView.java#L184-L201) / [`saveState()`](src/com/eggwall/chordwheel/ChordWheelView.java#L203-L218)). |
| [`MusicTheory.java`](src/com/eggwall/chordwheel/MusicTheory.java#L20-L465) | Pure, stateless music-theory engine: Circle of Fifths sector metadata ([`WHEEL`](src/com/eggwall/chordwheel/MusicTheory.java#L78-L91)), chord & Roman numeral resolution ([`resolveChordAt()`](src/com/eggwall/chordwheel/MusicTheory.java#L379-L464)), active-key scale-degree number formatter ([`formatScaleDegreeTone()`](src/com/eggwall/chordwheel/MusicTheory.java#L249-L257)), 252-voicing CAGED/Barre generator ([`buildGuitarVoicing()`](src/com/eggwall/chordwheel/MusicTheory.java#L268-L366)), and progression preset definitions ([`PRESETS`](src/com/eggwall/chordwheel/MusicTheory.java#L125-L131)). |
| [`GuitarSynthEngine.java`](src/com/eggwall/chordwheel/GuitarSynthEngine.java#L26-L261) | Real-time 44.1 kHz 16-bit mono physical-modeling Karplus-Strong 6-string guitar synthesizer running on a dedicated high-priority audio thread ([`audioLoop()`](src/com/eggwall/chordwheel/GuitarSynthEngine.java#L174-L260)). |
| [`MusicTheoryTest.java`](test/com/eggwall/chordwheel/MusicTheoryTest.java#L18-L229) | JUnit 4 test suite verifying fifth intervals, scale-degree numbers (`1 - 3 - 5`, `4 - 6 - 8`), 7th chords, chromatic alterations, all 252 guitar voicings, and progression presets. |

---

## 2. Visual Map: Screen Elements to Source Code Methods

### A. Two-Pane Landscape vs. Stacked Portrait Layout

![Landscape Two-Pane Layout Overview](docs/images/seg_landscape_two_pane.png)

In [`ChordWheelView.onDraw()`](src/com/eggwall/chordwheel/ChordWheelView.java#L387-L419), the view checks `final boolean isLandscape = (w >= h)`:
- **Landscape ([`drawLandscapeLayout()`](src/com/eggwall/chordwheel/ChordWheelView.java#L421-L447))**: Computes virtual unit `u = Math.min(w / 320.0f, h / 240.0f)`.
  - **Left Pane (`leftW`)**: Calls [`drawWheelHeaderAndLegend()`](src/com/eggwall/chordwheel/ChordWheelView.java#L481-L530) across the full height of the screen (`h - pad`).
  - **Right Pane (`rightX`, `rightW`)**: Vertically stacks [`drawChordInfoCard()`](src/com/eggwall/chordwheel/ChordWheelView.java#L635-L687), [`drawGuitarFretboardCard()`](src/com/eggwall/chordwheel/ChordWheelView.java#L689-L825), and [`drawBottomProgressionBar()`](src/com/eggwall/chordwheel/ChordWheelView.java#L859-L920).
- **Portrait ([`drawPortraitLayout()`](src/com/eggwall/chordwheel/ChordWheelView.java#L449-L479))**: Computes `u = Math.min(w / 240.0f, h / 380.0f)` and stacks the full-width Wheel pane on top, Chord Info Card + Fretboard Card in the middle, and the full-width Progression Bar along the bottom edge (`y = h - bottomH`).

---

### B. Circle of Fifths Wheel, Corner Pills, and Legend

![Circle of Fifths Wheel & Legend Segment](docs/images/seg_wheel_and_legend.png)

![Corner '?' and Active 'LOCK' Pills Segment](docs/images/seg_corner_pills_lock.png)

Rendered by [`drawWheelHeaderAndLegend()`](src/com/eggwall/chordwheel/ChordWheelView.java#L481-L530) and [`drawCircleOfFifthsWheel()`](src/com/eggwall/chordwheel/ChordWheelView.java#L532-L611):
- **Wheel Radii & Annular Sectors**:
  - `maxRadius = Math.min(paneW, wheelAvailH) * 0.495f` ([line 505](src/com/eggwall/chordwheel/ChordWheelView.java#L505)) allows the wheel to span edge-to-edge across `paneW`.
  - Consecutive concentric radii `mROuterMax`, `mROuterMin`, `mRInnerMax`, `mRInnerMin`, and `mRHubMax` ([lines 509–513](src/com/eggwall/chordwheel/ChordWheelView.java#L509-L513)) define the Outer Major ring (`ring = 0`), Inner Minor ring (`ring = 1`), and Center `vii°` Hub (`ring = 2`).
  - Each wedge is drawn as a closed `Path` between two `RectF` ovals in [`drawAnnularSector()`](src/com/eggwall/chordwheel/ChordWheelView.java#L613-L633) and polar-hit-tested in [`handleWheelTouch()`](src/com/eggwall/chordwheel/ChordWheelView.java#L1288-L1311).
- **Corner Pills (`?` and `LOCK`)**:
  - Because `mTopHelpPillRect` (`?`, [lines 518–521](src/com/eggwall/chordwheel/ChordWheelView.java#L518-L521)) and `mLockModePillRect` (`LOCK`, [lines 523–529](src/com/eggwall/chordwheel/ChordWheelView.java#L523-L529)) sit in the vacant upper corners outside the circular perimeter, `wheelTop` starts directly at `headerY` ([line 502](src/com/eggwall/chordwheel/ChordWheelView.java#L502)) and hit-testing checks `mTopHelpPillRect` and `mLockModePillRect` before `handleWheelTouch()` ([lines 1150–1164](src/com/eggwall/chordwheel/ChordWheelView.java#L1150-L1164)).
- **Diatonic Legend Pills (`I / vi`, `IV / ii`, `V / iii`)**:
  - Rendered at the bottom of the wheel pane ([lines 483–500](src/com/eggwall/chordwheel/ChordWheelView.java#L483-L500)).

---

### C. Chord Info Card & Scale-Degree Number Calculator

| `C Major` in Key of C (`1 - 3 - 5`) | `F Major` in Key of C (`4 - 6 - 8`) | `G7` in Key of C (`5 - 7 - 9 - 11`) |
| :---: | :---: | :---: |
| ![C Major Card](docs/images/seg_chord_c_135.png) | ![F Major Card](docs/images/seg_chord_f_468.png) | ![G7 Card](docs/images/seg_chord_g7_57911.png) |

- **UI Rendering ([`drawChordInfoCard()`](src/com/eggwall/chordwheel/ChordWheelView.java#L635-L687))**:
  - Draws `mResolved.chordName` in gold (`COL_GOLD`), the Roman numeral badge `mResolved.romanNum` ([lines 648–663](src/com/eggwall/chordwheel/ChordWheelView.java#L648-L663)), and the tappable `mSeventhPillRect` (`TRIADS` / `7THS`, [lines 665–672](src/com/eggwall/chordwheel/ChordWheelView.java#L665-L672)).
  - Draws `mResolved.funcDesc` (`Tonic Major`, `Subdominant`, `Dominant`), followed on the bottom baseline by `mResolved.notesStr` in white/silver and `(" + mResolved.degreesStr + ")` in light sky-blue ([lines 674–686](src/com/eggwall/chordwheel/ChordWheelView.java#L674-L686)).
- **Harmonic Resolution & Scale-Degree Math ([`MusicTheory.resolveChordAt()`](src/com/eggwall/chordwheel/MusicTheory.java#L379-L464))**:
  - [`MAJ_ROOT_DEG`](src/com/eggwall/chordwheel/MusicTheory.java#L235) and [`MIN_ROOT_DEG`](src/com/eggwall/chordwheel/MusicTheory.java#L236) map each sector's relative offset `rel = (sec - keySec + 12) % 12` to its diatonic root degree (`1..7`) in the active Major key.
  - [`formatScaleDegreeTone()`](src/com/eggwall/chordwheel/MusicTheory.java#L249-L257) compares each chord tone's pitch class (`n1, n2, n3, n4`) against the active Major scale's natural pitch class for `baseDeg`, `baseDeg + 2`, `baseDeg + 4`, and `baseDeg + 6`, prefixing `#`, `b`, `##`, or `bb` when a chord contains chromatic alterations (e.g. `2 - #4 - 6` for `D` in Key of C).
  - **How to switch to modulo-7 scale numbers (`4 - 6 - 1` instead of `4 - 6 - 8`)**: In [`formatScaleDegreeTone()`](src/com/eggwall/chordwheel/MusicTheory.java#L249-L257), replace `degNum` in the return strings with `((degNum - 1) % 7) + 1`.

---

### D. Guitar Fretboard Card, Voicing Generator & Speaker Button

| Open/CAGED Voicing (`voicingIdx = 0`) | Movable A-Barre Voicing (`voicingIdx = 2`) |
| :---: | :---: |
| ![Open Fretboard Segment](docs/images/seg_fretboard_open.png) | ![Barre Fretboard Segment](docs/images/seg_fretboard_barre_7fr.png) |

- **UI Rendering ([`drawGuitarFretboardCard()`](src/com/eggwall/chordwheel/ChordWheelView.java#L689-L825))**:
  - **Voicing Header Bar (`mVoicingBarRect`, [lines 693–705](src/com/eggwall/chordwheel/ChordWheelView.java#L693-L705))**: Displays `< mVoicing.label >` and `TAP / SWIPE`. Tapping `mVoicingBarRect` calls `cycleVoicing(1)` ([line 1174](src/com/eggwall/chordwheel/ChordWheelView.java#L1174)); horizontal swiping on `mFretboardCardRect` calls `cycleVoicing(dx < 0 ? 1 : -1)` ([lines 1239–1255](src/com/eggwall/chordwheel/ChordWheelView.java#L1239-L1255)).
  - **Base Fret Calculation ([lines 707–719](src/com/eggwall/chordwheel/ChordWheelView.java#L707-L719))**: Uses `baseFret = 1` (drawing the white nut bar at [lines 747–749](src/com/eggwall/chordwheel/ChordWheelView.java#L747-L749)) whenever `maxFret <= 4`; otherwise shifts the 4-fret window to `minFret` and draws the gold `"7fr"` label ([lines 750–759](src/com/eggwall/chordwheel/ChordWheelView.java#L750-L759)).
  - **Side Speaker Strum Button (`mStrumBadgeRect`, [`drawSpeakerButton()`](src/com/eggwall/chordwheel/ChordWheelView.java#L827-L857))**: Drawn as a resolution-independent vector speaker cone + dual sound-wave arcs to the right of the 6th-to-1st string grid (`spkX = x + w - spkSize - 5.0f * u`).
  - **Real-Time String Plucking on Drag ([lines 1231–1238](src/com/eggwall/chordwheel/ChordWheelView.java#L1231-L1238))**: During `ACTION_MOVE` inside `mFretGridRect`, [`findNearestString(x)`](src/com/eggwall/chordwheel/ChordWheelView.java#L1273-L1286) checks which string X-coordinate (`mStringX[0..5]`) the finger crossed and calls [`pluckSingleString(s)`](src/com/eggwall/chordwheel/ChordWheelView.java#L279-L289), triggering a 220 ms gold string glow (`mStringPluckTimeMs[s]`).
- **Voicing Lookup & Barre Generator ([`MusicTheory.buildGuitarVoicing()`](src/com/eggwall/chordwheel/MusicTheory.java#L268-L366))**:
  - `voicingIdx == 0`: Reads open/CAGED shapes from [`OPEN_MAJ`](src/com/eggwall/chordwheel/MusicTheory.java#L137-L150), [`OPEN_MIN`](src/com/eggwall/chordwheel/MusicTheory.java#L152-L165), [`OPEN_MAJ7`](src/com/eggwall/chordwheel/MusicTheory.java#L167-L180), [`OPEN_MIN7`](src/com/eggwall/chordwheel/MusicTheory.java#L182-L195), and [`OPEN_DOM7`](src/com/eggwall/chordwheel/MusicTheory.java#L197-L210), or computes compact 4-string rootshapes for `QUAL_DIM` and `QUAL_M7B5`.
  - `voicingIdx == 1`: Computes 6th-string E-Barre shapes anchored at `eFret = (rootPc - 4 + 12) % 12` ([lines 305–334](src/com/eggwall/chordwheel/MusicTheory.java#L305-L334)).
  - `voicingIdx == 2`: Computes 5th-string A-Barre shapes anchored at `aFret = (rootPc - 9 + 12) % 12` ([lines 335–365](src/com/eggwall/chordwheel/MusicTheory.java#L335-L365)).

---

### E. 4-Bar Progression Looper Bar & Modals

![Idle Looper Bar](docs/images/seg_looper_idle.png)

![Active Looper Bar](docs/images/seg_looper_active.png)

- **Progression Looper Bar ([`drawBottomProgressionBar()`](src/com/eggwall/chordwheel/ChordWheelView.java#L859-L920))**:
  - Uses horizontal scaling unit `ux = Math.min(u, w / 285.0f)` ([line 873](src/com/eggwall/chordwheel/ChordWheelView.java#L873)) so all 7 pills (`mLoopPlayPillRect`, `mAddProgPillRect`, `mProgSlotRects[0..3]`, `mHelpPillRect`) fit cleanly whether the bar spans the full screen width in Portrait (`x == 0`) or sits inside the right pane in Landscape (`x > 0.5f`).
  - Looper playback is driven by [`mLoopRunnable`](src/com/eggwall/chordwheel/ChordWheelView.java#L138-L150) on the main thread `Handler` at intervals of [`LOOP_STEP_MS = 1400L`](src/com/eggwall/chordwheel/ChordWheelView.java#L41).
- **Modals**:
  - **How Circle of Fifths Works Modal (`mShowGuide`, [`drawGuideModal()`](src/com/eggwall/chordwheel/ChordWheelView.java#L986-L1055))**:

![How Circle of Fifths Works Modal](docs/images/seg_modal_guide.png)

  - **Progression Presets & Guide Modal (`mShowHelp`, [`drawHelpModal()`](src/com/eggwall/chordwheel/ChordWheelView.java#L922-L984))**:

![Progression Presets Modal](docs/images/seg_modal_presets.png)

---

## 3. Step-by-Step Engineering Customization Recipes

### Recipe 1: Adding or Modifying 4-Bar Chord Progression Presets
Progression presets are defined in [`MusicTheory.PRESETS`](src/com/eggwall/chordwheel/MusicTheory.java#L125-L131) using **relative Circle-of-Fifths sector offsets** (`0..11`) and **ring indices** (`0 = Outer Major`, `1 = Inner Minor`, `2 = Center vii°`):
- Offset `0`, Ring `0` = **`I`** (Tonic Major)
- Offset `1`, Ring `0` = **`V`** (Dominant Major, 1 step clockwise)
- Offset `11`, Ring `0` = **`IV`** (Subdominant Major, 1 step counter-clockwise)
- Offset `0`, Ring `1` = **`vi`** (Relative Minor)
- Offset `11`, Ring `1` = **`ii`** (Supertonic Minor)
- Offset `1`, Ring `1` = **`iii`** (Mediant Minor)
- Offset `0`, Ring `2` = **`vii°`** (Leading-Tone Diminished)

To add a new preset:
1. Add a new `ProgPreset` entry to [`MusicTheory.PRESETS`](src/com/eggwall/chordwheel/MusicTheory.java#L125-L131). Non-diatonic offsets work seamlessly too (e.g., offset `10`, ring `0` is `bVII`; offset `8`, ring `1` is borrowed minor `iv`).
2. Update the preset count in [`ChordWheelView.mModalPresetRects`](src/com/eggwall/chordwheel/ChordWheelView.java#L119) and [`drawHelpModal()`](src/com/eggwall/chordwheel/ChordWheelView.java#L964-L973).
3. Update `assertEquals(5, MusicTheory.PRESETS.length)` in [`MusicTheoryTest.testProgressionPresetsAreValid()`](test/com/eggwall/chordwheel/MusicTheoryTest.java#L193-L212) and run `./gradlew testDebugUnitTest`.

### Recipe 2: Changing Guitar Tuning or Supporting Another Fretted Instrument (Drop D, DADGAD, Ukulele)
All string pitches and open-string labels are centralized at the top of [`MusicTheory.java`](src/com/eggwall/chordwheel/MusicTheory.java#L39-L40):
```java
public static final int[] STRING_MIDI = { 40, 45, 50, 55, 59, 64 };
public static final String[] STRING_NAMES = { "E", "A", "D", "G", "B", "e" };
```
- Change `STRING_MIDI` and `STRING_NAMES` (for example, Drop D changes `STRING_MIDI[0]` from `40` (`E2`) to `38` (`D2`)) and adjust the 6th-string fret offsets in [`MusicTheory.buildGuitarVoicing()`](src/com/eggwall/chordwheel/MusicTheory.java#L268-L366) and [`OPEN_MAJ`..`OPEN_DOM7`](src/com/eggwall/chordwheel/MusicTheory.java#L137-L210).
- Run [`MusicTheoryTest.testAll252GuitarVoicingsProduceValidChordTones()`](test/com/eggwall/chordwheel/MusicTheoryTest.java#L150-L190). This test computes `(STRING_MIDI[s] + fret) % 12` for every string in all 252 voicings and immediately flags any string whose fret does not produce a valid chord tone!

### Recipe 3: Tuning the Karplus-Strong Guitar Synthesizer
[`GuitarSynthEngine.java`](src/com/eggwall/chordwheel/GuitarSynthEngine.java#L26-L261) generates acoustic guitar audio mathematically with zero sample assets:
- **Sustain & Decay Rate**: In [`audioLoop()`](src/com/eggwall/chordwheel/GuitarSynthEngine.java#L222-L225), each delay-line sample passes through a 1-zero averaging filter multiplied by a Q15 damping factor:
  ```java
  int avg = (cur + nxt) >> 1;
  int damped = (avg * 32590) >> 15; // 32590 / 32768 = 0.99457
  ```
  Increase `32590` toward `32700` for longer sustain, or lower it toward `32300` for a palm-muted acoustic sound.
- **Pick Brightness vs. Warmth**: In [`exciteStringLocked()`](src/com/eggwall/chordwheel/GuitarSynthEngine.java#L134-L171), initial string excitation mixes a pseudo-random noise burst (`r / 3`) with fundamental + 2nd-harmonic sine waves (`warm`), followed by 2 passes of a smoothing low-pass filter (`pass < 2`). Increasing `pass < 3` yields a mellow nylon-string timbre; reducing to `pass < 1` yields a brighter steel-string attack.

---

## 4. Building, Testing, and Automated Screenshot Capture

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```
Executes [`MusicTheoryTest.java`](test/com/eggwall/chordwheel/MusicTheoryTest.java#L18-L229) on the local JVM in ~1 second.

### Build Debug & Signed Release Artifacts
```bash
# Debug APK (build/outputs/apk/debug/chordwheel-debug.apk)
./gradlew assembleDebug

# Signed Release APK & Play Store Bundle (configured via ant.properties)
./gradlew assembleRelease bundleRelease
```

### Drive UI States Programmatically via `adb` Intents
[`ChordWheelActivity.handleIntentExtras()`](src/com/eggwall/chordwheel/ChordWheelActivity.java#L47-L68) and [`ChordWheelView.configureDemoState()`](src/com/eggwall/chordwheel/ChordWheelView.java#L221-L242) allow automated scripts or tests to jump directly to any musical state, orientation, or modal via `adb shell am start`:

```bash
# Example: Key of C (0), inspecting F Major (sector 11, ring 0) with Key LOCKED
adb shell am start -n com.eggwall.chordwheel/.ChordWheelActivity \
  --ez demo_state true \
  --ei key_sector 0 \
  --ei cur_sector 11 \
  --ei cur_ring 0 \
  --ez key_locked true \
  --ez seventh_mode false \
  --ei voicing_idx 0

# Example: Open the '?' How-It-Works Help Modal
adb shell am start -n com.eggwall.chordwheel/.ChordWheelActivity \
  --ez demo_state true \
  --ez show_guide true
```
