package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import kotlin.math.max

class BubbleWithCaretShape(
    private val cornerRadius: Dp = 12.dp,
    private val caretWidth: Dp = 12.dp,
    private val caretHeight: Dp = 8.dp,
    private val caretOffsetX: Dp = 0.dp, // shift caret horizontally if needed
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: Density
    ): Outline {
        with(density) {
            val r = cornerRadius.toPx()
            val cw = caretWidth.toPx()
            val ch = caretHeight.toPx()
//            val cx = size.width / 2f + caretOffsetX.toPx()
            val cx = caretOffsetX.toPx()

            // Bubble body is reduced by caret height at the bottom
            val body = Rect(0f, 0f, size.width, max(0f, size.height - ch))
            val rr = RoundRect(body, r, r)

            val p = Path().apply {
                // rounded rect
                addRoundRect(rr)

                // triangular caret (bottom)
                moveTo(cx - cw / 2f, body.bottom)      // left base
                lineTo(cx, body.bottom + ch)           // tip
                lineTo(cx + cw / 2f, body.bottom)      // right base
                close()
            }
            return Outline.Generic(p)
        }
    }
}

@Composable
fun TriangleTooltipBubble(
    text: String,
    modifier: Modifier = Modifier,
    caretWidth: Dp = 12.dp,
    caretHeight: Dp = 8.dp,
    caretOffsetX: Dp = 0.dp,
    border: BorderStroke? = null
) {
    Surface(
        modifier = modifier,
        shape = BubbleWithCaretShape(
            cornerRadius = 12.dp,
            caretWidth = caretWidth,
            caretHeight = caretHeight,
            caretOffsetX = caretOffsetX
        ),
        color = PrimaryBackground,
        contentColor = White,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        border = border
    ) {
        // Add bottom padding so text doesn’t “sit on” the caret base visually
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
        )
    }
}
