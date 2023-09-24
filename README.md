# OpenOTP

[![Release](https://github.com/avan1235/open-otp/actions/workflows/release.yml/badge.svg)](https://github.com/avan1235/open-otp/releases/latest)

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
