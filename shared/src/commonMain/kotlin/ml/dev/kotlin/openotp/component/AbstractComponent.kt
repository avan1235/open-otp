package ml.dev.kotlin.openotp.component

import androidx.compose.material3.SnackbarHostState
import com.arkivanov.decompose.ComponentContext
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

    fun toast(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message, withDismissAction = true)
        }
    }
}