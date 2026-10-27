# Privacy Policy — EarTrainer — Functional Aural Skills & Fretboard Visualizer (`com.eggwall.eartrainer`)

**Effective Date:** October 23, 2026  
**Package Name:** `com.eggwall.eartrainer`

---

## 1. Summary: Zero Data Collection

**EarTrainer — Functional Aural Skills & Fretboard Visualizer** operates **100% offline** on your Android device. We do **not** collect, transmit, store on external servers, or share any personal data, usage analytics, audio recordings, or device identifiers.

---

## 2. Zero Android Permissions

As verified directly in [`AndroidManifest.xml`](AndroidManifest.xml), this application declares **zero `<uses-permission>` tags**:

- **No Internet Access (`android.permission.INTERNET` is NOT requested)**: The app cannot make network connections or transmit any data off your device.
- **No Microphone Access (`android.permission.RECORD_AUDIO` is NOT requested)**: All musical tones, clicks, and soundscapes are synthesized mathematically in memory by [`EarSynthEngine.java`](src/com/eggwall/eartrainer/EarSynthEngine.java).
- **No Location, Contacts, Camera, or Storage Access**: The app never accesses your personal files, photos, or sensors.

---

## 3. Local-Only Preferences

Your selected settings (such as mode, key, tempo, or session counters) are stored strictly locally on your device using Android's standard sandboxed `SharedPreferences` inside [`EarTrainerView.java`](src/com/eggwall/eartrainer/EarTrainerView.java) and [`EarTrainerActivity.java`](src/com/eggwall/eartrainer/EarTrainerActivity.java). Clearing the app's data or uninstalling the app permanently removes these preferences.

---

## 4. Zero Third-Party SDKs or Trackers

As verified in [`build.gradle`](build.gradle), the compiled application contains **zero third-party runtime dependencies**, advertising SDKs, crash reporters, or telemetry libraries (only `junit:junit:4.13.2` is used for local build-time unit testing in [`EarTheoryTest.java`](test/com/eggwall/eartrainer/EarTheoryTest.java)).
