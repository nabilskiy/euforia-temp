package digital.euforia.app.ui.util.widget.vibe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope


/**
 * Розмита колода сфер для фону. На A12+ — RenderEffect blur, на старших — м’який фолбек.
 */

fun DrawScope.drawBackgroundSphere(
    center: Offset,
    radius: Float,
    color: Color,
    blurPx: Float
) {
  fun ballBrush(center: Offset, ballColor: Color) = Brush.radialGradient(
        colors = listOf(
            ballColor.copy(alpha = 0.75f),
            ballColor.copy(alpha = 0.0f)
        ),
        center = center,
        radius = radius
    )

    drawCircle(
        brush = ballBrush(center, color), radius = radius, center = center,
        blendMode = androidx.compose.ui.graphics.BlendMode.Difference
    )

}