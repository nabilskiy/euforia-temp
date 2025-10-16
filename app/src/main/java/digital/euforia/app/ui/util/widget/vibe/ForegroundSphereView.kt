package digital.euforia.app.ui.util.widget.vibe


import android.graphics.BlendMode
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import androidx.compose.ui.graphics.toArgb
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Paint as AndroidPaint
import android.graphics.RenderEffect
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.eveningColors


fun DrawScope.drawForegroundSphere(
    center: Offset,
    rx: Float,
    ry: Float,
    bodyTop: Color,
    bodyBottom: Color,
    strokeTop: Color,
    strokeBottom: Color,
    strokeWidth: Float,
    bodyBlurPx: Float = 0f,
    screenCenter: Offset = Offset(size.width / 2f, size.height / 2f),
    selfRotationAngle: Float = 0f  // New parameter for self-rotation
) {

    // Calculate angle between ellipse center and screen center
    val angleToCenter = atan2(
        screenCenter.y - center.y,
        screenCenter.x - center.x
    ) * (180f / Math.PI.toFloat())/* + 90f*/ // Add 90 degrees to align the narrow side (y-axis) with the direction to center

    drawIntoCanvas { canvas ->
        // Save the current canvas state
        canvas.nativeCanvas.save()

        // Apply both rotations:
        // 1. Rotate to make the narrow side point to center
        // 2. Apply self-rotation on top of that
        canvas.nativeCanvas.rotate(angleToCenter + selfRotationAngle, center.x, center.y)

        // Define the oval bounds (now rotated)
        val left = center.x - rx
        val top = center.y - ry
        val right = center.x + rx
        val bottom = center.y + ry
        val oval = RectF(left, top, right, bottom)

        // Тіло з вертикальним градієнтом
        val bodyPaint = AndroidPaint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                left, top, left, bottom,
                bodyTop.toArgb(), bodyBottom.toArgb(), Shader.TileMode.CLAMP
            )
            if (bodyBlurPx > 0f) {
                maskFilter = BlurMaskFilter(bodyBlurPx, BlurMaskFilter.Blur.NORMAL)
            }
            blendMode = BlendMode.COLOR_DODGE
        }
        canvas.nativeCanvas.drawOval(oval, bodyPaint)

        val strokePaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            this.strokeWidth = strokeWidth
            shader = LinearGradient(
                left, top, left, bottom,
                strokeTop.toArgb(), strokeBottom.toArgb(), Shader.TileMode.CLAMP
            )
            blendMode = BlendMode.COLOR_DODGE

        }

        canvas.nativeCanvas.drawOval(oval, strokePaint)

        val outlineWidth = strokeWidth/4
        val strokeOval = RectF(left + outlineWidth, top + outlineWidth, right - outlineWidth, bottom - outlineWidth)
        val strokeOutlinePaint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            this.strokeWidth = outlineWidth
            shader = LinearGradient(
                left, top, left, bottom,
                White.copy(alpha = 0.05f).toArgb(), White.copy(alpha = 0.08f).toArgb(), Shader.TileMode.CLAMP
            )
            blendMode = BlendMode.COLOR_DODGE

        }

        canvas.nativeCanvas.drawOval(strokeOval, strokeOutlinePaint)

        // Restore the canvas to its original state
        canvas.nativeCanvas.restore()
    }
}
