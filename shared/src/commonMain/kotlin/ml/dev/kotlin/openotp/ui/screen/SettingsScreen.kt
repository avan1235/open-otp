package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.icerock.moko.resources.compose.stringResource
import ml.dev.kotlin.openotp.component.OpenOtpAppTheme
import ml.dev.kotlin.openotp.component.SettingsComponent
import ml.dev.kotlin.openotp.component.SortOtpDataBy
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.component.ClickableIconButton
import ml.dev.kotlin.openotp.ui.component.NamedDropdownMenu
import ml.dev.kotlin.openotp.ui.component.NamedSwitch

@Composable
internal fun SettingsScreen(
    component: SettingsComponent,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    SystemBarsScreen(
        top = accent,
        bottom = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(accent),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ClickableIconButton(
                        onClick = component::onExitSettings,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(OpenOtpResources.strings.back_icon_name),
                            tint = contentColorFor(accent),
                        )
                    }
                    Text(
                        text = stringResource(OpenOtpResources.strings.settings_screen_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColorFor(accent)
                    )
                }
            }
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
            }
        }
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
        AnimatedVisibility(visible = canReorderDataManually) {
            Text(
                text = stringResource(OpenOtpResources.strings.can_manually_reorder),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.75f)
            )
        }

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
    content: @Composable ColumnScope.() -> Unit
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
