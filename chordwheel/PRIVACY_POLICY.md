# Privacy Policy — Circle of Fifths (`com.eggwall.chordwheel`)

**Effective Date:** October 23, 2026  
**Application Name:** Circle of Fifths  
**Package Name:** `com.eggwall.chordwheel`  
**Developer:** Eggwall (`com.eggwall`)

---

## 1. Summary: Zero Data Collection

**Circle of Fifths** is a completely offline, standalone music-theory and guitar-chord application.

- **We do NOT collect, transmit, store on external servers, or share any personal data, device identifiers, usage analytics, crash reports, location data, microphone audio, or telemetry of any kind.**
- **We do NOT bundle any third-party SDKs, advertising networks, or analytics libraries.**
- **The application requests ZERO Android runtime or install-time permissions.**

Every claim in this privacy policy can be verified directly in the open source code linked below.

---

## 2. Technical Verification in Source Code

### A. Zero Android Permissions (`AndroidManifest.xml`)
The application manifest ([`AndroidManifest.xml`](AndroidManifest.xml#L1-L20)) declares **zero `<uses-permission>` tags** — in particular, it does **not** request `android.permission.INTERNET`, `ACCESS_NETWORK_STATE`, `RECORD_AUDIO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, or `AD_ID`. Because the app lacks the `INTERNET` permission, the Android operating system kernel prevents the application from opening any network sockets.

### B. Zero Third-Party Libraries (`build.gradle`)
As shown in [`build.gradle`](build.gradle#L61-L63), the production APK/AAB has **zero external runtime dependencies**. The only dependency in the project is `junit:junit:4.13.2` scoped exclusively to local JVM unit testing (`testImplementation`).

### C. Local-Only Musical State Persistence (`SharedPreferences`)
To restore your last selected musical key, chord, voicing index, and 4-bar chord progression when you reopen the app, Circle of Fifths saves a small set of integer and boolean musical preferences locally on your device using Android's private application sandbox (`Context.MODE_PRIVATE`) in [`ChordWheelView.java` (`loadState()` and `saveState()`, lines 184–218)](src/com/eggwall/chordwheel/ChordWheelView.java#L184-L218):

| Key Name | Type | Purpose |
| :--- | :--- | :--- |
| `key_sector` | `int` (`0..11`) | Currently active key signature on the Circle of Fifths |
| `cur_sector` | `int` (`0..11`) | Currently selected chord sector on the wheel |
| `cur_ring` | `int` (`0..2`) | Currently selected ring (`0 = Major`, `1 = Minor`, `2 = vii°`) |
| `key_locked` | `boolean` | Whether the `LOCK` mode toggle is active |
| `seventh_mode` | `boolean` | Whether `TRIADS` (`false`) or `7THS` (`true`) mode is active |
| `voicing_idx` | `int` (`0..2`) | Selected guitar voicing shape (`Open/CAGED`, `E-Barre`, `A-Barre`) |
| `preset_idx` | `int` (`0..4`) | Last selected 4-bar progression preset index |
| `prog_sec_0..3`, `prog_rng_0..3`, `prog_7th_0..3` | `int` / `boolean` | The 4 chords queued in the progression looper bar |

This data never leaves your device and can be erased at any time by clearing the app's storage in Android Settings or uninstalling the application.

### D. On-Device Real-Time Audio Synthesis (`GuitarSynthEngine.java`)
All guitar chord strums and string plucks are synthesized mathematically in memory on your device using a Karplus-Strong physical-modeling algorithm and played through the standard Android `AudioTrack` output stream ([`GuitarSynthEngine.java` lines 174–260](src/com/eggwall/chordwheel/GuitarSynthEngine.java#L174-L260)). The app never accesses your device's microphone or records audio.

---

## 3. Children's Privacy

Because **Circle of Fifths** collects no personal or device information from any user, it is safe for users of all ages and complies with the Children's Online Privacy Protection Act (COPPA) and the EU General Data Protection Regulation (GDPR).

---

## 4. Changes to This Policy

If future versions of the application ever change how data is handled, this [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md) file will be updated in the source repository alongside the corresponding code changes prior to release.
