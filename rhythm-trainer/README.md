# Rhythm Trainer

An Android rhythm training application written in Kotlin with Jetpack Compose (Material 3).

## Features
1. **Time Signature Selector**: Drop-down list supporting `4/4`, `3/4`, `2/2`, `5/4`, and `7/4`.
2. **Standard Musical Notation**: Renders whole, half, quarter, eighth, and sixteenth notes and their corresponding rests with bar lines and final double bar lines (without a 5-line staff or clef).
3. **Count-In & Single-Frequency Audio Playback**: Pressing **Play** counts down `4 .. 3 .. 2 .. 1` using clicks, then plays fixed-frequency (`440 Hz`) clicks/tones scaled to each note's exact duration while keeping rests silent.
4. **Edit Mode & Play Mode**: Toggle **Edit Mode** to select any note or rest and assign its duration (automatically subdividing or absorbing within the bar to maintain the exact time signature). Exit Edit Mode to view the note durations and play the rhythm.

## Building on Linux
```bash
./gradlew assembleDebug testDebugUnitTest
```
