package ml.dev.kotlin.openotp.component

import android.content.Context
import androidx.activity.ComponentActivity

actual class OpenOtpAppComponentContext(activity: ComponentActivity) {
    val applicationContext: Context = activity.applicationContext
}
