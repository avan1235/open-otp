package ml.dev.kotlin.openotp.backup

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ml.dev.kotlin.openotp.component.UserLinkedAccountsModel
import ml.dev.kotlin.openotp.util.createJsonHttpClient
import ml.dev.kotlin.openotp.util.randomBytesChallenge
import ml.dev.kotlin.openotp.util.safeHttpRequest
import ml.dev.kotlin.openotp.util.safeRequest
import org.kotlincrypto.hash.sha2.SHA256
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

sealed class DropboxService : OAuth2AccountService {

    protected val client: HttpClient by lazy(::createJsonHttpClient)

    data object Initialized : DropboxService(), OAuth2AccountService.Initialized {
        override fun requestPermissions(): RequestedPermissions? {
            val bytes = randomBytesChallenge(count = 32) ?: return null
            val codeVerifier = bytes.dropboxEncodeBase64()
            val codeChallenge = SHA256().digest(codeVerifier.encodeToByteArray()).dropboxEncodeBase64()
            val accessData = DropboxAccessData(codeVerifier, codeChallenge)
            return RequestedPermissions(accessData)
        }
    }

    class RequestedPermissions(
        private val accessData: DropboxAccessData,
    ) : DropboxService(), OAuth2AccountService.RequestedPermissions {

        override fun generateVerifyUri(): String =
            "https://www.dropbox.com/oauth2/authorize" +
                    "?client_id=$CLIENT_ID" +
                    "&response_type=code" +
                    "&token_access_type=offline" +
                    "&code_challenge=${accessData.codeChallenge}" +
                    "&code_challenge_method=S256"

        override suspend fun authenticateUser(userCode: String): Result<Authenticated> = client
            .safeRequest<DropboxOAuth2RefreshableTokenResponse> {
                method = HttpMethod.Post
                url("https://api.dropbox.com/oauth2/token")
                parameter("code", userCode)
                parameter("grant_type", "authorization_code")
                parameter("code_verifier", accessData.codeVerifier)
                parameter("client_id", CLIENT_ID)
            }
            .map { response ->
                val refreshableAccessData = response.toDropboxRefreshableAccessData()
                Authenticated(refreshableAccessData)
            }
    }

    class Authenticated(
        private val refreshableAccessData: DropboxRefreshableAccessData,
    ) : DropboxService(), OAuth2AccountService.Authenticated {

        override val isExpired: Boolean
            get() = Clock.System.now() >= refreshableAccessData.expiresAt

        override suspend fun refreshUserAccessToken(): Result<Authenticated> = client
            .safeRequest<DropboxOAuth2TokenResponse> {
                method = HttpMethod.Post
                url("https://api.dropbox.com/oauth2/token")
                parameter("grant_type", "refresh_token")
                parameter("refresh_token", refreshableAccessData.refreshToken)
                parameter("client_id", CLIENT_ID)
            }
            .map { response ->
                val refreshableAccessData = response.toDropboxRefreshableAccessData(refreshableAccessData.refreshToken)
                Authenticated(refreshableAccessData)
            }

        override suspend fun uploadBackupData(data: ByteArray): Result<Boolean> {
            val contentHash = data.dropboxContentHash()
            val apiArg = DropboxJson.encodeToString(DropboxUploadArg(contentHash))
            return client.safeRequest<DropboxUploadResponse> {
                method = HttpMethod.Post
                url("https://content.dropboxapi.com/2/files/upload")
                header(HttpHeaders.Authorization, "Bearer ${refreshableAccessData.accessToken}")
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                header(HttpHeaders.DropboxApiArg, apiArg)
                setBody(ByteArrayContent(data))
            }.map {
                it.contentHash == contentHash
            }
        }

        override suspend fun downloadBackupData(): ByteArray? {
            val apiArg = DropboxJson.encodeToString(DropboxDownloadArg())
            return client.safeHttpRequest {
                method = HttpMethod.Post
                url("https://content.dropboxapi.com/2/files/download")
                header(HttpHeaders.Authorization, "Bearer ${refreshableAccessData.accessToken}")
                header(HttpHeaders.DropboxApiArg, apiArg)
            }.map { response ->
                val header = response.headers[HttpHeaders.DropboxApiResult] ?: return@map null
                val downloadResponse = DropboxJson.decodeFromString<DropboxDownloadResponse>(header)
                val bytes = response.readBytes()
                bytes.takeIf { it.dropboxContentHash() == downloadResponse.contentHash }
            }.getOrNull()
        }

        override fun updateUserLinkedAccounts(linkedAccounts: UserLinkedAccountsModel): UserLinkedAccountsModel =
            linkedAccounts.copy(dropbox = refreshableAccessData)
    }
}

private val HttpHeaders.DropboxApiArg: String get() = "Dropbox-Api-Arg"
private val HttpHeaders.DropboxApiResult: String get() = "Dropbox-Api-Result"

data class DropboxAccessData(
    val codeVerifier: String,
    val codeChallenge: String,
)

@Serializable
data class DropboxRefreshableAccessData(
    val expiresAt: Instant,
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
private data class DropboxOAuth2RefreshableTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresInSeconds: Int,
    @SerialName("token_type") val tokenType: TokenType,
    @SerialName("refresh_token") val refreshToken: String,
) {
    @Serializable
    enum class TokenType { bearer }
}

@Serializable
private data class DropboxOAuth2TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresInSeconds: Int,
    @SerialName("token_type") val tokenType: TokenType,
) {
    @Serializable
    enum class TokenType { bearer }
}

@Serializable
private data class DropboxUploadArg(
    @SerialName("content_hash") val contentHash: String,
    @SerialName("path") val path: String = BACKUP_PATH,
    @SerialName("autorename") val autoRename: Boolean = false,
    @SerialName("mode") val mode: WriteMode = WriteMode.overwrite,
    @SerialName("mute") val mute: Boolean = true,
    @SerialName("strict_conflict") val strictConflict: Boolean = false,
) {
    @Serializable
    enum class WriteMode { add, overwrite, update }
}

@Serializable
private data class DropboxUploadResponse(
    @SerialName("content_hash") val contentHash: String,
)

@Serializable
private data class DropboxDownloadArg(
    @SerialName("path") val path: String = BACKUP_PATH,
)

@Serializable
private data class DropboxDownloadResponse(
    @SerialName("content_hash") val contentHash: String,
)

private val DropboxJson: Json = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
}

private fun DropboxOAuth2RefreshableTokenResponse.toDropboxRefreshableAccessData(): DropboxRefreshableAccessData =
    DropboxRefreshableAccessData(
        expiresAt = Clock.System.now() + expiresInSeconds.seconds / 2,
        accessToken = accessToken,
        refreshToken = refreshToken,
    )

private fun DropboxOAuth2TokenResponse.toDropboxRefreshableAccessData(refreshToken: String): DropboxRefreshableAccessData =
    DropboxRefreshableAccessData(
        expiresAt = Clock.System.now() + expiresInSeconds.seconds / 2,
        accessToken = accessToken,
        refreshToken = refreshToken,
    )

private const val CLIENT_ID: String = "gr00g1l67sjv015"

private const val BACKUP_PATH: String = "/OpenOTP.backup"

private fun ByteArray.dropboxEncodeBase64(): String = encodeBase64()
    .replace('+', '-')
    .replace('/', '_')
    .replace("=", "")

private fun ByteArray.dropboxContentHash(blockSize: Int = 4_194_304): String {
    var offset = 0
    val hashes = mutableListOf<ByteArray>()
    while (offset <= lastIndex) {
        val end = min(offset + blockSize - 1, lastIndex)
        val len = end - offset + 1
        val hash = SHA256().apply { update(this@dropboxContentHash, offset, len) }.digest()
        hashes += hash
        offset += blockSize
    }
    val concatenated = ByteArray(hashes.size * SHA256_BITS_COUNT) { index ->
        val blockNumber = index / SHA256_BITS_COUNT
        val indexInBlock = index - (blockNumber * SHA256_BITS_COUNT)
        val block = hashes[blockNumber]
        block[indexInBlock]
    }
    return SHA256().digest(concatenated).toHexString()
}

private const val SHA256_BITS_COUNT: Int = 32
