# SightRead — Staff Notation, Guitar Position & Piano Trainer (`com.eggwall.sightread`)

<p align="center">
  <img src="playstore-icon-512.png" width="128" alt="SightRead — Staff Notation, Guitar Position & Piano Trainer Icon" />
</p>

Interactive musical staff sight-reading trainer featuring engraved 5-line Treble Clef notation, 7 key signatures (`C`, `G`, `D`, `A`, `E`, `F`, `Bb`), 5 guitar neck position windows (`Open` .. `Pos 9`), 99 deterministic 2-bar etudes across 4 complexity levels, and compact 6-string Guitar Fretboard input.

---

## Screenshots

| Portrait Workspace | Landscape Two-Pane Workspace |
| :---: | :---: |
| ![Portrait](docs/images/seg_portrait_main.png) | ![Landscape](docs/images/seg_landscape_split.png) |

---

## Documentation

- **[User & Musician's Guide (`USER_GUIDE.md`)](USER_GUIDE.md)** — Visual tour, feature reference, and structured practice routines.
- **[Developer & Customization Guide (`DEVELOPER_GUIDE.md`)](DEVELOPER_GUIDE.md)** — 4-class architecture ([`SightReadTheory.java`](src/com/eggwall/sightread/SightReadTheory.java), [`SightReadSynthEngine.java`](src/com/eggwall/sightread/SightReadSynthEngine.java), [`SightReadView.java`](src/com/eggwall/sightread/SightReadView.java), [`SightReadActivity.java`](src/com/eggwall/sightread/SightReadActivity.java)), customization recipes, and UI layout map.
- **[Privacy Policy (`PRIVACY_POLICY.md`)](PRIVACY_POLICY.md)** — Zero permissions, zero network access, and zero third-party SDK verification.

---

## Window & Screen Timeout Behavior

- **Visible System Status Bar**: Uses `@android:style/Theme.DeviceDefault.NoActionBar` ([`AndroidManifest.xml`](AndroidManifest.xml)) with system-bar window-inset padding in [`SightReadActivity.java`](src/com/eggwall/sightread/SightReadActivity.java) so the top Android status bar remains visible at all times.
- **Standard Screen Timeout**: Does not set `FLAG_KEEP_SCREEN_ON`, respecting the user's normal device screen timeout.

---

## Build & Unit Test

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```
