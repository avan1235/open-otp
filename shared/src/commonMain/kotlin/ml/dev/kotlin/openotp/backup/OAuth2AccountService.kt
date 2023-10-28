package ml.dev.kotlin.openotp.backup

import ml.dev.kotlin.openotp.component.UserLinkedAccountsModel

sealed interface OAuth2AccountService {

    interface Initialized : OAuth2AccountService {

        fun requestPermissions(): RequestedPermissions?
    }

    interface RequestedPermissions : OAuth2AccountService {

        fun generateVerifyUri(): String

        suspend fun authenticateUser(userCode: String): Result<Authenticated>
    }

    interface Authenticated : OAuth2AccountService {

        val isExpired: Boolean

        fun updateUserLinkedAccounts(linkedAccounts: UserLinkedAccountsModel): UserLinkedAccountsModel

        suspend fun refreshUserAccessToken(): Result<Authenticated>

        suspend fun uploadBackupData(data: ByteArray): Result<Boolean>

        suspend fun downloadBackupData(): ByteArray?
    }
}
