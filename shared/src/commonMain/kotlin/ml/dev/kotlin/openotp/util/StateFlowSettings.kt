package ml.dev.kotlin.openotp.util

import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toSuspendSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext


private val SecureStoredMutableValueJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

class StateFlowSettings<T : Any>(
    private val name: String,
    context: OpenOtpAppComponentContext,
    private val serializer: KSerializer<T>,
    private val default: T,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val settings: SuspendSettings = createSettings(name, context).toSuspendSettings(dispatcher)

    private suspend fun readStoredValue(): T = when (val readEncoded = settings.getStringOrNull(name)) {
        null -> default
        else -> SecureStoredMutableValueJson.decodeFromString(serializer, readEncoded)
    }

    private suspend fun writeStoredValue(value: T) {
        val encoded = SecureStoredMutableValueJson.encodeToString(serializer, value)
        settings.putString(name, encoded)
    }

    private val _stateFlow: MutableStateFlow<T> = MutableStateFlow(runBlocking(dispatcher) { readStoredValue() })
    val stateFlow: StateFlow<T> = _stateFlow.asStateFlow()

    suspend fun update(function: (oldValue: T) -> T): T =
        _stateFlow.updateAndGet { current ->
            function(current).also { writeStoredValue(it) }
        }
}