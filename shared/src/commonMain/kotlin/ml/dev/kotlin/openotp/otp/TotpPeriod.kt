package ml.dev.kotlin.openotp.otp

import androidx.compose.runtime.Composable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DateTimeUnit.Companion.SECOND
import kotlin.time.Instant
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ml.dev.kotlin.openotp.shared.*
import ml.dev.kotlin.openotp.util.Named
import org.jetbrains.compose.resources.pluralStringResource

@Serializable
enum class TotpPeriod(
    private val step: Int,
    private val unit: DateTimeUnit.TimeBased,
) : Named {
    Fifteen(15, SECOND), Thirty(30, SECOND), Sixty(60, SECOND);

    @Composable
    override fun presentableName(): String = when (unit) {
        SECOND -> pluralStringResource(Res.plurals.totp_period_second_unit_presentation, step, step)
        else -> throw IllegalArgumentException("$unit is not localised")
    }

    @Transient
    val millis: Long = Instant
        .fromEpochMilliseconds(0L)
        .plus(step, unit)
        .toEpochMilliseconds()
}
