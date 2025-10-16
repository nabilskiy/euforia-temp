package digital.euforia.app.ui.util.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * A composable that shows its content with a delayed animation when the trigger condition becomes true.
 *
 * @param triggerCondition The condition that triggers the animation when it becomes true
 * @param delayMillis The delay in milliseconds before showing the content after the trigger condition becomes true
 * @param durationMillis The duration of the fade-in animation in milliseconds
 * @param enter The enter transition animation, defaults to fadeIn
 * @param modifier The modifier to be applied to the AnimatedVisibility
 * @param content The content to be displayed with the animation
 */
@Composable
fun DelayedVisibilityAnimation(
    triggerCondition: Boolean,
    delayMillis: Long = 1500,
    durationMillis: Int = 500,
    enter: EnterTransition = fadeIn(animationSpec = tween(durationMillis = durationMillis)),
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    // Trigger the delayed visibility when the condition becomes true
    LaunchedEffect(triggerCondition) {
        if (triggerCondition) {
            kotlinx.coroutines.delay(delayMillis)
            visible = true
        }
    }

    // Animated visibility with the specified enter transition
    AnimatedVisibility(
        visible = visible,
        enter = enter,
        modifier = modifier
    ) {
        Box {
            content()
        }
    }
}
