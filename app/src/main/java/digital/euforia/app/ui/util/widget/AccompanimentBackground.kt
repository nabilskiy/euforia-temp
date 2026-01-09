package digital.euforia.app.ui.util.widget

import androidx.annotation.Keep
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.DaytimeAccompanimentAccent
import digital.euforia.app.ui.theme.DaytimeAccompanimentMain
import digital.euforia.app.ui.theme.EveningAccompanimentAccent
import digital.euforia.app.ui.theme.EveningAccompanimentAccentVariant
import digital.euforia.app.ui.theme.EveningAccompanimentMain
import digital.euforia.app.ui.theme.MorningAccompanimentAccent
import digital.euforia.app.ui.theme.MorningAccompanimentMain
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.daytimeColors
import digital.euforia.app.ui.theme.daytimeColors2
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.ui.theme.eveningColors2
import digital.euforia.app.ui.theme.morningColors
import digital.euforia.app.ui.theme.morningColors2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * AccompanimentButton
 *
 * Three-bubble rotating animation with breathing effect and layered composition.
 * - Group rotation: 360° in 10s, linear, infinite
 * - Bubbles: equally spaced at 120°, same distance from center (bubbleCenterOffset)
 * - Breathing: scale 1.0..1.2, random 2–3s with easeInOut and auto-reverse, random pivot
 * - Layers: under (difference blend, alpha 0.45), upper (colorDodge, with shadows)
 * - Glass overlay and extra blur effect on top
 */
@Composable
fun AccompanimentBackground(
    orbitPeriodMs: Int = 120_000,
    extraOrbitPeriodMs: Int = 30_000,
    extraOrbitRadiusPx: Float? = null,
    colors: AccompanimentButtonColors,
    dimensions: AccompanimentButtonDimensions,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .blur(35.dp)
    ) {
        val transition = rememberInfiniteTransition(label = "accompaniment_orbits")
        // Group rotation for 3 balls (0..1 -> 0..2π)
        val tGroup by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = orbitPeriodMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "tGroup"
        )
        val tExtra by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = extraOrbitPeriodMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "tExtra"
        )
        val breathe by transition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2400,
                    easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f) // easeInOut
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathe"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            // density-aware sphere radius from dp (size = diameter)
            val radius = (dimensions.ballSizeDp / 2f).dp.toPx() * breathe

            val shadowRadius = (dimensions.ballSizeDp / 4f).dp.toPx() * breathe

            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)
            val angle = tGroup * 2f * PI.toFloat()
            val phase = 2f * PI.toFloat() / 3f // 120°

            val pA = Offset(
                x = center.x + cos(angle) * dimensions.orbitRadius,
                y = center.y + sin(angle) * dimensions.orbitRadius
            )
            val pB = Offset(
                x = center.x + cos(angle + phase) * dimensions.orbitRadius,
                y = center.y + sin(angle + phase) * dimensions.orbitRadius
            )
            val pC = Offset(
                x = center.x + cos(angle + 2f * phase) * dimensions.orbitRadius,
                y = center.y + sin(angle + 2f * phase) * dimensions.orbitRadius
            )

            // Independent black ball
            val extraR = extraOrbitRadiusPx ?: (dimensions.orbitRadius + 100f)
            val extraAngle = tExtra * 2f * PI.toFloat()
            val pExtra = Offset(
                x = center.x + cos(extraAngle) * extraR,
                y = center.y + sin(extraAngle) * extraR
            )

            // Radial gradient with stops from 0.1f (inner) to 0.8f (outer)
            fun ballBrush(center: Offset, base: Color) = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to base.copy(alpha = 1f),
//                        0.1f to base.copy(alpha = 0.8f),
//                        0.8f to base.copy(alpha = 0.4f),
                    1.0f to base.copy(alpha = 0f)
                ),
                center = center,
                radius = radius
            )

            fun shadowBallBrush(center: Offset, base: Color) = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to base.copy(alpha = 0.4f),
//                    0.1f to base.copy(alpha = 0.2f),
//                    0.8f to base.copy(alpha = 0.1f),
                    1.0f to base.copy(alpha = 0f)
                ),
                center = center,
                radius = radius
            )

            // base underlayer using Difference blending at a low alpha, as intended
            drawRect(
                color = colors.backgroundColor,
                size = size,
                blendMode = BlendMode.Difference,
                alpha = 0.45f
            )

            // draw trio 120° apart
            drawCircle(
                brush = ballBrush(pA, colors.firstColor), radius = radius, center = pA,
                blendMode = BlendMode.Screen
            )
            drawCircle(
                brush = ballBrush(pB, colors.secondColor), radius = radius, center = pB,
                blendMode = BlendMode.Screen
            )
            drawCircle(
                brush = ballBrush(pC, colors.thirdColor), radius = radius, center = pC,
                blendMode = BlendMode.Screen
            )

            // draw independent black ball
            drawCircle(
                brush = shadowBallBrush(pExtra, colors.extraBallColor),
                radius = radius,
                center = pExtra
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(color = White.copy(alpha = 0.04f)))
    }
}


@Keep
enum class AccompanimentButtonColors(
    val firstColor: Color,
    val secondColor: Color,
    val thirdColor: Color,
    val extraBallColor: Color = Black,
    val backgroundColor: Color
) {
    MORNING(
        firstColor = morningColors2[2],
        secondColor = morningColors2[1],
        thirdColor = morningColors2[0],
        backgroundColor = PrimaryBackground
    ),
    DAYTIME(
        firstColor = daytimeColors2[0],
        secondColor = DaytimeAccompanimentMain,
        thirdColor = DaytimeAccompanimentMain,
        backgroundColor = PrimaryBackground
    ),
    EVENING(
        firstColor = eveningColors2[2],
        secondColor = eveningColors2[1],
        thirdColor = eveningColors[2],
        backgroundColor = PrimaryBackground
    )
}

@Keep
enum class AccompanimentButtonDimensions(
    val ballSizeDp: Float, val orbitRadius: Float,
    val iconSize: Dp = 56.dp,
    val iconPadding: Dp = 8.dp,
    val titleFontSize: TextUnit = 20.sp,
    val titleLineHeight: TextUnit = 24.sp
) {
    PRIMARY(ballSizeDp = 420f, orbitRadius = 220f),
    SECONDARY(
        ballSizeDp = 300f, orbitRadius = 200f,
        iconSize = 32.dp, iconPadding = 6.dp,
        titleFontSize = 14.sp,
        titleLineHeight = 18.sp
    )
}