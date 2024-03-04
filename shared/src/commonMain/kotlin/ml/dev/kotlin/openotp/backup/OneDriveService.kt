package ml.dev.kotlin.openotp.backup

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ml.dev.kotlin.openotp.component.UserLinkedAccountsModel
import ml.dev.kotlin.openotp.util.createJsonHttpClient
import ml.dev.kotlin.openotp.util.randomBytesChallenge
import ml.dev.kotlin.openotp.util.safeHttpRequest
import ml.dev.kotlin.openotp.util.safeRequest
import org.kotlincrypto.hash.sha2.SHA256
import kotlin.time.Duration.Companion.seconds

sealed class OneDriveService : OAuth2AccountService {

    protected val client: HttpClient by lazy(::createJsonHttpClient)

    data object Initialized : OneDriveService(), OAuth2AccountService.Initialized {
        override fun requestPermissions(): RequestedPermissions? {
            val bytes = randomBytesChallenge(count = 32) ?: return null
            val codeVerifier = bytes.oneDriveEncodeBase64()
            val codeChallenge = SHA256().digest(codeVerifier.encodeToByteArray()).oneDriveEncodeBase64()
            val accessData = OneDriveAccessData(codeVerifier, codeChallenge)
            return RequestedPermissions(accessData)
        }
    }

    class RequestedPermissions(
        private val accessData: OneDriveAccessData,
    ) : OneDriveService(), OAuth2AccountService.RequestedPermissions {

        override fun generateVerifyUri(): String =
            "https://login.microsoftonline.com/common/oauth2/v2.0/authorize" +
                    "?client_id=$CLIENT_ID" +
                    "&response_type=code" +
                    "&redirect_uri=https%3A%2F%2Fopen-otp.procyk.in" +
                    "&response_mode=fragment" +
                    "&scope=offline_access+files.readwrite.all" +
                    "&code_challenge=${accessData.codeChallenge}" +
                    "&code_challenge_method=S256"

        override suspend fun authenticateUser(userCode: String): Result<Authenticated> =
            client.safeRequest<OneDriveOAuth2RefreshableTokenResponse> {
                method = HttpMethod.Post
                url("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                setBody(FormDataContent(parameters {
                    append("code", userCode)
                    append("grant_type", "authorization_code")
                    append("code_verifier", accessData.codeVerifier)
                    append("client_id", CLIENT_ID)
                    append("redirect_uri", "https://open-otp.procyk.in")
                    append("scope", "files.readwrite.all")
                }))
            }.map { response ->
                val refreshableAccessData = response.toOneDriveRefreshableAccessData()
                Authenticated(refreshableAccessData)
            }

    }

    class Authenticated(
        private val refreshableAccessData: OneDriveRefreshableAccessData,
    ) : OneDriveService(), OAuth2AccountService.Authenticated {

        override val isExpired: Boolean
            get() = Clock.System.now() >= refreshableAccessData.expiresAt

        override suspend fun refreshUserAccessToken(): Result<Authenticated> =
            client.safeRequest<OneDriveOAuth2TokenResponse> {
                method = HttpMethod.Post
                url("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                setBody(FormDataContent(parameters {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshableAccessData.refreshToken)
                    append("client_id", CLIENT_ID)
                    append("scope", "files.readwrite.all")
                }))
            }.map { response ->
                val refreshableAccessData = response.toOneDriveRefreshableAccessData(refreshableAccessData.refreshToken)
                Authenticated(refreshableAccessData)
            }

        override suspend fun uploadBackupData(data: ByteArray): Result<Boolean> =
            client.safeRequest<OneDriveUploadResponse> {
                method = HttpMethod.Put
                url("https://graph.microsoft.com/v1.0/me/drive/root:/$BACKUP_PATH:/content")
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                header(HttpHeaders.Authorization, "Bearer ${refreshableAccessData.accessToken}")
                setBody(ByteArrayContent(data))
            }.map { response ->
                response.file.hashes.sha256Hash == data.oneDriveContentHash()
            }

        override suspend fun downloadBackupData(): ByteArray? =
            client.safeHttpRequest {
                method = HttpMethod.Get
                url("https://graph.microsoft.com/v1.0/me/drive/root:/$BACKUP_PATH:/content")
                header(HttpHeaders.Authorization, "Bearer ${refreshableAccessData.accessToken}")
            }.map { response ->
                val downloadLocation = response.headers[HttpHeaders.ContentLocation] ?: return@map null
                client.safeHttpRequest {
                    method = HttpMethod.Get
                    url(downloadLocation)
                }
                    .map { it.readBytes() }
                    .getOrNull()
            }.getOrNull()

        override fun updateUserLinkedAccounts(linkedAccounts: UserLinkedAccountsModel): UserLinkedAccountsModel =
            linkedAccounts.copy(onedrive = refreshableAccessData)
    }
}

data class OneDriveAccessData(
    val codeVerifier: String,
    val codeChallenge: String,
)

@Serializable
data class OneDriveRefreshableAccessData(
    val expiresAt: Instant,
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
private data class OneDriveOAuth2RefreshableTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresInSeconds: Int,
    @SerialName("token_type") val tokenType: TokenType,
    @SerialName("refresh_token") val refreshToken: String,
) {
    @Serializable
    enum class TokenType { Bearer }
}

@Serializable
private data class OneDriveOAuth2TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresInSeconds: Int,
    @SerialName("token_type") val tokenType: TokenType,
) {
    @Serializable
    enum class TokenType { Bearer }
}

@Serializable
private data class OneDriveUploadResponse(
    @SerialName("file") val file: File,
) {
    @Serializable
    data class File(
        @SerialName("hashes") val hashes: Hashes,
    ) {
        @Serializable
        data class Hashes(
            @SerialName("sha256Hash") val sha256Hash: String,
        )
    }
}

private fun OneDriveOAuth2RefreshableTokenResponse.toOneDriveRefreshableAccessData(): OneDriveRefreshableAccessData =
    OneDriveRefreshableAccessData(
        expiresAt = Clock.System.now() + expiresInSeconds.seconds / 2,
        accessToken = accessToken,
        refreshToken = refreshToken,
    )

private fun OneDriveOAuth2TokenResponse.toOneDriveRefreshableAccessData(refreshToken: String): OneDriveRefreshableAccessData =
    OneDriveRefreshableAccessData(
        expiresAt = Clock.System.now() + expiresInSeconds.seconds / 2,
        accessToken = accessToken,
        refreshToken = refreshToken,
    )

private const val CLIENT_ID: String = "8612f175-11d3-4dea-960a-d2cb89867a33"

private const val BACKUP_PATH: String = "OpenOTP/OpenOTP.backup"

private fun ByteArray.oneDriveEncodeBase64(): String = encodeBase64()
    .replace('+', '-')
    .replace('/', '_')
    .replace("=", "")

private fun ByteArray.oneDriveContentHash(): String =
    SHA256().digest(this).toHexString(format = HexFormat.UpperCase)
