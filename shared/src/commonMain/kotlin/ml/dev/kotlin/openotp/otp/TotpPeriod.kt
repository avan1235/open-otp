package ml.dev.kotlin.openotp.otp

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DateTimeUnit.Companion.SECOND
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class TotpPeriod(
    private val step: Long,
    private val unit: DateTimeUnit.TimeBased,
) : Named {
    Fifteen(15, SECOND), Thirty(30, SECOND), Sixty(60, SECOND);

    @Transient
    override val presentableName: String =
        "$step ${unit.toString().toLowerCase(Locale.current)}${if (step != 1L) "s" else ""}"

    @Transient
    val millis: Long = Instant
        .fromEpochMilliseconds(0L)
        .plus(step, unit)
        .toEpochMilliseconds()
}



