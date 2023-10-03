# OpenOTP

[![Platforms](https://img.shields.io/badge/mobile-Android%20%7C%20iOS-blue)](https://github.com/avan1235/open-otp/releases)
[![Platforms](https://img.shields.io/badge/desktop-Windows%20%7C%20macOS%20%7C%20Linux-blue)](https://github.com/avan1235/open-otp/releases)

[![Build](https://img.shields.io/github/actions/workflow/status/avan1235/open-otp/release.yml?label=Build&color=green)](https://github.com/avan1235/open-otp/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/avan1235/open-otp?label=Release&color=green)](https://github.com/avan1235/open-otp/releases)
[![Google Play](https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dml.dev.kotlin.openotp.OpenOtp%26l%3DGoogle%2520Play%26m%3D%24version)](https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp)

[![License: MIT](https://img.shields.io/badge/License-MIT-red.svg)](./LICENSE.md)
[![GitHub Repo stars](https://img.shields.io/github/stars/avan1235/open-otp?style=social)](https://github.com/avan1235/open-otp/stargazers)
[![Fork OpenOTP](https://img.shields.io/github/forks/avan1235/open-otp?logo=github&style=social)](https://github.com/avan1235/open-otp/fork)

### Android

![android](https://github.com/avan1235/open-otp/assets/11787040/56d8fa10-f686-4f95-838b-3730770b8d41)

### iOS

![ios](https://github.com/avan1235/open-otp/assets/11787040/8ec711cd-e010-42b2-9167-90657f868c1f)

## Download and run application

### Download compiled application

#### Google Play

Latest Android version is available on
[Google Play](https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp).

<a href='https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp'><img alt='Get it on Google Play' width="300" src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

#### GitHub Releases

You can download compiled version of application from
[GitHub Releases](https://github.com/avan1235/open-otp/releases).

You can find the compiled vesion of the application for Android, Linux, macOS and Windows.

Please note that for running unsigned version of macOS application, you need to temporarily
disable Gatekeeper, so after installing the application run

```shell
sudo xattr -dr com.apple.quarantine  /Applications/OpenOTP.app
```

in the terminal. You can learn more about this
[here](https://web.archive.org/web/20230318124537/https://disable-gatekeeper.github.io/).

To install Linux version run:

```shell
sudo dpkg -i  openotp.deb
```

### Build application locally

The project is configured with with Gradle and you can find the
latest release build commands in the [release.yml](./.github/workflows/release.yml) file.

Example build commands for particular platforms:
- desktop: `./gradlew desktopApp:packageDistributionForCurrentOS`
- Android: `./gradlew androidApp:assembleDebug`
- iOS: open [iosApp.xcodeproj](./iosApp/iosApp.xcodeproj) in Xcode and run the build 
(you might need to configure the `Team` in `Signing & Capabilities`)

## Tech Stack

### Tools & Libraries

- [Kotlin](https://kotlinlang.org/)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) (UI)
- [Material3 design](https://m3.material.io/) (UI components)
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) (structured concurrency)
- [Kotlin Datetime](https://github.com/Kotlin/kotlinx-datetime) (current time access)
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization) (data serialization)
- [Koin](https://github.com/InsertKoinIO/koin) (DI)
- [Kermit](https://github.com/touchlab/Kermit) (Logging)
- [Decompose](https://github.com/arkivanov/Decompose) (lifecycle-aware business logic)
- [Essenty](https://github.com/arkivanov/Essenty) (handy additions to Decompose)
- [moko resources](https://github.com/icerockdev/moko-resources) (resources for localization)
- [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings) (persistence of keys)
- [uuid](https://github.com/benasher44/uuid) (UUID generation)
- [ByteBuffer](https://github.com/DitchOoM/buffer) (allocate and modify `byte[]` natively)
- [Uri KMP](https://github.com/eygraber/uri-kmp) (parsing URI from QR codes)
- [Webcam Capture API](https://github.com/sarxos/webcam-capture/) (desktop camera bindings)
- [zxing](https://github.com/zxing/zxing) (desktop QR codes recognition)
- [AndroidX Security](https://developer.android.com/jetpack/androidx/releases/security) (`EncryptedSharedPreferences` for Multiplatform Settings)
- [AndroidX CameraX](https://developer.android.com/jetpack/androidx/releases/camera) (camera access on Android)
- [ML Kit Barcode scanning](https://developer.android.com/jetpack/androidx/releases/camera) (Android QR codes recognition)
- [Accompanist](https://github.com/google/accompanist) (permissions and system UI control on Android)

### Build & CI

- [Gradle KTS](https://docs.gradle.org/current/userguide/kotlin_dsl.html) (Kotlin DSL)
- [Gradle version catalogs](https://developer.android.com/build/migrate-to-catalogs) (dependencies versions)
- [Github Actions](https://github.com/avan1235/open-otp/actions) (CI/CD)
