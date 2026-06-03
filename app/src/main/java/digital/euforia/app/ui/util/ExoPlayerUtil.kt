package digital.euforia.app.ui.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import digital.euforia.app.service.soundscapes.pauseActiveSoundscapeIfNeeded


@Composable
fun rememberExoPlayer(
    @RawRes videoRes: Int,
    context: Context = LocalContext.current,
    nearEndLeadMs: Long = 0L,
    onNearEnd: (() -> Unit)? = null,
    onPlaybackComplete: (() -> Unit)? = null,
): ExoPlayer {
    val onNearEndState = rememberUpdatedState(onNearEnd)
    val onPlaybackCompleteState = rememberUpdatedState(onPlaybackComplete)

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .path(videoRes.toString()).build()
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            pauseActiveSoundscapeIfNeeded(context)
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlaybackCompleteState.value?.invoke()
                    }
                }
            })
        }
    }

    if (nearEndLeadMs > 0L) {
        LaunchedEffect(player, nearEndLeadMs) {
            var nearEndFired = false
            while (isActive) {
                val duration = player.duration
                if (duration != C.TIME_UNSET && duration > 0L) {
                    val remaining = duration - player.currentPosition
                    if (!nearEndFired && remaining in 1..nearEndLeadMs) {
                        nearEndFired = true
                        onNearEndState.value?.invoke()
                    }
                }
                if (player.playbackState == Player.STATE_ENDED) break
                delay(50L)
            }
        }
    }

    return player
}
