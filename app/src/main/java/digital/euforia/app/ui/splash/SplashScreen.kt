package digital.euforia.app.ui.splash

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import digital.euforia.app.R
import digital.euforia.app.domain.model.config.CriticalUpdateConfig
import digital.euforia.app.ui.navigation.Home
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.Splash
import digital.euforia.app.ui.navigation.Video
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.LocalizedScope
import digital.euforia.app.ui.util.openAppPage
import digital.euforia.app.ui.util.widget.CriticalUpdateDialog
import digital.euforia.app.ui.util.widget.HeadphonesInfoView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SplashScreen(navController: NavHostController, viewModel: SplashViewModel) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val hazeState = remember { HazeState() }
    var isCriticalUpdateDialogShown by remember { mutableStateOf(false) }
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(
            navController = navController,
            sideEffect = sideEffect,
            onShowCriticalUpdate = { _, _ ->
                isCriticalUpdateDialogShown = true
            }
        )
    }

    SplashScreenContent(state.currentStep)

    if (isCriticalUpdateDialogShown) {
        state.criticalUpdateConfig?.let { config: CriticalUpdateConfig ->
            CriticalUpdateDialog(
                hazeState = hazeState,
                isCancellable = config.cancelable,
                onUpdate = {
                    openAppPage(context)
                },
                onDismiss = {
                    isCriticalUpdateDialogShown = false
                    viewModel.onDismissCriticalUpdate()
                    viewModel.proceedNavigation(state.isOnboardingCompleted)
                }
            )
        }
    }
}

@Composable
private fun SplashScreenContent(
    currentStep: SplashSteps
) {
    BackHandler(enabled = true) {
        // Disable back press on splash screen
    }

    val localizedRes = LocalLocalizedRes.current

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground).systemBarsPadding()) {
        val localizedRes = LocalLocalizedRes.current

        var showHeadphonesInfo by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            // small delay for nicer staging with the progress indicator
            delay(180)
            showHeadphonesInfo = true
        }
        Text(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
            text = localizedRes.string(R.string.copyright_euforia),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium, fontSize = 12.sp,
                brush = PremiumGradient
            ),
        )

        Column(
            modifier = Modifier.align(Alignment.Center).statusBarsPadding().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProgressIndicator(
                Modifier
            )

//            AnimatedVisibility(
//                visible = showHeadphonesInfo,
//                enter = fadeIn()
//            ) {
                Text(
                    text = localizedRes.string(currentStep.titleResId),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = White.copy(alpha = 0.7f)
                )
//            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = showHeadphonesInfo,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it })
        ) {
            HeadphonesInfoView(
                modifier = Modifier.navigationBarsPadding().padding(bottom = 24.dp)
            )
        }
    }
}

private fun handleSideEffect(
    navController: NavHostController,
    sideEffect: SplashSideEffect,
    onShowCriticalUpdate: (CriticalUpdateConfig, Boolean) -> Unit
) {
    when (sideEffect) {
        is SplashSideEffect.ShowCriticalUpdate -> {
            onShowCriticalUpdate(sideEffect.config, sideEffect.isOnboardingCompleted)
        }

        is SplashSideEffect.NavigateOnboarding -> {
            navController.navigate(Video) {
                popUpTo(Splash()) { inclusive = true }
            }
        }

        is SplashSideEffect.NavigateHome -> {
            navController.navigate(Home(sideEffect.deepLinkUri)) {
                popUpTo(Splash()) { inclusive = true }
            }
        }

        is SplashSideEffect.NavigateDestination -> {
            if (sideEffect.destination is HomeDestination.AudioPlayer ||
                sideEffect.destination is HomeDestination.PublicationPlayer ||
                sideEffect.destination is HomeDestination.PublicationDetails ||
                sideEffect.destination is HomeDestination.ProgramDetails
            ) {
                navController.navigate(HomeDestination.Plan) {
                    popUpTo(Splash()) { inclusive = true }
                }
                navController.navigate(sideEffect.destination)
            } else {
                navController.navigate(sideEffect.destination) {
                    popUpTo(Splash()) { inclusive = true }
                }
            }
        }

        else -> {}
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    EuforiaTheme {
        LocalizedScope(langTag = "en") {
            EuforiaTheme() {
                SplashScreenContent(SplashSteps.CHECKING_PURCHASES)
            }
        }
    }
}
