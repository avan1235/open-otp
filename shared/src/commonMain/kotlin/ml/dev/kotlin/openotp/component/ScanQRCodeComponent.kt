package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import com.eygraber.uri.Uri
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.otp.*
import ml.dev.kotlin.openotp.qr.QRResult
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.StateFlowSettings
import ml.dev.kotlin.openotp.util.isValidBase32Secret
import ml.dev.kotlin.openotp.util.letFalse
import org.koin.core.component.get

interface ScanQRCodeComponent {

    fun onQRCodeScanned(result: QRResult): Boolean

    fun onCancelClick()
}

class ScanQRCodeComponentImpl(
    componentContext: ComponentContext,
    private val navigateOnCancel: (message: String?) -> Unit,
) : AbstractComponent(componentContext), ScanQRCodeComponent {

    private val userOtpCodeData: StateFlowSettings<StoredOtpCodeData> = get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    override fun onQRCodeScanned(result: QRResult): Boolean = when (result) {
        is QRResult.QRError -> navigateOnCancel(invalidQRCodeMessage).letFalse()
        is QRResult.QRSuccess -> {
            val distinctOtpData = result.nonEmptyCodes.distinct().map(::extractQRCodeUserOtpCodeData)
            val nonNullOtpData = distinctOtpData.filterNotNull()
            if (distinctOtpData.size != nonNullOtpData.size) {
                navigateOnCancel(invalidQRCodeMessage)
            } else {
                val updated = nonNullOtpData.map { otpData ->
                    userOtpCodeData.updateInScope { it + otpData }
                }
                scope
                    .launch { updated.awaitAll() }
                    .invokeOnCompletion { navigateOnCancel(null) }
            }
            false
        }
    }

    override fun onCancelClick() {
        navigateOnCancel(null)
    }

    private fun extractQRCodeUserOtpCodeData(qrRawValue: String?): OtpData? = try {
        val uri = qrRawValue.uri

        uri.validateScheme()
        val secret = uri.secret
        val issuer = uri.issuer
        val name = uri.label
        val hmacAlgorithm = uri.hmacAlgorithm
        val codeDigits = uri.codeDigits

        when (uri.otpType) {
            OtpType.TOTP -> {
                val period = uri.period
                val config = TotpConfig(
                    period = period ?: TotpConfig.DEFAULT.period,
                    codeDigits = codeDigits ?: TotpConfig.DEFAULT.codeDigits,
                    hmacAlgorithm = hmacAlgorithm ?: TotpConfig.DEFAULT.hmacAlgorithm
                )
                TotpData(issuer, name, secret, config)
            }

            else -> {
                val counter = uri.counter
                val config = HotpConfig(
                    codeDigits = codeDigits ?: HotpConfig.DEFAULT.codeDigits,
                    hmacAlgorithm = hmacAlgorithm ?: HotpConfig.DEFAULT.hmacAlgorithm
                )
                HotpData(issuer, name, secret, counter, config)
            }
        }
    } catch (_: InvalidQRCodeException) {
        null
    }

    private val invalidQRCodeMessage: String
        get() = stringResource(OpenOtpResources.strings.invalid_qr_code)
}

private object InvalidQRCodeException : IllegalArgumentException()

private val Uri.otpType: OtpType
    get() = when (host) {
        "totp" -> OtpType.TOTP
        "hotp" -> OtpType.HOTP
        else -> throw InvalidQRCodeException
    }

private val Uri.secret: String
    get() = getQueryParameter("secret")
        ?.takeIf { it.isValidBase32Secret }
        ?: throw InvalidQRCodeException

private val Uri.issuer: String?
    get() = getQueryParameter("issuer")

private val Uri.label: String?
    get() = path?.removePrefix("/")

private val Uri.hmacAlgorithm: HmacAlgorithm?
    get() = when (getQueryParameter("algorithm")) {
        "sha1" -> HmacAlgorithm.SHA1
        "sha256" -> HmacAlgorithm.SHA256
        "sha512" -> HmacAlgorithm.SHA512
        null -> null
        else -> throw InvalidQRCodeException
    }

private val Uri.codeDigits: OtpDigits?
    get() = when (getQueryParameter("digits")) {
        "6" -> OtpDigits.Six
        "8" -> OtpDigits.Eight
        null -> null
        else -> throw InvalidQRCodeException
    }

private val Uri.period: TotpPeriod?
    get() = when (getQueryParameter("period")) {
        "15" -> TotpPeriod.Fifteen
        "30" -> TotpPeriod.Thirty
        "60" -> TotpPeriod.Sixty
        null -> null
        else -> throw InvalidQRCodeException
    }

private val Uri.counter: HotpCounter
    get() = getQueryParameter("counter")
        ?.toLongOrNull()
        ?.takeIf { it.isValid() }
        ?: throw InvalidQRCodeException

private fun Uri.validateScheme(): Unit =
    if ("otpauth" != scheme) throw InvalidQRCodeException else Unit

private val String?.uri: Uri
    get() = this
        ?.let(Uri.Companion::parseOrNull)
        ?: throw InvalidQRCodeException
