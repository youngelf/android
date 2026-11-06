# Rhythm Trainer — Developer & Customization Guide

This guide explains the modular pure-Java architecture of **`com.eggwall.rhythmtrainer`** (`minSdk = 19`, zero external runtime dependencies) so software engineers and musician-coders can customize its rhythm models, synthesis engine, or custom `Canvas` notation renderer.

---

## 1. Modular Source Layout

Under [`src/com/eggwall/rhythmtrainer/`](src/com/eggwall/rhythmtrainer/):

| File | Responsibility |
| :--- | :--- |
| [`TimeSignature.java`](src/com/eggwall/rhythmtrainer/TimeSignature.java), [`NoteDuration.java`](src/com/eggwall/rhythmtrainer/NoteDuration.java), [`RhythmElement.java`](src/com/eggwall/rhythmtrainer/RhythmElement.java), [`Measure.java`](src/com/eggwall/rhythmtrainer/Measure.java), [`ElementPosition.java`](src/com/eggwall/rhythmtrainer/ElementPosition.java) | Pure Java rhythm domain model: time signatures (`4/4`, `3/4`, `2/2`, `5/4`, `7/4`), note/rest durations, and measure containers. |
| [`RhythmBuilder.java`](src/com/eggwall/rhythmtrainer/RhythmBuilder.java) | Pure Java bar-subdivision and absorption engine (`updateElementInMeasure`), random rhythm generator (`randomMeasure`), and default measure presets. |
| [`RhythmAudioEngine.java`](src/com/eggwall/rhythmtrainer/RhythmAudioEngine.java) | Background-thread `44.1 kHz` 16-bit mono `AudioTrack` (`MODE_STREAM`) synthesizer generating 4-beat count-in clicks and `440 Hz` sustained note tones with envelope shaping and bar-level UI callbacks. |
| [`RhythmTrainerView.java`](src/com/eggwall/rhythmtrainer/RhythmTrainerView.java) | Custom hardware-accelerated `Canvas` `View` rendering the studio transport header, time-signature & tempo deck, engraved rhythm score with barlines and vector note/rest glyphs, duration palette dock, and scrollable Help modal. |
| [`AppI18n.java`](src/com/eggwall/rhythmtrainer/AppI18n.java) | 20-language runtime translation, auto-fit `Canvas` text rendering, and scrollable Help modal renderer. |
| [`MainActivity.java`](src/com/eggwall/rhythmtrainer/MainActivity.java) | Android `Activity` hosting `RhythmTrainerView` with status-bar/cutout inset protection from API 19 through API 36. |
| [`RhythmTrainerTest.java`](test/com/eggwall/rhythmtrainer/RhythmTrainerTest.java) | JUnit 4 unit test suite verifying bar duration invariants across edits, time-signature changes, and PCM synthesis. |

---

## 2. How Engineers Can Customize & Extend the App

- **Adding Dotted Notes or Triplets**: Extend `NoteDuration` in [`NoteDuration.java`](src/com/eggwall/rhythmtrainer/NoteDuration.java) and add corresponding glyph drawing in [`RhythmTrainerView.java`](src/com/eggwall/rhythmtrainer/RhythmTrainerView.java).
- **Changing Playback Pitch or Timbre**: Adjust `FIXED_FREQUENCY_HZ` (`440.0`) or the waveform generator in [`RhythmAudioEngine.java`](src/com/eggwall/rhythmtrainer/RhythmAudioEngine.java).

## 3. Building & Running Unit Tests

```bash
./gradlew assembleDebug testDebugUnitTest
```
