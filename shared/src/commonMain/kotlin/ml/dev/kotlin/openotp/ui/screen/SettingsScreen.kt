package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.component.OpenOtpAppTheme
import ml.dev.kotlin.openotp.component.SettingsComponent
import ml.dev.kotlin.openotp.component.SettingsComponentImpl
import ml.dev.kotlin.openotp.component.SettingsComponentImpl.Linked
import ml.dev.kotlin.openotp.component.SettingsComponentImpl.Unlinked
import ml.dev.kotlin.openotp.component.SortOtpDataBy
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.component.NamedBox
import ml.dev.kotlin.openotp.ui.component.NamedDropdownMenu
import ml.dev.kotlin.openotp.ui.component.NamedSwitch
import ml.dev.kotlin.openotp.util.TopBarClickableIconScreen

@Composable
internal fun SettingsScreen(
    component: SettingsComponent,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    TopBarClickableIconScreen(
        onIconClick = component::onExitSettings,
        accent = accent,
        text = stringResource(OpenOtpResources.strings.settings_screen_name),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = 64.dp,
                    start = 20.dp,
                    end = 20.dp,
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            LookAndFeelSettingsGroup(component)
            CodesManagementSettingsGroup(component)
            SecuritySettingsGroup(component)
            CloudBackupsSettingsGroup(component)
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun CloudBackupsSettingsGroup(component: SettingsComponent) {
    SettingsGroup(
        name = stringResource(OpenOtpResources.strings.cloud_backups_group_name),
    ) {
        val states by component.linkedAccountsStates.subscribeAsState()
        for (state in states) {
            LinkdedAccountState(state)
        }
    }
}

@Composable
private fun LinkdedAccountState(accountState: SettingsComponentImpl.LinkedAccountState) {
    NamedBox(
        icon = accountState.icon,
        name = accountState.iconContentDescription(),
        iconModifier = Modifier.alpha(
            alpha = when (accountState) {
                is Unlinked -> 0.7f
                is Linked -> 1.0f
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.End,
        ) {
            ElevatedButton(onClick = accountState::onClick) {
                Text(text = accountState.presentableName())
            }
        }
    }
}

@Composable
private fun SecuritySettingsGroup(component: SettingsComponent) {
    if (!component.isAuthenticationAvailable) return

    SettingsGroup(
        name = stringResource(OpenOtpResources.strings.security_group_name),
    ) {
        val requireAuthentication by component.requireAuthentication.subscribeAsState()
        NamedSwitch(
            name = stringResource(OpenOtpResources.strings.require_authentication),
            checked = requireAuthentication,
            onCheckedChange = component::onRequireAuthenticationChange,
            nameModifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}

@Composable
private fun CodesManagementSettingsGroup(component: SettingsComponent) {
    SettingsGroup(
        name = stringResource(OpenOtpResources.strings.codes_management_group_name),
    ) {
        val confirmOtpDataDelete by component.confirmOtpDataDelete.subscribeAsState()
        NamedSwitch(
            name = stringResource(OpenOtpResources.strings.confirm_codes_deletion),
            checked = confirmOtpDataDelete,
            onCheckedChange = component::onConfirmOtpDataDeleteChange,
        )
        Divider()

        val sortOtpDataBy by component.sortOtpDataBy.subscribeAsState()
        NamedDropdownMenu(
            name = stringResource(OpenOtpResources.strings.sort_type),
            selected = sortOtpDataBy,
            onSelected = component::onSelectedSortType,
            anyItems = SortOtpDataBy.entries
        )
        val canReorderDataManually by component.canReorderDataManually.subscribeAsState()
        val reorderManuallyVisibleState =
            remember { MutableTransitionState(false) }.apply { targetState = canReorderDataManually }
        AnimatedVisibility(
            visibleState = reorderManuallyVisibleState,
        ) {
            Text(
                text = stringResource(OpenOtpResources.strings.can_manually_reorder),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.75f).padding(bottom = 12.dp)
            )
        }
        var visibleDetailedOptions by remember { mutableStateOf(!canReorderDataManually) }
        visibleDetailedOptions = when {
            reorderManuallyVisibleState.run { targetState && currentState } -> false
            reorderManuallyVisibleState.run { !targetState && !currentState } -> true
            else -> visibleDetailedOptions
        }
        AnimatedVisibility(visible = visibleDetailedOptions) {
            Column {
                val showSortedGroupsHeaders by component.showSortedGroupsHeaders.subscribeAsState()
                NamedSwitch(
                    name = stringResource(OpenOtpResources.strings.show_headers),
                    checked = showSortedGroupsHeaders,
                    onCheckedChange = component::onShowSortedGroupsHeadersChange,
                )

                val sortOtpDataNullsFirst by component.sortOtpDataNullsFirst.subscribeAsState()
                NamedSwitch(
                    name = stringResource(OpenOtpResources.strings.nulls_first),
                    checked = sortOtpDataNullsFirst,
                    onCheckedChange = component::onSortNullsFirstChange,
                )

                val sortOtpDataReversed by component.sortOtpDataReversed.subscribeAsState()
                NamedSwitch(
                    name = stringResource(OpenOtpResources.strings.reversed_sort),
                    checked = sortOtpDataReversed,
                    onCheckedChange = component::onSortReversedChange,
                )
            }
        }
    }
}

@Composable
private fun LookAndFeelSettingsGroup(component: SettingsComponent) {
    SettingsGroup(
        name = stringResource(OpenOtpResources.strings.look_and_feel_group_name),
    ) {
        val theme by component.theme.subscribeAsState()

        NamedDropdownMenu(
            name = stringResource(OpenOtpResources.strings.theme),
            icon = when (theme.isDarkTheme()) {
                true -> Icons.Filled.DarkMode
                false -> Icons.Filled.LightMode
            },
            selected = theme,
            onSelected = component::onSelectedTheme,
            anyItems = OpenOtpAppTheme.entries
        )
    }
}

@Composable
private fun SettingsGroup(
    name: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Text(
            text = name,
            color = contentColorFor(MaterialTheme.colorScheme.background),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}
