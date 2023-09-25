# OpenOTP

[![Build](https://img.shields.io/github/actions/workflow/status/avan1235/open-otp/release.yml?label=Build)](https://github.com/avan1235/open-otp/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/avan1235/open-otp?label=Release)](https://github.com/avan1235/open-otp/releases)
[![Google Play](https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dml.dev.kotlin.openotp.OpenOtp%26l%3DGoogle%2520Play%26m%3D%24version)](https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp)

[![License: MIT](https://img.shields.io/badge/License-MIT-red.svg)](./LICENSE.md)
[![GitHub Repo stars](https://img.shields.io/github/stars/avan1235/open-otp?style=social)](https://github.com/avan1235/open-otp/stargazers)
[![Fork OpenOTP](https://img.shields.io/github/forks/avan1235/open-otp?logo=github&style=social)](https://github.com/avan1235/open-otp/fork)

## Download and run application

Android released version is available on
[Google Play](https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp).

<a href='https://play.google.com/store/apps/details?id=ml.dev.kotlin.openotp.OpenOtp'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

You can download compiled version of application from
[release page](https://github.com/avan1235/open-otp/releases).

Client applications are configured to work with the server deployed on test server.
You can download them to play with "production" version of application - to run it locally,
you should compile server and clients by yourself.

Please note that for running unsigned version of macOS application, you need to temporarily
disable Gatekeeper, so executing command

```shell
sudo xattr -dr com.apple.quarantine  /Applications/OpenOTP.app
```

is required to be able to run the app on macOS. You can learn more about this
[here](https://web.archive.org/web/20230318124537/https://disable-gatekeeper.github.io/).

To install Linux version run:

```shell
sudo dpkg -i  openotp.deb
```

## Tech Stack

### Core

- 100% [Kotlin](https://kotlinlang.org/)
- 100% [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [Material3 design](https://m3.material.io/) (UI components)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (structured concurrency)
- [Koin](https://insert-koin.io/) (DI)

### Local Persistence

- [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings)
- [Android Security](https://developer.android.com/jetpack/androidx/releases/security) (Android `SharedPreferences`
  implemented with `EncryptedSharedPreferences`)

### Build & CI

- [Gradle KTS](https://docs.gradle.org/current/userguide/kotlin_dsl.html) (Kotlin DSL)
- [Gradle version catalogs](https://developer.android.com/build/migrate-to-catalogs) (dependencies versions)
- [Github Actions](https://github.com/avan1235/open-otp/actions) (CI/CD)
