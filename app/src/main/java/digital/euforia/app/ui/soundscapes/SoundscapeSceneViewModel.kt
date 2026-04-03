/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapeDownloadsFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.QueueSoundscapeDownloadUseCase
import digital.euforia.app.domain.usecase.soundscapes.SaveSoundscapePresetUseCase
import digital.euforia.app.domain.usecase.soundscapes.SyncSoundscapesCatalogUseCase
import digital.euforia.app.service.soundscapes.SoundscapeLayerState
import digital.euforia.app.service.soundscapes.SoundscapePlaybackController
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SoundscapeSceneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val soundscapesRepository: SoundscapesRepository,
    private val syncSoundscapesCatalogUseCase: SyncSoundscapesCatalogUseCase,
    private val savePresetUseCase: SaveSoundscapePresetUseCase,
    private val queueDownloadUseCase: QueueSoundscapeDownloadUseCase,
    private val getDownloadsFlowUseCase: GetSoundscapeDownloadsFlowUseCase,
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
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
        intent {
            syncSoundscapesCatalogUseCase()
            val remoteScene = soundscapesRepository.getSceneDetails(sceneId).dataOrNull
            val scene = remoteScene?.toEntity() ?: soundscapesRepository.getSceneById(sceneId)
            if (scene == null) {
                return@intent
            }
            val isPremium = profilePreferences.getIsPremium()
            if (scene.pro && !isPremium) {
                postSideEffect(SoundscapeSceneSideEffect.NavigateToPaywall)
                return@intent
            }
            val sceneLayers = remoteScene
                ?.sceneSounds
                ?.mapIndexedNotNull { index, soundItem ->
                    val soundId = soundItem.sound?.id ?: return@mapIndexedNotNull null
                    SoundscapeLayerState(
                        id = soundId,
                        title = soundItem.sound.name.orEmpty().ifBlank { "Layer ${index + 1}" },
                        volume = ((soundItem.volume ?: 100).coerceIn(0, 100) / 100f),
                        muted = false
                    )
                }
                .orEmpty()
            val floatingButtons = remoteScene
                ?.sceneSounds
                ?.mapIndexedNotNull { index, soundItem ->
                    val s = soundItem.sound ?: return@mapIndexedNotNull null
                    val soundId = s.id ?: return@mapIndexedNotNull null
                    val posXf = soundItem.posX?.let { it.coerceIn(0, 100) / 100f }
                        ?: (0.12f + (index % 4) * 0.22f).coerceIn(0.08f, 0.92f)
                    val posYf = soundItem.posY?.let { it.coerceIn(0, 100) / 100f }
                        ?: (0.22f + index * 0.14f).coerceIn(0.15f, 0.85f)
                    SoundFloatingButtonUi(
                        id = soundId,
                        title = s.name.orEmpty().ifBlank { "Sound ${index + 1}" },
                        imageUrl = s.imageUrl?.takeIf { it.isNotBlank() },
                        posXFraction = posXf,
                        posYFraction = posYf,
                    )
                }
                .orEmpty()
            if (playbackController.playback.value.sceneId != sceneId) {
                playbackController.start(
                    sceneId = sceneId,
                    sceneTitle = scene.name,
                    layers = sceneLayers.ifEmpty {
                        listOf(
                        SoundscapeLayerState(id = sceneId * 100 + 1, title = "Rain", volume = 0.55f),
                        SoundscapeLayerState(id = sceneId * 100 + 2, title = "Forest", volume = 0.45f),
                        )
                    }
                )
            }
            val level = appPreferences.getSoundscapesLevel() / 100f
            playbackController.setMusicVolume(level)
            val musicFactor = remoteScene?.sceneMusics?.firstOrNull()?.volume
                ?.coerceIn(0, 100)?.div(100f) ?: 1f
            reduce {
                state.copy(
                    title = scene.name,
                    subtitle = remoteScene?.subtitle.orEmpty(),
                    videoUrl = scene.videoUrl ?: remoteScene?.video?.url,
                    imageUrl = listOf(
                        scene.imageUrl,
                        scene.imagePreviewUrl,
                        remoteScene?.imageCoverUrl,
                        remoteScene?.imageUrl,
                        remoteScene?.imagePreviewUrl
                    ).firstOrNull { !it.isNullOrBlank() },
                    sceneMusicUrl = remoteScene.resolveDefaultSceneMusicUrl(),
                    sceneMusicTitle = remoteScene?.sceneMusics?.firstOrNull()?.music?.name?.takeIf { !it.isNullOrBlank() },
                    sceneMusicVolumeFactor = musicFactor,
                    isPro = scene.pro,
                    musicVolume = level,
                    soundFloatingButtons = floatingButtons,
                )
            }
            analyticSender.scenesItemClick()
        }
    }

    private fun NetworkScene?.resolveDefaultSceneMusicUrl(): String? {
        val sm = this?.sceneMusics?.firstOrNull() ?: return null
        return sm.musicFileUrl?.takeIf { it.isNotBlank() }
            ?: sm.music?.fileUrl?.takeIf { it.isNotBlank() }
            ?: sm.music?.file?.url?.takeIf { it.isNotBlank() }
    }

    fun onPlayPause() = playbackController.playPause()
    fun onLayerVolume(layerId: Int, volume: Float) = playbackController.setLayerVolume(layerId, volume)
    fun onLayerMute(layerId: Int, muted: Boolean) = playbackController.muteLayer(layerId, muted)
    fun onLayerRemove(layerId: Int) = playbackController.removeLayer(layerId)
    fun onTimerChange(minutes: Int?) = playbackController.setTimer(minutes?.times(60))

    fun onMusicVolume(volume: Float) {
        playbackController.setMusicVolume(volume)
        intent {
            appPreferences.setSoundscapesLevel((volume * 100).toInt())
        }
    }

    fun onSavePreset() {
        val playback = playbackController.playback.value
        val layersJson = playback.layers.joinToString(separator = "|") { "${it.id}:${it.volume}:${it.muted}" }
        intent {
            savePresetUseCase(
                digital.euforia.app.data.db.entity.SoundscapePreset(
                    name = playback.sceneTitle.ifBlank { "Preset" },
                    sceneId = sceneId,
                    layersJson = layersJson,
                    musicVolume = playback.musicVolume
                )
            )
            reduce { state.copy(isDirty = false) }
        }
    }

    fun onDownloadScene() {
        intent {
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
        intent {
            playbackController.playback.collectLatest { playback ->
                if (playback.sceneId != sceneId) return@collectLatest
                reduce {
                    state.copy(
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
        intent {
            getDownloadsFlowUseCase().collectLatest { items ->
                val item = items.firstOrNull { it.sceneId == sceneId }
                reduce {
                    state.copy(
                        downloadState = item?.status ?: SoundscapeDownloadItem.STATUS_NOT_DOWNLOADED
                    )
                }
            }
        }
    }
}

data class SoundscapeSceneState(
    val sceneId: Int,
    val title: String = "",
    val subtitle: String = "",
    val videoUrl: String? = null,
    val imageUrl: String? = null,
    /** First entry from `scene_musics`: `music_file_url` or nested music file URL. */
    val sceneMusicUrl: String? = null,
    /** Display name of the default scene music track. */
    val sceneMusicTitle: String? = null,
    val sceneMusicVolumeFactor: Float = 1f,
    val isPro: Boolean = false,
    val isPlaying: Boolean = false,
    val musicVolume: Float = 0.35f,
    /** Positions + icons from API `scene_sounds` for floating controls. */
    val soundFloatingButtons: List<SoundFloatingButtonUi> = emptyList(),
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

data class SoundFloatingButtonUi(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    /** 0f..1f, maps from API `pos_x` 0–100 or fallback layout. */
    val posXFraction: Float,
    /** 0f..1f, maps from API `pos_y` 0–100 or fallback layout. */
    val posYFraction: Float,
)

sealed class SoundscapeSceneSideEffect {
    data object NavigateToPaywall : SoundscapeSceneSideEffect()
}

