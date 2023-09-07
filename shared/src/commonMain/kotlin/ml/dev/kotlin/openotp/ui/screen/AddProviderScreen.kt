package ml.dev.kotlin.openotp.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import ml.dev.kotlin.openotp.component.AddHotpProviderComponent
import ml.dev.kotlin.openotp.component.AddOtpProviderComponent
import ml.dev.kotlin.openotp.component.AddTotpProviderComponent
import ml.dev.kotlin.openotp.otp.HmacAlgorithm
import ml.dev.kotlin.openotp.otp.OtpDigits
import ml.dev.kotlin.openotp.otp.OtpType
import ml.dev.kotlin.openotp.otp.OtpType.HOTP
import ml.dev.kotlin.openotp.otp.OtpType.TOTP
import ml.dev.kotlin.openotp.otp.TotpPeriod
import ml.dev.kotlin.openotp.ui.component.*

@Composable
internal fun AddProviderScreen(
    totpComponent: AddTotpProviderComponent,
    hotpComponent: AddHotpProviderComponent,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var selected by remember { mutableStateOf(OtpType.entries.first()) }
        TabRow(selectedTabIndex = selected.ordinal) {
            OtpType.entries.forEach { type ->
                Tab(
                    text = { Text(type.presentableName) },
                    selected = type == selected,
                    onClick = { selected = type },
                    icon = {
                        when (type) {
                            TOTP -> Icon(imageVector = Icons.Default.Update, contentDescription = "add totp")
                            HOTP -> Icon(imageVector = Icons.Default.Pin, contentDescription = "add hotp")
                        }
                    }
                )
            }
        }
        BoxWithConstraints {
            val totpOffset by animateDpAsState(
                targetValue = when (selected) {
                    TOTP -> 0.dp
                    HOTP -> -maxWidth
                }
            )
            val hotpOffset by animateDpAsState(
                targetValue = when (selected) {
                    TOTP -> maxWidth
                    HOTP -> 0.dp
                }
            )
            Box(modifier = Modifier.offset(x = totpOffset)) {
                AddTotpProviderScreen(totpComponent)
            }
            Box(modifier = Modifier.offset(x = hotpOffset)) {
                AddHotpProviderScreen(hotpComponent)
            }
            AddProviderFormConfirmButtons(
                component = when (selected) {
                    TOTP -> totpComponent
                    HOTP -> hotpComponent
                }
            )
        }
    }
}

@Composable
private fun AddTotpProviderScreen(component: AddTotpProviderComponent) {
    AddOtpProviderScreen(component) {
        val selectedAlgorithm by component.algorithm.subscribeAsState()
        NamedDropdownMenu(
            name = "Algorithm",
            selected = selectedAlgorithm,
            anyItems = HmacAlgorithm.entries,
            onSelected = component::onAlgorithmSelected,
        )

        val selectedDigits by component.digits.subscribeAsState()
        NamedDropdownMenu(
            name = "Digits",
            selected = selectedDigits,
            anyItems = OtpDigits.entries,
            onSelected = component::onDigitsSelected,
        )

        val selectedPeriod by component.period.subscribeAsState()
        NamedDropdownMenu(
            name = "Period",
            selected = selectedPeriod,
            anyItems = TotpPeriod.entries,
            onSelected = component::onPeriodSelected,
        )
    }
}

@Composable
private fun AddHotpProviderScreen(component: AddHotpProviderComponent) {
    AddOtpProviderScreen(component) {
        val counter by component.counter.subscribeAsState()
        val counterIsError by component.counterIsError.subscribeAsState()
        FormField(
            name = "Counter",
            text = counter,
            onTextChange = component::onCounterChanged,
            isError = counterIsError,
            keyboardType = KeyboardType.Number,
        )

        val selectedAlgorithm by component.algorithm.subscribeAsState()
        NamedDropdownMenu(
            name = "Algorithm",
            selected = selectedAlgorithm,
            anyItems = HmacAlgorithm.entries,
            onSelected = component::onAlgorithmSelected,
        )

        val selectedDigits by component.digits.subscribeAsState()
        NamedDropdownMenu(
            name = "Digits",
            selected = selectedDigits,
            anyItems = OtpDigits.entries,
            onSelected = component::onDigitsSelected,
        )
    }
}

@Composable
private fun AddOtpProviderScreen(
    component: AddOtpProviderComponent,
    advancedSettingsContent: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AccountDetails(component)
            FormGroup(
                groupName = "Advanced Settings",
                content = advancedSettingsContent,
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AddProviderFormConfirmButtons(component: AddOtpProviderComponent) {
    FormConfirmButtons(
        confirm = FormConfirmButtonData(
            text = "Save",
            contentDescription = "save",
            imageVector = Icons.Outlined.Save,
            onClick = component::onSaveClicked,
        ),
        cancel = FormConfirmButtonData(
            text = "Cancel",
            contentDescription = "cancel",
            imageVector = Icons.Outlined.Cancel,
            onClick = component::onCancelClicked,
        )
    )
}

@Composable
private fun AccountDetails(component: AddOtpProviderComponent) {
    FormGroup(
        groupName = "Account Details"
    ) {
        val issuer by component.issuer.subscribeAsState()
        FormField(
            name = "Issuer",
            text = issuer,
            onTextChange = component::onIssuerChanged,
        )

        val accountName by component.accountName.subscribeAsState()
        FormField(
            name = "Account Name",
            text = accountName,
            onTextChange = component::onAccountNameChanged,
        )

        val secret by component.secret.subscribeAsState()
        val secretIsError by component.secretIsError.subscribeAsState()
        FormField(
            name = "Secret",
            text = secret,
            isError = secretIsError,
            onTextChange = component::onSecretChanged,
        )
    }
}
