# Rhythm Trainer — Developer & Customization Guide

This guide explains the modular Kotlin + Jetpack Compose architecture of **`com.eggwall.rhythmtrainer`** so software engineers and musician-coders can customize its rhythm models, synthesis engine, or notation renderer.

---

## 1. Modular Source Layout

Under [`app/src/main/java/com/eggwall/rhythmtrainer/`](app/src/main/java/com/eggwall/rhythmtrainer/):

| File | Responsibility |
| :--- | :--- |
| [`RhythmModels.kt`](app/src/main/java/com/eggwall/rhythmtrainer/RhythmModels.kt) | Pure Kotlin rhythm model: time signatures, note/rest durations, bar subdivision/absorption math (`replaceElementInBar`), and duration descriptions. |
| [`RhythmAudioEngine.kt`](app/src/main/java/com/eggwall/rhythmtrainer/RhythmAudioEngine.kt) | Real-time `44.1 kHz` PCM `AudioTrack` synthesizer generating count-in clicks and `440 Hz` sustained note tones with envelope shaping. |
| [`NotationCanvas.kt`](app/src/main/java/com/eggwall/rhythmtrainer/NotationCanvas.kt) | Custom Compose `Canvas` renderer drawing note heads, stems, flags, rests, time signatures, and bar lines. |
| [`RhythmViewModel.kt`](app/src/main/java/com/eggwall/rhythmtrainer/RhythmViewModel.kt) | State management for tempo, time signature, Edit/Play mode, and playback cursor synchronization. |
| [`MainActivity.kt`](app/src/main/java/com/eggwall/rhythmtrainer/MainActivity.kt) | Compose UI layout hosting the transport controls, interactive two-bar notation cards, and duration picker. |
| [`RhythmTrainerTest.kt`](app/src/test/java/com/eggwall/rhythmtrainer/RhythmTrainerTest.kt) | JUnit 4 unit test suite verifying bar duration invariants across edits and time-signature changes. |

---

## 2. How Engineers Can Customize & Extend the App

- **Adding Dotted Notes or Triplets**: Extend `NoteDuration` in [`RhythmModels.kt`](app/src/main/java/com/eggwall/rhythmtrainer/RhythmModels.kt) and add corresponding glyph drawing in [`NotationCanvas.kt`](app/src/main/java/com/eggwall/rhythmtrainer/NotationCanvas.kt).
- **Changing Playback Pitch or Timbre**: Adjust `TONE_FREQ_HZ` (`440.0`) or the waveform generator in [`RhythmAudioEngine.kt`](app/src/main/java/com/eggwall/rhythmtrainer/RhythmAudioEngine.kt).

## 3. Building & Running Unit Tests

```bash
./gradlew assembleDebug testDebugUnitTest
```
