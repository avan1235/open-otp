package ml.dev.kotlin.openotp.util

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import java.util.prefs.Preferences

actual fun createSettings(name: String, context: OpenOtpAppComponentContext): Settings =
    PreferencesSettings(Preferences.userRoot())
