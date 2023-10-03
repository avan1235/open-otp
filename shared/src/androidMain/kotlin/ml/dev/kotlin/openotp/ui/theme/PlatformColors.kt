package ml.dev.kotlin.openotp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
actual fun rememberPlatformColors(top: Color, bottom: Color) {
    val sysUiController = rememberSystemUiController()
    SideEffect {
        sysUiController.setSystemBarsColor(top)
        sysUiController.setNavigationBarColor(bottom)
    }
}
