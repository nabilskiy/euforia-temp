package digital.euforia.app.ui.player.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.session.MediaController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Exposes playback progress as a pair of floats (currentMs to durationMs).
 * Polls the provided MediaController at the given interval while it is active.
 */
@Composable
fun rememberPlaybackProgress(
    controller: MediaController?,
    intervalMs: Long = 50L
): Pair<Float, Float> {
    val current = remember { mutableStateOf(0f) }
    val duration = remember { mutableStateOf(1f) }

    LaunchedEffect(controller) {
        val c = controller ?: return@LaunchedEffect
        // Initialize duration if available
        val d0 = c.duration
        if (d0 > 0) duration.value = d0.toFloat()

        while (isActive && controller === c) {
            val d = c.duration
            if (d > 0 && d.toFloat() != duration.value) {
                duration.value = d.toFloat()
            }
            current.value = c.currentPosition.toFloat()
            delay(intervalMs)
        }
    }

    return current.value to duration.value
}