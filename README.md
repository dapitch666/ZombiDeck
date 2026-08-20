# ZombiDeck
[![Android Instrumented Tests](https://github.com/dapitch666/ZombiDeck/actions/workflows/android_instrumented_tests.yaml/badge.svg)](https://github.com/dapitch666/ZombiDeck/actions/workflows/android_instrumented_tests.yaml)
[![Unit Tests](https://github.com/dapitch666/ZombiDeck/actions/workflows/unit_tests.yaml/badge.svg)](https://github.com/dapitch666/ZombiDeck/actions/workflows/unit_tests.yaml)
[![Release APK](https://github.com/dapitch666/ZombiDeck/actions/workflows/release_apk.yaml/badge.svg)](https://github.com/dapitch666/ZombiDeck/actions/workflows/release_apk.yaml)

## Description

ZombiDeck is an Android companion app that helps players run the zombie spawn deck for Zombicide 2nd Edition.
It lets you quickly draw cards, track danger level, and tune deck difficulty with optional expansions.

## Game Compatibility

- Base game required: Zombicide 2nd Edition

Compatible expansions:

- Washington ZC (no new Zombie cards, so naturally compatible)
- Fort Hendrix
- Rio Z Janeiro (no new Zombie cards, so naturally compatible)
- Danny Trejo

## APK installation

### From GitHub Releases

1. Download the latest APK from the project Releases page.
2. On your Android device, allow installing apps from unknown sources.
3. Open the APK and complete the installation.

### From local build (ADB)

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Setup

Prerequisites:

- Android Studio (recent stable version)
- Android SDK Platform 36 (project `compileSdk = 36`)
- JDK 21 (project Java/Kotlin toolchain)
- [GitHub CLI](https://cli.github.com/) (`gh`), authenticated (`gh auth login`) — required by the `release*` tasks below

Project setup:

```bash
git clone https://github.com/dapitch666/ZombiDeck.git
cd ZombiDeck
./gradlew tasks
```

Then open the project in Android Studio and let Gradle sync.

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

## Versioning

Application version is managed in `version.properties`:

```properties
VERSION_NAME=1.0.0
VERSION_CODE=1
```

Useful Gradle commands:

```bash
./gradlew printVersion
./gradlew bumpPatch
./gradlew bumpMinor
./gradlew bumpMajor
```

Automated release commands:

```bash
./gradlew releasePatch
./gradlew releaseMinor
./gradlew releaseMajor
```

`main` is protected — no direct pushes are allowed, including from repo admins. Each `release*` task must be run from an up-to-date `main`, and:

1. Checks for a clean working tree.
2. Bumps the version and commits `version.properties` on a new `release/vX.Y.Z` branch.
3. Pushes that branch and opens a PR into `main` via `gh`.
4. Sets the PR to auto-merge (squash) once required checks pass.

Once the PR merges, the `Tag Release` workflow tags `main` with `vX.Y.Z`, which triggers `Release APK` (build, sign, and publish).

Release signing is enabled when these environment variables are provided:

- `KEYSTORE_PATH`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## Testing

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:lintDebug
```

## Contributing

1. Fork the repository and create a branch from `main`.
2. Make focused changes with clear commit messages.
3. Run unit tests and lint locally before opening a PR.
4. Open a pull request with context, screenshots (if UI changes), and test notes.

## License

This project is distributed under the terms described in the `LICENSE` file.

## Publishing APK to GitHub

- Create these GitHub Actions secrets before publishing:
  - `ANDROID_KEYSTORE_BASE64`
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
- Run a Gradle release task (for example `./gradlew releasePatch`).
- The pushed tag triggers the workflow, builds a signed release APK, uploads artifacts, and publishes a GitHub Release.
