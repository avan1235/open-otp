package ml.dev.kotlin.openotp.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ml.dev.kotlin.openotp.util.StateFlowSettings
import ml.dev.kotlin.openotp.util.coroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.map as coroutinesFlowMap
import ml.dev.kotlin.openotp.util.asValue as asValueUtil

abstract class AbstractComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext, KoinComponent {

    protected val scope: CoroutineScope = coroutineScope(Dispatchers.Main.immediate)

    private val snackbarHostState: SnackbarHostState = get<SnackbarHostState>()

    private val openOtpAppComponentContext: OpenOtpAppComponentContext = get<OpenOtpAppComponentContext>()

    fun toast(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
        }
    }

    fun stringResource(resource: StringResource): String =
        openOtpAppComponentContext.stringResource(resource)

    fun stringResource(resource: StringResource, vararg args: Any): String =
        openOtpAppComponentContext.stringResource(resource, *args)

    fun stringResource(resource: PluralsResource, quantity: Int): String =
        openOtpAppComponentContext.stringResource(resource, quantity)

    fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String =
        openOtpAppComponentContext.stringResource(resource, quantity, *args)

    protected fun <T, M> StateFlow<T>.map(
        coroutineScope: CoroutineScope = scope,
        mapper: (value: T) -> M
    ): StateFlow<M> =
        coroutinesFlowMap(mapper)
            .stateIn(
                coroutineScope,
                SharingStarted.Eagerly,
                mapper(value),
            )

    protected fun <T : Any> StateFlow<T>.asValue(
        lifecycle: Lifecycle = this@AbstractComponent.lifecycle,
        context: CoroutineContext = Dispatchers.Main.immediate,
    ): Value<T> = asValueUtil(lifecycle, context)

    protected fun <T : Any> StateFlowSettings<T>.updateInScope(
        scope: CoroutineScope = this@AbstractComponent.scope,
        f: (oldValue: T) -> T,
    ): Deferred<T> = scope.async {
        this@updateInScope.update(f)
    }
}