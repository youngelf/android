# LickTrainer — Guitar Tab Sequencer & Speed Builder — Developer & Customization Guide

This guide explains the architecture of **`com.eggwall.licktrainer`** and how engineers can extend its music theory/model tables, real-time DSP audio synthesis, custom Canvas UI, and JUnit 4 test suite.

---

## 1. Four-Class Modular Architecture

The application follows a strict separation of concerns under [`src/com/eggwall/licktrainer/`](src/com/eggwall/licktrainer/) with zero third-party runtime libraries:

| File | Responsibility | Threading & Dependencies |
| :--- | :--- | :--- |
| [`LickTheory.java`](src/com/eggwall/licktrainer/LickTheory.java) | Pure, stateless music-theory / state model, data tables, and deterministic algorithms. | **Zero Android UI dependencies** — 100% testable on host JVM via JUnit 4. |
| [`LickSynthEngine.java`](src/com/eggwall/licktrainer/LickSynthEngine.java) | Real-time `44.1 kHz` 16-bit mono PCM synthesizer (`AudioTrack.MODE_STREAM`) with pre-allocated voice buffers. | Dedicated high-priority daemon audio thread; **zero heap allocations** inside the audio loop. |
| [`LickTrainerView.java`](src/com/eggwall/licktrainer/LickTrainerView.java) | Custom `android.view.View` handling DPI-independent vector rendering, Portrait/Landscape split geometry, touch hit-testing, and the `?` Help Modal. | Android UI thread (`onDraw`, `onTouchEvent`, `onKeyDown`). |
| [`LickTrainerActivity.java`](src/com/eggwall/licktrainer/LickTrainerActivity.java) | Lifecycle host (`Activity`) managing visible top status-bar insets (`wrapWithStatusBarInsets`), standard screen timeout (no `FLAG_KEEP_SCREEN_ON`), audio engine start/stop (`onResume`/`onPause`), `SharedPreferences` persistence, and intent extras. | Android Main thread. |
| [`LickTheoryTest.java`](test/com/eggwall/licktrainer/LickTheoryTest.java) | Host-JVM JUnit 4 unit test suite verifying invariants across [`LickTheory.java`](src/com/eggwall/licktrainer/LickTheory.java). | Runs in ~1 second via `./gradlew testDebugUnitTest`. |

---

## 2. Screen Regions & Rendering Map in [`LickTrainerView.java`](src/com/eggwall/licktrainer/LickTrainerView.java)

### Top Header Bar
![Header Controls Segment](docs/images/seg_header_controls.png)

Rendered in [`LickTrainerView.java`](src/com/eggwall/licktrainer/LickTrainerView.java), storing hit rectangles (`RectF`) for the top-left `?` Help pill and mode/key/preset selectors.

### Primary Interactive Canvas
![Core Interactive Canvas Segment](docs/images/seg_core_canvas.png)

All coordinates are computed dynamically from `getWidth()` and `getHeight()` inside `onDraw(Canvas)` so the layout scales cleanly from compact phones to tablets.

### Transport & Control Dock
![Bottom Dock Segment](docs/images/seg_bottom_dock.png)

### Responsive Landscape Split-Pane
![Landscape Split Segment](docs/images/seg_landscape_split.png)

When `getWidth() > getHeight()`, [`LickTrainerView.java`](src/com/eggwall/licktrainer/LickTrainerView.java) automatically switches to a side-by-side two-pane layout.

---

## 3. How Engineers Can Customize & Extend the App

- **Adding New Licks & Verifying Biomechanics**: Add a `LickDef` to [`LickTheory.LICKS`](src/com/eggwall/licktrainer/LickTheory.java) and run `./gradlew testDebugUnitTest`—[`LickTheory.verifyLickLibrary()`](src/com/eggwall/licktrainer/LickTheory.java) automatically verifies scale pitch-class masks and hammer-on/pull-off/slide biomechanics.
- **Customizing Speed Ladders**: Edit [`LickTheory.LADDERS`](src/com/eggwall/licktrainer/LickTheory.java) to configure custom BPM increments and bar intervals.
- **Modifying Lead/Backing Synthesis**: Adjust string damping and pick transients in [`LickSynthEngine.java`](src/com/eggwall/licktrainer/LickSynthEngine.java).

---

## 4. Building, Testing & Verifying

Run the JUnit 4 unit tests and build both Debug and Release APKs using the Gradle wrapper (configured in [`build.gradle`](build.gradle) and [`gradle.properties`](gradle.properties)):

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

- **Unit Test Report**: `build/reports/tests/testDebugUnitTest/index.html`
- **Debug APK**: `build/outputs/apk/debug/`
- **Release APK**: `build/outputs/apk/release/`
