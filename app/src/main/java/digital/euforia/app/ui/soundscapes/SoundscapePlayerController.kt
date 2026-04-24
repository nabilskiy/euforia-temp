/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package digital.euforia.app.ui.soundscapes

import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import digital.euforia.app.service.soundscapes.SoundscapePlaybackService

@Composable
fun rememberSoundscapeMediaController(
    onIsPlayingChanged: (Boolean) -> Unit = {}
): MediaController? {
    val context = LocalContext.current
    val controllerState = remember { mutableStateOf<MediaController?>(null) }
    val updatedOnPlayingChanged = rememberUpdatedState(newValue = onIsPlayingChanged)

    LaunchedEffect(Unit) {
        if (controllerState.value != null) return@LaunchedEffect
        val token = SessionToken(context, ComponentName(context, SoundscapePlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            { controllerState.value = future.get() },
            context.mainExecutor
        )
    }

    DisposableEffect(controllerState.value) {
        val controller = controllerState.value
        if (controller == null) return@DisposableEffect onDispose { }
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatedOnPlayingChanged.value.invoke(isPlaying)
            }
        }
        controller.addListener(listener)
        onDispose { controller.removeListener(listener) }
    }

    DisposableEffect(Unit) {
        onDispose {
            controllerState.value?.release()
            controllerState.value = null
        }
    }

    return controllerState.value
}

