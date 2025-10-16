package digital.euforia.app.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.navigation.NavHostController
import digital.euforia.app.ui.navigation.Onboarding
import digital.euforia.app.ui.navigation.Video
import digital.euforia.app.ui.onboarding.IntroPlayerView
import digital.euforia.app.ui.theme.Black
import kotlinx.coroutines.delay

private const val VIDEO_ANIMATION_DURATION = 2500

@Composable
fun VideoScreen(
    navController: NavHostController,
    viewModel: VideoViewModel
) {
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
//        enter = fadeIn(animationSpec = tween(durationMillis = VIDEO_ANIMATION_DURATION)),
        exit = fadeOut(animationSpec = tween(durationMillis = VIDEO_ANIMATION_DURATION))
    ) {
        VideoContent(
            onSkipClick = {
                isVisible = false
                navigateToOnboarding(navController)
            },
            onPlaybackComplete = { completed ->
                //probably it navigates again on skip click
                if (completed) {
                    isVisible = false
                    navigateToOnboarding(navController)
                }
            }
        )
    }
}

@Composable
private fun VideoContent(
    onSkipClick: () -> Unit,
    onPlaybackComplete: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
//            .background(Black)
    ) {
        IntroPlayerView(
            onSkipClick = onSkipClick,
            onPlaybackComplete = onPlaybackComplete
        )
    }
}

private fun navigateToOnboarding(navController: NavHostController) {
    navController.navigate(Onboarding) {
        popUpTo(Video) { inclusive = true }
    }
}
