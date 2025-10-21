package digital.euforia.app.ui.splash

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.Home
import digital.euforia.app.ui.navigation.Onboarding
import digital.euforia.app.ui.navigation.Splash
import digital.euforia.app.ui.navigation.Video
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.PrimaryBackground
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
        Icon(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
            painter = painterResource(R.drawable.ic_euforia_label),
            contentDescription = null,
            tint = Color.Unspecified
        )

        ProgressIndicator(Modifier.align(Alignment.Center))
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
