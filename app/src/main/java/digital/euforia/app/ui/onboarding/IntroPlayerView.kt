package digital.euforia.app.ui.onboarding

import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.rememberExoPlayer
import digital.euforia.app.ui.util.widget.noRippleClickable

@OptIn(UnstableApi::class)
@Composable
fun BoxScope.IntroPlayerView(
    modifier: Modifier = Modifier,
    onSkipClick: () -> Unit,
    onPlaybackComplete: (Boolean) -> Unit
) {
    val exoPlayer = rememberExoPlayer(
        videoRes = R.raw.vid_intro,
        onPlaybackComplete = {
            onPlaybackComplete(true)
        }
    )

    DisposableEffect(
        exoPlayer
    ) {
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
                    }
                }
        },
        update = { root ->
            root.findViewById<PlayerView>(R.id.player_view).player = exoPlayer
        },
        modifier = modifier.fillMaxSize()
    )
    SkipText(onClick = onSkipClick)
}

@Composable
private fun BoxScope.SkipText(onClick: () -> Unit) {
    val localizedResources = LocalLocalizedRes.current

    Text(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 22.dp, end = 8.dp)
            .noRippleClickable(onClick = onClick)
            .padding(8.dp)
            .statusBarsPadding(),
        text = localizedResources.string(R.string.skip),
        color = White.copy(alpha = 0.5f),
        style = MaterialTheme.typography.bodyMedium
    )
}
