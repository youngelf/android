# Pomodoro — Focus Timer, Task Tracker & Ambient Soundscape — Developer & Customization Guide

This guide explains the architecture of **`com.eggwall.pomodoro`** and how engineers can extend its music theory/model tables, real-time DSP audio synthesis, custom Canvas UI, and JUnit 4 test suite.

---

## 1. Four-Class Modular Architecture

The application follows a strict separation of concerns under [`src/com/eggwall/pomodoro/`](src/com/eggwall/pomodoro/) with zero third-party runtime libraries:

| File | Responsibility | Threading & Dependencies |
| :--- | :--- | :--- |
| [`PomodoroModel.java`](src/com/eggwall/pomodoro/PomodoroModel.java) | Pure, stateless music-theory / state model, data tables, and deterministic algorithms. | **Zero Android UI dependencies** — 100% testable on host JVM via JUnit 4. |
| [`PomodoroSynthEngine.java`](src/com/eggwall/pomodoro/PomodoroSynthEngine.java) | Real-time `44.1 kHz` 16-bit mono PCM synthesizer (`AudioTrack.MODE_STREAM`) with pre-allocated voice buffers. | Dedicated high-priority daemon audio thread; **zero heap allocations** inside the audio loop. |
| [`PomodoroView.java`](src/com/eggwall/pomodoro/PomodoroView.java) | Custom `android.view.View` handling DPI-independent vector rendering, Portrait/Landscape split geometry, touch hit-testing, and the `?` Help Modal. | Android UI thread (`onDraw`, `onTouchEvent`, `onKeyDown`). |
| [`PomodoroActivity.java`](src/com/eggwall/pomodoro/PomodoroActivity.java) | Lifecycle host (`Activity`) managing visible top status-bar insets (`wrapWithStatusBarInsets`), standard screen timeout (no `FLAG_KEEP_SCREEN_ON`), audio engine start/stop (`onResume`/`onPause`), `SharedPreferences` persistence, and intent extras. | Android Main thread. |
| [`PomodoroModelTest.java`](test/com/eggwall/pomodoro/PomodoroModelTest.java) | Host-JVM JUnit 4 unit test suite verifying invariants across [`PomodoroModel.java`](src/com/eggwall/pomodoro/PomodoroModel.java). | Runs in ~1 second via `./gradlew testDebugUnitTest`. |

---

## 2. Screen Regions & Rendering Map in [`PomodoroView.java`](src/com/eggwall/pomodoro/PomodoroView.java)

### Top Header Bar
![Header Controls Segment](docs/images/seg_header_controls.png)

Rendered in [`PomodoroView.java`](src/com/eggwall/pomodoro/PomodoroView.java), storing hit rectangles (`RectF`) for the top-left `?` Help pill and mode/key/preset selectors.

### Primary Interactive Canvas
![Core Interactive Canvas Segment](docs/images/seg_core_canvas.png)

All coordinates are computed dynamically from `getWidth()` and `getHeight()` inside `onDraw(Canvas)` so the layout scales cleanly from compact phones to tablets.

### Transport & Control Dock
![Bottom Dock Segment](docs/images/seg_bottom_dock.png)

### Responsive Landscape Split-Pane
![Landscape Split Segment](docs/images/seg_landscape_split.png)

When `getWidth() > getHeight()`, [`PomodoroView.java`](src/com/eggwall/pomodoro/PomodoroView.java) automatically switches to a side-by-side two-pane layout.

---

## 3. How Engineers Can Customize & Extend the App

- **Adding Timer Presets or Default Tasks**: Modify [`PomodoroModel.PRESETS`](src/com/eggwall/pomodoro/PomodoroModel.java) or [`PomodoroModel.DEFAULT_TASKS`](src/com/eggwall/pomodoro/PomodoroModel.java) and run [`PomodoroModelTest.java`](test/com/eggwall/pomodoro/PomodoroModelTest.java).
- **Customizing Ambient Noise Filters & Bell Harmonics**: Edit the procedural DSP generators in [`PomodoroSynthEngine.java`](src/com/eggwall/pomodoro/PomodoroSynthEngine.java).
- **Modifying the Radial Ring & Task Card Layout**: Edit [`PomodoroView.java`](src/com/eggwall/pomodoro/PomodoroView.java).

---

## 4. Building, Testing & Verifying

Run the JUnit 4 unit tests and build both Debug and Release APKs using the Gradle wrapper (configured in [`build.gradle`](build.gradle) and [`gradle.properties`](gradle.properties)):

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

- **Unit Test Report**: `build/reports/tests/testDebugUnitTest/index.html`
- **Debug APK**: `build/outputs/apk/debug/`
- **Release APK**: `build/outputs/apk/release/`
