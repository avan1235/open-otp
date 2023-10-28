package ml.dev.kotlin.openotp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import ml.dev.kotlin.openotp.ui.icons.*

object OtpIcons

val String?.issuerIcon: ImageVector
    get() = when {
        this == null -> Icons.Default.Lock
        contains("amazon", ignoreCase = true) -> OtpIcons.Amazon
        contains("cloudflare", ignoreCase = true) -> OtpIcons.Cloudflare
        contains("dropbox", ignoreCase = true) -> OtpIcons.Dropbox
        contains("facebook", ignoreCase = true) -> OtpIcons.Facebook
        contains("github", ignoreCase = true) -> OtpIcons.Github
        contains("gitlab", ignoreCase = true) -> OtpIcons.Gitlab
        contains("google", ignoreCase = true) -> OtpIcons.Google
        contains("instagram", ignoreCase = true) -> OtpIcons.Instagram
        contains("microsoft", ignoreCase = true) -> OtpIcons.Microsoft
        contains("reddit", ignoreCase = true) -> OtpIcons.Reddit
        contains("twitter", ignoreCase = true) -> OtpIcons.Twitter
        else -> Icons.Default.Lock
    }
