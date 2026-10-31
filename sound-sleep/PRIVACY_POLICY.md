# Privacy Policy — Sound Sleep — Bedside Night Clock & White Noise (`com.eggwall.soundsleep`)

**Effective Date:** October 30, 2026  
**Developer:** Eggwall / Vikram Aggarwal

## 1. Overview
**Sound Sleep — Bedside Night Clock & White Noise** (`com.eggwall.soundsleep`) is a 100% offline-first Android bedside clock and sleep-audio application. We respect your privacy completely: the application does **not** collect, transmit, store on external servers, sell, or share any personal data, device identifiers, location data, or usage analytics.

## 2. Data Collection & Permissions
- **No Personal Data Collected:** The app does not ask for your name, email address, phone number, account credentials, or payment details.
- **No Internet or Network Access:** The app requests **zero** network permissions (`android.permission.INTERNET` is not declared in [`AndroidManifest.xml`](AndroidManifest.xml)). All clock rendering, burn-in drift animations, and audio playback run 100% locally on your device.
- **No Third-Party SDKs or Ads:** The app contains no advertising SDKs, no crash-reporting trackers, and no third-party analytics libraries (verified in [`build.gradle`](build.gradle)).
- **Strictly Scoped Local Media & Playback Permissions:** The permissions declared in [`AndroidManifest.xml`](AndroidManifest.xml#L5-L11) are used exclusively for local bedside playback on your device:
  - `READ_MEDIA_AUDIO` (Android 13+) / `READ_EXTERNAL_STORAGE` (up to Android 12): Optional runtime permission requested in [`SleepActivity.musicPressed()`](src/com/eggwall/soundsleep/SleepActivity.java#L417-L429) solely to discover and play local audio files that you place in your device's `Music/sleeping` folder via [`AudioService.querySleepingTracksFromMediaStore()`](src/com/eggwall/soundsleep/AudioService.java#L370-L398). If you decline this permission (or only use White Noise), the app plays its bundled offline audio resources (`res/raw/noise.ogg` and `res/raw/jingle.mp3`) without accessing device storage.
  - `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, and `WAKE_LOCK`: Used in [`AudioService.java`](src/com/eggwall/soundsleep/AudioService.java#L228-L505) to keep your chosen white noise or sleep music playing smoothly in the background overnight.
  - `POST_NOTIFICATIONS`: Used only to display the ongoing playback notification ([`AudioService.setForegroundService()`](src/com/eggwall/soundsleep/AudioService.java#L473-L505)) while audio is actively playing so you can return to the clock with a single tap.

## 3. Local Device Storage
All application state is transient or stored strictly locally on your device. No audio files, file names, or usage patterns ever leave your phone or tablet.

## 4. Children's Privacy
Because the application collects no personal information of any kind and operates entirely offline without network access, it is safe for users of all ages and complies with COPPA and GDPR requirements.

## 5. Contact
If you have any questions about this Privacy Policy, you may inspect the open-source source code included with this distribution or contact the developer through the Google Play Store listing page.
