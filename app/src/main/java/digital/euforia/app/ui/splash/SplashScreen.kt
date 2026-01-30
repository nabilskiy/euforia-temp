package digital.euforia.app.ui.splash

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.Home
import digital.euforia.app.ui.navigation.Splash
import digital.euforia.app.ui.navigation.Video
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.ProgressIndicator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SplashScreen(navController: NavHostController, viewModel: SplashViewModel) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(navController, sideEffect)
    }
    SplashScreenContent()
}

@Composable
private fun SplashScreenContent() {
    BackHandler(enabled = true) {
        // Disable back press on splash screen
    }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground).systemBarsPadding()) {
        val localizedRes = LocalLocalizedRes.current
        Text(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
            text = localizedRes.string(R.string.copyright_euforia),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium, fontSize = 12.sp,
                brush = PremiumGradient
            ),
        )

        ProgressIndicator(Modifier.align(Alignment.Center).statusBarsPadding().padding(top = 16.dp))
    }
}

private fun handleSideEffect(navController: NavHostController, sideEffect: SplashSideEffect) {
    when (sideEffect) {
        is SplashSideEffect.NavigateOnboarding -> {
            navController.navigate(Video) {
                popUpTo(Splash) { inclusive = true }
            }
        }

        is SplashSideEffect.NavigateHome -> {
            navController.navigate(Home) {
                popUpTo(Splash) { inclusive = true }
            }
        }

        else -> {}
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    EuforiaTheme {
        SplashScreenContent()
    }
}
