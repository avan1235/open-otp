package ml.dev.kotlin.openotp.otp

import androidx.compose.runtime.Immutable
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ml.dev.kotlin.openotp.ui.component.DragDropListData

@Immutable
@Serializable
sealed class OtpData {
    abstract val issuer: String?
    abstract val accountName: String?
    abstract val secret: String
    abstract val uuid: String

    abstract fun code(timestamp: Long): String

    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean

    val namePresentation: String? by lazy {
        val issuer = issuer?.takeIf { it.isNotBlank() }
        val accountName = accountName
            ?.takeIf { it.isNotBlank() }
            ?.run {
                when {
                    startsWith("$issuer: ") -> removePrefix("$issuer: ")
                    startsWith("$issuer:") -> removePrefix("$issuer:")
                    else -> this
                }
            }
        when {
            issuer != null && accountName != null -> "$issuer: $accountName"
            issuer != null -> issuer
            accountName != null -> accountName
            else -> null
        }
    }
}

@Immutable
@Serializable
data class HotpData(
    override val issuer: String?,
    override val accountName: String?,
    override val secret: String,
    private val counter: Long,
    private val config: HotpConfig,
    override val uuid: String,
) : OtpData() {
    @Transient
    private val authenticator: HotpGenerator =
        HotpGenerator(secret, config)

    override fun code(timestamp: Long): String =
        authenticator.generate(counter)

    fun increaseCounter(): HotpData =
        copy(counter = counter + 1)

    companion object {
        operator fun invoke(
            issuer: String?,
            accountName: String?,
            secret: String,
            counter: Long,
            config: HotpConfig,
        ): OtpData = HotpData(issuer, accountName, secret, counter, config, uuid4().toString())
    }
}

@Immutable
@Serializable
data class TotpData(
    override val issuer: String?,
    override val accountName: String?,
    override val secret: String,
    private val config: TotpConfig,
    override val uuid: String,
) : OtpData() {
    @Transient
    private val authenticator: TotpGenerator =
        TotpGenerator(secret, config)

    @Transient
    val periodMillis: Int =
        authenticator.config.period.millis.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    override fun code(timestamp: Long): String =
        authenticator.generate(timestamp)

    fun timeslotLeft(timestamp: Long): Double =
        authenticator.timeslotLeft(timestamp)

    companion object {
        operator fun invoke(
            issuer: String?,
            accountName: String?,
            secret: String,
            config: TotpConfig,
        ): OtpData = TotpData(issuer, accountName, secret, config, uuid4().toString())
    }
}

typealias StoredOtpCodeData = List<OtpData>

typealias PresentedOtpCodeData = DragDropListData<OtpData>
