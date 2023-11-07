package ml.dev.kotlin.openotp.component

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.openotp.LINKED_ACCOUNTS_SYNC_STATE_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.USER_LINKED_ACCOUNTS_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.USER_OTP_CODE_DATA_MODULE_QUALIFIER
import ml.dev.kotlin.openotp.backup.*
import ml.dev.kotlin.openotp.backup.SerializedStoredOtpCodeDataBackup.Companion.toSerializedBackup
import ml.dev.kotlin.openotp.otp.StoredOtpCodeData
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.util.StateFlowSettings
import ml.dev.kotlin.openotp.util.unit
import org.koin.core.component.get

abstract class AbstractBackupComponent(
    componentContext: ComponentContext,
) : AbstractComponent(componentContext) {

    protected val _userOtpCodeData: StateFlowSettings<StoredOtpCodeData> =
        get(USER_OTP_CODE_DATA_MODULE_QUALIFIER)

    protected val _linkedAccountsSyncState: MutableStateFlow<LinkedAccountsSyncState> =
        get(LINKED_ACCOUNTS_SYNC_STATE_MODULE_QUALIFIER)

    protected val _userLinkedAccounts: StateFlowSettings<UserLinkedAccountsModel> =
        get(USER_LINKED_ACCOUNTS_MODULE_QUALIFIER)

    protected fun refreshBackup(download: Boolean = false) {
        val linkedAccounts = _userLinkedAccounts.stateFlow.value
        val syncAccountsCount = UserLinkedAccountType.entries.count { it.isLinked(linkedAccounts) }

        if (syncAccountsCount == 0) {
            _linkedAccountsSyncState.value = LinkedAccountsSyncState.NothingToSync
            return
        }
        _linkedAccountsSyncState.value = LinkedAccountsSyncState.Refreshing

        scope.launch {
            var success = true
            try {
                if (download) {
                    downloadBackup(linkedAccounts)
                }
                val backup = _userOtpCodeData.stateFlow.value.toSerializedBackup()
                uploadBackup(linkedAccounts, backup)
            } catch (_: Exception) {
                success = false
            } finally {
                if (!success) _linkedAccountsSyncState.value = LinkedAccountsSyncState.NotSynced
            }
        }
    }

    private suspend fun uploadBackup(
        linkedAccounts: UserLinkedAccountsModel,
        backup: SerializedStoredOtpCodeDataBackup,
    ) {
        supervisorScope {
            val jobs = mutableListOf<Deferred<Boolean>>()
            for (accountType in UserLinkedAccountType.entries) {
                val service = accountType.createAuthenticatedService(linkedAccounts) ?: continue
                jobs += async { service.uploadBackup(backup) }
            }
            val results = jobs.awaitAll()
            when {
                results.isEmpty() -> Unit

                results.all { it } -> {
                    _linkedAccountsSyncState.value = LinkedAccountsSyncState.Synced
                    toast(stringResource(OpenOtpResources.strings.synced_all))
                }

                results.any { it } -> {
                    _linkedAccountsSyncState.value = LinkedAccountsSyncState.Synced
                    toast(stringResource(OpenOtpResources.strings.failed_some))
                }

                else -> {
                    _linkedAccountsSyncState.value = LinkedAccountsSyncState.NotSynced
                    toast(stringResource(OpenOtpResources.strings.failed_all))
                }
            }
        }
    }

    private suspend fun downloadBackup(
        linkedAccounts: UserLinkedAccountsModel,
    ) {
        val currentCodeData = _userOtpCodeData.stateFlow.value
        val updatedCodeData = supervisorScope {
            val jobs = mutableListOf<Deferred<StoredOtpCodeDataBackup?>>()
            for (accountType in UserLinkedAccountType.entries) {
                val service = accountType.createAuthenticatedService(linkedAccounts) ?: continue
                jobs += async { service.downloadBackup() }
            }
            val downloadedData = jobs.awaitAll().filterNotNull()
            downloadedData.merge(current = currentCodeData)
        }
        _userOtpCodeData.updateInScope { updatedCodeData }.unit()
    }
}
