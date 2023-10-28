package ml.dev.kotlin.openotp.backup

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.StoredOtpCodeData

@Serializable
data class StoredOtpCodeDataBackup(
    val codes: StoredOtpCodeData,
    val createdAt: Instant,
)

class SerializedStoredOtpCodeDataBackup(
    private val backup: StoredOtpCodeDataBackup,
) {
    val data: ByteArray by lazy { Cbor.encodeToByteArray(backup) }

    companion object {
        fun StoredOtpCodeData.toSerializedBackup(): SerializedStoredOtpCodeDataBackup {
            val backup = StoredOtpCodeDataBackup(this, createdAt = Clock.System.now())
            return SerializedStoredOtpCodeDataBackup(backup)
        }
    }
}

suspend fun OAuth2AccountService.Authenticated.uploadBackup(
    backup: SerializedStoredOtpCodeDataBackup,
): Boolean {
    val service = this.takeIf { !it.isExpired } ?: refreshUserAccessToken().getOrNull() ?: return false
    return service.uploadBackupData(backup.data).getOrNull() ?: false
}

suspend fun OAuth2AccountService.Authenticated.downloadBackup(): StoredOtpCodeDataBackup? {
    val service = takeIf { !it.isExpired } ?: refreshUserAccessToken().getOrNull() ?: return null
    val data = service.downloadBackupData() ?: return null
    return Cbor.decodeFromByteArray(data)
}

fun List<StoredOtpCodeDataBackup>.merge(current: StoredOtpCodeData): StoredOtpCodeData {
    val newestBackup = maxByOrNull { it.createdAt } ?: return current
    val result = LinkedHashMap<String, OtpData>()
    for (codeData in current) {
        result[codeData.uuid] = codeData
    }
    for (codeData in newestBackup.codes) {
        if (codeData.uuid in result) continue
        result[codeData.uuid] = codeData
    }
    return result.entries.map { it.value }
}
