package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.onboardingV3.OnboardingV3Page
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun PlaceholderPage(
    page: OnboardingV3Page,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val title = page.titleRes?.let { localizedRes.string(it) } ?: page.stepId

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "TODO: $title",
            color = White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}
