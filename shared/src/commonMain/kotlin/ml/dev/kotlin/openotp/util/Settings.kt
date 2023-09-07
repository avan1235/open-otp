package ml.dev.kotlin.openotp.util

import com.russhwolf.settings.Settings
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext

expect fun createSettings(name: String, context: OpenOtpAppComponentContext): Settings
