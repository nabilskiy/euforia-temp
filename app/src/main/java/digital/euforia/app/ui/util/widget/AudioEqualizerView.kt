package digital.euforia.app.ui.util.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.OnboardingSphereColors
import digital.euforia.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * A 50x50 audio equalizer widget with 4 vertical bars that animate to random heights.
 * The bars are vertically symmetrical (mirrored from the center).
 */
@Composable
fun AudioEqualizerView(
    modifier: Modifier = Modifier,
    barColor: Color = White,
    animationDurationMs: Int = 500
) {
    val sizeModifier = modifier.size(50.dp)

    // Create states for target heights of each bar
    var bar1Target by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var bar2Target by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var bar3Target by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var bar4Target by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }

    // Initial values for the bars
    var bar1Initial by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var bar2Initial by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var bar3Initial by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }
    var bar4Initial by remember { mutableStateOf(Random.nextFloat() * 0.6f + 0.2f) }

    // Create infinite transition for continuous animation
    val transition = rememberInfiniteTransition(label = "equalizer")

    // Effect to periodically update target heights
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(animationDurationMs.toLong())

            // Save current targets as new initial values
            bar1Initial = bar1Target
            bar2Initial = bar2Target
            bar3Initial = bar3Target
            bar4Initial = bar4Target

            // Generate new random targets (between 0.2 and 0.8 for good visibility)
            bar1Target = Random.nextFloat() * 0.6f + 0.2f
            bar2Target = Random.nextFloat() * 0.6f + 0.2f
            bar3Target = Random.nextFloat() * 0.6f + 0.2f
            bar4Target = Random.nextFloat() * 0.6f + 0.2f
        }
    }

    // Animate 4 bars with different phases to create a more natural effect
    val bar1Height = transition.animateFloat(
        initialValue = bar1Initial,
        targetValue = bar1Target,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2Height = transition.animateFloat(
        initialValue = bar2Initial,
        targetValue = bar2Target,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height = transition.animateFloat(
        initialValue = bar3Initial,
        targetValue = bar3Target,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val bar4Height = transition.animateFloat(
        initialValue = bar4Initial,
        targetValue = bar4Target,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Canvas(modifier = sizeModifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        // Calculate bar width and spacing
        val barCount = 4
        val barWidth = width / (barCount * 4) // Bar width is half of the available space per bar
        val spacing = width / (barCount * 4)  // Spacing is the other half

        // Calculate total width used by all bars and spacing
        val totalContentWidth = (barWidth * barCount) + (spacing * (barCount + 1))

        // Calculate left offset to center the content
        val leftOffset = (width - totalContentWidth) / 2

        // Draw the 4 bars
        val barHeights = listOf(
            bar1Height.value,
            bar2Height.value,
            bar3Height.value,
            bar4Height.value
        )

        barHeights.forEachIndexed { index, heightFactor ->
            // Calculate x position for the current bar with centering offset
            val x = leftOffset + spacing + (index * (barWidth + spacing))

            // Calculate the full height of the bar (total height * height factor)
            val barFullHeight = (centerY * heightFactor * 2)

            // Calculate the top position to center the bar vertically
            val topY = centerY - (barFullHeight / 2)

            // Define corner radius (half of bar width for rounded caps)
            val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

            // Draw the bar as a single rounded rectangle
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, topY),
                size = Size(barWidth, barFullHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
