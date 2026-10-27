# PocketBand — Backing Track & Groove Workstation (`com.eggwall.pocketband`)

<p align="center">
  <img src="playstore-icon-512.png" width="128" alt="PocketBand — Backing Track & Groove Workstation Icon" />
</p>

4-bar chord progression looper, 16-step 5-track drum & bass sequencer, 8 genre presets, 8 chord-progression templates, 4 bassline patterns, and real-time multi-track DSP synthesizer for solo improvisation.

---

## Screenshots

| Portrait Workspace | Landscape Two-Pane Workspace |
| :---: | :---: |
| ![Portrait](docs/images/seg_portrait_main.png) | ![Landscape](docs/images/seg_landscape_split.png) |

---

## Documentation

- **[User & Musician's Guide (`USER_GUIDE.md`)](USER_GUIDE.md)** — Visual tour, feature reference, and structured practice routines.
- **[Developer & Customization Guide (`DEVELOPER_GUIDE.md`)](DEVELOPER_GUIDE.md)** — 4-class architecture ([`BandTheory.java`](src/com/eggwall/pocketband/BandTheory.java), [`BandSynthEngine.java`](src/com/eggwall/pocketband/BandSynthEngine.java), [`PocketBandView.java`](src/com/eggwall/pocketband/PocketBandView.java), [`PocketBandActivity.java`](src/com/eggwall/pocketband/PocketBandActivity.java)), customization recipes, and UI layout map.
- **[Privacy Policy (`PRIVACY_POLICY.md`)](PRIVACY_POLICY.md)** — Zero permissions, zero network access, and zero third-party SDK verification.

---

## Window & Screen Timeout Behavior

- **Visible System Status Bar**: Uses `@android:style/Theme.DeviceDefault.NoActionBar` ([`AndroidManifest.xml`](AndroidManifest.xml)) with system-bar window-inset padding in [`PocketBandActivity.java`](src/com/eggwall/pocketband/PocketBandActivity.java) so the top Android status bar remains visible at all times.
- **Keep Screen On (`FLAG_KEEP_SCREEN_ON`)**: Keeps the screen awake while the app is in the foreground ([`PocketBandActivity.java`](src/com/eggwall/pocketband/PocketBandActivity.java)) for uninterrupted hands-free backing-track improvisation.

---

## Build & Unit Test

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```
