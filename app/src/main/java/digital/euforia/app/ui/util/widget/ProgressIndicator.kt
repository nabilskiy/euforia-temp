package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun ProgressIndicator(modifier: Modifier = Modifier) {
    val rotations = 8
    var currentRotation by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100L)
            currentRotation = (currentRotation + 1) % rotations
        }
    }

    Canvas(modifier = modifier.size(32.dp)) {
        val canvasSize = size.minDimension
        val rectWidth = canvasSize / 12
        val rectHeight = canvasSize / 5
        val radius = canvasSize / 2 - rectHeight / 2

        for (i in 0 until rotations) {
            val angle = i * (360f / rotations)
            val alphaFactor = ((rotations + i - currentRotation) % rotations).toFloat() / rotations
            val rectColor = White.copy(alpha = 0.2f + 0.8f * (1 - alphaFactor))

            rotate(angle) {
                drawRoundRect(
                    color = rectColor,
                    topLeft = Offset(canvasSize / 2 - rectWidth / 2, (canvasSize / 2) - radius),
                    size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                    cornerRadius = CornerRadius(rectWidth / 2, rectWidth / 2),
                    style = Fill
                )
            }
        }
    }
}