# OpenOTP

A cross-platform, open-source one-time password (OTP) authenticator app supporting TOTP and HOTP standards.

[![Platforms](https://img.shields.io/badge/mobile-Android%20%7C%20iOS-blue)](https://github.com/avan1235/open-otp/releases)
[![Platforms](https://img.shields.io/badge/desktop-Windows%20%7C%20macOS%20%7C%20Linux-blue)](https://github.com/avan1235/open-otp/releases)
[![Platforms](https://img.shields.io/badge/web-wasmJs-blue)](https://open-otp.procyk.in)

[![Build](https://img.shields.io/github/actions/workflow/status/avan1235/open-otp/release.yml?label=Build&color=green)](https://github.com/avan1235/open-otp/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/avan1235/open-otp?label=Release&color=green)](https://github.com/avan1235/open-otp/releases)
[![Google Play](https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dml.dev.kotlin.openotp.OpenOtp%26l%3DGoogle%2520Play%26m%3D%24version)](https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp)

[![License: MIT](https://img.shields.io/badge/License-MIT-red.svg)](./LICENSE.md)
[![GitHub Repo stars](https://img.shields.io/github/stars/avan1235/open-otp?style=social)](https://github.com/avan1235/open-otp/stargazers)
[![Fork OpenOTP](https://img.shields.io/github/forks/avan1235/open-otp?logo=github&style=social)](https://github.com/avan1235/open-otp/fork)

![presentation](https://github.com/avan1235/open-otp/assets/11787040/a268e72d-8ef4-4878-9e61-bf242f2313d3)

## Features

- **TOTP & HOTP support** — Generate time-based and counter-based one-time passwords
- **QR code scanning** — Add accounts by scanning QR codes using the device camera (mobile) or webcam (desktop)
- **Cloud backup & restore** — Backup and restore your OTP secrets to Dropbox or OneDrive
- **Biometric authentication** — Secure your OTP secrets with fingerprint/face recognition (Android)
- **Cloud account linking** — Link cloud storage accounts for seamless backup management
- **Dark & light themes** — Automatic theme switching with system preference detection
- **Search & filter** — Quickly find accounts with real-time search
- **Drag & drop reordering** — Reorganize your accounts with drag and drop
- **Multi-platform** — Single codebase running on Android, iOS, Windows, macOS, Linux, and the web

## Platforms

| Platform | Status | Details |
|----------|--------|---------|
| 🤖 Android | ✅ Available | [Google Play](https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp) / [GitHub Releases](https://github.com/avan1235/open-otp/releases) |
| 🍎 iOS | ✅ Available | [GitHub Releases](https://github.com/avan1235/open-otp/releases) (IPA) |
| 🖥️ Windows | ✅ Available | [GitHub Releases](https://github.com/avan1235/open-otp/releases) (MSI installer) |
| 🍏 macOS | ✅ Available | [GitHub Releases](https://github.com/avan1235/open-otp/releases) (DMG) |
| 🐧 Linux | ✅ Available | [GitHub Releases](https://github.com/avan1235/open-otp/releases) (DEB package) |
| 🌐 Web | ✅ Available | [open-otp.procyk.in](https://open-otp.procyk.in) (Kotlin/WASM) |

## Download and run application

### Web

Try the web version directly in your browser:
[**open-otp.procyk.in**](https://open-otp.procyk.in)

### Mobile

#### Google Play

Latest Android version is available on
[Google Play](https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp).

<a href='https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp'><img alt='Get it on Google Play' width="300" src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

#### GitHub Releases

You can download compiled versions of the application from
[GitHub Releases](https://github.com/avan1235/open-otp/releases).

Available formats:
- **Android**: APK (debug/release), AAB (bundle)
- **Windows**: MSI installer
- **macOS**: DMG image
- **Linux**: DEB package

Please note that for running unsigned version of macOS application, you need to temporarily
disable Gatekeeper. After installing the application, run:

```shell
sudo xattr -dr com.apple.quarantine /Applications/OpenOTP.app
```

You can learn more about this
[here](https://web.archive.org/web/20230318124537/https://disable-gatekeeper.github.io/).

To install the Linux version, run:

```shell
sudo dpkg -i openotp.deb
```

### Build application locally

The project is configured with Gradle, and you can find the
latest release build commands in the [release.yml](./.github/workflows/release.yml) file.

Example build commands for particular platforms:

- **Desktop**: `./gradlew desktopApp:packageDistributionForCurrentOS`
- **Android**: `./gradlew androidApp:assembleDebug`
- **iOS**: Open [iosApp/iosApp.xcodeproj](./iosApp/iosApp.xcodeproj) in Xcode and run the build
  (you may need to configure the `Team` in *Signing & Capabilities*)
- **Web**: `./gradlew webApp:wasmJsBrowserDistribution`

## Tech Stack

### Core

- [Kotlin](https://kotlinlang.org/) — Primary language
- [Kotlin/Wasm](https://kotl.in/wasm) — Web target via Kotlin/Wasm JS
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) — Shared UI layer
- [Material 3](https://m3.material.io/) — Modern UI components and theming

### Architecture & Concurrency

- [Decompose](https://github.com/arkivanov/Decompose) — Lifecycle-aware business logic components
- [Essenty](https://github.com/arkivanov/Essenty) — Instance and state keepers for Decompose
- [Koin](https://github.com/InsertKoinIO/koin) — Dependency injection
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) — Structured concurrency
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html) — Reactive streams

### Data & Crypto

- [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings) — Persistent storage for OTP secrets
- [Kotlin Crypto](https://github.com/kotlincrypto) — HMAC-SHA1/SHA2, SHA-2 hashing for OTP generation
- [Base32 Encoding](https://github.com/amlcurran/encoding) — Secret encoding/decoding
- [Kotlin Datetime](https://github.com/Kotlin/kotlinx-datetime) — Time-based OTP calculations
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization) — JSON and CBOR serialization
- [Ktor](https://ktor.io/) — HTTP client for cloud backup services
- [UUID](https://github.com/benasher44/uuid) — Unique identifier generation
- [Uri KMP](https://github.com/eygraber/uri-kmp) — URI parsing for `otpauth://` QR code links

### Platform-Specific

- **Android**: [AndroidX Security](https://developer.android.com/jetpack/androidx/releases/security) (`EncryptedSharedPreferences`), [Biometric](https://developer.android.com/jetpack/androidx/releases/biometric) API
- **Desktop**: [Webcam Capture](https://github.com/sarxos/webcam-capture/) + [ZXing](https://github.com/zxing/zxing) for QR code scanning
- **iOS**: Native camera integration via Compose Multiplatform

### Compose Extensions

- [Camera Permission](https://github.com/procyk22/compose-extensions) — Cross-platform camera permission handling
- [Camera QR](https://github.com/procyk22/compose-extensions) — Cross-platform QR code scanning UI

### Build & CI/CD

- [Gradle KTS](https://docs.gradle.org/current/userguide/kotlin_dsl.html) — Kotlin DSL build scripts
- [Gradle Version Catalogs](https://developer.android.com/build/migrate-to-catalogs) — Centralized dependency management
- [GitHub Actions](https://github.com/avan1235/open-otp/actions) — Automated builds and releases
- [GitHub Pages](https://pages.github.com/) — Web app deployment (CNAME: open-otp.procyk.in)

## Privacy

OpenOTP is designed with privacy in mind. Your OTP secrets are stored locally on your device and are never transmitted to any server, except when you explicitly choose to back them up to your own cloud storage account. See [PRIVACY_POLICY.md](./PRIVACY_POLICY.md) for details.

## License

This project is licensed under the MIT License — see the [LICENSE.md](./LICENSE.md) file for details.
