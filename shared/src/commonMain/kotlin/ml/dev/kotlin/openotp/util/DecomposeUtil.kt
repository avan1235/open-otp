package ml.dev.kotlin.openotp.util

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun LifecycleOwner.coroutineScope(context: CoroutineContext): CoroutineScope {
    val scope = CoroutineScope(context)
    lifecycle.doOnDestroy(scope::cancel)
    return scope
}

fun <T : Any> StateFlow<T>.asValue(
    lifecycle: Lifecycle,
    context: CoroutineContext = Dispatchers.Main.immediate,
): Value<T> =
    asValue(
        initialValue = value,
        lifecycle = lifecycle,
        context = context,
    )

fun <T : Any> Flow<T>.asValue(
    initialValue: T,
    lifecycle: Lifecycle,
    context: CoroutineContext = Dispatchers.Main.immediate,
): Value<T> {
    val value = MutableValue(initialValue)
    var scope: CoroutineScope? = null

    lifecycle.subscribe(
        object : Lifecycle.Callbacks {
            override fun onStart() {
                scope = CoroutineScope(context).apply {
                    launch {
                        collect { value.value = it }
                    }
                }
            }

            override fun onStop() {
                scope?.cancel()
                scope = null
            }
        }
    )

    return value
}
