package ml.dev.kotlin.openotp.util

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext

actual fun createSettings(name: String, context: OpenOtpAppComponentContext): Settings =
    KeychainSettings(name)
