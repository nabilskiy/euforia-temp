package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.ui.onboardingV3.OnboardingV3NotificationSetting
import digital.euforia.app.ui.onboardingV3.OnboardingV3NotificationSlot
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.TimePickerView

@Composable
fun NotificationsSetupV3Page(
    settings: List<OnboardingV3NotificationSetting>,
    timeOfDayConfig: TimeOfDayConfig,
    isPageActive: Boolean,
    onToggle: (OnboardingV3NotificationSlot, Boolean) -> Unit,
    onTimeChanged: (OnboardingV3NotificationSlot, Pair<Int, Int>) -> Unit,
) {
    if (!isPageActive) return

    val appear = remember { Animatable(0f) }
    var expandedSlot by remember { mutableStateOf<OnboardingV3NotificationSlot?>(null) }
    LaunchedEffect(isPageActive) {
        if (isPageActive) {
            appear.snapTo(0f)
            appear.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 250.dp, bottom = 126.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        settings.forEachIndexed { index, item ->
            NotificationTimeCard(
                setting = item,
                timeOfDayConfig = timeOfDayConfig,
                isExpanded = expandedSlot == item.slot,
                onToggle = { onToggle(item.slot, it) },
                onExpandChange = { expandedSlot = if (it) item.slot else null },
                onTimeChanged = { onTimeChanged(item.slot, it) },
                modifier = Modifier.graphicsLayer {
                    val value = ((appear.value - index * 0.14f) / 0.72f).coerceIn(0f, 1f)
                    alpha = value
                    translationY = (1f - value) * 56f
                    scaleX = 0.94f + value * 0.06f
                    scaleY = 0.94f + value * 0.06f
                },
            )
        }
    }
}

@Composable
private fun NotificationTimeCard(
    setting: OnboardingV3NotificationSetting,
    timeOfDayConfig: TimeOfDayConfig,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit,
    onExpandChange: (Boolean) -> Unit,
    onTimeChanged: (Pair<Int, Int>) -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val titleRes = when (setting.slot) {
        OnboardingV3NotificationSlot.Morning -> R.string.intro_notifications_morning_title
        OnboardingV3NotificationSlot.Daytime -> R.string.intro_notifications_daytime_title
        OnboardingV3NotificationSlot.Evening -> R.string.intro_notifications_evening_title
    }

    val hourBounds = timeOfDayConfig.notificationHourBounds(setting.slot)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(Color(0xFF1F2228), RoundedCornerShape(28.dp))
            .clickable(enabled = setting.enabled) { onExpandChange(!isExpanded) }
            .padding(horizontal = 24.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = localizedRes.string(titleRes),
                color = White.copy(alpha = if (setting.enabled) 0.8f else 0.4f),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text(
                    text = "%d:%02d".format(setting.hour, setting.minute),
                    color = White.copy(alpha = if (setting.enabled) 1f else 0.4f),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 34.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "›",
                    color = White.copy(alpha = if (setting.enabled) 0.6f else 0.25f),
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .graphicsLayer {
                            rotationZ = if (isExpanded) 90f else 0f
                        },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Light,
                    ),
                )
                TimePickerView(
                    expanded = isExpanded && setting.enabled,
                    initialTime = setting.hour to setting.minute,
                    fromHour = hourBounds.first,
                    toHour = hourBounds.second,
                    onTimeChanged = onTimeChanged,
                    onExpandedChange = onExpandChange,
                )
            }
        }
        V3Switch(
            checked = setting.enabled,
            onCheckedChange = onToggle,
        )
    }
}

private fun TimeOfDayConfig.notificationHourBounds(
    slot: OnboardingV3NotificationSlot,
): Pair<Int, Int> = when (slot) {
    OnboardingV3NotificationSlot.Morning -> morningBegin to (daytimeBegin - 1).coerceAtLeast(morningBegin)
    OnboardingV3NotificationSlot.Daytime -> daytimeBegin to (eveningBegin - 1).coerceAtLeast(daytimeBegin)
    OnboardingV3NotificationSlot.Evening -> eveningBegin to morningBegin
}

@Composable
private fun V3Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 62.dp, height = 36.dp)
            .background(
                color = if (checked) Color(0xFF4257D5) else White.copy(alpha = 0.16f),
                shape = CircleShape,
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (checked) 30.dp else 4.dp)
                .size(28.dp)
                .background(White, CircleShape),
        )
    }
}
