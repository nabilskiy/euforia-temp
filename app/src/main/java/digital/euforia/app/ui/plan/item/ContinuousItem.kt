package digital.euforia.app.ui.plan.item

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.plan.PlanViewItems
import digital.euforia.app.ui.theme.DescriptionDisabled
import digital.euforia.app.ui.theme.SecondaryText
import digital.euforia.app.ui.theme.StreakBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.CircleButtonBackground
import digital.euforia.app.ui.theme.CircleButtonIcon
import digital.euforia.app.ui.theme.Vibe2Color
import digital.euforia.app.ui.theme.Vibe3Color
import kotlin.math.max
import kotlin.math.min

fun LazyListScope.continuousItem(
    days: Int
) = item(key = PlanViewItems.STREAK, contentType = PlanViewItems.STREAK) {

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StreakBackground)
            .padding(20.dp)
    ) {
        ShareButton(modifier = Modifier.align(Alignment.TopEnd), onClick = {})
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.continuous_days_title).uppercase(),
                color = DescriptionDisabled,
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = days.toString(),
                color = White,
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = stringResource(R.string.continuous_days_text),
                color = SecondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
            ContinuousDaysProgressBar(days, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RowScope.LabelAtPosition(
    fraction: Float,
    text: String,
    style: TextStyle
) {
    val leftWeight = fraction.coerceIn(0f, 1f).coerceAtLeast(0.0001f)
    val rightWeight = (1f - fraction).coerceAtLeast(0.0001f)

    Spacer(Modifier.weight(leftWeight, fill = true))
    Text(text = text, style = style)
    Spacer(Modifier.weight(rightWeight, fill = true))
}

@Composable
private fun ShareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(CircleButtonBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_share), // update icon
            contentDescription = "Share",
            tint = CircleButtonIcon,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ContinuousDaysProgressBar(
    currentValue: Int,
    modifier: Modifier = Modifier
) {
    val steps = remember {
        listOf(
            ProgressStep(value = 30, portion = 0.15f, textRes = R.string.continuous_days_30),
            ProgressStep(value = 90, portion = 0.20f, textRes = R.string.continuous_days_90),
            ProgressStep(value = 365, portion = 0.65f, textRes = R.string.continuous_days_365)
        )
    }

    val clampedValue = max(0, currentValue)
    val progress = remember(clampedValue) {
        calculateProgress(clampedValue, steps)
    }

    Box(
        modifier = modifier
            .padding(bottom = 18.dp)
            .height(44.dp)
    ) {
        // background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .align(Alignment.CenterStart)
                .background(
                    color = DescriptionDisabled.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(2.dp)
                )
        )

        // foreground
        AnimatedProgressBar(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(16.dp)
                .align(Alignment.CenterStart)
        )

        // label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.Start
        ) {
            steps.forEachIndexed { index, step ->
                val isAchieved = clampedValue >= step.value
                val markerColor = if (isAchieved) White else DescriptionDisabled

                Box(
                    modifier = Modifier
                        .weight(step.portion)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(26.dp)
                            .align(Alignment.CenterEnd)
                            .background(markerColor)
                    )

                    Text(
                        maxLines = 1,
                        text = stringResource(step.textRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = markerColor,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(
                                x = if (index < steps.size - 1) 24.dp else 0.dp,
                                y = 30.dp
                            ),
                        textAlign = if (index < steps.size - 1) TextAlign.Center else TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(animatedProgress)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Vibe2Color,
                        Vibe3Color
                    )
                )
            )
    )
}

private data class ProgressStep(
    val value: Int,
    val portion: Float,
    @StringRes val textRes: Int
)

private fun calculateProgress(currentValue: Int, steps: List<ProgressStep>): Float {
    var accumulatedProgress = 0f
    var remainingValue = currentValue

    for (step in steps) {
        if (currentValue >= step.value) {
            accumulatedProgress += step.portion
            remainingValue = currentValue - step.value
        } else {
            val previousTotal = currentValue - remainingValue
            val stepRange = step.value - previousTotal
            val progressInStep = remainingValue.toFloat() / stepRange.toFloat()
            accumulatedProgress += progressInStep * step.portion
            break
        }
    }

    return min(accumulatedProgress, 1f)
}