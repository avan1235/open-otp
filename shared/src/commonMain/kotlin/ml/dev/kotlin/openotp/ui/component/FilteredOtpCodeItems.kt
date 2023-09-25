package ml.dev.kotlin.openotp.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.otp.UserOtpCodeData
import ml.dev.kotlin.openotp.shared.OpenOtpResources

@Composable
internal fun FilteredOtpCodeItems(
    codeData: UserOtpCodeData,
    timestamp: Long,
    searchActive: Boolean,
    onOtpCodeDataDismiss: (OtpData) -> Boolean,
    onSearchBarActiveChange: (Boolean) -> Unit,
    onRestartCode: (OtpData) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }
            .zIndex(1f),
        contentAlignment = Alignment.TopCenter,
    ) {
        var searchQuery by rememberSaveable { mutableStateOf("") }
        val filteredCodeData = remember(searchQuery, codeData) {
            if (searchQuery.isEmpty()) emptyList() else codeData.filter {
                it.accountName?.contains(searchQuery, ignoreCase = true) == true ||
                        it.issuer?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        val interactionSource = remember { MutableInteractionSource() }
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            active = searchActive,
            onActiveChange = onSearchBarActiveChange,
            placeholder = {
                Text(
                    text = stringResource(OpenOtpResources.strings.search_field),
                    style = searchBarTextStyle(enabled = false, interactionSource)
                )
            },
            leadingIcon = {
                when (searchActive) {
                    false -> Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(OpenOtpResources.strings.search_icon_name),
                    )

                    true -> IconButton(
                        onClick = {
                            onSearchBarActiveChange(false)
                            searchQuery = ""
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(OpenOtpResources.strings.back_icon_name)
                        )
                    }
                }
            },
            trailingIcon = {
                if (searchActive && searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(OpenOtpResources.strings.remove_icon_name),
                        )
                    }
                }
            },
            interactionSource = interactionSource,
        ) {
            OtpCodeItems(filteredCodeData, timestamp, onOtpCodeDataDismiss, onRestartCode)
        }
    }
}
