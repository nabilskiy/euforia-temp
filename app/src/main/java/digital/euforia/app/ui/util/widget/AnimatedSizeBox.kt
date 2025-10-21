package digital.euforia.app.ui.util.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White

/**
 * A composable that animates its size when pressed.
 *
 * @param onClick The callback to be invoked when the box is clicked
 * @param pressedScale The scale factor to apply when the box is pressed (should be less than 1.0)
 * @param animationDurationMillis The duration of the animation in milliseconds
 * @param modifier The modifier to be applied to the box
 * @param content The content to be displayed inside the box
 */
@Composable
fun AnimatedSizeBox(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    pressedScale: Float = 0.98f,
    animationDurationMillis: Int = 200,
    content: @Composable BoxScope.() -> Unit
) {
    onClick ?: return Box(modifier = modifier, content = content)

    // Track pressed state
    var isPressed by remember { mutableStateOf(false) }

    // Animate scale based on pressed state
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = animationDurationMillis),
        label = "pressAnimation"
    )

    // Apply the animation and handle press events
    val animatedModifier = modifier
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onClick() }
            )
        }

    // Render the content with the animated modifier
    Box(
        modifier = animatedModifier
    ) {
        content()
    }
}

@Composable
fun AnimatedVerticalShrink(isVisible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 300)
        ),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        content()
    }
}
