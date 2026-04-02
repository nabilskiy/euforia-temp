/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class SoundscapeLayerState(
    val id: Int,
    val title: String,
    val volume: Float = 1f,
    val muted: Boolean = false,
)

data class SoundscapePlaybackState(
    val sceneId: Int? = null,
    val sceneTitle: String = "",
    val isPlaying: Boolean = false,
    val musicVolume: Float = 0.35f,
    val layers: List<SoundscapeLayerState> = emptyList(),
    val timerSeconds: Int? = null,
    val engineHealth: String = "OK",
)

interface SoundEngine {
    val playback: StateFlow<SoundscapePlaybackState>
    fun start(sceneId: Int, sceneTitle: String, layers: List<SoundscapeLayerState>)
    fun playPause()
    fun setMusicVolume(volume: Float)
    fun setLayerVolume(layerId: Int, volume: Float)
    fun muteLayer(layerId: Int, muted: Boolean)
    fun removeLayer(layerId: Int)
    fun setTimer(seconds: Int?)
}

@Singleton
class SoundscapePlaybackController @Inject constructor() : SoundEngine {
    private val _playback = MutableStateFlow(SoundscapePlaybackState())
    override val playback: StateFlow<SoundscapePlaybackState> = _playback.asStateFlow()

    override fun start(sceneId: Int, sceneTitle: String, layers: List<SoundscapeLayerState>) {
        val startedAt = System.currentTimeMillis()
        _playback.value = SoundscapePlaybackState(
            sceneId = sceneId,
            sceneTitle = sceneTitle,
            isPlaying = true,
            layers = layers.take(10),
        )
        Timber.tag("SOUNDSCAPES_METRICS").d(
            "scene_start sceneId=%s layers=%s startupMs=%s",
            sceneId,
            layers.size.coerceAtMost(10),
            System.currentTimeMillis() - startedAt
        )
    }

    override fun playPause() {
        _playback.update { it.copy(isPlaying = !it.isPlaying) }
    }

    override fun setMusicVolume(volume: Float) {
        _playback.update { it.copy(musicVolume = volume.coerceIn(0f, 1f)) }
    }

    override fun setLayerVolume(layerId: Int, volume: Float) {
        _playback.update { state ->
            state.copy(
                layers = state.layers.map { layer ->
                    if (layer.id == layerId) layer.copy(volume = volume.coerceIn(0f, 1f)) else layer
                }
            )
        }
        Timber.tag("SOUNDSCAPES_METRICS").d("layer_volume layerId=%s volume=%.2f", layerId, volume)
    }

    override fun muteLayer(layerId: Int, muted: Boolean) {
        _playback.update { state ->
            state.copy(
                layers = state.layers.map { layer ->
                    if (layer.id == layerId) layer.copy(muted = muted) else layer
                }
            )
        }
    }

    override fun removeLayer(layerId: Int) {
        _playback.update { state ->
            state.copy(layers = state.layers.filterNot { it.id == layerId })
        }
    }

    override fun setTimer(seconds: Int?) {
        _playback.update { it.copy(timerSeconds = seconds) }
    }
}

