# LickTrainer — Guitar Tab Sequencer & Speed Builder (`com.eggwall.licktrainer`)

<p align="center">
  <img src="playstore-icon-512.png" width="128" alt="LickTrainer — Guitar Tab Sequencer & Speed Builder Icon" />
</p>

Interactive 6-string guitar tablature step sequencer, zoomed 8-fret neck inspector, 33 curated exercises across 6 categories (including 8 Jazz 7th diatonic arpeggio lessons), 12-key transposition, and automated Speed-Builder BPM ladders.

---

## Screenshots

| Portrait Workspace | Landscape Two-Pane Workspace |
| :---: | :---: |
| ![Portrait](docs/images/seg_portrait_main.png) | ![Landscape](docs/images/seg_landscape_split.png) |

---

## Documentation

- **[User & Musician's Guide (`USER_GUIDE.md`)](USER_GUIDE.md)** — Visual tour, feature reference, and structured practice routines.
- **[Developer & Customization Guide (`DEVELOPER_GUIDE.md`)](DEVELOPER_GUIDE.md)** — 4-class architecture ([`LickTheory.java`](src/com/eggwall/licktrainer/LickTheory.java), [`LickSynthEngine.java`](src/com/eggwall/licktrainer/LickSynthEngine.java), [`LickTrainerView.java`](src/com/eggwall/licktrainer/LickTrainerView.java), [`LickTrainerActivity.java`](src/com/eggwall/licktrainer/LickTrainerActivity.java)), customization recipes, and UI layout map.
- **[Privacy Policy (`PRIVACY_POLICY.md`)](PRIVACY_POLICY.md)** — Zero permissions, zero network access, and zero third-party SDK verification.

---

## Window & Screen Timeout Behavior

- **Visible System Status Bar**: Uses `@android:style/Theme.DeviceDefault.NoActionBar` ([`AndroidManifest.xml`](AndroidManifest.xml)) with system-bar window-inset padding in [`LickTrainerActivity.java`](src/com/eggwall/licktrainer/LickTrainerActivity.java) so the top Android status bar remains visible at all times.
- **Standard Screen Timeout**: Does not set `FLAG_KEEP_SCREEN_ON`, respecting the user's normal device screen timeout.

---

## Build & Unit Test

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```
