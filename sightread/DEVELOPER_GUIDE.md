# SightRead — Staff Notation, Guitar Position & Piano Trainer — Developer & Customization Guide

This guide explains the architecture of **`com.eggwall.sightread`** and how engineers can extend its music theory/model tables, real-time DSP audio synthesis, custom Canvas UI, and JUnit 4 test suite.

---

## 1. Four-Class Modular Architecture

The application follows a strict separation of concerns under [`src/com/eggwall/sightread/`](src/com/eggwall/sightread/) with zero third-party runtime libraries:

| File | Responsibility | Threading & Dependencies |
| :--- | :--- | :--- |
| [`SightReadTheory.java`](src/com/eggwall/sightread/SightReadTheory.java) | Pure, stateless music-theory / state model, data tables, and deterministic algorithms. | **Zero Android UI dependencies** — 100% testable on host JVM via JUnit 4. |
| [`SightReadSynthEngine.java`](src/com/eggwall/sightread/SightReadSynthEngine.java) | Real-time `44.1 kHz` 16-bit mono PCM synthesizer (`AudioTrack.MODE_STREAM`) with pre-allocated voice buffers. | Dedicated high-priority daemon audio thread; **zero heap allocations** inside the audio loop. |
| [`SightReadView.java`](src/com/eggwall/sightread/SightReadView.java) | Custom `android.view.View` handling DPI-independent vector rendering, Portrait/Landscape split geometry, touch hit-testing, and the `?` Help Modal. | Android UI thread (`onDraw`, `onTouchEvent`, `onKeyDown`). |
| [`SightReadActivity.java`](src/com/eggwall/sightread/SightReadActivity.java) | Lifecycle host (`Activity`) managing visible top status-bar insets (`wrapWithStatusBarInsets`), standard screen timeout (no `FLAG_KEEP_SCREEN_ON`), audio engine start/stop (`onResume`/`onPause`), `SharedPreferences` persistence, and intent extras. | Android Main thread. |
| [`SightReadTheoryTest.java`](test/com/eggwall/sightread/SightReadTheoryTest.java) | Host-JVM JUnit 4 unit test suite verifying invariants across [`SightReadTheory.java`](src/com/eggwall/sightread/SightReadTheory.java). | Runs in ~1 second via `./gradlew testDebugUnitTest`. |

---

## 2. Screen Regions & Rendering Map in [`SightReadView.java`](src/com/eggwall/sightread/SightReadView.java)

### Top Header Bar
![Header Controls Segment](docs/images/seg_header_controls.png)

Rendered in [`SightReadView.java`](src/com/eggwall/sightread/SightReadView.java), storing hit rectangles (`RectF`) for the top-left `?` Help pill and mode/key/preset selectors.

### Primary Interactive Canvas
![Core Interactive Canvas Segment](docs/images/seg_core_canvas.png)

All coordinates are computed dynamically from `getWidth()` and `getHeight()` inside `onDraw(Canvas)` so the layout scales cleanly from compact phones to tablets.

### Transport & Control Dock
![Bottom Dock Segment](docs/images/seg_bottom_dock.png)

### Responsive Landscape Split-Pane
![Landscape Split Segment](docs/images/seg_landscape_split.png)

When `getWidth() > getHeight()`, [`SightReadView.java`](src/com/eggwall/sightread/SightReadView.java) automatically switches to a side-by-side two-pane layout.

---

## 3. How Engineers Can Customize & Extend the App

- **Adding Key Signatures or Neck Positions**: Extend [`SightReadTheory.KEYS`](src/com/eggwall/sightread/SightReadTheory.java) or [`SightReadTheory.POSITIONS`](src/com/eggwall/sightread/SightReadTheory.java) and run [`SightReadTheoryTest.java`](test/com/eggwall/sightread/SightReadTheoryTest.java).
- **Customizing Melodic Etude Generation**: Adjust the deterministic LCG rules and rhythmic beaming weights in [`SightReadTheory.generateSightPiece()`](src/com/eggwall/sightread/SightReadTheory.java).
- **Modifying Staff Engraving & Audio Synthesis**: Edit [`SightReadView.java`](src/com/eggwall/sightread/SightReadView.java) and [`SightReadSynthEngine.java`](src/com/eggwall/sightread/SightReadSynthEngine.java).

---

## 4. Building, Testing & Verifying

Run the JUnit 4 unit tests and build both Debug and Release APKs using the Gradle wrapper (configured in [`build.gradle`](build.gradle) and [`gradle.properties`](gradle.properties)):

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

- **Unit Test Report**: `build/reports/tests/testDebugUnitTest/index.html`
- **Debug APK**: `build/outputs/apk/debug/`
- **Release APK**: `build/outputs/apk/release/`
