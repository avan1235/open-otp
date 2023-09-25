package ml.dev.kotlin.openotp.component

import android.content.Context
import androidx.activity.ComponentActivity
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.*

actual class OpenOtpAppComponentContext(activity: ComponentActivity) {
    val applicationContext: Context = activity.applicationContext

    actual fun stringResource(resource: StringResource): String =
        StringDesc.Resource(resource).toString(applicationContext)

    actual fun stringResource(resource: StringResource, vararg args: Any): String =
        StringDesc.ResourceFormatted(resource, *args).toString(applicationContext)

    actual fun stringResource(resource: PluralsResource, quantity: Int): String =
        StringDesc.Plural(resource, quantity).toString(applicationContext)

    actual fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String =
        StringDesc.PluralFormatted(resource, quantity, *args).toString(applicationContext)
}
