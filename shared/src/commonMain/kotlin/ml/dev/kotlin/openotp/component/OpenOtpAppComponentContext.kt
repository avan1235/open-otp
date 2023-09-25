package ml.dev.kotlin.openotp.component

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

expect class OpenOtpAppComponentContext {
    fun stringResource(resource: StringResource): String
    fun stringResource(resource: StringResource, vararg args: Any): String
    fun stringResource(resource: PluralsResource, quantity: Int): String
    fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String
}
