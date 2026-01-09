package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import digital.euforia.app.R
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.White

@Composable
fun PremiumButton(modifier: Modifier = Modifier, text: String, gradient: Brush = PremiumGradient, onClick : () -> Unit) {
    AnimatedSizeBox(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        var size by remember { mutableStateOf(IntSize.Zero) }

        // Infinite shimmer animation moving horizontally
        val infiniteTransition = rememberInfiniteTransition(label = "premiumShimmer")
        val shimmerOffsetX by infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "premiumShimmerOffset"
        )

        Box(
            modifier = Modifier
                .height(56.dp)
                .background(brush = gradient, shape = CircleShape)
                .clip(CircleShape)
                .fillMaxWidth()
                .onGloballyPositioned { size = it.size },
            contentAlignment = Alignment.Center
        ) {
            // Shimmer overlay: a soft moving highlight across the button
            if (size.width > 0 && size.height > 0) {
                val widthF = size.width.toFloat()
                val animatedStart = (shimmerOffsetX * widthF)
                val brush = Brush.linearGradient(
                    colors = listOf(
                        White.copy(alpha = 0f),
                        White.copy(alpha = 0.25f),
                        White.copy(alpha = 0f)
                    ),
                    start = Offset(x = animatedStart - widthF, y = 0f),
                    end = Offset(x = animatedStart, y = 0f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = brush, shape = CircleShape)
                        .clip(CircleShape)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = White,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}