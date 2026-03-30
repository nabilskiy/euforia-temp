package digital.euforia.app.ui.howitworks

import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigation.NavHostController
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.theme.PrimaryBackground
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun HowItWorksScreen(
    navController: NavHostController,
    viewModel: HowItWorksViewModel,
    navBarVisibilityState: MutableState<Boolean>
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }
    NavBarlessScreen(navBarVisibilityState) {
        HowItWorksContent(videoUrl = state.videoUrl)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun HowItWorksContent(videoUrl: String?) {
    val context = LocalContext.current
    if (videoUrl.isNullOrBlank()) return

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

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        AndroidView(
            factory = { ctx ->
                LayoutInflater.from(ctx)
                    .inflate(R.layout.player_view_texture, null, false).also { root ->
                        root.findViewById<PlayerView>(R.id.player_view).apply {
                            this.player = exoPlayer
                            setShutterBackgroundColor(Color.TRANSPARENT)
                            setKeepContentOnPlayerReset(true)
                            // Center video and fit within parent while preserving aspect ratio
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            // Enable default ExoPlayer controls
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
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        )
    }
}

private fun handleSideEffect(sideEffect: HowItWorksSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}