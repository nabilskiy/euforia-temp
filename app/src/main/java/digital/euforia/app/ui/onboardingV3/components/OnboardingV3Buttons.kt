package digital.euforia.app.ui.onboardingV3.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import kotlin.math.PI
import kotlin.math.cos

private val OnboardingV3ButtonGradientColors = listOf(
    Color(0xFFE29B31),
    Color(0xFFFF5589),
    Color(0xFF204FC0),
)

private val OnboardingV3AnimatedGradientColors = listOf(
    OnboardingV3ButtonGradientColors[0],
    OnboardingV3ButtonGradientColors[1],
    OnboardingV3ButtonGradientColors[2],
    OnboardingV3ButtonGradientColors[1],
    OnboardingV3ButtonGradientColors[0],
)

private const val OnboardingV3GradientAnimationDurationMillis = 2_900

@Composable
fun V3PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    horizontalPadding: Dp = 0.dp,
    onPositioned: (Rect) -> Unit = {},
    onClick: () -> Unit,
) {
    V3ButtonFrame(
        modifier = modifier,
        isEnabled = isEnabled,
        horizontalPadding = horizontalPadding,
        pressedScale = 0.985f,
        onPositioned = onPositioned,
        onClick = onClick,
    ) {
        Text(
            text = text,
            color = PrimaryButtonText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
fun V3GradientTitleButton(
    text: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    horizontalPadding: Dp = 0.dp,
    onClick: () -> Unit,
) {
    val phase = rememberV3ButtonGradientPhase(label = "v3GradientTitleButton")
    val textBrush = Brush.linearGradient(
        colors = OnboardingV3AnimatedGradientColors,
        start = Offset(x = -360f + 720f * phase, y = 0f),
        end = Offset(x = 360f + 720f * phase, y = 0f),
    )

    V3ButtonFrame(
        modifier = modifier,
        isEnabled = isEnabled,
        horizontalPadding = horizontalPadding,
        pressedScale = 0.96f,
        gradientPhase = phase,
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                brush = textBrush,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun rememberV3ButtonGradientPhase(label: String): Float {
    val infiniteTransition = rememberInfiniteTransition(label = label)
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = OnboardingV3GradientAnimationDurationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "${label}Phase",
    )
    return phase
}

@Composable
fun Modifier.v3BeaconLiftCapsuleAnimation(durationMillis: Int = 2_900): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "v3BeaconLiftCapsule")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
        ),
        label = "v3BeaconLiftCapsulePhase",
    )
    val pulse = 0.5f - (cos(phase * PI.toFloat() * 2f) * 0.5f)
    val scale = 0.994f + pulse * 0.032f
    val lift = -4f - pulse * 7f

    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationY = lift
    }
}

@Composable
private fun V3ButtonFrame(
    modifier: Modifier,
    isEnabled: Boolean,
    horizontalPadding: Dp,
    pressedScale: Float,
    gradientPhase: Float? = null,
    onPositioned: (Rect) -> Unit = {},
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val glowPhase = gradientPhase ?: rememberV3ButtonGradientPhase(label = "v3PrimaryButtonGlow")
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) pressedScale else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "v3ButtonPressedScale",
    )
    val alpha = if (isEnabled) 1f else 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isEnabled) {
                V3ButtonGlow(gradientPhase = glowPhase)
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .onGloballyPositioned { onPositioned(it.boundsInRoot()) }
                    .background(White, CircleShape)
                    .then(
                        if (isEnabled) {
                            Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onClick,
                            )
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun BoxScope.V3ButtonGlow(
    gradientPhase: Float,
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = OnboardingV3AnimatedGradientColors,
        startX = -360f + 720f * gradientPhase,
        endX = 360f + 720f * gradientPhase,
    )

    V3GlowCapsule(
        brush = gradientBrush,
        alpha = 0.45f,
        blurRadius = 8.dp,
        scaleX = 1.02f,
        scaleY = 1.06f,
    )
    V3GlowCapsule(
        brush = gradientBrush,
        alpha = 0.90f,
        blurRadius = 3.dp,
        scaleX = 1.01f,
        scaleY = 1.03f,
    )
}

@Composable
private fun BoxScope.V3GlowCapsule(
    brush: Brush,
    alpha: Float,
    blurRadius: Dp,
    scaleX: Float,
    scaleY: Float,
) {
    Canvas(
        modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
            }
            .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded),
    ) {
        drawRoundRect(
            brush = brush,
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
            alpha = alpha,
        )
    }
}
