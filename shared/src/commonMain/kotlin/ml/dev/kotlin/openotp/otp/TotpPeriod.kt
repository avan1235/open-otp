package ml.dev.kotlin.openotp.otp

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DateTimeUnit.Companion.SECOND
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class TotpPeriod(
    private val step: Int,
    private val unit: DateTimeUnit.TimeBased,
) : Named {
    Fifteen(15, SECOND), Thirty(30, SECOND), Sixty(60, SECOND);

    @Transient
    override val OpenOtpAppComponentContext.presentableName: String
        get() = when (unit) {
            SECOND -> stringResource(OpenOtpResources.plurals.totp_period_second_unit_presentation, step, step)
            else -> throw IllegalArgumentException("$unit is not localised")
        }

    @Transient
    val millis: Long = Instant
        .fromEpochMilliseconds(0L)
        .plus(step, unit)
        .toEpochMilliseconds()
}



