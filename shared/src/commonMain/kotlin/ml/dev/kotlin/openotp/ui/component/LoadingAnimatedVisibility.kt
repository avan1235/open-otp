package ml.dev.kotlin.openotp.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LoadingAnimatedVisibility(
    visibleContent: Boolean,
    loadingVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    loadingHorizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    precedingContent: @Composable (ColumnScope.() -> Unit)? = null,
    centerContent: @Composable (ColumnScope.() -> Unit)? = {
        CircularProgressIndicator(Modifier.padding(12.dp))
    },
    followingContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = !visibleContent,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = loadingHorizontalAlignment,
                verticalArrangement = loadingVerticalArrangement,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                if (precedingContent != null) {
                    precedingContent()
                }
                if (centerContent != null) {
                    centerContent()
                }
                if (followingContent != null) {
                    followingContent()
                }
            }
        }
    }
}