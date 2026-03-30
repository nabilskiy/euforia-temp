package digital.euforia.app.ui.util.widget

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.CalendarGradient
import digital.euforia.app.ui.theme.PremiumColors
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.TasksGradient
import digital.euforia.app.ui.theme.White

@Composable
fun UpgradeView(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "upgradeShimmer")
    val shimmerOffsetX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "upgradeShimmerOffset"
    )

    val brush = if (size.width > 0) {
        val widthF = size.width.toFloat()
        Brush.linearGradient(
            colors = PremiumColors.reversed() + PremiumColors.last(),
            start = Offset(x = shimmerOffsetX * widthF, y = 0f),
            end = Offset(x = (shimmerOffsetX + 3f) * widthF, y = 0f)
        )
    } else {
        PremiumGradient
    }

    Text(
        text = "UPGRADE",
        modifier = modifier
            .noRippleClickable { onClick() }
            .onGloballyPositioned { size = it.size },
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 12.sp,
            fontWeight = SemiBold,
            brush = brush
        ),
    )
}

@Composable
fun MaxView(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "maxShimmer")
    val shimmerOffsetX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "maxShimmerOffset"
    )

    val brush = if (size.width > 0) {
        val widthF = size.width.toFloat()
        Brush.linearGradient(
            colors = PremiumColors.reversed() + PremiumColors.last(),
            start = Offset(x = shimmerOffsetX * widthF, y = 0f),
            end = Offset(x = (shimmerOffsetX + 3f) * widthF, y = 0f)
        )
    } else {
        PremiumGradient
    }

    Text(
        text = "MAX",
        modifier = modifier
            .noRippleClickable { onClick() }
            .onGloballyPositioned { size = it.size },
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 12.sp,
            fontWeight = SemiBold,
            brush = brush
        ),
    )
}

@Composable
fun MaxBadge(modifier: Modifier = Modifier) {
    Text(
        text = "MAX",
        modifier = modifier.background(color = White, shape = RoundedCornerShape(4.dp))
            .padding(2.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 8.sp,
            lineHeight = 8.sp,
            fontWeight = Medium,
            brush = PremiumGradient
        ),
    )
}


@Composable
fun MaxView(modifier: Modifier = Modifier) {
    Text(
        text = "MAX",
        modifier = modifier.background(color = White, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontWeight = SemiBold,
            brush = CalendarGradient
        ),
    )
}