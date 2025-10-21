package digital.euforia.app.ui.util.widget

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

inline fun Modifier.applyIf(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

fun Modifier.fadeTop(height: Dp = 30.dp): Modifier = this
    // Force offscreen so blend mode works reliably
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()

        val h = height.toPx()
        val stop = (h / size.height).coerceIn(0f, 1f)

        // DstIn keeps destination (your content) where this rect is opaque
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,  // 0% at very top (hide)
                    stop to Color.Black,      // reach 100% alpha at 20.dp
                    1f to Color.Black         // fully keep the rest
                )
            ),
            blendMode = BlendMode.DstIn
        )
    }
