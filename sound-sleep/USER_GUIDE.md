# Sound Sleep — Bedside Night Clock & White Noise — User Guide

**Sound Sleep (`com.eggwall.soundsleep`)** is a minimalist, zero-glare bedside night clock and sleep sound machine designed to stay on your nightstand all night while optionally playing continuous white noise or your favorite sleep music.

---

## 1. Visual Overview

### Portrait Bedside Workspace
![Portrait Main Workspace](docs/images/seg_portrait_main.png)

### Top Half — Sleep Music Control Zone (`@+id/note`)
![Top Half Sleep Music Zone](docs/images/seg_header_controls.png)

- **Top Half of Screen (Music Note Icon)**: The entire upper half of the display acts as a giant invisible button (`@+id/startMusic` in [`res/layout/main.xml`](res/layout/main.xml#L14-L22)) so you can operate it in a dark room without hunting for small targets.
- **Custom `Music/sleeping` Folder or Built-In Lullaby**: Tapping anywhere on the top half plays shuffled audio tracks from your device's `Music/sleeping` directory (if present), or automatically falls back to the bundled looping lullaby jingle (`res/raw/jingle.mp3`).

### All-Night Clock Display (`@+id/clock`)
![All-Night Clock Display](docs/images/seg_core_canvas.png)

- **Large `120dp` Display on Pure Black (`#000000`)**: Displays the current time in `h:mm` format across both 12-hour and 24-hour modes (`TextClock` in [`res/layout/main.xml`](res/layout/main.xml#L46-L52)) while keeping the screen awake (`FLAG_KEEP_SCREEN_ON`).
- **Automatic 10-Minute Burn-In Drift**: Every 10 minutes (`DELAY = 10 * 60 * 1000` ms), the clock and icons smoothly glide to new random coordinates on screen to prevent OLED/AMOLED pixel burn-in.

### Bottom Half — White Noise Control Zone (`@+id/cloud`)
![Bottom Half White Noise Zone](docs/images/seg_bottom_dock.png)

- **Bottom Half of Screen (Rain Cloud Icon)**: Tap anywhere on the lower half of the display (`@+id/startWhiteNoise` in [`res/layout/main.xml`](res/layout/main.xml#L23-L31)) to start continuous, seamless white noise (`res/raw/noise.ogg`).

### Active Playback Pause Indicator (`||`)
![Active Playback Pause State](docs/images/seg_help_modal.png)

- **Pause Icon & One-Tap Silence**: While White Noise or Sleep Music is playing, its corresponding icon changes to a **Pause (`||`)** symbol (`@drawable/pause`). Tap the active half of the screen a second time to stop playback and return to silence.

### Landscape Nightstand Dock Layout
Rotate your phone or tablet horizontally on a bedside charging stand for a wide **Landscape Nightstand View**:

![Landscape Nightstand Layout](docs/images/seg_landscape_split.png)

---

## 2. Key Features

- **Immersive Fullscreen Night Mode**: Automatically hides the system status bar, title bar, and navigation buttons (`Home`, `Back`, `Recents`) using `WindowInsetsController` (Android 11+) and `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` (Android 4.4–10) in [`SleepActivity.setGlobalScreenSettings()`](src/com/eggwall/soundsleep/SleepActivity.java#L371-L397).
- **Progressive Overnight Icon Dimming**: Every 10 minutes, the Music Note and Rain Cloud icons step down in opacity (`mAlphaDecrement` in [`SleepActivity.changeIconLocation()`](src/com/eggwall/soundsleep/SleepActivity.java#L239-L266)) so your bedroom stays as dark as possible. Tapping anywhere immediately restores icon brightness.
- **Smart Gesture Navigation Filtering**: Upward swipes starting from the bottom half of the screen (used to go Home or open Recent Apps) are automatically detected and cancelled in [`SleepActivity.dispatchTouchEvent()`](src/com/eggwall/soundsleep/SleepActivity.java#L335-L356) so leaving the app never accidentally toggles white noise.
- **Background Playback Service**: Audio continues playing reliably even if you switch apps or lock the screen, managed by [`AudioService.java`](src/com/eggwall/soundsleep/AudioService.java) with a foreground media notification that brings you back to the clock when tapped.

---

## 3. How to Use Custom Sleep Music (`Music/sleeping`)

1. **Built-In Audio Works Out of the Box**: No setup is required to use the bundled continuous White Noise (bottom half) or the bundled Lullaby Jingle (top half).
2. **Adding Your Own Sleep Tracks**: Create a folder named `sleeping` inside your device's `Music` directory (`[Internal Storage]/Music/sleeping/`) and copy your favorite ambient MP3, OGG, or FLAC tracks into it.
3. **Shuffled All-Night Playback**: Tap the top half of the screen. Once you grant audio read permission (if prompted), **Sound Sleep** queries Android `MediaStore` for tracks inside `Music/sleeping` and shuffles through them continuously all night.

---

## 4. Zero-Distraction, 100% Offline Design

- **100% Offline & Ad-Free**: Requests zero internet permissions, contains zero advertisements or analytics trackers, and works reliably in Airplane Mode.
- **Top 20 World Languages**: Localized across 20 languages via [`res/values-<lang>/strings.xml`](res/values/strings.xml).
