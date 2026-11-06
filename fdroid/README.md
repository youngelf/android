# Submitting the Eggwall Android App Suite to F-Droid

This directory (`fdroid/`) contains the complete F-Droid submission recipes (`fdroiddata` YAML files), tag-creation script, readiness verifier, and submission helper script for all **10 Android applications** in this repository, prepared according to the official **[Submitting to F-Droid Quick Start Guide](https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/)** and **[F-Droid Build Metadata Reference](https://f-droid.org/en/docs/Build_Metadata_Reference/)**.

---

## 1. Summary of Applications, F-Droid Metadata Files & Required Git Tags

Because all 10 apps live in a single Git repository (`https://github.com/youngelf/aaxx`), each app uses a per-app tag prefix (`<subdir>-v<versionName>`) paired with `UpdateCheckMode: Tags ^<subdir>-v.+$` and `subdir: <gradle-module-dir>` so F-Droid's `checkupdates` bot and build server can track and build each app independently.

| Application | Package ID (`applicationId`) | Version (`versionCode`) | Required Git Tag | `subdir` in Repo | `fdroiddata` YAML Recipe | Upstream Fastlane Dir |
| :--- | :--- | :---: | :--- | :--- | :--- | :--- |
| **ChordWheel** | `com.eggwall.chordwheel` | `1.4` (`4`) | `chordwheel-v1.4` | `chordwheel` | [`metadata/com.eggwall.chordwheel.yml`](metadata/com.eggwall.chordwheel.yml) | [`../chordwheel/fastlane/metadata/android/`](../chordwheel/fastlane/metadata/android/) |
| **Fretboard Explorer** | `com.eggwall.fretboard` | `2.0` (`2`) | `fretboard-v2.0` | `fretboard` | [`metadata/com.eggwall.fretboard.yml`](metadata/com.eggwall.fretboard.yml) | [`../fretboard/fastlane/metadata/android/`](../fretboard/fastlane/metadata/android/) |
| **Guitar Lick Trainer** | `com.eggwall.licktrainer` | `1.0` (`1`) | `licktrainer-v1.0` | `licktrainer` | [`metadata/com.eggwall.licktrainer.yml`](metadata/com.eggwall.licktrainer.yml) | [`../licktrainer/fastlane/metadata/android/`](../licktrainer/fastlane/metadata/android/) |
| **Band (PocketBand)** | `com.eggwall.pocketband` | `1.0` (`1`) | `pocketband-v1.0` | `pocketband` | [`metadata/com.eggwall.pocketband.yml`](metadata/com.eggwall.pocketband.yml) | [`../pocketband/fastlane/metadata/android/`](../pocketband/fastlane/metadata/android/) |
| **Ear Trainer** | `com.eggwall.eartrainer` | `1.0` (`1`) | `eartrainer-v1.0` | `eartrainer` | [`metadata/com.eggwall.eartrainer.yml`](metadata/com.eggwall.eartrainer.yml) | [`../eartrainer/fastlane/metadata/android/`](../eartrainer/fastlane/metadata/android/) |
| **Precision Metronome** | `com.eggwall.metronome` | `1.0` (`1`) | `metronome-v1.0` | `metronome` | [`metadata/com.eggwall.metronome.yml`](metadata/com.eggwall.metronome.yml) | [`../metronome/fastlane/metadata/android/`](../metronome/fastlane/metadata/android/) |
| **Pomodoro Focus** | `com.eggwall.pomodoro` | `1.0` (`1`) | `pomodoro-v1.0` | `pomodoro` | [`metadata/com.eggwall.pomodoro.yml`](metadata/com.eggwall.pomodoro.yml) | [`../pomodoro/fastlane/metadata/android/`](../pomodoro/fastlane/metadata/android/) |
| **SightRead** | `com.eggwall.sightread` | `1.0` (`1`) | `sightread-v1.0` | `sightread` | [`metadata/com.eggwall.sightread.yml`](metadata/com.eggwall.sightread.yml) | [`../sightread/fastlane/metadata/android/`](../sightread/fastlane/metadata/android/) |
| **Rhythm Trainer** | `com.eggwall.rhythmtrainer` | `1.0.0` (`1`) | `rhythm-trainer-v1.0.0` | `rhythm-trainer` | [`metadata/com.eggwall.rhythmtrainer.yml`](metadata/com.eggwall.rhythmtrainer.yml) | [`../rhythm-trainer/fastlane/metadata/android/`](../rhythm-trainer/fastlane/metadata/android/) |
| **Sound Sleep** | `com.eggwall.soundsleep` | `1.11` (`11`) | `sound-sleep-v1.11` | `sound-sleep` | [`metadata/com.eggwall.soundsleep.yml`](metadata/com.eggwall.soundsleep.yml) | [`../sound-sleep/fastlane/metadata/android/`](../sound-sleep/fastlane/metadata/android/) |

---

## 2. F-Droid Compliance Changes Prepared in This Repo

1. **Disabled Google-Encrypted `DependencyInfoBlock` (`dependenciesInfo`)**:
   - Added `dependenciesInfo { includeInApk = false; includeInBundle = false }` to all 10 apps' `build.gradle` / `app/build.gradle.kts` files. F-Droid's scanner rejects APKs that contain AGP's default Google-encrypted dependency signing block.
2. **Removed Workstation-Specific `org.gradle.java.home`**:
   - Removed `org.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64` from all 10 `gradle.properties` files so F-Droid's build containers can use their own provisioned OpenJDK.
3. **Standard Upstream Fastlane Directory Structure (`<app>/fastlane/metadata/android/`)**:
   - Populated `<app>/fastlane/metadata/android/<locale>/` across all **20 supported languages** (`title.txt`, `short_description.txt`, `full_description.txt`), plus `en-US/changelogs/<versionCode>.txt`, `en-US/images/icon.png` (`512×512`), `en-US/images/featureGraphic.png` (`1024×500`), and localized `images/phoneScreenshots/` across all 8 EFIGSCJK locales (`en-US`, `fr-FR`, `it-IT`, `de-DE`, `es-ES`, `zh-CN`, `ja-JP`, `ko-KR`).
4. **Explicit `LICENSE` (`Apache-2.0`) Files & 100% FOSS / Public-Domain Assets**:
   - Placed standard `LICENSE` (and `COPYING`) files at the repository root and inside each app's directory.
   - Replaced all legacy third-party icons and audio in `sound-sleep` with 100% Apache-2.0 vector/raster icons (`res/drawable/rain.xml`, `music.xml`, `pause.xml`, `ic_launcher_foreground.xml`), real-time procedural `AudioTrack` brownian/white sleep noise synthesis in [`AudioService.java`](../sound-sleep/src/com/eggwall/soundsleep/AudioService.java), and a bundled public-domain Brahms *Wiegenlied* (Op. 49, No. 4, 1868 — CC0 / Public Domain) lullaby (`res/raw/jingle.ogg`) so all 10 apps have zero non-commercial or proprietary asset restrictions.

---

## 3. Step-by-Step Workflow & Shell Scripts

### Step 1: Verify F-Droid Readiness
Run the verification script at any time to confirm all 10 apps' metadata, Fastlane trees, licenses, and Gradle configurations are in place:
```bash
./fdroid/verify_fdroid_readiness.sh
```

### Step 2: Commit & Push Repo Changes, Then Create & Push Git Tags
Once we finish iterating and you commit + push the repository changes to `origin main`, run [`./fdroid/create_fdroid_tags.sh`](create_fdroid_tags.sh) to create the 10 annotated release tags and push them to GitHub:
```bash
# Preview / create annotated tags locally:
./fdroid/create_fdroid_tags.sh

# Or create and push all 10 tags directly to origin (GitHub):
./fdroid/create_fdroid_tags.sh --push
```

### Step 3: Copy YAML Recipes into Your `fdroiddata` GitLab Fork
1. Fork [`https://gitlab.com/fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata) on GitLab and clone it locally:
   ```bash
   git clone git@gitlab.com:<your-gitlab-username>/fdroiddata.git ~/fdroiddata
   ```
2. Run [`./fdroid/submit_to_fdroiddata.sh`](submit_to_fdroiddata.sh) to copy the 10 `.yml` files into `~/fdroiddata/metadata/`:
   ```bash
   ./fdroid/submit_to_fdroiddata.sh ~/fdroiddata
   ```
3. Create a separate branch per app in `~/fdroiddata`, commit its `metadata/<appId>.yml`, and open a Merge Request on GitLab.
