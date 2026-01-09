package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.CalendarGradient
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.TasksGradient
import digital.euforia.app.ui.theme.White

@Composable
fun UpgradeView(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Text(
        text = "UPGRADE",
        modifier = modifier.noRippleClickable { onClick() },
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 12.sp,
            fontWeight = SemiBold,
            brush = PremiumGradient
        ),
    )
}

@Composable
fun MaxView(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Text(
        text = "MAX",
        modifier = modifier.noRippleClickable { onClick() },
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 12.sp,
            fontWeight = SemiBold,
            brush = PremiumGradient
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