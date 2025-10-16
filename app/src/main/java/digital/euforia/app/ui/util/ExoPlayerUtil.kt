package digital.euforia.app.ui.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer


@Composable
fun rememberExoPlayer(
    @RawRes videoRes: Int,
    context: Context = LocalContext.current,
    onPlaybackComplete: (() -> Unit)? = null
) = remember {
    ExoPlayer.Builder(context).build().apply {
        val uri = Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .path(videoRes.toString()).build()
        repeatMode = ExoPlayer.REPEAT_MODE_OFF
        setMediaItem(MediaItem.fromUri(uri))
        prepare()
        playWhenReady = true

        if (onPlaybackComplete != null) {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlaybackComplete()
                    }
                }
            })
        }
    }
}
