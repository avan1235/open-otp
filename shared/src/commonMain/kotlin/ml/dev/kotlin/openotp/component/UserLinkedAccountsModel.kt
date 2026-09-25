package ml.dev.kotlin.openotp.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import ml.dev.kotlin.openotp.backup.*
import ml.dev.kotlin.openotp.shared.*
import ml.dev.kotlin.openotp.ui.OtpIcons
import ml.dev.kotlin.openotp.ui.icons.Dropbox
import ml.dev.kotlin.openotp.ui.icons.OneDrive
import org.jetbrains.compose.resources.stringResource

@Serializable
data class UserLinkedAccountsModel(
    val dropbox: DropboxRefreshableAccessData? = null,
    val onedrive: OneDriveRefreshableAccessData? = null,
)

enum class UserLinkedAccountType {
    Dropbox {
        override val icon: ImageVector = OtpIcons.Dropbox

        @Composable
        override fun iconContentDescription(): String = stringResource(Res.string.dropbox_name)

        override fun reset(model: UserLinkedAccountsModel) =
            model.copy(dropbox = null)

        override fun isLinked(model: UserLinkedAccountsModel) =
            model.dropbox != null

        override fun createService() =
            DropboxService.Initialized

        override fun createAuthenticatedService(linkedAccounts: UserLinkedAccountsModel): DropboxService.Authenticated? =
            linkedAccounts.dropbox?.let(DropboxService::Authenticated)
    },
    OneDrive {
        override val icon: ImageVector = OtpIcons.OneDrive

        @Composable
        override fun iconContentDescription(): String = stringResource(Res.string.onedrive_name)

        override fun reset(model: UserLinkedAccountsModel) =
            model.copy(onedrive = null)

        override fun isLinked(model: UserLinkedAccountsModel) =
            model.onedrive != null

        override fun createService() =
            OneDriveService.Initialized

        override fun createAuthenticatedService(linkedAccounts: UserLinkedAccountsModel): OneDriveService.Authenticated? =
            linkedAccounts.onedrive?.let(OneDriveService::Authenticated)
    }
    ;

    abstract val icon: ImageVector

    @Composable
    abstract fun iconContentDescription(): String

    abstract fun reset(model: UserLinkedAccountsModel): UserLinkedAccountsModel

    abstract fun isLinked(model: UserLinkedAccountsModel): Boolean

    abstract fun createService(): OAuth2AccountService.Initialized

    abstract fun createAuthenticatedService(linkedAccounts: UserLinkedAccountsModel): OAuth2AccountService.Authenticated?
}