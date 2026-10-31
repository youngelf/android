# Sound Sleep — Bedside Night Clock & White Noise (`com.eggwall.soundsleep`)

<p align="center">
  <img src="playstore-icon-512.png" width="128" alt="Sound Sleep — Bedside Night Clock & White Noise 512x512 Icon" />
</p>

> **Play Store Short Description:** Minimalist all-night bedside clock with soothing white noise and sleep music.

![Feature Graphic](feature-graphic-1024x500.png)

## Overview

**Sound Sleep** (`com.eggwall.soundsleep`) is a minimalist night-clock and sleep-aid application for Android. It is designed to sit on a nightstand all night with minimal visual distraction while optionally playing soothing audio:

- **All-Night Clock Display**: Displays the current time in a large `h:mm` format (`120dip`) on a pure black background while disabling the device's screen timeout (`FLAG_KEEP_SCREEN_ON` and `android:keepScreenOn="true"`).
- **Immersive Fullscreen**: Hides the title/action bar, system status bar, and on-screen navigation buttons (Home, Recents, Back) so the clock uses the entire screen without bright system UI chrome.
- **Screen Burn-In Protection & Progressive Dimming**: Every 10 minutes, the clock and control icons smoothly animate to new random positions on screen, and the icons progressively dim so the display becomes as dark as possible overnight. Touching a control restores icon brightness.
- **White Noise & Sleep Music**:
  - **Top half of screen (Music Note)**: Plays random tracks from `[ExternalStorage]/Music/sleeping` if present, or falls back to a bundled looping jingle (`res/raw/jingle.mp3`).
  - **Bottom half of screen (Rain Cloud)**: Loops bundled white noise (`res/raw/noise.ogg`).
  - Tapping the active half a second time stops playback and returns to silence.

---

## Phone Screenshots

### Main Workspace (Portrait & Landscape)
| Portrait Workspace | Landscape Workspace |
| :---: | :---: |
| ![Portrait Main](docs/images/seg_portrait_main.png) | ![Landscape Main](docs/images/seg_landscape_split.png) |

### Additional Functionality Screenshots (2 Portrait + 2 Landscape)
| Portrait Feature 1 (Bedside Clock) | Portrait Feature 2 (White Noise Active) |
| :---: | :---: |
| ![Portrait 1](docs/images/phone_portrait_1.png) | ![Portrait 2](docs/images/phone_portrait_2.png) |

| Landscape Feature 1 (Nightstand Dock) | Landscape Feature 2 (Sleep Music Active) |
| :---: | :---: |
| ![Landscape 1](docs/images/phone_landscape_1.png) | ![Landscape 2](docs/images/phone_landscape_2.png) |

---

## Documentation & Play Store Assets

- **[User Guide (`USER_GUIDE.md`)](USER_GUIDE.md)** — Visual tour of the top/bottom half-screen touch controls, burn-in drift, progressive dimming, and custom `Music/sleeping` folder setup.
- **[Developer & Customization Guide (`DEVELOPER_GUIDE.md`)](DEVELOPER_GUIDE.md)** — Component breakdown (`SleepActivity` and `AudioService`), state machine, UI layout map, and customization recipes.
- **[Suite Porting & Architecture Playbook (`../porting.md`)](../porting.md)** — Reusable engineering guide for building and packaging custom Android audio apps.
- **[Play Store Listing & Assets (`PLAY_STORE_LISTING.md`)](PLAY_STORE_LISTING.md)** — Short description, full description ([`PLAY_STORE_DESCRIPTION.md`](PLAY_STORE_DESCRIPTION.md)), 512×512 icon, 1024×500 Miyazaki-style feature graphic, and phone screenshots.
- **[Privacy Policy (`PRIVACY_POLICY.md`)](PRIVACY_POLICY.md)** — Zero internet access, zero data collection, and zero third-party SDK verification.

---

## Device & Android Version Compatibility

- **Supported Device Architectures**: All Android CPU architectures (`armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`) across phones and tablets, as the app compiles to pure Dalvik/ART bytecode (`classes.dex`) with no native NDK binary dependencies.
- **Minimum Android Version**: **Android 4.4 (KitKat, API level 19)** (`minSdkVersion="19"`).
- **Target Android Version**: **Android 15 (API level 36)** (`targetSdkVersion="36"`).
- **Maximum Android Version**: **No upper limit** (`maxSdkVersion` is not set). The app is forward-compatible with Android 15+ and automatically adapts to modern platform APIs:
  - Uses `WindowInsetsController` on Android 11+ (API 30+) and `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` on Android 4.4–10 (API 19–29).
  - Uses `NotificationChannel` on Android 8.0+ (API 26+).
  - Uses `PendingIntent.FLAG_IMMUTABLE` on Android 6.0+ (API 23+).
  - Uses `Context.RECEIVER_NOT_EXPORTED` on Android 13+ (API 33+).
  - Stores `resources.arsc` uncompressed and 4-byte aligned as required on Android 11+ (API 30+).

---

## Architecture

The project contains two primary components in [`src/com/eggwall/soundsleep/`](src/com/eggwall/soundsleep/) and a single main layout in [`res/layout/main.xml`](res/layout/main.xml):

1. **[`SleepActivity`](src/com/eggwall/soundsleep/SleepActivity.java)**:
   - Hosts the fullscreen UI defined in [`res/layout/main.xml`](res/layout/main.xml):
     - A root `FrameLayout` (`@+id/toplevel`) with a black background and `android:keepScreenOn="true"`.
     - Two invisible full-width `Button` regions stacked vertically (`@+id/startMusic` on the top half, `@+id/startWhiteNoise` on the bottom half) that capture taps across the top and bottom halves of the screen.
     - Two status `ImageView` icons (`@+id/note` for music and `@+id/cloud` for white noise) that swap to a pause icon (`@drawable/pause`) when their respective audio mode is active.
     - A framework `TextClock` (`@+id/clock`) configured to display `h:mm` in both 12-hour and 24-hour modes.
   - Manages immersive fullscreen mode and `FLAG_KEEP_SCREEN_ON` in `onCreate()`, `onResume()`, and `onWindowFocusChanged()`.
   - Schedules a periodic `Runnable` (`mChangeClockLocation`) on the main-thread `Handler` every 10 minutes (`DELAY = 10 * 60 * 1000` ms) to reposition the clock and icons and gradually reduce icon alpha.
   - Sends command `Intent`s (`MUSIC`, `WHITE_NOISE`, `GET_STATUS`) to `AudioService` and listens for package-scoped status broadcasts (`MESSAGE_SILENCE`, `MESSAGE_MUSIC`, `MESSAGE_WHITE_NOISE`) to keep the icons synchronized with playback state.

2. **[`AudioService`](src/com/eggwall/soundsleep/AudioService.java)**:
   - A background Android `Service` managing a `MediaPlayer` instance with a `PARTIAL_WAKE_LOCK` so audio continues playing even if the user switches away from the activity.
   - Handles state transitions between `SILENCE`, `MUSIC`, and `WHITE_NOISE`. Receiving the currently playing command a second time stops playback and stops the service.
   - Posts an ongoing notification (`NOTIFICATION_ID = 1`) while audio is playing that returns the user to `SleepActivity` when tapped, and broadcasts state updates back to `SleepActivity`.

<!-- I18N_EFIGSCJK_START -->
---

## Internationalization (i18n) — Top 20 World Languages & EFIGSCJK Preview

**Sound Sleep** is localized across the **top 20 world languages** via Android resource qualifiers (`res/values-<lang>/strings.xml`):

- **Supported 20 Languages:** English (`en-US`), French (`fr-FR`), Italian (`it-IT`), German (`de-DE`), Spanish (`es-ES`), Simplified Chinese (`zh-CN`), Japanese (`ja-JP`), Korean (`ko-KR`), Portuguese (`pt-BR`), Russian (`ru-RU`), Arabic (`ar`), Hindi (`hi-IN`), Bengali (`bn-BD`), Indonesian (`id`), Turkish (`tr-TR`), Vietnamese (`vi`), Polish (`pl-PL`), Dutch (`nl-NL`), Thai (`th`), and Ukrainian (`uk`).
- **Google Play Console "Import translations with AI" (All 20 Languages):** Upload **[`PLAY_STORE_AI_IMPORT.csv`](PLAY_STORE_AI_IMPORT.csv)** (recommended), **[`PLAY_STORE_AI_IMPORT.md`](PLAY_STORE_AI_IMPORT.md)**, or **[`PLAY_STORE_AI_IMPORT.txt`](PLAY_STORE_AI_IMPORT.txt)** in **Grow users → Store presence → Store listings → Edit store listing → Import translations with AI** to populate **App name** (`≤ 30` chars), **Short description** (`≤ 80` chars), and **Full description** (`≤ 4000` chars) across all 20 languages in one step.
- **Fastlane / Per-Locale Store Metadata:** See **[`playstore/metadata/android/`](playstore/metadata/android/)** (all 20 locales) and **[`PLAY_STORE_I18N_EFIGSCJK.md`](PLAY_STORE_I18N_EFIGSCJK.md)**.

### EFIGSCJK 8-Language Visual Contact Sheet (Click Thumbnail to Inspect Full Resolution)

Click the thumbnail below to open the full-resolution `2260×2640` 8-language contact sheet ([`docs/images/i18n/efigscjk_grid.png`](docs/images/i18n/efigscjk_grid.png)), or open any individual `1080×2400` language screenshot directly:

- **Individual Full-Resolution Screenshots:** [**English (`en`)**](docs/images/i18n/en.png) · [**Français (`fr`)**](docs/images/i18n/fr.png) · [**Italiano (`it`)**](docs/images/i18n/it.png) · [**Deutsch (`de`)**](docs/images/i18n/de.png) · [**Español (`es`)**](docs/images/i18n/es.png) · [**中文 (`zh`)**](docs/images/i18n/zh.png) · [**日本語 (`ja`)**](docs/images/i18n/ja.png) · [**한국어 (`ko`)**](docs/images/i18n/ko.png)

[![Sound Sleep EFIGSCJK Thumbnail — Click to open full image](docs/images/i18n/efigscjk_grid_thumb.png)](docs/images/i18n/efigscjk_grid.png)
<!-- I18N_EFIGSCJK_END -->

---

## Build and Install Steps

### Prerequisites
- Java 21 JDK (`/usr/lib/jvm/java-21-openjdk-amd64`)
- Gradle Wrapper (`./gradlew`, included in the repository; uses Gradle 8.14 and Android Gradle Plugin 8.9.0)
- Android SDK with **Platform 36** (`platforms/android-36/android.jar`) and **Build-Tools 36.0.0** (`build-tools/36.0.0/`)

### 1. Configure SDK Location
Ensure `sdk.dir` in `local.properties` points to your Android SDK installation (or set the `ANDROID_HOME` environment variable):

```properties
sdk.dir=/home/viki/Android/Sdk
```

### 2. Build the App Bundle (`.aab`) and APK
From the project root directory:

- **Build Play Store Android App Bundle (`.aab`) for release**:
  ```bash
  ./gradlew bundleRelease
  ```
  Produces a signed release Android App Bundle at `build/outputs/bundle/release/SoundSleep-release.aab`.

- **Build APK for release**:
  ```bash
  ./gradlew assembleRelease
  ```
  Produces a signed, zipaligned release APK at `build/outputs/apk/release/SoundSleep-release.apk`.

- **Build APK for debug**:
  ```bash
  ./gradlew assembleDebug
  ```
  Produces a debug-signed APK at `build/outputs/apk/debug/SoundSleep-debug.apk`.

- **Clean build artifacts**:
  ```bash
  ./gradlew clean
  ```

### 3. Install on a Device
Connect an Android device with USB debugging enabled (or start an emulator) and run:

```bash
adb install -r build/outputs/apk/release/SoundSleep-release.apk
```

---

## Production Signing Key Configuration

Release signing is configured in **`build.gradle`** (`android.signingConfigs.release`), which reads the keystore path, alias, and passwords from **`ant.properties`**:

```properties
key.store=release-keystore.key
key.alias=sleep sounds release key
key.store.password=...
key.alias.password=...
```

Both `./gradlew bundleRelease` and `./gradlew assembleRelease` automatically sign the output `.aab` and `.apk` using `release-keystore.key` and the credentials defined in `ant.properties`. To switch to a different keystore or alias, update those properties in `ant.properties` (or directly in the `signingConfigs.release` block of `build.gradle`).
