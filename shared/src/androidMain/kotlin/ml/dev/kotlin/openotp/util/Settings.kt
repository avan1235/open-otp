package ml.dev.kotlin.openotp.util

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext

actual fun createSettings(name: String, context: OpenOtpAppComponentContext): Settings {
    val masterKey = MasterKey.Builder(context.applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    val sharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        name,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    return SharedPreferencesSettings(sharedPreferences)
}
