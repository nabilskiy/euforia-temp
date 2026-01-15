package digital.euforia.app.ui.programs.exercise


import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.programs.ExerciseUi
import digital.euforia.app.ui.util.widget.ErrorView
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.ProgressIndicator
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ExerciseScreen(
    navController: NavHostController,
    viewModel: ExerciseViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    NavBarlessScreen(navBarVisibilityState) {
        ExerciseContent(
            exercise = state.exercise,
            navController = navController,
            isPremium = state.isPremium,
            isLoading = state.isLoading,
            errorState = state.errorState,
            onRetryClick = { },
            onDownloadsClick = { }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExerciseContent(
    exercise: ExerciseUi?,
    navController: NavHostController,
    isPremium: Boolean,
    isLoading: Boolean,
    errorState: ErrorViewState?,
    onRetryClick: () -> Unit,
    onDownloadsClick: () -> Unit
) {
    val videoUrl = exercise?.videoUrl

    if (videoUrl.isNullOrBlank()) return

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            ProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorState != null) {
            ErrorView(
                modifier = Modifier.align(Alignment.Center),
                state = errorState,
                onRetryClick = onRetryClick,
                onDownloadsClick = onDownloadsClick
            )
        } else {
            ExerciseVideoPlayer(
                videoUrl = videoUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExerciseVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            LayoutInflater.from(ctx)
                .inflate(R.layout.player_view_texture, null, false).also { root ->
                    root.findViewById<PlayerView>(R.id.player_view).apply {
                        this.player = exoPlayer
                        setShutterBackgroundColor(Color.TRANSPARENT)
                        setKeepContentOnPlayerReset(true)
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        useController = true
                        controllerShowTimeoutMs = 3000
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        setControllerHideOnTouch(true)
                        showController()
                    }
                }
        },
        update = { root ->
            root.findViewById<PlayerView>(R.id.player_view).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        modifier = modifier
    )
}

private fun handleSideEffect(sideEffect: ExerciseSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}