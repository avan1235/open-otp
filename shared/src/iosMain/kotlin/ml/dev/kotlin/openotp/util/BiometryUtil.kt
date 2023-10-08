package ml.dev.kotlin.openotp.util

import androidx.compose.runtime.Composable
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import platform.Foundation.NSError
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.performBlock
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicy
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class BiometryAuthenticator {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun checkBiometryAuthentication(
        requestTitle: String,
        requestReason: String,
    ): Boolean {
        val laContext = LAContext()

        val (canEvaluate, error) = memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val canEvaluate = runCatchingOrNull {
                laContext.canEvaluatePolicy(POLICY, error = error.ptr)
            }
            canEvaluate to error.value
        }

        if (error != null) throw error.toException()
        if (canEvaluate == null) return false

        return callbackToCoroutine { callback ->
            laContext.evaluatePolicy(
                policy = POLICY,
                localizedReason = requestReason,
                reply = mainContinuation { result: Boolean, error: NSError? ->
                    callback(result, error)
                }
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun isBiometricAvailable(): Boolean {
        return LAContext().canEvaluatePolicy(POLICY, error = null)
    }
}

@Composable
actual fun BindBiometryAuthenticatorEffect(biometryAuthenticator: BiometryAuthenticator) = Unit

private inline fun <T1, T2> mainContinuation(
    noinline block: (T1, T2) -> Unit,
): (T1, T2) -> Unit = { arg1, arg2 ->
    if (NSThread.isMainThread()) {
        block.invoke(arg1, arg2)
    } else {
        MainRunDispatcher.run {
            block.invoke(arg1, arg2)
        }
    }
}

private object MainRunDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) =
        NSRunLoop.mainRunLoop.performBlock { block.run() }
}

private suspend fun <T> callbackToCoroutine(callbackCall: ((T?, NSError?) -> Unit) -> Unit): T =
    suspendCoroutine { continuation ->
        callbackCall { data, error ->
            when {
                data != null -> continuation.resume(data)
                else -> continuation.resumeWithException(error.toException())
            }
        }
    }

private fun NSError?.toException(): Exception = when {
    this == null -> NullPointerException("NSError is null")
    else -> Exception(this.description())
}

private val POLICY: LAPolicy = LAPolicyDeviceOwnerAuthenticationWithBiometrics
