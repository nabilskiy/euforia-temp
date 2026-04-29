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
    val instanceKey: String = id.toString(),
    val title: String,
    val audioUrl: String? = null,
    val volume: Float = 1f,
    val muted: Boolean = false,
)

data class SoundscapePlaybackState(
    val sceneId: Int? = null,
    val sceneTitle: String = "",
    val isPlaying: Boolean = false,
    val musicVolume: Float = 0.35f,
    val sceneMusicUrl: String? = null,
    val sceneMusicVolumeFactor: Float = 1f,
    val ambientMode: Boolean = false,
    val layers: List<SoundscapeLayerState> = emptyList(),
    val timerSeconds: Int? = null,
    val engineHealth: String = "OK",
    val playlistSceneIds: List<Int> = emptyList(),
    val sceneImageUrl: String? = null,
)

private const val MAX_SOUND_LAYERS = 12

interface SoundEngine {
    val playback: StateFlow<SoundscapePlaybackState>
    fun start(
        sceneId: Int,
        sceneTitle: String,
        layers: List<SoundscapeLayerState>,
        sceneImageUrl: String? = null,
        sceneMusicUrl: String? = null,
        sceneMusicVolumeFactor: Float = 1f,
        ambientMode: Boolean = false,
    )
    fun playPause()
    /** Sets transport playing state (used by MediaSession sync; avoids toggle races). */
    fun setPlaying(playing: Boolean)
    fun setAmbientMode(enabled: Boolean)
    fun setPlaylist(sceneIds: List<Int>)
    fun renameCurrentScene(title: String)
    fun canPlayPrev(): Boolean
    fun canPlayNext(): Boolean
    fun prevSceneId(): Int?
    fun nextSceneId(): Int?
    fun setMusicVolume(volume: Float)
    fun setSceneMusic(musicUrl: String?, musicVolumeFactor: Float = 1f)
    fun setLayerVolume(layerKey: String, volume: Float)
    fun muteLayer(layerKey: String, muted: Boolean)
    fun removeLayer(layerKey: String)
    fun addLayer(layer: SoundscapeLayerState)
    fun setTimer(seconds: Int?)
}

@Singleton
class SoundscapePlaybackController @Inject constructor() : SoundEngine {
    private val _playback = MutableStateFlow(SoundscapePlaybackState())
    override val playback: StateFlow<SoundscapePlaybackState> = _playback.asStateFlow()

    override fun start(
        sceneId: Int,
        sceneTitle: String,
        layers: List<SoundscapeLayerState>,
        sceneImageUrl: String?,
        sceneMusicUrl: String?,
        sceneMusicVolumeFactor: Float,
        ambientMode: Boolean,
    ) {
        val startedAt = System.currentTimeMillis()
        _playback.value = SoundscapePlaybackState(
            sceneId = sceneId,
            sceneTitle = sceneTitle,
            isPlaying = true,
            sceneMusicUrl = sceneMusicUrl,
            sceneMusicVolumeFactor = sceneMusicVolumeFactor.coerceIn(0f, 1f),
            ambientMode = ambientMode,
            sceneImageUrl = sceneImageUrl,
            layers = layers.take(MAX_SOUND_LAYERS),
        )
        Timber.tag("SOUNDSCAPES_METRICS").d(
            "scene_start sceneId=%s layers=%s startupMs=%s",
            sceneId,
            layers.size.coerceAtMost(MAX_SOUND_LAYERS),
            System.currentTimeMillis() - startedAt
        )
    }

    override fun playPause() {
        _playback.update { it.copy(isPlaying = !it.isPlaying) }
    }

    override fun setPlaying(playing: Boolean) {
        _playback.update { state ->
            if (state.isPlaying == playing) state else state.copy(isPlaying = playing)
        }
    }

    override fun setAmbientMode(enabled: Boolean) {
        _playback.update { it.copy(ambientMode = enabled) }
    }

    override fun setPlaylist(sceneIds: List<Int>) {
        _playback.update { it.copy(playlistSceneIds = sceneIds.distinct()) }
    }

    override fun renameCurrentScene(title: String) {
        _playback.update { it.copy(sceneTitle = title) }
    }

    override fun canPlayPrev(): Boolean = prevSceneId() != null

    override fun canPlayNext(): Boolean = nextSceneId() != null

    override fun prevSceneId(): Int? {
        val state = _playback.value
        val current = state.sceneId ?: return null
        val index = state.playlistSceneIds.indexOf(current)
        if (index <= 0) return null
        return state.playlistSceneIds.getOrNull(index - 1)
    }

    override fun nextSceneId(): Int? {
        val state = _playback.value
        val current = state.sceneId ?: return null
        val index = state.playlistSceneIds.indexOf(current)
        if (index < 0 || index >= state.playlistSceneIds.lastIndex) return null
        return state.playlistSceneIds.getOrNull(index + 1)
    }

    override fun setMusicVolume(volume: Float) {
        _playback.update { it.copy(musicVolume = volume.coerceIn(0f, 1f)) }
    }

    override fun setSceneMusic(musicUrl: String?, musicVolumeFactor: Float) {
        _playback.update {
            it.copy(
                sceneMusicUrl = musicUrl?.takeIf(String::isNotBlank),
                sceneMusicVolumeFactor = musicVolumeFactor.coerceIn(0f, 1f)
            )
        }
    }

    override fun setLayerVolume(layerKey: String, volume: Float) {
        _playback.update { state ->
            val idx = state.layers.indexOfFirst { it.instanceKey == layerKey }
            if (idx < 0) state else {
                val next = state.layers.toMutableList()
                next[idx] = next[idx].copy(volume = volume.coerceIn(0f, 1f))
                state.copy(layers = next)
            }
        }
        Timber.tag("SOUNDSCAPES_METRICS").d("layer_volume layerKey=%s volume=%.2f", layerKey, volume)
    }

    override fun muteLayer(layerKey: String, muted: Boolean) {
        _playback.update { state ->
            val idx = state.layers.indexOfFirst { it.instanceKey == layerKey }
            if (idx < 0) state else {
                val next = state.layers.toMutableList()
                next[idx] = next[idx].copy(muted = muted)
                state.copy(layers = next)
            }
        }
    }

    override fun removeLayer(layerKey: String) {
        _playback.update { state ->
            val idx = state.layers.indexOfFirst { it.instanceKey == layerKey }
            if (idx < 0) state else {
                val next = state.layers.toMutableList()
                next.removeAt(idx)
                state.copy(layers = next)
            }
        }
    }

    override fun addLayer(layer: SoundscapeLayerState) {
        _playback.update { state ->
            if (state.layers.any { it.instanceKey == layer.instanceKey }) state
            else state.copy(layers = (state.layers + layer).take(MAX_SOUND_LAYERS))
        }
    }

    override fun setTimer(seconds: Int?) {
        _playback.update { it.copy(timerSeconds = seconds) }
    }
}

