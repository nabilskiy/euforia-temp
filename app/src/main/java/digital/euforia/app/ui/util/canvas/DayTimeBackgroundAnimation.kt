package digital.euforia.app.ui.util.canvas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.ui.theme.EveningAccompanimentAccentVariant
import digital.euforia.app.ui.theme.daytimeColors
import digital.euforia.app.ui.theme.daytimeColors2
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.ui.theme.eveningColors2
import digital.euforia.app.ui.theme.morningColors
import digital.euforia.app.ui.theme.morningColors2

fun Modifier.timeOfDayBackgroundAnimation(timeOfDay: TimeOfDay): Modifier = composed {
    val baseColor = when (timeOfDay) {
        TimeOfDay.MORNING -> morningColors.getOrElse(1) { morningColors2.first() }
        TimeOfDay.DAYTIME -> daytimeColors.getOrElse(0) { daytimeColors2.first() }
        TimeOfDay.EVENING -> eveningColors.getOrElse(0) { EveningAccompanimentAccentVariant }
    }

    // Breathing animation for the sphere radius
    val transition = rememberInfiniteTransition(label = "breathing-bg")
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = { t ->
                // easeInOutCubic
                if (t < 0.5f) 4f * t * t * t else 1f - (-2f * t + 2f).let { it * it * it } / 2f
            }),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing-scale"
    )

    drawWithCache {
        val paddingPx = 32.dp.toPx()
        val center = Offset(paddingPx * 2, paddingPx)
        val minDim = size.minDimension
        val baseRadius = minDim * 0.95f // base size of the sphere
        val radius = (baseRadius * scale).coerceAtLeast(minDim * 0.35f)

        val brush = Brush.radialGradient(
            colors = listOf(
                baseColor.copy(alpha = 1f),
                baseColor.copy(alpha = 0.55f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        )

        onDrawBehind {
            drawRect(Color.Transparent) // ensure layer
            drawCircle(
                brush = brush,
                radius = radius,
                center = center,
                blendMode = BlendMode.Screen
            )
        }
    }
}