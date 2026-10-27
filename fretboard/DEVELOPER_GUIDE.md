# Fretboard Explorer & Scale Trainer — Developer & Customization Guide

This guide explains the architecture of **`com.eggwall.fretboard`** and how engineers can extend its music theory/model tables, real-time DSP audio synthesis, custom Canvas UI, and JUnit 4 test suite.

---

## 1. Four-Class Modular Architecture

The application follows a strict separation of concerns under [`src/com/eggwall/fretboard/`](src/com/eggwall/fretboard/) with zero third-party runtime libraries:

| File | Responsibility | Threading & Dependencies |
| :--- | :--- | :--- |
| [`FretboardTheory.java`](src/com/eggwall/fretboard/FretboardTheory.java) | Pure, stateless music-theory / state model, data tables, and deterministic algorithms. | **Zero Android UI dependencies** — 100% testable on host JVM via JUnit 4. |
| [`GuitarSynthEngine.java`](src/com/eggwall/fretboard/GuitarSynthEngine.java) | Real-time `44.1 kHz` 16-bit mono PCM synthesizer (`AudioTrack.MODE_STREAM`) with pre-allocated voice buffers. | Dedicated high-priority daemon audio thread; **zero heap allocations** inside the audio loop. |
| [`FretboardView.java`](src/com/eggwall/fretboard/FretboardView.java) | Custom `android.view.View` handling DPI-independent vector rendering, Portrait/Landscape split geometry, touch hit-testing, and the `?` Help Modal. | Android UI thread (`onDraw`, `onTouchEvent`, `onKeyDown`). |
| [`FretboardActivity.java`](src/com/eggwall/fretboard/FretboardActivity.java) | Lifecycle host (`Activity`) managing visible top status-bar insets (`wrapWithStatusBarInsets`), standard screen timeout (no `FLAG_KEEP_SCREEN_ON`), audio engine start/stop (`onResume`/`onPause`), `SharedPreferences` persistence, and intent extras. | Android Main thread. |
| [`FretboardTheoryTest.java`](test/com/eggwall/fretboard/FretboardTheoryTest.java) | Host-JVM JUnit 4 unit test suite verifying invariants across [`FretboardTheory.java`](src/com/eggwall/fretboard/FretboardTheory.java). | Runs in ~1 second via `./gradlew testDebugUnitTest`. |

---

## 2. Screen Regions & Rendering Map in [`FretboardView.java`](src/com/eggwall/fretboard/FretboardView.java)

### Top Header Bar
![Header Controls Segment](docs/images/seg_header_controls.png)

Rendered in [`FretboardView.java`](src/com/eggwall/fretboard/FretboardView.java), storing hit rectangles (`RectF`) for the top-left `?` Help pill and mode/key/preset selectors.

### Primary Interactive Canvas
![Core Interactive Canvas Segment](docs/images/seg_core_canvas.png)

All coordinates are computed dynamically from `getWidth()` and `getHeight()` inside `onDraw(Canvas)` so the layout scales cleanly from compact phones to tablets.

### Transport & Control Dock
![Bottom Dock Segment](docs/images/seg_bottom_dock.png)

### Responsive Landscape Split-Pane
![Landscape Split Segment](docs/images/seg_landscape_split.png)

When `getWidth() > getHeight()`, [`FretboardView.java`](src/com/eggwall/fretboard/FretboardView.java) automatically switches to a side-by-side two-pane layout.

---

## 3. How Engineers Can Customize & Extend the App

- **Adding New Scales or Modes**: Append a new `ScaleDef` entry to [`FretboardTheory.SCALES`](src/com/eggwall/fretboard/FretboardTheory.java) with a 12-element semitone role mask (`4` = Root, `2` = Chord Skeleton, `3` = Color Tone, `1` = Passing Tone).
- **Adding New Guitar Tunings**: Add a `GuitarTuning` entry with 6 open-string MIDI pitches to [`FretboardTheory.TUNINGS`](src/com/eggwall/fretboard/FretboardTheory.java).
- **Adjusting Plucked-String Timbre**: Modify the Karplus-Strong low-pass damping and brightness blend in [`GuitarSynthEngine.java`](src/com/eggwall/fretboard/GuitarSynthEngine.java).

---

## 4. Building, Testing & Verifying

Run the JUnit 4 unit tests and build both Debug and Release APKs using the Gradle wrapper (configured in [`build.gradle`](build.gradle) and [`gradle.properties`](gradle.properties)):

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

- **Unit Test Report**: `build/reports/tests/testDebugUnitTest/index.html`
- **Debug APK**: `build/outputs/apk/debug/`
- **Release APK**: `build/outputs/apk/release/`
