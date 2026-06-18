package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.onboardingV3.OnboardingV3NotificationSetting
import digital.euforia.app.ui.onboardingV3.OnboardingV3NotificationSlot
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun NotificationsSetupV3Page(
    settings: List<OnboardingV3NotificationSetting>,
    isPageActive: Boolean,
    onToggle: (OnboardingV3NotificationSlot, Boolean) -> Unit,
) {
    if (!isPageActive) return

    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(650))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 280.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(34.dp),
    ) {
        settings.forEachIndexed { index, item ->
            NotificationTimeCard(
                setting = item,
                onToggle = { onToggle(item.slot, it) },
                modifier = Modifier.graphicsLayer {
                    val value = ((appear.value - index * 0.08f) / 0.84f).coerceIn(0f, 1f)
                    alpha = value
                    translationY = (1f - value) * 30f
                    scaleX = 0.98f + value * 0.02f
                    scaleY = 0.98f + value * 0.02f
                },
            )
        }
    }
}

@Composable
private fun NotificationTimeCard(
    setting: OnboardingV3NotificationSetting,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val titleRes = when (setting.slot) {
        OnboardingV3NotificationSlot.Morning -> R.string.intro_notifications_morning_title
        OnboardingV3NotificationSlot.Daytime -> R.string.intro_notifications_daytime_title
        OnboardingV3NotificationSlot.Evening -> R.string.intro_notifications_evening_title
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color(0xFF1F2228), RoundedCornerShape(25.dp))
            .padding(horizontal = 24.dp, vertical = 22.dp),
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
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
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
                        fontSize = 42.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "›",
                    color = White.copy(alpha = if (setting.enabled) 0.6f else 0.25f),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Light,
                    ),
                )
            }
        }
        V3Switch(
            checked = setting.enabled,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun V3Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 78.dp, height = 46.dp)
            .background(
                color = if (checked) Color(0xFF4257D5) else White.copy(alpha = 0.16f),
                shape = CircleShape,
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (checked) 34.dp else 4.dp)
                .size(38.dp)
                .background(White, CircleShape),
        )
    }
}
