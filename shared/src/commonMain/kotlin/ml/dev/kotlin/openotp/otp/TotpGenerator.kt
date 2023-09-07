package ml.dev.kotlin.openotp.otp

import kotlin.math.floor


class TotpGenerator(
    secret: String,
    val config: TotpConfig,
) {
    private val hotpGenerator: HotpGenerator =
        HotpGenerator(secret, config.toHotpConfig())

    private fun counter(timestamp: Long): Long {
        val millis = config.period.millis
        if (millis <= 0L) {
            return 0
        }
        return floor(timestamp.toDouble().div(millis)).toLong()
    }

    fun generate(timestamp: Long): String =
        hotpGenerator.generate(counter(timestamp))

    private fun timeslotStart(timestamp: Long): Long {
        val counter = counter(timestamp)
        val timeStepMillis = config.period.millis.toDouble()
        return (counter * timeStepMillis).toLong()
    }

    fun timeslotLeft(timestamp: Long): Double {
        val diff = timestamp - timeslotStart(timestamp)
        return 1.0 - diff.toDouble() / config.period.millis.toDouble()
    }
}