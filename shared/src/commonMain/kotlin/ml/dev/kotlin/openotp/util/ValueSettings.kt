package ml.dev.kotlin.openotp.util

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext


private val SecureStoredMutableValueJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

class ValueSettings<T : Any>(
    private val name: String,
    context: OpenOtpAppComponentContext,
    private val serializer: KSerializer<T>,
    private val default: T,
) {
    private val settings: Settings = createSettings(name, context)

    private val _value: MutableValue<T> = run {
        val storedValue = run init@{
            val result = settings.getStringOrNull(name) ?: return@init null
            SecureStoredMutableValueJson.decodeFromString(serializer, result)
        }
        MutableValue(storedValue ?: default)
    }

    val value: Value<T> = _value

    fun get(): T = _value.value

    fun set(value: T) {
        val storedValue = run update@{
            val encoded = SecureStoredMutableValueJson.encodeToString(serializer, value)
            settings[name] = encoded
            val stored = settings.getStringOrNull(name) ?: return@update default
            SecureStoredMutableValueJson.decodeFromString(serializer, stored)
        }
        _value.value = storedValue
    }
}