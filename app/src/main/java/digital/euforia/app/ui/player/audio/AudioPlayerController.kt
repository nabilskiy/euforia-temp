@file:OptIn(UnstableApi::class)

package digital.euforia.app.ui.player.audio

import android.content.ComponentName
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import digital.euforia.app.service.AudioPlaybackService

@Composable
fun rememberMediaController(
    timeOfDayUrl: Uri?,
    playWhenReady: Boolean = true,
    onIsPlayingChanged: (Boolean) -> Unit = {},
    onEnded: () -> Unit = {}
): MediaController? {
    val context = LocalContext.current
    val controllerState = remember { mutableStateOf<MediaController?>(null) }

    // Keep latest callbacks to avoid capturing stale lambdas in listeners
    val updatedOnPlayingChanged = rememberUpdatedState(newValue = onIsPlayingChanged)
    val updatedOnEnded = rememberUpdatedState(newValue = onEnded)

    LaunchedEffect(timeOfDayUrl) {
        if (!timeOfDayUrl?.path.isNullOrBlank() && controllerState.value == null) {
            val token = SessionToken(context, ComponentName(context, AudioPlaybackService::class.java))
            val future = MediaController.Builder(context, token).buildAsync()
            future.addListener({
                val c = future.get()
                controllerState.value = c
                c.setMediaItem(MediaItem.fromUri(timeOfDayUrl))
                c.prepare()
                if (playWhenReady) c.playWhenReady = true
//                c.play()
            }, context.mainExecutor)
        }
    }

    DisposableEffect(controllerState.value) {
        val controller = controllerState.value
        if (controller == null) return@DisposableEffect onDispose { }

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatedOnPlayingChanged.value.invoke(isPlaying)
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) updatedOnEnded.value.invoke()
            }
        }
        controller.addListener(listener)

        onDispose {
            controller.removeListener(listener)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controllerState.value?.release()
            controllerState.value = null
        }
    }

    return controllerState.value
}