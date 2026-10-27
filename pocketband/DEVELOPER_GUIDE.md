# PocketBand — Backing Track & Groove Workstation — Developer & Customization Guide

This guide explains the architecture of **`com.eggwall.pocketband`** and how engineers can extend its music theory/model tables, real-time DSP audio synthesis, custom Canvas UI, and JUnit 4 test suite.

---

## 1. Four-Class Modular Architecture

The application follows a strict separation of concerns under [`src/com/eggwall/pocketband/`](src/com/eggwall/pocketband/) with zero third-party runtime libraries:

| File | Responsibility | Threading & Dependencies |
| :--- | :--- | :--- |
| [`BandTheory.java`](src/com/eggwall/pocketband/BandTheory.java) | Pure, stateless music-theory / state model, data tables, and deterministic algorithms. | **Zero Android UI dependencies** — 100% testable on host JVM via JUnit 4. |
| [`BandSynthEngine.java`](src/com/eggwall/pocketband/BandSynthEngine.java) | Real-time `44.1 kHz` 16-bit mono PCM synthesizer (`AudioTrack.MODE_STREAM`) with pre-allocated voice buffers. | Dedicated high-priority daemon audio thread; **zero heap allocations** inside the audio loop. |
| [`PocketBandView.java`](src/com/eggwall/pocketband/PocketBandView.java) | Custom `android.view.View` handling DPI-independent vector rendering, Portrait/Landscape split geometry, touch hit-testing, and the `?` Help Modal. | Android UI thread (`onDraw`, `onTouchEvent`, `onKeyDown`). |
| [`PocketBandActivity.java`](src/com/eggwall/pocketband/PocketBandActivity.java) | Lifecycle host (`Activity`) managing visible top status-bar insets (`wrapWithStatusBarInsets`), `FLAG_KEEP_SCREEN_ON` for hands-free backing-track loops, audio engine start/stop (`onResume`/`onPause`), `SharedPreferences` persistence, and intent extras. | Android Main thread. |
| [`BandTheoryTest.java`](test/com/eggwall/pocketband/BandTheoryTest.java) | Host-JVM JUnit 4 unit test suite verifying invariants across [`BandTheory.java`](src/com/eggwall/pocketband/BandTheory.java). | Runs in ~1 second via `./gradlew testDebugUnitTest`. |

---

## 2. Screen Regions & Rendering Map in [`PocketBandView.java`](src/com/eggwall/pocketband/PocketBandView.java)

### Top Header Bar
![Header Controls Segment](docs/images/seg_header_controls.png)

Rendered in [`PocketBandView.java`](src/com/eggwall/pocketband/PocketBandView.java), storing hit rectangles (`RectF`) for the top-left `?` Help pill and mode/key/preset selectors.

### Primary Interactive Canvas
![Core Interactive Canvas Segment](docs/images/seg_core_canvas.png)

All coordinates are computed dynamically from `getWidth()` and `getHeight()` inside `onDraw(Canvas)` so the layout scales cleanly from compact phones to tablets.

### Transport & Control Dock
![Bottom Dock Segment](docs/images/seg_bottom_dock.png)

### Responsive Landscape Split-Pane
![Landscape Split Segment](docs/images/seg_landscape_split.png)

When `getWidth() > getHeight()`, [`PocketBandView.java`](src/com/eggwall/pocketband/PocketBandView.java) automatically switches to a side-by-side two-pane layout.

---

## 3. How Engineers Can Customize & Extend the App

- **Adding Genre Presets or Chord Templates**: Extend [`BandTheory.GENRE_PRESETS`](src/com/eggwall/pocketband/BandTheory.java) or [`BandTheory.PROGRESSIONS`](src/com/eggwall/pocketband/BandTheory.java) and verify with [`BandTheoryTest.java`](test/com/eggwall/pocketband/BandTheoryTest.java).
- **Customizing Drum & Bass DSP Voices**: Edit the per-instrument synthesis routines in [`BandSynthEngine.java`](src/com/eggwall/pocketband/BandSynthEngine.java).
- **Modifying Sequencer Layout**: Adjust the Portrait/Landscape split geometry in [`PocketBandView.java`](src/com/eggwall/pocketband/PocketBandView.java).

---

## 4. Building, Testing & Verifying

Run the JUnit 4 unit tests and build both Debug and Release APKs using the Gradle wrapper (configured in [`build.gradle`](build.gradle) and [`gradle.properties`](gradle.properties)):

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

- **Unit Test Report**: `build/reports/tests/testDebugUnitTest/index.html`
- **Debug APK**: `build/outputs/apk/debug/`
- **Release APK**: `build/outputs/apk/release/`
