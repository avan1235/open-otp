package ml.dev.kotlin.openotp.util

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext

actual fun createSettings(name: String, context: OpenOtpAppComponentContext): Settings =
    StorageSettings()
