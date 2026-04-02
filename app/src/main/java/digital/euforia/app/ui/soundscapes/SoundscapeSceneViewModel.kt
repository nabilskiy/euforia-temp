/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapeDownloadsFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.QueueSoundscapeDownloadUseCase
import digital.euforia.app.domain.usecase.soundscapes.SaveSoundscapePresetUseCase
import digital.euforia.app.domain.usecase.soundscapes.SyncSoundscapesCatalogUseCase
import digital.euforia.app.service.soundscapes.SoundscapeLayerState
import digital.euforia.app.service.soundscapes.SoundscapePlaybackController
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SoundscapeSceneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val syncSoundscapesCatalogUseCase: SyncSoundscapesCatalogUseCase,
    private val savePresetUseCase: SaveSoundscapePresetUseCase,
    private val queueDownloadUseCase: QueueSoundscapeDownloadUseCase,
    private val getDownloadsFlowUseCase: GetSoundscapeDownloadsFlowUseCase,
    private val appPreferences: AppPreferences,
    private val playbackController: SoundscapePlaybackController,
    private val analyticSender: AnalyticSender,
) : ViewModel(), ContainerHost<SoundscapeSceneState, SoundscapeSceneSideEffect> {
    private val sceneId: Int = savedStateHandle["sceneId"] ?: 0

    override val container = container<SoundscapeSceneState, SoundscapeSceneSideEffect>(
        initialState = SoundscapeSceneState(sceneId = sceneId),
        onCreate = {
            ensureSceneLoaded()
            observePlayback()
            observeDownloads()
        }
    )

    private fun ensureSceneLoaded() {
        viewModelScope.launch {
            syncSoundscapesCatalogUseCase()
            if (playbackController.playback.value.sceneId != sceneId) {
                playbackController.start(
                    sceneId = sceneId,
                    sceneTitle = "Soundscape #$sceneId",
                    layers = listOf(
                        SoundscapeLayerState(id = sceneId * 100 + 1, title = "Rain", volume = 0.55f),
                        SoundscapeLayerState(id = sceneId * 100 + 2, title = "Forest", volume = 0.45f),
                    )
                )
            }
            val level = appPreferences.getSoundscapesLevel() / 100f
            playbackController.setMusicVolume(level)
            analyticSender.scenesItemClick()
        }
    }

    fun onPlayPause() = playbackController.playPause()
    fun onLayerVolume(layerId: Int, volume: Float) = playbackController.setLayerVolume(layerId, volume)
    fun onLayerMute(layerId: Int, muted: Boolean) = playbackController.muteLayer(layerId, muted)
    fun onLayerRemove(layerId: Int) = playbackController.removeLayer(layerId)
    fun onTimerChange(minutes: Int?) = playbackController.setTimer(minutes?.times(60))

    fun onMusicVolume(volume: Float) {
        playbackController.setMusicVolume(volume)
        viewModelScope.launch {
            appPreferences.setSoundscapesLevel((volume * 100).toInt())
        }
    }

    fun onSavePreset() {
        val playback = playbackController.playback.value
        val layersJson = playback.layers.joinToString(separator = "|") { "${it.id}:${it.volume}:${it.muted}" }
        viewModelScope.launch {
            savePresetUseCase(
                digital.euforia.app.data.db.entity.SoundscapePreset(
                    name = playback.sceneTitle.ifBlank { "Preset" },
                    sceneId = sceneId,
                    layersJson = layersJson,
                    musicVolume = playback.musicVolume
                )
            )
            reduceState { copy(isDirty = false) }
        }
    }

    fun onDownloadScene() {
        viewModelScope.launch {
            queueDownloadUseCase(
                SoundscapeDownloadItem(
                    id = "scene_$sceneId",
                    sceneId = sceneId,
                    title = "Scene $sceneId",
                    url = "scene://$sceneId",
                    status = SoundscapeDownloadItem.STATUS_QUEUED,
                    progress = 0
                )
            )
        }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            playbackController.playback.collectLatest { playback ->
                if (playback.sceneId != sceneId) return@collectLatest
                reduceState {
                    copy(
                        title = playback.sceneTitle,
                        isPlaying = playback.isPlaying,
                        layers = playback.layers.map { SoundLayerUi(it.id, it.title, it.volume, it.muted) },
                        musicVolume = playback.musicVolume,
                        timerSeconds = playback.timerSeconds
                    )
                }
            }
        }
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            getDownloadsFlowUseCase().collectLatest { items ->
                val item = items.firstOrNull { it.sceneId == sceneId }
                reduceState { copy(downloadState = item?.status ?: SoundscapeDownloadItem.STATUS_NOT_DOWNLOADED) }
            }
        }
    }
}

data class SoundscapeSceneState(
    val sceneId: Int,
    val title: String = "",
    val isPlaying: Boolean = false,
    val musicVolume: Float = 0.35f,
    val layers: List<SoundLayerUi> = emptyList(),
    val timerSeconds: Int? = null,
    val isDirty: Boolean = false,
    val isPremiumLocked: Boolean = false,
    val downloadState: String = SoundscapeDownloadItem.STATUS_NOT_DOWNLOADED,
    val engineHealth: String = "OK",
)

data class SoundLayerUi(
    val id: Int,
    val title: String,
    val volume: Float,
    val muted: Boolean,
)

sealed class SoundscapeSceneSideEffect

