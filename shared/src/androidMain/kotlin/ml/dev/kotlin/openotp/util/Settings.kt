package ml.dev.kotlin.openotp.util

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext

actual fun createSettings(name: String, context: OpenOtpAppComponentContext): Settings =
    SharedPreferencesSettings(context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE))
