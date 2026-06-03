package digital.euforia.app.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import digital.euforia.app.ui.navigation.Onboarding
import digital.euforia.app.ui.navigation.Video
import digital.euforia.app.ui.onboarding.IntroPlayerView
import org.orbitmvi.orbit.compose.collectAsState

/** Fade-out duration; should match [INTRO_FADE_OUT_LEAD_MS] so the screen is gone when the video ends. */
private const val VIDEO_ANIMATION_DURATION = 1_000

/** Start fade-out this many ms before intro video ends. */
private const val INTRO_FADE_OUT_LEAD_MS = 1_000L

@Composable
fun VideoScreen(
    navController: NavHostController,
    viewModel: VideoViewModel
) {
    val state by viewModel.collectAsState()

    var isVisible by remember { mutableStateOf(true) }
    var hasNavigated by remember { mutableStateOf(false) }

    fun navigateOnce() {
        if (hasNavigated) return
        hasNavigated = true
        navigateToOnboarding(navController)
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(durationMillis = VIDEO_ANIMATION_DURATION)),
    ) {
        VideoContent(
            nearEndLeadMs = INTRO_FADE_OUT_LEAD_MS,
            onSkipClick = {
                isVisible = false
                navigateOnce()
            },
            onFadeOutStart = {
                isVisible = false
            },
            onPlaybackComplete = {
                navigateOnce()
            },
        )
    }
}

@Composable
private fun VideoContent(
    nearEndLeadMs: Long,
    onSkipClick: () -> Unit,
    onFadeOutStart: () -> Unit,
    onPlaybackComplete: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IntroPlayerView(
            nearEndLeadMs = nearEndLeadMs,
            onSkipClick = onSkipClick,
            onFadeOutStart = onFadeOutStart,
            onPlaybackComplete = { onPlaybackComplete() },
        )
    }
}

private fun navigateToOnboarding(navController: NavHostController) {
    navController.navigate(Onboarding) {
        popUpTo(Video) { inclusive = true }
    }
}
