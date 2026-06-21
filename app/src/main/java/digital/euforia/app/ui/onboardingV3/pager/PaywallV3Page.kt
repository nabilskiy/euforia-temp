package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import digital.euforia.app.ui.util.SubscriptionActivityLauncher

@Composable
fun PaywallV3Page(
    isPageActive: Boolean,
    onNextClick: () -> Unit,
) {
    if (!isPageActive) return

    SubscriptionActivityLauncher(
        screenId = 18,
        onSuccess = onNextClick,
        onClose = onNextClick,
    ) { launchSubscription ->
        var didLaunch by remember { mutableStateOf(false) }
        LaunchedEffect(isPageActive) {
            if (isPageActive && !didLaunch) {
                didLaunch = true
                launchSubscription()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF17191F)),
        )
    }
}
