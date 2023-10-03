package ml.dev.kotlin.openotp.ui.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import ml.dev.kotlin.openotp.ui.component.SearchBarDefaults.InputFieldHeight
import kotlin.math.max
import kotlin.math.min


@Composable
internal fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit,
) {
    val animationProgress = animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = if (active) AnimationEnterFloatSpec else AnimationExitFloatSpec
    )
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val defaultInputFieldShape = SearchBarDefaults.inputFieldShape
    val defaultFullScreenShape = SearchBarDefaults.fullScreenShape
    val useFullScreenShape by remember {
        derivedStateOf(structuralEqualityPolicy()) { animationProgress.value == 1f }
    }
    val animatedShape = remember(useFullScreenShape, shape) {
        when {
            shape == defaultInputFieldShape ->
                GenericShape { size, _ ->
                    val radius = with(density) {
                        (SearchBarCornerRadius * (1 - animationProgress.value)).toPx()
                    }
                    addRoundRect(RoundRect(size.toRect(), CornerRadius(radius)))
                }

            useFullScreenShape -> defaultFullScreenShape
            else -> shape
        }
    }
    val unconsumedInsets = remember { MutableWindowInsets() }
    val topPadding = remember(density) {
        derivedStateOf {
            SearchBarVerticalPadding + unconsumedInsets.asPaddingValues(density).calculateTopPadding()
        }
    }
    val localDensity = LocalDensity.current
    Surface(
        shape = animatedShape,
        color = colors.containerColor,
        contentColor = contentColorFor(colors.containerColor),
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        modifier = modifier
            .zIndex(1f)
            .onConsumedWindowInsetsChanged { consumedInsets ->
                unconsumedInsets.insets = windowInsets.exclude(consumedInsets)
            }
            .consumeWindowInsets(unconsumedInsets)
            .layout { measurable, constraints ->
                with(localDensity) {
                    val animatedTopPadding =
                        lerp(topPadding.value, 0.dp, animationProgress.value).roundToPx()
                    val startWidth = min(constraints.maxWidth, SearchBarMaxWidth.roundToPx()) -
                            (2 * SearchBarHorizontalPadding).roundToPx()
                    val startHeight = max(constraints.minHeight, InputFieldHeight.roundToPx())
                        .coerceAtMost(constraints.maxHeight)
                    val endWidth = constraints.maxWidth
                    val endHeight = constraints.maxHeight
                    val width = lerp(startWidth.toDp(), endWidth.toDp(), animationProgress.value).roundToPx()
                    val height = lerp(startHeight.toDp(), endHeight.toDp(), animationProgress.value).roundToPx() +
                            animatedTopPadding
                    val placeable = measurable.measure(
                        Constraints.fixed(width, height).offset(vertical = -animatedTopPadding)
                    )
                    layout(width, height) {
                        placeable.placeRelative(0, animatedTopPadding)
                    }
                }
            }
    ) {
        Column {
            val animatedInputFieldPadding = remember {
                AnimatedPaddingValues(animationProgress, topPadding)
            }
            SearchBarInputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onActiveChange = onActiveChange,
                modifier = Modifier.padding(paddingValues = animatedInputFieldPadding),
                enabled = enabled,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                colors = colors.inputFieldColors,
                interactionSource = interactionSource,
            )
            val showResults by remember {
                derivedStateOf(structuralEqualityPolicy()) { animationProgress.value > 0 }
            }
            if (showResults) {
                Column(Modifier.graphicsLayer { alpha = animationProgress.value }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        content()
                    }
                }
            }
        }
    }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val shouldClearFocus = !active && isFocused
    LaunchedEffect(active) {
        if (shouldClearFocus) {
            delay(AnimationDelayMillis.toLong())
            focusManager.clearFocus()
        }
    }
}

@Composable
private fun SearchBarInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = SearchBarDefaults.inputFieldColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusRequester = remember { FocusRequester() }
    val textStyle = searchBarTextStyle(enabled, interactionSource)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .height(InputFieldHeight)
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onActiveChange(true) }
            .semantics {
                onClick {
                    focusRequester.requestFocus()
                    true
                }
            },
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(cursorColor().value),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        interactionSource = interactionSource,
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = query,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = placeholder,
                leadingIcon = leadingIcon?.let { leading ->
                    {
                        Box(Modifier.offset(x = SearchBarIconOffsetX)) { leading() }
                    }
                },
                trailingIcon = trailingIcon?.let { trailing ->
                    {
                        Box(Modifier.offset(x = -SearchBarIconOffsetX)) { trailing() }
                    }
                },
                shape = SearchBarDefaults.inputFieldShape,
                colors = colors,
                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                container = {},
            )
        }
    )
}

object SearchBarDefaults {
    val TonalElevation: Dp = 6.dp
    val ShadowElevation: Dp = 4.dp
    val InputFieldHeight: Dp = 56.dp
    val inputFieldShape: Shape @Composable get() = RectangleShape
    val fullScreenShape: Shape @Composable get() = RectangleShape
    val windowInsets: WindowInsets @Composable get() = WindowInsets.statusBars

    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        inputFieldColors: TextFieldColors = inputFieldColors(),
    ): SearchBarColors = SearchBarColors(
        containerColor = containerColor,
        inputFieldColors = inputFieldColors,
    )

    @Composable
    fun inputFieldColors(
        focusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledInputOpacity),
        cursorColor: Color = MaterialTheme.colorScheme.primary,
        selectionColors: TextSelectionColors = LocalTextSelectionColors.current,
        focusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface,
        unfocusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledLeadingIconOpacity),
        focusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledTrailingIconOpacity),
        focusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledInputOpacity),
    ): TextFieldColors =
        TextFieldDefaults.colors(
            focusedTextColor = focusedTextColor,
            unfocusedTextColor = unfocusedTextColor,
            disabledTextColor = disabledTextColor,
            cursorColor = cursorColor,
            selectionColors = selectionColors,
            focusedLeadingIconColor = focusedLeadingIconColor,
            unfocusedLeadingIconColor = unfocusedLeadingIconColor,
            disabledLeadingIconColor = disabledLeadingIconColor,
            focusedTrailingIconColor = focusedTrailingIconColor,
            unfocusedTrailingIconColor = unfocusedTrailingIconColor,
            disabledTrailingIconColor = disabledTrailingIconColor,
            focusedPlaceholderColor = focusedPlaceholderColor,
            unfocusedPlaceholderColor = unfocusedPlaceholderColor,
            disabledPlaceholderColor = disabledPlaceholderColor,
        )
}

class SearchBarColors(
    val containerColor: Color,
    val inputFieldColors: TextFieldColors,
)

private class AnimatedPaddingValues(
    val animationProgress: State<Float>,
    val topPadding: State<Dp>,
) : PaddingValues {
    override fun calculateTopPadding(): Dp = topPadding.value * animationProgress.value
    override fun calculateBottomPadding(): Dp = SearchBarVerticalPadding * animationProgress.value
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        SearchBarHorizontalPadding * animationProgress.value

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        SearchBarHorizontalPadding * animationProgress.value
}

internal val SearchBarCornerRadius: Dp = InputFieldHeight / 2
internal val SearchBarMaxWidth: Dp = 720.dp
internal val SearchBarVerticalPadding: Dp = 12.dp
internal val SearchBarHorizontalPadding: Dp = 16.dp
internal val SearchBarIconOffsetX: Dp = 4.dp

private const val AnimationEnterDurationMillis: Int = 400
private const val AnimationExitDurationMillis: Int = 350
private const val AnimationDelayMillis: Int = 100
private val AnimationEnterEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val AnimationExitEasing = CubicBezierEasing(0.0f, 1.0f, 0.0f, 1.0f)
private val AnimationEnterFloatSpec: FiniteAnimationSpec<Float> = tween(
    durationMillis = AnimationEnterDurationMillis,
    delayMillis = AnimationDelayMillis,
    easing = AnimationEnterEasing,
)
private val AnimationExitFloatSpec: FiniteAnimationSpec<Float> = tween(
    durationMillis = AnimationExitDurationMillis,
    delayMillis = AnimationDelayMillis,
    easing = AnimationExitEasing,
)
private const val DisabledInputOpacity = 0.38f
private const val DisabledTrailingIconOpacity = 0.38f
private const val DisabledLeadingIconOpacity = 0.38f

@Composable
private fun textColor(
    enabled: Boolean,
    interactionSource: InteractionSource,
    disabledTextColor: Color = MaterialTheme.colorScheme.onSurface
        .copy(alpha = DisabledInputOpacity),
    focusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
): State<Color> {
    val focused by interactionSource.collectIsFocusedAsState()

    val targetValue = when {
        !enabled -> disabledTextColor
        focused -> focusedTextColor
        else -> unfocusedTextColor
    }
    return rememberUpdatedState(targetValue)
}

@Composable
private fun cursorColor(
    cursorColor: Color = MaterialTheme.colorScheme.primary,
): State<Color> {
    return rememberUpdatedState(cursorColor)
}

@Composable
internal fun searchBarTextStyle(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,

    ): TextStyle {
    val textColor = LocalTextStyle.current.color.takeOrElse {
        textColor(enabled, interactionSource = interactionSource).value
    }
    return LocalTextStyle.current.merge(TextStyle(color = textColor))
}