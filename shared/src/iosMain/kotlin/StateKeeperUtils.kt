import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import platform.Foundation.NSCoder
import platform.Foundation.NSString
import platform.Foundation.decodeTopLevelObjectOfClass
import platform.Foundation.encodeObject

private val json: Json = Json {
    allowStructuredMapKeys = true
}

private const val STATE_KEY = "open-otp-saved-state"

@Suppress("UNUSED")
fun save(coder: NSCoder, state: SerializableContainer) {
    coder.encodeObject(`object` = json.encodeToString(SerializableContainer.serializer(), state), forKey = STATE_KEY)
}

@Suppress("UNUSED")
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun restore(coder: NSCoder): SerializableContainer? {
    val string = coder.decodeTopLevelObjectOfClass(aClass = NSString, forKey = STATE_KEY, error = null) as? String? ?: return null
    return try {
        json.decodeFromString(SerializableContainer.serializer(), string)
    } catch (e: Exception) {
        null
    }
}