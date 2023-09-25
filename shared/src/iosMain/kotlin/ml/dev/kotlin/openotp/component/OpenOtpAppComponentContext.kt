package ml.dev.kotlin.openotp.component

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.*

actual class OpenOtpAppComponentContext {

    actual fun stringResource(resource: StringResource): String =
        StringDesc.Resource(resource).localized()

    actual fun stringResource(resource: StringResource, vararg args: Any): String =
        StringDesc.ResourceFormatted(resource, *args).localized()

    actual fun stringResource(resource: PluralsResource, quantity: Int): String =
        StringDesc.Plural(resource, quantity).localized()

    actual fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String =
        StringDesc.PluralFormatted(resource, quantity, *args).localized()
}