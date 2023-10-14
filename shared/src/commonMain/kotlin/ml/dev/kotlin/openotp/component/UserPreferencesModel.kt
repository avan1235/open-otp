package ml.dev.kotlin.openotp.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import ml.dev.kotlin.openotp.component.OpenOtpAppTheme.System
import ml.dev.kotlin.openotp.component.SortOtpDataBy.Dont
import ml.dev.kotlin.openotp.otp.OtpData
import ml.dev.kotlin.openotp.shared.OpenOtpResources
import ml.dev.kotlin.openotp.ui.theme.*
import ml.dev.kotlin.openotp.util.Named

@Serializable
enum class OpenOtpAppTheme : Named {
    System, Light, Dark;

    override val OpenOtpAppComponentContext.presentableName: String
        get() = when (this@OpenOtpAppTheme) {
            Light -> stringResource(OpenOtpResources.strings.light_theme_name)
            Dark -> stringResource(OpenOtpResources.strings.dark_theme_name)
            System -> stringResource(OpenOtpResources.strings.system_theme_name)
        }

    @Composable
    fun colorScheme(): ColorScheme = when (isDarkTheme()) {
        true -> DarkColors
        false -> LightColors
    }

    @Composable
    fun isDarkTheme(): Boolean = when (this) {
        Light -> false
        Dark -> true
        System -> isSystemInDarkTheme()
    }
}

@Serializable
enum class SortOtpDataBy(
    val selector: ((OtpData) -> String?)?,
) : Named {
    Dont(selector = null),
    Issuer(selector = { data -> data.issuer?.takeIf { it.isNotBlank() } }),
    AccountName(selector = { data -> data.accountName?.takeIf { it.isNotBlank() } });

    override val OpenOtpAppComponentContext.presentableName: String
        get() = when (this@SortOtpDataBy) {
            Dont -> stringResource(OpenOtpResources.strings.dont_sort_name)
            Issuer -> stringResource(OpenOtpResources.strings.issuer_sort_name)
            AccountName -> stringResource(OpenOtpResources.strings.account_name_sort_name)
        }

    val OpenOtpAppComponentContext.defaultGroupName: String
        get() = when (this@SortOtpDataBy) {
            Dont -> stringResource(OpenOtpResources.strings.default_group_name_dont_sort_name)
            Issuer -> stringResource(OpenOtpResources.strings.default_group_name_issuer_sort_name)
            AccountName -> stringResource(OpenOtpResources.strings.default_group_name_account_name_sort_name)
        }
}

@Serializable
data class UserPreferencesModel(
    val theme: OpenOtpAppTheme = System,
    val sortOtpDataBy: SortOtpDataBy = Dont,
    val sortOtpDataNullsFirst: Boolean = false,
    val sortOtpDataReversed: Boolean = false,
    val confirmOtpDataDelete: Boolean = true,
    val requireAuthentication: Boolean = false,
    val showSortedGroupsHeaders: Boolean = true,
)

private val LightColors: ColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)
