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
import digital.euforia.app.data.repository.MusicRepository
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
import digital.euforia.app.service.soundscapes.SoundscapeSoundsManager
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SoundscapeSceneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val soundscapesRepository: SoundscapesRepository,
    private val musicRepository: MusicRepository,
    private val syncSoundscapesCatalogUseCase: SyncSoundscapesCatalogUseCase,
    private val savePresetUseCase: SaveSoundscapePresetUseCase,
    private val queueDownloadUseCase: QueueSoundscapeDownloadUseCase,
    private val getDownloadsFlowUseCase: GetSoundscapeDownloadsFlowUseCase,
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val playbackController: SoundscapePlaybackController,
    private val soundsManager: SoundscapeSoundsManager,
    private val analyticSender: AnalyticSender,
) : ViewModel(), ContainerHost<SoundscapeSceneState, SoundscapeSceneSideEffect> {
    private val sceneId: Int = savedStateHandle["sceneId"] ?: 0

    override val container = container<SoundscapeSceneState, SoundscapeSceneSideEffect>(
        initialState = SoundscapeSceneState(sceneId = sceneId),
        onCreate = {
            ensureSceneLoaded()
            syncSoundsCatalog()
            observePlayback()
            observeDownloads()
            observeAvailableSounds()
            observeAvailableMusic()
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
            val level = appPreferences.getSoundscapesLevel() / 100f
            val loaded = buildLoadedSceneData(
                repository = soundscapesRepository,
                sceneId = sceneId,
                remoteScene = remoteScene,
                fallbackMusicVolume = level,
                createFloatingButton = ::createFloatingButton
            )
            if (playbackController.playback.value.sceneId != sceneId) {
                playbackController.start(
                    sceneId = sceneId,
                    sceneTitle = scene.name,
                    layers = loaded.finalLayers.ifEmpty {
                        listOf(
                        SoundscapeLayerState(id = sceneId * 100 + 1, title = "Rain", audioUrl = null, volume = 0.55f),
                        SoundscapeLayerState(id = sceneId * 100 + 2, title = "Forest", audioUrl = null, volume = 0.45f),
                        )
                    }
                )
            }
            playbackController.setMusicVolume(loaded.resolvedMusicVolume)
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
                    selectedMusicId = loaded.selectedMusicId ?: remoteScene?.sceneMusics?.firstOrNull()?.music?.id,
                    sceneMusicUrl = loaded.selectedMusicUrl ?: remoteScene.resolveDefaultSceneMusicUrl(),
                    sceneMusicTitle = loaded.selectedMusicTitle
                        ?: remoteScene?.sceneMusics?.firstOrNull()?.music?.name?.takeIf { !it.isNullOrBlank() },
                    sceneMusicVolumeFactor = loaded.musicFactor,
                    isPro = scene.pro,
                    musicVolume = loaded.resolvedMusicVolume,
                    soundFloatingButtons = loaded.finalButtons,
                    defaultSceneSoundIds = loaded.defaultSceneSoundIds,
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

    private fun syncSoundsCatalog() {
        intent { soundscapesRepository.syncSoundsCatalog() }
    }

    private fun syncMusicCatalog() {
        intent { musicRepository.syncCatalog() }
    }

    private fun observeAvailableSounds() {
        intent {
            soundscapesRepository.getAllSoundsFlow().collectLatest { sounds ->
                val available = sounds.map { sound ->
                    AvailableSoundUi(
                        id = sound.id,
                        categoryId = sound.categoryId,
                        title = sound.name.ifBlank { "Sound ${sound.id}" },
                        imageUrl = sound.imageUrl.takeIf { it.isNotBlank() },
                                fileUrl = sound.fileUrl.takeIf { it.isNotBlank() },
                    )
                }
                val availableById = available.associateBy { it.id }
                reduce {
                    state.copy(
                        availableSounds = available,
                        soundFloatingButtons = state.soundFloatingButtons.map { button ->
                            val sound = availableById[button.id]
                            if (sound == null) button
                            else button.copy(
                                title = button.title.ifBlank { sound.title },
                                imageUrl = button.imageUrl ?: sound.imageUrl
                            )
                        }
                    )
                }
            }
        }
        intent {
            soundscapesRepository.getAllSoundCategoriesFlow().collectLatest { categories ->
                reduce {
                    state.copy(
                        soundCategories = categories.map {
                            SoundCategoryUi(
                                id = it.id,
                                title = it.name.ifBlank { "Category ${it.id}" },
                                alias = it.alias,
                                position = it.position,
                            )
                        }
                    )
                }
            }
        }
    }

    private fun observeAvailableMusic() {
        syncMusicCatalog()
        intent {
            musicRepository.getAllFlow().collectLatest { music ->
                reduce {
                    state.copy(
                        availableMusic = music.map {
                            SceneMusicUi(
                                id = it.id,
                                categoryId = it.categoryId,
                                title = it.name.ifBlank { "Music ${it.id}" },
                                imageUrl = it.imageUrl.takeIf { url -> url.isNotBlank() },
                                fileUrl = it.fileUrl.takeIf { url -> !url.isNullOrBlank() } ?: it.file?.url,
                            )
                        }
                    )
                }
            }
        }
        intent {
            musicRepository.getAllCategoriesFlow().collectLatest { categories ->
                reduce {
                    state.copy(
                        musicCategories = categories.map {
                            SceneMusicCategoryUi(
                                id = it.id,
                                title = it.name.ifBlank { "Category ${it.id}" },
                                alias = it.alias,
                                position = it.position,
                            )
                        }
                    )
                }
            }
        }
    }

    fun onPlayPause() = playbackController.playPause()
    fun onLayerVolume(layerId: Int, volume: Float) = playbackController.setLayerVolume(layerId, volume)
    fun onLayerMute(layerId: Int, muted: Boolean) = playbackController.muteLayer(layerId, muted)
    fun onLayerRemove(layerId: Int) = playbackController.removeLayer(layerId)
    fun onTimerChange(minutes: Int?) = playbackController.setTimer(minutes?.times(60))

    fun onApplySoundsSelection(selectedSoundIds: Set<Int>) {
        intent {
            val selected = selectedSoundIds.take(10).toSet()
            val playback = playbackController.playback.value
            val existingById = playback.layers.associateBy { it.id }
            val availableById = state.availableSounds.associateBy { it.id }

            // Remove deselected sounds.
            playback.layers
                .asSequence()
                .map { it.id }
                .filter { it !in selected }
                .forEach(playbackController::removeLayer)

            // Add newly selected sounds.
            selected.forEach { soundId ->
                if (soundId !in existingById) {
                    val sound = availableById[soundId]
                    playbackController.addLayer(
                        SoundscapeLayerState(
                            id = soundId,
                            title = sound?.title ?: "Sound $soundId",
                            audioUrl = sound?.fileUrl,
                            volume = 0.6f,
                            muted = false,
                        )
                    )
                }
            }

            val currentButtonsById = state.soundFloatingButtons.associateBy { it.id }
            val nextButtons = selected.mapIndexedNotNull { index, soundId ->
                currentButtonsById[soundId] ?: availableById[soundId]?.let { sound ->
                    createFloatingButton(sound, index, selected.size)
                }
            }
            reduce { state.copy(soundFloatingButtons = nextButtons, isDirty = true) }
            persistLocalSceneState()
        }
    }

    private fun createFloatingButton(
        sound: AvailableSoundUi,
        index: Int,
        total: Int,
    ): SoundFloatingButtonUi {
        val col = index % 4
        val row = index / 4
        val rows = ((total - 1) / 4 + 1).coerceAtLeast(1)
        val x = (0.15f + col * 0.22f).coerceIn(0.08f, 0.92f)
        val yBase = if (rows == 1) 0.42f else 0.24f + (row * (0.5f / (rows - 1).coerceAtLeast(1)))
        return SoundFloatingButtonUi(
            id = sound.id,
            title = sound.title,
            imageUrl = sound.imageUrl,
            posXFraction = x,
            posYFraction = yBase.coerceIn(0.18f, 0.82f),
        )
    }

    fun onMusicVolume(volume: Float) {
        playbackController.setMusicVolume(volume)
        intent {
            appPreferences.setSoundscapesLevel((volume * 100).toInt())
            persistLocalSceneState()
        }
    }

    fun onApplyMusicSelection(musicId: Int?) {
        intent {
            val selected = state.availableMusic.firstOrNull { it.id == musicId }
            reduce {
                state.copy(
                    selectedMusicId = selected?.id,
                    sceneMusicUrl = selected?.fileUrl,
                    sceneMusicTitle = selected?.title,
                    isDirty = true,
                )
            }
            persistLocalSceneState()
        }
    }

    fun onPreviewMusicSelection(musicId: Int?) {
        intent {
            val selected = state.availableMusic.firstOrNull { it.id == musicId }
            reduce {
                state.copy(
                    selectedMusicId = selected?.id,
                    sceneMusicUrl = selected?.fileUrl,
                    sceneMusicTitle = selected?.title,
                )
            }
        }
    }

    fun onSoundButtonPositionChanged(soundId: Int, posXFraction: Float, posYFraction: Float) {
        intent {
            val updated = state.soundFloatingButtons.map { button ->
                if (button.id == soundId) {
                    button.copy(
                        posXFraction = posXFraction.coerceIn(0f, 1f),
                        posYFraction = posYFraction.coerceIn(0f, 1f),
                    )
                } else {
                    button
                }
            }
            reduce { state.copy(soundFloatingButtons = updated, isDirty = true) }
            persistLocalSceneState()
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
                soundsManager.render(playback)
                reduce {
                    state.copy(
                        title = playback.sceneTitle,
                        isPlaying = playback.isPlaying,
                        layers = playback.layers.map { SoundLayerUi(it.id, it.title, it.volume, it.muted) },
                        musicVolume = playback.musicVolume,
                        timerSeconds = playback.timerSeconds
                    )
                }
                persistLocalSceneState()
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
    private suspend fun persistLocalSceneState() {
        persistLocalSceneState(
            repository = soundscapesRepository,
            sceneState = container.stateFlow.value,
            playbackState = playbackController.playback.value
        )
    }

    override fun onCleared() {
        soundsManager.releaseAll()
        super.onCleared()
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
    val selectedMusicId: Int? = null,
    /** Display name of the default scene music track. */
    val sceneMusicTitle: String? = null,
    val sceneMusicVolumeFactor: Float = 1f,
    val isPro: Boolean = false,
    val isPlaying: Boolean = false,
    val musicVolume: Float = 0.35f,
    /** Positions + icons from API `scene_sounds` for floating controls. */
    val soundFloatingButtons: List<SoundFloatingButtonUi> = emptyList(),
    /** Sound ids from API `scene_sounds` in response order (scene defaults for picker "Playing now"). */
    val defaultSceneSoundIds: List<Int> = emptyList(),
    val layers: List<SoundLayerUi> = emptyList(),
    val timerSeconds: Int? = null,
    val isDirty: Boolean = false,
    val isPremiumLocked: Boolean = false,
    val downloadState: String = SoundscapeDownloadItem.STATUS_NOT_DOWNLOADED,
    val engineHealth: String = "OK",
    val availableSounds: List<AvailableSoundUi> = emptyList(),
    val soundCategories: List<SoundCategoryUi> = emptyList(),
    val availableMusic: List<SceneMusicUi> = emptyList(),
    val musicCategories: List<SceneMusicCategoryUi> = emptyList(),
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

data class AvailableSoundUi(
    val id: Int,
    val categoryId: Int,
    val title: String,
    val imageUrl: String?,
    val fileUrl: String?,
)

data class SoundCategoryUi(
    val id: Int,
    val title: String,
    val alias: String = "",
    val position: Int = 0,
)

data class SceneMusicUi(
    val id: Int,
    val categoryId: Int,
    val title: String,
    val imageUrl: String?,
    val fileUrl: String?,
)

data class SceneMusicCategoryUi(
    val id: Int,
    val title: String,
    val alias: String = "",
    val position: Int = 0,
)

sealed class SoundscapeSceneSideEffect {
    data object NavigateToPaywall : SoundscapeSceneSideEffect()
}

