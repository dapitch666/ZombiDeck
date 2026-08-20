# ZombiDeck
[![Android Instrumented Tests](https://github.com/dapitch666/ZombiDeck/actions/workflows/android_instrumented_tests.yaml/badge.svg)](https://github.com/dapitch666/ZombiDeck/actions/workflows/android_instrumented_tests.yaml)
[![Unit Tests](https://github.com/dapitch666/ZombiDeck/actions/workflows/unit_tests.yaml/badge.svg)](https://github.com/dapitch666/ZombiDeck/actions/workflows/unit_tests.yaml)
[![Release APK](https://github.com/dapitch666/ZombiDeck/actions/workflows/release_apk.yaml/badge.svg)](https://github.com/dapitch666/ZombiDeck/actions/workflows/release_apk.yaml)
[![Tag Release](https://github.com/dapitch666/ZombiDeck/actions/workflows/tag_release.yaml/badge.svg)](https://github.com/dapitch666/ZombiDeck/actions/workflows/tag_release.yaml)

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

## Releasing

`main` is protected — no direct pushes, including from repo admins. Releases are triggered entirely from GitHub Actions:

1. Go to the **Actions** tab → **Release** workflow → **Run workflow**, and pick `patch`, `minor`, or `major`.
2. The workflow bumps `version.properties` on a new `release/vX.Y.Z` branch, opens a PR into `main`, and sets it to auto-merge.
3. Once the required checks (`Unit Tests`, `Android Instrumented Tests`) pass, the PR merges itself.
4. The `Tag Release` workflow then tags `main` with `vX.Y.Z` and calls `Release APK` in the same run to build, sign, and publish the APK to GitHub Releases.

Release signing is enabled when these environment variables are provided:

- `KEYSTORE_PATH`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

### One-time setup for maintainers

- Add these repository secrets (Settings → Secrets and variables → Actions):
  - `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — for signing the release APK.
  - `RELEASE_PAT` — a fine-grained personal access token scoped to this repo only, with **Contents: Read and write** and **Pull requests: Read and write** permissions. Used by the `Release` workflow so the branch push and PR it creates trigger the required status checks (actions run with the default `GITHUB_TOKEN` don't trigger other workflows).
- Enable **Allow auto-merge** under Settings → General → Pull Requests.
- Enable branch protection on `main`: require a pull request before merging, require the `test` and `instrumented-tests` status checks (these are the exact job ids — that's how they appear in the required-checks picker, with no workflow-name prefix), and don't exempt administrators.

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
