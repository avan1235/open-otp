package ml.dev.kotlin.openotp.component

import androidx.compose.material3.SnackbarHostState
import com.arkivanov.decompose.ComponentContext
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.util.coroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

abstract class AbstractComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext, KoinComponent {

    protected val scope: CoroutineScope = coroutineScope(Dispatchers.Main)

    private val snackbarHostState: SnackbarHostState = get<SnackbarHostState>()

    private val openOtpAppComponentContext: OpenOtpAppComponentContext = get<OpenOtpAppComponentContext>()

    fun toast(message: String, withDismissAction: Boolean = true) {
        scope.launch {
            snackbarHostState.showSnackbar(message, withDismissAction = withDismissAction)
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
}