# EarTrainer — Functional Aural Skills & Fretboard Visualizer (`com.eggwall.eartrainer`)

<p align="center">
  <img src="playstore-icon-512.png" width="128" alt="EarTrainer — Functional Aural Skills & Fretboard Visualizer Icon" />
</p>

4-mode functional ear trainer (Intervals, Chords, Scale Degrees/Solfege, and Melodic Progressions) with interactive 12-fret guitar neck visualization, 3 difficulty tiers, song mnemonics, and 8-voice Karplus-Strong guitar synthesis.

---

## Screenshots

| Portrait Workspace | Landscape Two-Pane Workspace |
| :---: | :---: |
| ![Portrait](docs/images/seg_portrait_main.png) | ![Landscape](docs/images/seg_landscape_split.png) |

---

## Documentation

- **[User & Musician's Guide (`USER_GUIDE.md`)](USER_GUIDE.md)** — Visual tour, feature reference, and structured practice routines.
- **[Developer & Customization Guide (`DEVELOPER_GUIDE.md`)](DEVELOPER_GUIDE.md)** — 4-class architecture ([`EarTheory.java`](src/com/eggwall/eartrainer/EarTheory.java), [`EarSynthEngine.java`](src/com/eggwall/eartrainer/EarSynthEngine.java), [`EarTrainerView.java`](src/com/eggwall/eartrainer/EarTrainerView.java), [`EarTrainerActivity.java`](src/com/eggwall/eartrainer/EarTrainerActivity.java)), customization recipes, and UI layout map.
- **[Privacy Policy (`PRIVACY_POLICY.md`)](PRIVACY_POLICY.md)** — Zero permissions, zero network access, and zero third-party SDK verification.

---

## Window & Screen Timeout Behavior

- **Visible System Status Bar**: Uses `@android:style/Theme.DeviceDefault.NoActionBar` ([`AndroidManifest.xml`](AndroidManifest.xml)) with system-bar window-inset padding in [`EarTrainerActivity.java`](src/com/eggwall/eartrainer/EarTrainerActivity.java) so the top Android status bar remains visible at all times.
- **Standard Screen Timeout**: Does not set `FLAG_KEEP_SCREEN_ON`, respecting the user's normal device screen timeout.

---

## Build & Unit Test

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```
