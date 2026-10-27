# Pomodoro — Focus Timer, Task Tracker & Ambient Soundscape (`com.eggwall.pomodoro`)

<p align="center">
  <img src="playstore-icon-512.png" width="128" alt="Pomodoro — Focus Timer, Task Tracker & Ambient Soundscape Icon" />
</p>

Distraction-free Pomodoro focus workstation with 60 FPS radial countdown ring, 4 workflow presets (`25/5`, `50/10`, `15/3`, `45/15`), interactive 6-slot practice/study task checklist, 4-tomato cycle tracker, and real-time procedural ambient noise & singing-bowl chime synthesizer.

---

## Screenshots

| Portrait Workspace | Landscape Two-Pane Workspace |
| :---: | :---: |
| ![Portrait](docs/images/seg_portrait_main.png) | ![Landscape](docs/images/seg_landscape_split.png) |

---

## Documentation

- **[User & Musician's Guide (`USER_GUIDE.md`)](USER_GUIDE.md)** — Visual tour, feature reference, and structured practice routines.
- **[Developer & Customization Guide (`DEVELOPER_GUIDE.md`)](DEVELOPER_GUIDE.md)** — 4-class architecture ([`PomodoroModel.java`](src/com/eggwall/pomodoro/PomodoroModel.java), [`PomodoroSynthEngine.java`](src/com/eggwall/pomodoro/PomodoroSynthEngine.java), [`PomodoroView.java`](src/com/eggwall/pomodoro/PomodoroView.java), [`PomodoroActivity.java`](src/com/eggwall/pomodoro/PomodoroActivity.java)), customization recipes, and UI layout map.
- **[Privacy Policy (`PRIVACY_POLICY.md`)](PRIVACY_POLICY.md)** — Zero permissions, zero network access, and zero third-party SDK verification.

---

## Window & Screen Timeout Behavior

- **Visible System Status Bar**: Uses `@android:style/Theme.DeviceDefault.NoActionBar` ([`AndroidManifest.xml`](AndroidManifest.xml)) with system-bar window-inset padding in [`PomodoroActivity.java`](src/com/eggwall/pomodoro/PomodoroActivity.java) so the top Android status bar remains visible at all times.
- **Standard Screen Timeout**: Does not set `FLAG_KEEP_SCREEN_ON`, respecting the user's normal device screen timeout.

---

## Build & Unit Test

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
```
