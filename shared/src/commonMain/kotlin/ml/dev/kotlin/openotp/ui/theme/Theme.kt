package ml.dev.kotlin.openotp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ml.dev.kotlin.openotp.component.OpenOtpAppComponent

@Composable
internal fun OpenOtpTheme(
    component: OpenOtpAppComponent,
    content: @Composable () -> Unit
) {
    val theme by component.theme.subscribeAsState()

    MaterialTheme(
        colorScheme = theme.colorScheme(),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}