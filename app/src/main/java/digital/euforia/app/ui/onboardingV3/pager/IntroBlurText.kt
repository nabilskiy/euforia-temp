package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.White
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val INTRO_TEXT_TRANSITION_MS = 1_000
internal const val INTRO_TEXT_HOLD_MS = 2_600
internal const val INTRO_BRAND_REVEAL_MS = 1_800
internal const val INTRO_BEFORE_LAST_TEXT_MS = 1_000
internal const val INTRO_SHOW_BUTTONS_MS = 1_000

internal class IntroBlurTextAnimState {
    val alpha = Animatable(1f)
    val scale = Animatable(1f)
    val blurRadius = Animatable(0f)
}

@Composable
internal fun rememberIntroBlurTextAnimState(): IntroBlurTextAnimState {
    return remember { IntroBlurTextAnimState() }
}

internal suspend fun animateIntroTextBlurOut(state: IntroBlurTextAnimState) {
    coroutineScope {
        launch {
            state.alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(INTRO_TEXT_TRANSITION_MS, easing = FastOutLinearInEasing),
            )
        }
        launch {
            state.scale.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(INTRO_TEXT_TRANSITION_MS, easing = FastOutLinearInEasing),
            )
        }
        launch {
            state.blurRadius.animateTo(
                targetValue = 20f,
                animationSpec = tween(INTRO_TEXT_TRANSITION_MS, easing = FastOutLinearInEasing),
            )
        }
    }
}

internal suspend fun animateIntroTextBlurIn(state: IntroBlurTextAnimState) {
    coroutineScope {
        launch {
            state.alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(INTRO_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing),
            )
        }
        launch {
            state.scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(INTRO_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing),
            )
        }
        launch {
            state.blurRadius.animateTo(
                targetValue = 0f,
                animationSpec = tween(INTRO_TEXT_TRANSITION_MS, easing = LinearOutSlowInEasing),
            )
        }
    }
}

internal suspend fun showIntroText(
    state: IntroBlurTextAnimState,
    text: String,
    onTextChanged: (String) -> Unit,
    holdAfterShow: Boolean = true,
) {
    animateIntroTextBlurOut(state)
    onTextChanged(text)
    animateIntroTextBlurIn(state)
    if (holdAfterShow && text.isNotBlank()) {
        delay(INTRO_TEXT_HOLD_MS.toLong())
    }
}

internal fun Modifier.introAnimatedTextBlur(radius: Dp): Modifier {
    return blur(radius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
}

@Composable
internal fun IntroBlurText(
    text: String,
    animState: IntroBlurTextAnimState,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 24.dp,
    lineHeight: TextUnit = fontSize * 1.15f,
    maxLines: Int = 3,
) {
    Text(
        text = text,
        color = White,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        lineHeight = lineHeight,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            lineHeight = lineHeight,
            textAlign = TextAlign.Center,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .graphicsLayer {
                val scale = animState.scale.value
                scaleX = scale
                scaleY = scale
            }
            .alpha(animState.alpha.value)
            .introAnimatedTextBlur(animState.blurRadius.value.dp),
    )
}
