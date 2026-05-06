/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.analytics.soundscapeLayerSettingsOpenedCompat
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.model.BackgroundMediaItem
import digital.euforia.app.data.repository.BackgroundImportRepository
import digital.euforia.app.data.repository.MusicRepository
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.model.config.ScenePlayerConfig
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapeDownloadsFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.DeleteSoundscapeDownloadUseCase
import digital.euforia.app.domain.usecase.soundscapes.QueueSoundscapeDownloadUseCase
import digital.euforia.app.domain.usecase.soundscapes.SaveSoundscapePresetUseCase
import digital.euforia.app.domain.usecase.soundscapes.SyncSoundscapesCatalogUseCase
import digital.euforia.app.service.soundscapes.SoundscapeLayerState
import digital.euforia.app.service.soundscapes.SoundscapePlaybackController
import digital.euforia.app.service.soundscapes.SoundscapeAudioCacheManager
import digital.euforia.app.service.soundscapes.SoundscapeAssetType
import digital.euforia.app.service.soundscapes.SoundscapeOfflineManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SoundscapeSceneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val applicationContext: Context,
    private val soundscapesRepository: SoundscapesRepository,
    private val backgroundImportRepository: BackgroundImportRepository,
    private val musicRepository: MusicRepository,
    private val syncSoundscapesCatalogUseCase: SyncSoundscapesCatalogUseCase,
    private val savePresetUseCase: SaveSoundscapePresetUseCase,
    private val queueDownloadUseCase: QueueSoundscapeDownloadUseCase,
    private val deleteDownloadUseCase: DeleteSoundscapeDownloadUseCase,
    private val getDownloadsFlowUseCase: GetSoundscapeDownloadsFlowUseCase,
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val playbackController: SoundscapePlaybackController,
    private val audioCacheManager: SoundscapeAudioCacheManager,
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
    private val analyticSender: AnalyticSender,
) : ViewModel(), ContainerHost<SoundscapeSceneState, SoundscapeSceneSideEffect> {
    private val sceneId: Int = savedStateHandle["sceneId"] ?: 0
    private val playlistId: Int? = savedStateHandle["playlistId"]
    private val openedPresetId: Int? = presetIdFromCopySceneId(sceneId)
    private var sceneCategoryAliasesById: Map<Int, String> = emptyMap()

    override val container = container<SoundscapeSceneState, SoundscapeSceneSideEffect>(
        initialState = SoundscapeSceneState(sceneId = sceneId),
        onCreate = {
            ensureSceneLoaded()
            syncSoundsCatalog()
            observePlayback()
            observeDownloads()
            observeAvailableSounds()
            observeSceneCategories()
            observeAvailableMusic()
            observeFavoriteMusic()
            observeSoundAnimationsSetting()
        }
    )

    private var preparationJob: Job? = null

    private fun ensureSceneLoaded() {
        intent {
            val openedPreset = openedPresetId?.let { soundscapesRepository.getPresetById(it) }
            val originalSceneId = openedPreset?.sceneId ?: sceneId
            val localStateSceneId = if (openedPreset != null) sceneId else null

            // Load local scene first to avoid black screen while network sync/details are pending.
            val localScene = soundscapesRepository.getSceneById(originalSceneId)
            if (localScene != null) {
                val localAlias = localScene.categoryId?.let(sceneCategoryAliasesById::get)
                reduce {
                    state.copy(
                        title = openedPreset?.name?.ifBlank { localScene.name } ?: localScene.name,
                        videoUrl = localScene.videoUrl,
                        imageUrl = localScene.imagePreviewUrl ?: localScene.imageUrl,
                        isPro = localScene.pro,
                        sceneCategoryId = localScene.categoryId,
                        sceneCategoryAlias = localAlias,
                        originalSceneId = originalSceneId,
                        presetId = openedPreset?.id,
                    )
                }
            }

            syncSoundscapesCatalogUseCase()
            val remoteScene = soundscapesRepository.getSceneDetails(originalSceneId).dataOrNull
            val scene = remoteScene?.toEntity() ?: soundscapesRepository.getSceneById(originalSceneId) ?: localScene
            if (scene == null) {
                reduce {
                    state.copy(
                        error = SoundscapeError(
                            type = SoundscapeErrorType.NETWORK,
                            message = "Failed to load scene"
                        )
                    )
                }
                return@intent
            }
            val isPremium = profilePreferences.getIsPremium()
            if (scene.pro && !isPremium) {
                postSideEffect(SoundscapeSceneSideEffect.NavigateToPaywall)
                return@intent
            }
            val level = appPreferences.getSoundscapesLevel() / 100f
            val scenePlayerConfig = remoteConfigFetcher.getScenePlayerConfig()
            playbackController.setAmbientMode(scenePlayerConfig.ambientMode)
            val playlistSceneIds = playlistId
                ?.let { id -> soundscapesRepository.getPlaylistById(id)?.sceneIds }
                .orEmpty()
            playbackController.setPlaylist(playlistSceneIds)
            val loaded = buildLoadedSceneData(
                repository = soundscapesRepository,
                sceneId = originalSceneId,
                remoteScene = remoteScene,
                localStateSceneId = localStateSceneId,
                preset = openedPreset,
                fallbackMusicVolume = level,
                createFloatingButton = { sound, index, total, instanceKey ->
                    createFloatingButton(sound, index, total, instanceKey)
                }
            )
            val readyDownload = soundscapesRepository.getDownloadsSnapshot()
                .firstOrNull {
                    it.sceneId == originalSceneId &&
                        if (openedPreset != null) {
                            presetIdFromDownloadId(it.id) == openedPreset.id
                        } else {
                            it.id == "scene_$originalSceneId"
                        } &&
                        it.status == SoundscapeDownloadItem.STATUS_READY
                }
            val manifest = SoundscapeOfflineManifest.fromJsonOrNull(readyDownload?.localPath)
            val resolvedVideoUrl = resolveVideoPlaybackUrl(scene, remoteScene, manifest)
            val resolvedImageUrl = resolveImagePlaybackUrl(scene, remoteScene, manifest)
            val resolvedSceneMusicUrl = resolveMusicPlaybackUrl(
                preferredUrl = loaded.selectedMusicUrl ?: remoteScene.resolveDefaultSceneMusicUrl(),
                manifest = manifest
            )
            playbackController.setMusicVolume(loaded.resolvedMusicVolume)
            playbackController.setSceneMusic(
                musicUrl = resolvedSceneMusicUrl,
                musicVolumeFactor = loaded.musicFactor
            )
            val finalButtonsWithResolvedImages = loaded.finalButtons.map { button ->
                button.copy(
                    imageUrl = manifest?.findLocalUrl(button.imageUrl, SoundscapeAssetType.SOUND_ICON)
                        ?: button.imageUrl
                )
            }
            reduce {
                val sceneCategoryAlias = scene.categoryId?.let(sceneCategoryAliasesById::get)
                state.copy(
                    title = openedPreset?.name?.ifBlank { scene.name } ?: scene.name,
                    subtitle = remoteScene?.subtitle.orEmpty(),
                    videoUrl = loaded.backgroundVideoUrl ?: resolvedVideoUrl,
                    imageUrl = listOf(
                        loaded.backgroundImageUrl,
                        resolvedImageUrl,
                        scene.imageUrl,
                        scene.imagePreviewUrl,
                        remoteScene?.imageCoverUrl,
                        remoteScene?.imageUrl,
                        remoteScene?.imagePreviewUrl
                    ).firstOrNull { !it.isNullOrBlank() },
                    selectedMusicId = loaded.selectedMusicId ?: remoteScene?.sceneMusics?.firstOrNull()?.music?.id,
                    sceneMusicUrl = resolvedSceneMusicUrl,
                    sceneMusicTitle = loaded.selectedMusicTitle
                        ?: remoteScene?.sceneMusics?.firstOrNull()?.music?.name?.takeIf { !it.isNullOrBlank() },
                    sceneMusicVolumeFactor = loaded.musicFactor,
                    isPro = scene.pro,
                    sceneCategoryId = scene.categoryId,
                    sceneCategoryAlias = sceneCategoryAlias,
                    musicVolume = loaded.resolvedMusicVolume,
                    scenePlayerConfig = scenePlayerConfig,
                    playlistId = playlistId,
                    originalSceneId = originalSceneId,
                    presetId = openedPreset?.id,
                    isPreparing = true,
                    preparingCompleted = 0,
                    preparingTotal = loaded.finalLayers.mapNotNull { it.audioUrl?.takeIf(String::isNotBlank) }.distinct().size,
                    soundFloatingButtons = finalButtonsWithResolvedImages,
                    defaultSceneSoundIds = loaded.defaultSceneSoundIds,
                    backgroundSource = loaded.backgroundSource,
                    suggestedSoundIds = resolveSuggestedSoundIds(
                        available = state.availableSounds,
                        sceneCategoryAlias = sceneCategoryAlias,
                        sceneCategoryId = scene.categoryId
                    ),
                )
            }
            val sceneTitleForPlayback = container.stateFlow.value.title.ifBlank { scene.name }
            startScenePreparation(sceneTitleForPlayback, loaded.finalLayers)
            analyticSender.scenesItemClick()
        }
    }

    private fun startScenePreparation(sceneTitle: String, layers: List<SoundscapeLayerState>) {
        preparationJob?.cancel()
        preparationJob = viewModelScope.launch {
            val resolvedLayers = layers.map { layer ->
                val resolvedAudioUrl = audioCacheManager.resolvePlaybackUrl(
                    url = layer.audioUrl,
                    type = SoundscapeAssetType.SOUND
                )
                if (resolvedAudioUrl != null) layer.copy(audioUrl = resolvedAudioUrl) else layer
            }
            val preparedLayers = audioCacheManager.prepareSceneLayers(resolvedLayers) { completed, total ->
                intent {
                    reduce {
                        state.copy(
                            isPreparing = total > 0,
                            preparingCompleted = completed,
                            preparingTotal = total,
                        )
                    }
                }
            }
            intent {
                reduce { state.copy(isPreparing = false, preparingCompleted = 0, preparingTotal = 0) }
            }
            val layersForPlayback = preparedLayers.ifEmpty {
                listOf(
                    SoundscapeLayerState(
                        id = sceneId * 100 + 1,
                        instanceKey = "${sceneId}:fallback:1",
                        title = "Rain",
                        audioUrl = null,
                        volume = 0.55f
                    ),
                    SoundscapeLayerState(
                        id = sceneId * 100 + 2,
                        instanceKey = "${sceneId}:fallback:2",
                        title = "Forest",
                        audioUrl = null,
                        volume = 0.45f
                    ),
                )
            }
            Timber.tag("SOUNDSCAPES_AUDIO").d(
                "scene_prepared sceneId=%s layers=%s withAudio=%s",
                sceneId,
                layersForPlayback.size,
                layersForPlayback.count { !it.audioUrl.isNullOrBlank() }
            )
            if (playbackController.playback.value.sceneId != sceneId) {
                val ambientMode = container.stateFlow.value.scenePlayerConfig.ambientMode
                playbackController.start(
                    sceneId = sceneId,
                    sceneTitle = sceneTitle,
                    sceneImageUrl = container.stateFlow.value.imageUrl,
                    sceneMusicUrl = container.stateFlow.value.sceneMusicUrl,
                    sceneMusicVolumeFactor = container.stateFlow.value.sceneMusicVolumeFactor,
                    ambientMode = ambientMode,
                    layers = layersForPlayback
                )
            } else {
                layersForPlayback.forEach { playbackController.addLayer(it) }
                playbackController.setPlaying(true)
            }
        }
    }

    private fun NetworkScene?.resolveDefaultSceneMusicUrl(): String? {
        val sm = this?.sceneMusics?.firstOrNull() ?: return null
        return sm.musicFileUrl?.takeIf { it.isNotBlank() }
            ?: sm.music?.fileUrl?.takeIf { it.isNotBlank() }
            ?: sm.music?.file?.url?.takeIf { it.isNotBlank() }
    }

    private suspend fun resolveVideoPlaybackUrl(
        scene: Scene,
        remoteScene: NetworkScene?,
        manifest: SoundscapeOfflineManifest?,
    ): String? {
        val remoteVideo = scene.videoUrl ?: remoteScene?.video?.url ?: remoteScene?.videoUrl
        val manifestVideo = manifest?.findLocalUrl(remoteVideo, SoundscapeAssetType.VIDEO)
            ?: manifest?.firstLocalUrlByType(SoundscapeAssetType.VIDEO)
        return manifestVideo ?: audioCacheManager.resolvePlaybackUrl(remoteVideo, SoundscapeAssetType.VIDEO)
    }

    private suspend fun resolveImagePlaybackUrl(
        scene: Scene,
        remoteScene: NetworkScene?,
        manifest: SoundscapeOfflineManifest?,
    ): String? {
        val remotePreview = scene.imagePreviewUrl ?: remoteScene?.imagePreviewUrl ?: remoteScene?.imageCoverUrl
        val remoteBackground = scene.imageUrl ?: remoteScene?.imageUrl
        return manifest?.findLocalUrl(remotePreview, SoundscapeAssetType.IMAGE_PREVIEW)
            ?: manifest?.firstLocalUrlByType(SoundscapeAssetType.IMAGE_PREVIEW)
            ?: manifest?.findLocalUrl(remoteBackground, SoundscapeAssetType.IMAGE_BACKGROUND)
            ?: manifest?.firstLocalUrlByType(SoundscapeAssetType.IMAGE_BACKGROUND)
            ?: audioCacheManager.resolvePlaybackUrl(remotePreview, SoundscapeAssetType.IMAGE_PREVIEW)
            ?: audioCacheManager.resolvePlaybackUrl(remoteBackground, SoundscapeAssetType.IMAGE_BACKGROUND)
    }

    private suspend fun resolveMusicPlaybackUrl(
        preferredUrl: String?,
        manifest: SoundscapeOfflineManifest?,
    ): String? {
        return manifest?.findLocalUrl(preferredUrl, SoundscapeAssetType.MUSIC)
            ?: manifest?.firstLocalUrlByType(SoundscapeAssetType.MUSIC)
            ?: audioCacheManager.resolvePlaybackUrl(preferredUrl, SoundscapeAssetType.MUSIC)
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
                        isContinuous = sound.continuous,
                        minRepeatDelaySec = sound.minRepeatDelay ?: 0,
                        maxRepeatDelaySec = sound.maxRepeatDelay ?: 300,
                    )
                }
                val availableById = available.associateBy { it.id }
                reduce {
                    state.copy(
                        availableSounds = available,
                        suggestedSoundIds = resolveSuggestedSoundIds(
                            available = available,
                            sceneCategoryAlias = state.sceneCategoryAlias,
                            sceneCategoryId = state.sceneCategoryId
                        ),
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

    private fun observeSceneCategories() {
        intent {
            soundscapesRepository.getSceneCategoriesFlow().collectLatest { categories ->
                sceneCategoryAliasesById = categories.associate { it.id to it.alias }
                val alias = categories.firstOrNull { it.id == state.sceneCategoryId }?.alias
                reduce {
                    state.copy(
                        sceneCategoryAlias = alias,
                        suggestedSoundIds = resolveSuggestedSoundIds(
                            available = state.availableSounds,
                            sceneCategoryAlias = alias,
                            sceneCategoryId = state.sceneCategoryId
                        )
                    )
                }
            }
        }
    }

    private fun resolveSuggestedSoundIds(
        available: List<AvailableSoundUi>,
        sceneCategoryAlias: String?,
        sceneCategoryId: Int?,
    ): List<Int> {
        if (available.isEmpty()) return emptyList()
        val suggestions = remoteConfigFetcher.getSoundsSuggestionsMap()
        if (suggestions.isEmpty()) return emptyList()
        val keys = listOfNotNull(
            sceneCategoryAlias?.takeIf { it.isNotBlank() },
            sceneCategoryId?.toString(),
            "*"
        )
        val candidateIds = keys.firstNotNullOfOrNull { key ->
            suggestions[key]?.takeIf { it.isNotEmpty() }
        }.orEmpty()
        if (candidateIds.isEmpty()) return emptyList()
        val availableById = available.associateBy { it.id }
        return candidateIds.distinct().mapNotNull { availableById[it]?.id }
    }

    private fun resolveSuggestedMusicIds(
        available: List<SceneMusicUi>,
    ): List<Int> {
        if (available.isEmpty()) return emptyList()
        val candidateIds = remoteConfigFetcher.getMusicSuggestionsIds()
        if (candidateIds.isEmpty()) return emptyList()
        val availableById = available.associateBy { it.id }
        return candidateIds.distinct().mapNotNull { availableById[it]?.id }
    }

    private fun observeAvailableMusic() {
        syncMusicCatalog()
        intent {
            musicRepository.getAllFlow().collectLatest { music ->
                val available = music.map {
                    SceneMusicUi(
                        id = it.id,
                        categoryId = it.categoryId,
                        title = it.name.ifBlank { "Music ${it.id}" },
                        imageUrl = it.imageUrl.takeIf { url -> url.isNotBlank() },
                        fileUrl = it.fileUrl.takeIf { url -> !url.isNullOrBlank() } ?: it.file?.url,
                    )
                }
                reduce {
                    state.copy(
                        availableMusic = available,
                        suggestedMusicIds = resolveSuggestedMusicIds(available)
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

    private fun observeFavoriteMusic() {
        intent {
            appPreferences.getFavoriteMusicIdsFlow().collectLatest { ids ->
                reduce { state.copy(favoriteMusicIds = ids) }
            }
        }
    }

    private fun observeSoundAnimationsSetting() {
        intent {
            appPreferences.isSoundAnimationsEnabledFlow().collectLatest { enabled ->
                reduce { state.copy(soundAnimationsEnabled = enabled) }
            }
        }
    }

    fun onSoundAnimationsEnabledChanged(enabled: Boolean) {
        intent {
            appPreferences.setSoundAnimationsEnabled(enabled)
            reduce { state.copy(soundAnimationsEnabled = enabled) }
        }
    }

    fun onToggleMusicFavorite(musicId: Int) {
        intent {
            val current = state.favoriteMusicIds
            val next = if (musicId in current) current - musicId else current + musicId
            appPreferences.setFavoriteMusicIds(next)
            reduce { state.copy(favoriteMusicIds = next) }
        }
    }

    fun onPlayPause() = playbackController.playPause()
    fun onLayerVolume(layerKey: String, volume: Float) {
        analyticSender.soundscapeLayerVolumeChanged(layerKey = layerKey, volume = volume)
        playbackController.setLayerVolume(layerKey, volume)
        intent {
            reduce { state.copy(isDirty = true) }
            persistLocalSceneState()
        }
    }

    fun onLayerRepeatInterval(layerKey: String, seconds: Int) {
        playbackController.setLayerRepeatInterval(layerKey, seconds)
        intent {
            reduce { state.copy(isDirty = true) }
        }
    }
    fun onLayerRepeatIntervalChangeFinished() {
        intent { persistLocalSceneState() }
    }
    fun onLayerMute(layerKey: String, muted: Boolean) {
        playbackController.muteLayer(layerKey, muted)
        intent {
            reduce { state.copy(isDirty = true) }
            persistLocalSceneState()
        }
    }
    fun onLayerSettingsOpened(layerKey: String, soundId: Int, title: String) {
        analyticSender.soundscapeLayerSettingsOpenedCompat(
            layerKey = layerKey,
            soundId = soundId,
            title = title
        )
    }
    fun onLayerRemove(layerKey: String) {
        analyticSender.soundscapeLayerSettingsDeleted(layerKey = layerKey)
        playbackController.removeLayer(layerKey)
        intent {
            analyticSender.soundscapeLayerRemoved()
            reduce { state.copy(isDirty = true) }
            persistLocalSceneState()
        }
    }
    fun onTimerChange(minutes: Int?) {
        playbackController.setTimer(minutes?.times(60))
        intent {
            reduce { state.copy(isDirty = true) }
            persistLocalSceneState()
        }
    }

    fun onSetTimerSeconds(seconds: Int) {
        val normalized = seconds.coerceAtLeast(0)
        playbackController.setTimer(normalized.takeIf { it > 0 })
        intent {
            reduce { state.copy(isDirty = true) }
            persistLocalSceneState()
        }
    }

    fun onDisableTimer() = onSetTimerSeconds(0)

    fun onUnsplashBackgroundPickerOpened() {
        intent {
            reduce {
                state.copy(
                    backgroundSearchQuery = "",
                    backgroundSearchItems = emptyList(),
                    backgroundSearchError = null,
                    backgroundSearchLoading = true,
                    backgroundSearchRequestNonce = state.backgroundSearchRequestNonce + 1,
                )
            }
        }
    }

    fun onPexelsBackgroundPickerOpened(initialVideoTab: Boolean = true) {
        intent {
            reduce {
                state.copy(
                    pexelsVideoTabSelected = initialVideoTab,
                    backgroundSearchQuery = "",
                    backgroundSearchItems = emptyList(),
                    backgroundSearchError = null,
                    backgroundSearchLoading = true,
                    backgroundSearchRequestNonce = state.backgroundSearchRequestNonce + 1,
                )
            }
        }
    }

    fun onBackgroundImportQueryChange(query: String) {
        intent { reduce { state.copy(backgroundSearchQuery = query) } }
    }

    fun onPexelsImportVideoTabChange(selectedVideo: Boolean) {
        intent {
            reduce {
                state.copy(
                    pexelsVideoTabSelected = selectedVideo,
                    backgroundSearchRequestNonce = state.backgroundSearchRequestNonce + 1,
                )
            }
        }
    }

    fun refreshUnsplashBackgroundSearch() {
        viewModelScope.launch {
            val query = container.stateFlow.value.backgroundSearchQuery
            intent {
                reduce { state.copy(backgroundSearchLoading = true, backgroundSearchError = null) }
            }
            val result = withContext(Dispatchers.IO) {
                backgroundImportRepository.searchUnsplashPhotos(query)
            }
            intent {
                reduce {
                    when (result) {
                        is ResultWrapper.Success -> state.copy(
                            backgroundSearchLoading = false,
                            backgroundSearchItems = result.data,
                            backgroundSearchError = null,
                        )
                        is ResultWrapper.Failure -> state.copy(
                            backgroundSearchLoading = false,
                            backgroundSearchItems = emptyList(),
                            backgroundSearchError = result.throwable.message
                                ?: result.throwable::class.java.simpleName,
                        )
                    }
                }
            }
        }
    }

    fun refreshPexelsBackgroundSearch() {
        viewModelScope.launch {
            val snapshot = container.stateFlow.value
            val query = snapshot.backgroundSearchQuery
            val videos = snapshot.pexelsVideoTabSelected
            intent {
                reduce { state.copy(backgroundSearchLoading = true, backgroundSearchError = null) }
            }
            val result = withContext(Dispatchers.IO) {
                backgroundImportRepository.searchPexels(query, videos)
            }
            intent {
                reduce {
                    when (result) {
                        is ResultWrapper.Success -> state.copy(
                            backgroundSearchLoading = false,
                            backgroundSearchItems = result.data,
                            backgroundSearchError = null,
                        )
                        is ResultWrapper.Failure -> state.copy(
                            backgroundSearchLoading = false,
                            backgroundSearchItems = emptyList(),
                            backgroundSearchError = result.throwable.message
                                ?: result.throwable::class.java.simpleName,
                        )
                    }
                }
            }
        }
    }

    fun onBackgroundImageSelected(imageUrl: String) {
        val normalized = imageUrl.trim()
        if (normalized.isBlank()) return
        intent {
            reduce {
                state.copy(
                    imageUrl = normalized,
                    videoUrl = null,
                    backgroundSource = "image",
                    isDirty = true,
                )
            }
            persistLocalSceneState()
        }
    }

    fun onBackgroundVideoSelected(videoUrl: String, previewImageUrl: String? = null) {
        val normalized = videoUrl.trim()
        if (normalized.isBlank()) return
        intent {
            reduce {
                state.copy(
                    videoUrl = normalized,
                    imageUrl = previewImageUrl?.takeIf { it.isNotBlank() } ?: state.imageUrl,
                    backgroundSource = "video",
                    isDirty = true,
                )
            }
            persistLocalSceneState()
        }
    }

    fun onBackgroundLocalFileSelected(uri: Uri, mimeType: String?) {
        viewModelScope.launch {
            val lowerMime = mimeType?.lowercase().orEmpty()
            if (lowerMime.startsWith("video/")) {
                val imported = importLocalVideo(applicationContext, uri)
                if (imported != null) {
                    intent {
                        reduce {
                            state.copy(
                                videoUrl = imported.videoUrl,
                                imageUrl = imported.previewImageUrl ?: state.imageUrl,
                                backgroundSource = "local_video",
                                isDirty = true,
                            )
                        }
                        persistLocalSceneState()
                    }
                }
            }
        }
    }

    fun onBackgroundCroppedLocalImageSelected(imageUrl: String) {
        onBackgroundImageSelected(imageUrl)
        intent {
            reduce { state.copy(backgroundSource = "local_image") }
            persistLocalSceneState()
        }
    }

    fun onSaveAndDownload(rawName: String) {
        val playback = playbackController.playback.value
        val layersJson = playback.layers.joinToString(separator = "|") { "${it.id}:${it.volume}:${it.muted}" }
        val requestedName = rawName.trim()
        intent {
            val sourceState = state
            val finalName = requestedName.ifBlank {
                playback.sceneTitle.ifBlank { sourceState.title.ifBlank { "Preset" } }
            }
            val savedPreset = savePresetUseCase(
                SoundscapePreset(
                    id = sourceState.presetId ?: 0,
                    name = finalName,
                    sceneId = sourceState.originalSceneId,
                    layersJson = layersJson,
                    musicVolume = playback.musicVolume
                )
            )
            val copySceneId = copySceneIdFromPresetId(savedPreset.id)
            val nextState = sourceState.copy(
                sceneId = copySceneId,
                presetId = savedPreset.id,
                title = savedPreset.name,
                isDirty = false
            )
            persistLocalSceneState(
                repository = soundscapesRepository,
                sceneState = nextState,
                playbackState = playback
            )
            val sceneTitle = nextState.title.ifBlank { "Scene ${nextState.originalSceneId}" }
            analyticSender.soundscapeDownloadQueued(nextState.originalSceneId)
            queueDownloadUseCase(
                SoundscapeDownloadItem(
                    id = copyDownloadIdFromPresetId(savedPreset.id),
                    sceneId = nextState.originalSceneId,
                    title = sceneTitle,
                    url = "scene://${nextState.originalSceneId}",
                    status = SoundscapeDownloadItem.STATUS_QUEUED,
                    progress = 0
                )
            )
            reduce { nextState }
        }
    }

    fun onDeleteDownloadedScene() {
        val state = container.stateFlow.value
        val downloadItemId = state.downloadItemId
            ?: state.presetId?.let(::copyDownloadIdFromPresetId)
            ?: return
        intent {
            deleteDownloadUseCase(downloadItemId)
            playbackController.stop(fadeOut = false)
            reduce {
                state.copy(
                    downloadState = SoundscapeDownloadItem.STATUS_NOT_DOWNLOADED,
                    downloadItemId = null
                )
            }
        }
    }

    fun onApplySoundsSelection(selectedSoundIds: Set<Int>) {
        intent {
            val selected = selectedSoundIds.take(12).toSet()
            val playback = playbackController.playback.value
            val existingById = playback.layers.associateBy { it.id }
            val availableById = state.availableSounds.associateBy { it.id }

            // Remove deselected sounds.
            playback.layers
                .asSequence()
                .map { it.instanceKey to it.id }
                .filter { (_, soundId) -> soundId !in selected }
                .forEach {
                    analyticSender.soundscapeLayerRemoved()
                    playbackController.removeLayer(it.first)
                }

            // Add newly selected sounds.
            selected.forEach { soundId ->
                if (soundId !in existingById) {
                    analyticSender.soundscapeLayerAdded()
                    val sound = availableById[soundId]
                    val resolvedUrl = runCatching {
                        audioCacheManager.ensureCached(sound?.fileUrl.orEmpty())
                    }.getOrDefault(sound?.fileUrl.orEmpty())
                    sound?.imageUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let { imageUrl ->
                            runCatching {
                                audioCacheManager.ensureCached(
                                    url = imageUrl,
                                    type = SoundscapeAssetType.SOUND_ICON
                                )
                            }
                        }
                    playbackController.addLayer(
                        SoundscapeLayerState(
                            id = soundId,
                            instanceKey = "$soundId:manual",
                            title = sound?.title ?: "Sound $soundId",
                            audioUrl = resolvedUrl.ifBlank { sound?.fileUrl },
                            volume = 0.6f,
                            muted = false,
                            isContinuous = sound?.isContinuous == true,
                            minRepeatDelaySec = sound?.minRepeatDelaySec ?: 0,
                            maxRepeatDelaySec = sound?.maxRepeatDelaySec ?: 300,
                            repeatIntervalSec = if (sound?.isContinuous == true) 0 else (sound?.minRepeatDelaySec ?: 30),
                        )
                    )
                }
            }

            val currentButtonsByKey = state.soundFloatingButtons.associateBy { it.instanceKey }
            val nextButtons = selected.mapIndexedNotNull { index, soundId ->
                currentButtonsByKey.values.firstOrNull { it.id == soundId } ?: availableById[soundId]?.let { sound ->
                    createFloatingButton(sound, index, selected.size, "$soundId:manual")
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
        instanceKey: String = sound.id.toString(),
    ): SoundFloatingButtonUi {
        val col = index % 4
        val row = index / 4
        val rows = ((total - 1) / 4 + 1).coerceAtLeast(1)
        val x = (0.15f + col * 0.22f).coerceIn(0.08f, 0.92f)
        val yBase = if (rows == 1) 0.42f else 0.24f + (row * (0.5f / (rows - 1).coerceAtLeast(1)))
        return SoundFloatingButtonUi(
            id = sound.id,
            instanceKey = instanceKey,
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
            reduce { state.copy(isDirty = true) }
            persistLocalSceneState()
        }
    }

    fun onApplyMusicSelection(musicId: Int?) {
        intent {
            analyticSender.soundscapeMusicChanged()
            val selected = state.availableMusic.firstOrNull { it.id == musicId }
            playbackController.setSceneMusic(
                musicUrl = selected?.fileUrl,
                musicVolumeFactor = state.sceneMusicVolumeFactor
            )
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
            playbackController.setSceneMusic(
                musicUrl = selected?.fileUrl,
                musicVolumeFactor = state.sceneMusicVolumeFactor
            )
            reduce {
                state.copy(
                    selectedMusicId = selected?.id,
                    sceneMusicUrl = selected?.fileUrl,
                    sceneMusicTitle = selected?.title,
                )
            }
        }
    }

    fun onSoundButtonPositionChanged(instanceKey: String, posXFraction: Float, posYFraction: Float) {
        intent {
            val updated = state.soundFloatingButtons.map { button ->
                if (button.instanceKey == instanceKey) {
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
            val sourceState = state
            val savedPreset = savePresetUseCase(
                SoundscapePreset(
                    id = sourceState.presetId ?: 0,
                    name = playback.sceneTitle.ifBlank { sourceState.title.ifBlank { "Preset" } },
                    sceneId = sourceState.originalSceneId,
                    layersJson = layersJson,
                    musicVolume = playback.musicVolume
                )
            )
            val copySceneId = copySceneIdFromPresetId(savedPreset.id)
            val nextState = sourceState.copy(
                sceneId = copySceneId,
                presetId = savedPreset.id,
                title = savedPreset.name,
                isDirty = false
            )
            persistLocalSceneState(
                repository = soundscapesRepository,
                sceneState = nextState,
                playbackState = playback
            )
            reduce { nextState }
        }
    }

    fun onRenameScene(newTitle: String) {
        val title = newTitle.trim()
        if (title.isBlank()) return
        playbackController.renameCurrentScene(title)
        intent {
            reduce { state.copy(title = title, isDirty = true) }
            persistLocalSceneState()
        }
    }

    fun onDiscardChangesAndExit() {
        intent {
            reduce { state.copy(isDirty = false) }
        }
    }

    fun onDownloadScene() {
        intent {
            val sceneTitle = state.title.ifBlank { "Scene ${state.originalSceneId}" }
            val presetId = state.presetId
            if (presetId == null) return@intent
            analyticSender.soundscapeDownloadQueued(state.originalSceneId)
            queueDownloadUseCase(
                SoundscapeDownloadItem(
                    id = copyDownloadIdFromPresetId(presetId),
                    sceneId = state.originalSceneId,
                    title = sceneTitle,
                    url = "scene://${state.originalSceneId}",
                    status = SoundscapeDownloadItem.STATUS_QUEUED,
                    progress = 0
                )
            )
        }
    }

    fun onCancelPreparation() {
        preparationJob?.cancel()
        analyticSender.soundscapePreparationCancelled()
        intent {
            reduce { state.copy(isPreparing = false, preparingCompleted = 0, preparingTotal = 0) }
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
                        layers = playback.layers.map {
                            SoundLayerUi(
                                id = it.id,
                                instanceKey = it.instanceKey,
                                title = it.title,
                                volume = it.volume,
                                muted = it.muted,
                                isContinuous = it.isContinuous,
                                minRepeatDelaySec = it.minRepeatDelaySec,
                                maxRepeatDelaySec = it.maxRepeatDelaySec,
                                repeatIntervalSec = it.repeatIntervalSec,
                                repeatRemainingMs = it.repeatRemainingMs,
                            )
                        },
                        musicVolume = playback.musicVolume,
                        sceneMusicUrl = playback.sceneMusicUrl,
                        sceneMusicVolumeFactor = playback.sceneMusicVolumeFactor,
                        timerSeconds = playback.timerSeconds,
                        timerRemainingSeconds = playback.timerRemainingSeconds
                    )
                }
            }
        }
    }

    private fun observeDownloads() {
        intent {
            getDownloadsFlowUseCase().collectLatest { items ->
                val current = container.stateFlow.value
                val item = items.firstOrNull { download ->
                    if (current.presetId != null) {
                        presetIdFromDownloadId(download.id) == current.presetId
                    } else {
                        download.id == "scene_${current.originalSceneId}"
                    }
                }
                reduce {
                    state.copy(
                        downloadState = item?.status ?: SoundscapeDownloadItem.STATUS_NOT_DOWNLOADED,
                        downloadItemId = item?.id,
                        downloadProgress = item?.progress ?: 0,
                        isSceneDownloadInProgress = item?.status == SoundscapeDownloadItem.STATUS_QUEUED ||
                            item?.status == SoundscapeDownloadItem.STATUS_DOWNLOADING
                    )
                }
            }
        }
    }
    private suspend fun persistLocalSceneState() {
        val sceneState = container.stateFlow.value
        if (sceneState.presetId == null) return
        persistLocalSceneState(
            repository = soundscapesRepository,
            sceneState = sceneState,
            playbackState = playbackController.playback.value
        )
    }

    override fun onCleared() {
        preparationJob?.cancel()
        super.onCleared()
    }
}

data class SoundscapeSceneState(
    val sceneId: Int,
    val originalSceneId: Int = sceneId,
    val presetId: Int? = null,
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
    val sceneCategoryId: Int? = null,
    val sceneCategoryAlias: String? = null,
    val scenePlayerConfig: ScenePlayerConfig = ScenePlayerConfig(),
    val playlistId: Int? = null,
    val isPlaying: Boolean = false,
    val musicVolume: Float = 0.35f,
    /** Positions + icons from API `scene_sounds` for floating controls. */
    val soundFloatingButtons: List<SoundFloatingButtonUi> = emptyList(),
    /** Sound ids from API `scene_sounds` in response order (scene defaults for picker "Playing now"). */
    val defaultSceneSoundIds: List<Int> = emptyList(),
    val layers: List<SoundLayerUi> = emptyList(),
    val timerSeconds: Int? = null,
    val timerRemainingSeconds: Int? = null,
    val isDirty: Boolean = false,
    val isPremiumLocked: Boolean = false,
    val downloadState: String = SoundscapeDownloadItem.STATUS_NOT_DOWNLOADED,
    val downloadItemId: String? = null,
    val downloadProgress: Int = 0,
    val isSceneDownloadInProgress: Boolean = false,
    val engineHealth: String = "OK",
    val availableSounds: List<AvailableSoundUi> = emptyList(),
    val suggestedSoundIds: List<Int> = emptyList(),
    val soundCategories: List<SoundCategoryUi> = emptyList(),
    val availableMusic: List<SceneMusicUi> = emptyList(),
    val suggestedMusicIds: List<Int> = emptyList(),
    val favoriteMusicIds: Set<Int> = emptySet(),
    val soundAnimationsEnabled: Boolean = true,
    val musicCategories: List<SceneMusicCategoryUi> = emptyList(),
    val isPreparing: Boolean = false,
    val preparingCompleted: Int = 0,
    val preparingTotal: Int = 0,
    val error: SoundscapeError? = null,
    /** Stock background import (Unsplash / Pexels) */
    val backgroundSearchQuery: String = "",
    val backgroundSearchItems: List<BackgroundMediaItem> = emptyList(),
    val backgroundSearchLoading: Boolean = false,
    val backgroundSearchError: String? = null,
    val backgroundSearchRequestNonce: Int = 0,
    val pexelsVideoTabSelected: Boolean = false,
    val backgroundSource: String? = null,
)

data class SoundLayerUi(
    val id: Int,
    val instanceKey: String,
    val title: String,
    val volume: Float,
    val muted: Boolean,
    val isContinuous: Boolean,
    val minRepeatDelaySec: Int,
    val maxRepeatDelaySec: Int,
    val repeatIntervalSec: Int,
    val repeatRemainingMs: Long?,
)

data class SoundFloatingButtonUi(
    val id: Int,
    val instanceKey: String,
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
    val isContinuous: Boolean,
    val minRepeatDelaySec: Int,
    val maxRepeatDelaySec: Int,
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

