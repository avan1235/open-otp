import ml.dev.kotlin.openotp.ui.theme.md_theme_dark_background
import ml.dev.kotlin.openotp.ui.theme.md_theme_light_background
import platform.UIKit.UIColor

@Suppress("UNUSED")
val MD_THEME_LIGHT_BACKGROUND: UIColor = UIColor(
    red = md_theme_light_background.red.toDouble(),
    green = md_theme_light_background.green.toDouble(),
    blue = md_theme_light_background.blue.toDouble(),
    alpha = md_theme_light_background.alpha.toDouble(),
)

@Suppress("UNUSED")
val MD_THEME_DARK_BACKGROUND: UIColor = UIColor(
    red = md_theme_dark_background.red.toDouble(),
    green = md_theme_dark_background.green.toDouble(),
    blue = md_theme_dark_background.blue.toDouble(),
    alpha = md_theme_dark_background.alpha.toDouble(),
)