/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.usecase.soundscapes.GetDefaultSoundscapeScenesUseCase
import digital.euforia.app.domain.usecase.soundscapes.GetReadyDownloadedScenesFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapePresetsFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapesCatalogFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.SoundscapeCategorySection
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_DEFAULT_PLAYLIST
import digital.euforia.app.domain.usecase.soundscapes.SOUNDSCAPE_SECTION_MY
import digital.euforia.app.domain.usecase.soundscapes.SaveSoundscapePresetUseCase
import digital.euforia.app.domain.usecase.soundscapes.SyncSoundscapesCatalogUseCase
import digital.euforia.app.service.soundscapes.SoundscapeLayerState
import digital.euforia.app.service.soundscapes.SoundscapePlaybackController
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.soundscapes.scene.copySceneIdFromPresetId
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

enum class SoundscapeQuickFilter {
    SLEEP,
    MEDITATION,
    FOCUS,
    RELAX,
}

private fun Scene.matchesQuickFilter(filter: SoundscapeQuickFilter): Boolean {
    val hay = "$alias $name".lowercase()
    return when (filter) {
        SoundscapeQuickFilter.SLEEP ->
            listOf("sleep", "night", "dream", "moon").any { it in hay }
        SoundscapeQuickFilter.MEDITATION ->
            listOf("meditat", "zen", "lotus", "calm").any { it in hay }
        SoundscapeQuickFilter.FOCUS ->
            listOf("focus", "work", "study", "flow").any { it in hay }
        SoundscapeQuickFilter.RELAX ->
            listOf("relax", "nature", "forest", "rain", "beach").any { it in hay }
    }
}

private fun applySceneFilters(
    scenes: List<Scene>,
    query: String,
    quickFilter: SoundscapeQuickFilter?,
): List<Scene> {
    var list = scenes
    if (quickFilter != null) {
        list = list.filter { it.matchesQuickFilter(quickFilter) }
    }
    if (query.isNotBlank()) {
        list = list.filter { it.name.contains(query, ignoreCase = true) }
    }
    return list
}

private fun applyFiltersToSections(
    sections: List<SoundscapeCategorySection>,
    query: String,
    quickFilter: SoundscapeQuickFilter?,
): List<SoundscapeCategorySection> {
    return sections.mapNotNull { sec ->
        val filtered = applySceneFilters(sec.scenes, query, quickFilter)
        if (filtered.isEmpty()) null else sec.copy(scenes = filtered)
    }
}

private fun buildDisplaySections(
    sections: List<SoundscapeCategorySection>,
    defaultScenes: List<Scene>,
    myScenes: List<Scene>,
    query: String,
    quickFilter: SoundscapeQuickFilter?,
): List<SoundscapeCategorySection> {
    val filteredSections = applyFiltersToSections(sections, query, quickFilter)
    val filteredDefault = applySceneFilters(defaultScenes, query, quickFilter)
    val filteredMy = applySceneFilters(myScenes, query, quickFilter)
    val result = mutableListOf<SoundscapeCategorySection>()
    if (filteredDefault.isNotEmpty()) {
        result += SoundscapeCategorySection(
            categoryId = SOUNDSCAPE_SECTION_DEFAULT_PLAYLIST,
            title = "",
            scenes = filteredDefault
        )
    }
    if (filteredMy.isNotEmpty()) {
        result += SoundscapeCategorySection(
            categoryId = SOUNDSCAPE_SECTION_MY,
            title = "",
            scenes = filteredMy
        )
    }
    result += filteredSections
    return result
}

@HiltViewModel
class SoundscapesViewModel @Inject constructor(
    private val syncSoundscapesCatalogUseCase: SyncSoundscapesCatalogUseCase,
    private val getSoundscapesCatalogFlowUseCase: GetSoundscapesCatalogFlowUseCase,
    private val getDefaultSoundscapeScenesUseCase: GetDefaultSoundscapeScenesUseCase,
    private val getReadyDownloadedScenesFlowUseCase: GetReadyDownloadedScenesFlowUseCase,
    private val getSoundscapePresetsFlowUseCase: GetSoundscapePresetsFlowUseCase,
    private val saveSoundscapePresetUseCase: SaveSoundscapePresetUseCase,
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val playbackController: SoundscapePlaybackController,
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
    private val analyticSender: AnalyticSender,
) : ViewModel(), ContainerHost<SoundscapesState, SoundscapesSideEffect> {

    override val container = container<SoundscapesState, SoundscapesSideEffect>(
        initialState = SoundscapesState(),
        onCreate = {
            observePremium()
            observeCatalog()
            observePresets()
            observePlayback()
            observeReadyDownloads()
            refresh()
        }
    )

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState { copy(isPremium = isPremium) }
            }
        }
    }

    fun refresh() {
        intent {
            reduce { state.copy(isLoading = true) }
            syncSoundscapesCatalogUseCase.invoke()
                .onFailure { reduce { state.copy(error = it.message ?: "Sync failed") } }
                .onFinish { reduce { state.copy(isLoading = false) } }
        }
    }

    fun onSearchQueryChanged(query: String) {
        analyticSender.soundscapeSearchQueryChanged(query.length)
        reduceState {
            copy(
                query = query,
                displaySections = buildDisplaySections(
                    sections = categorySections,
                    defaultScenes = defaultScenes,
                    myScenes = myScenes,
                    query = query,
                    quickFilter = quickFilter
                )
            )
        }
    }

    fun onQuickFilterSelected(filter: SoundscapeQuickFilter?) {
        reduceState {
            copy(
                quickFilter = filter,
                displaySections = buildDisplaySections(
                    sections = categorySections,
                    defaultScenes = defaultScenes,
                    myScenes = myScenes,
                    query = query,
                    quickFilter = filter
                )
            )
        }
    }

    fun onSceneClick(scene: Scene) {
        intent {
            val isPremium = profilePreferences.getIsPremium()
            if (scene.pro && !isPremium) {
                postSideEffect(SoundscapesSideEffect.OpenPaywall)
            } else {
                if (playbackController.playback.value.sceneId != null &&
                    playbackController.playback.value.sceneId != scene.id
                ) {
                    playbackController.stop(fadeOut = true)
                }
                postSideEffect(SoundscapesSideEffect.OpenScene(scene.id))
            }
        }
    }

    fun onPlaylistClick(playlistId: Int) {
        analyticSender.soundscapePlaylistOpen(playlistId)
        intent { postSideEffect(SoundscapesSideEffect.OpenPlaylist(playlistId)) }
    }

    fun onSearchOpened() {
        analyticSender.soundscapeSearchOpen()
    }

    fun onSearchSuggestionClick() {
        analyticSender.soundscapeSearchSuggestionClick()
    }

    fun onPopularSceneClick() {
        analyticSender.soundscapePopularSceneClick()
    }

    fun onQuickPlay(scene: Scene) {
        playbackController.start(
            sceneId = scene.id,
            sceneTitle = scene.name,
            sceneImageUrl = scene.imagePreviewUrl ?: scene.imageUrl,
                    sceneMusicUrl = null,
                    sceneMusicVolumeFactor = 1f,
            ambientMode = false,
            layers = listOf(
                SoundscapeLayerState(id = scene.id * 100 + 1, title = "Rain", volume = 0.6f),
                SoundscapeLayerState(id = scene.id * 100 + 2, title = "Wind", volume = 0.45f),
            )
        )
        analyticSender.todayScenesClick()
    }

    fun onSavePreset(name: String, sceneId: Int, layersJson: String) {
        intent {
            val preset = SoundscapePreset(name = name, sceneId = sceneId, layersJson = layersJson)
            saveSoundscapePresetUseCase(preset)
            appPreferences.setSoundscapesLastPreset(preset.id)
        }
    }

    private fun observeCatalog() {
        intent {
            getSoundscapesCatalogFlowUseCase.invoke().collectLatest { catalog ->
                val defaultScenes = getDefaultSoundscapeScenesUseCase(
                    allScenes = catalog.scenes,
                    playlists = catalog.playlists
                )
                reduce {
                    val nextSections = catalog.categorySections
                    state.copy(
                        scenes = catalog.scenes,
                        categorySections = nextSections,
                        defaultScenes = defaultScenes,
                        playlists = catalog.playlists,
                        searchSuggestions = remoteConfigFetcher.getScenesSearchSuggestions(),
                        popularScenes = resolvePopularScenes(catalog.scenes),
                        myScenes = resolveMyScenes(
                            allScenes = catalog.scenes,
                            presets = state.presets,
                        ),
                        displaySections = buildDisplaySections(
                            sections = nextSections,
                            defaultScenes = defaultScenes,
                            myScenes = resolveMyScenes(
                                allScenes = catalog.scenes,
                                presets = state.presets,
                            ),
                            query = state.query,
                            quickFilter = state.quickFilter
                        )
                    )
                }
            }
        }
    }

    private fun resolvePopularScenes(allScenes: List<Scene>): List<Scene> {
        if (allScenes.isEmpty()) return emptyList()
        val byId = allScenes.associateBy { it.id }
        val fromRemote = remoteConfigFetcher.getScenesPopularIds()
            .mapNotNull(byId::get)
        return if (fromRemote.isNotEmpty()) {
            fromRemote
        } else {
            allScenes.shuffled().take(5)
        }
    }

    private fun observePresets() {
        intent {
            getSoundscapePresetsFlowUseCase().collectLatest { presets ->
                reduce {
                    val nextMy = resolveMyScenes(
                        allScenes = state.scenes,
                        presets = presets,
                    )
                    state.copy(
                        presets = presets,
                        myScenes = nextMy,
                        displaySections = buildDisplaySections(
                            sections = state.categorySections,
                            defaultScenes = state.defaultScenes,
                            myScenes = nextMy,
                            query = state.query,
                            quickFilter = state.quickFilter
                        )
                    )
                }
            }
        }
    }

    private fun observeReadyDownloads() {
        intent {
            getReadyDownloadedScenesFlowUseCase().collectLatest { downloaded ->
                reduce {
                    state.copy(
                        downloadedScenes = downloaded,
                        myScenes = state.myScenes,
                        displaySections = buildDisplaySections(
                            sections = state.categorySections,
                            defaultScenes = state.defaultScenes,
                            myScenes = state.myScenes,
                            query = state.query,
                            quickFilter = state.quickFilter
                        )
                    )
                }
            }
        }
    }

    private fun resolveMyScenes(
        allScenes: List<Scene>,
        presets: List<SoundscapePreset>,
    ): List<Scene> {
        if (allScenes.isEmpty()) return emptyList()
        val byId = allScenes.associateBy { it.id }
        return presets.mapNotNull { preset ->
            val original = byId[preset.sceneId] ?: return@mapNotNull null
            original.copy(
                id = copySceneIdFromPresetId(preset.id),
                name = preset.name.ifBlank { original.name },
            )
        }
    }

    private fun observePlayback() {
        intent {
            playbackController.playback.collectLatest { playback ->
                reduce {
                    state.copy(
                        miniPlayer = MiniPlayerUi(
                            isVisible = playback.sceneId != null,
                            title = playback.sceneTitle,
                            isPlaying = playback.isPlaying
                        ),
                        activeSceneId = playback.sceneId,
                        activeSceneImageUrl = playback.sceneImageUrl,
                        isPlaybackActive = playback.isPlaying
                    )
                }
            }
        }
    }

    fun onMiniPlayerClick() {
        val sceneId = playbackController.playback.value.sceneId ?: return
        intent {
            val scene = state.scenes.firstOrNull { it.id == sceneId } ?: state.myScenes.firstOrNull { it.id == sceneId }
            val isPro = scene?.pro == true
            val isPremium = profilePreferences.getIsPremium()
            if (isPro && !isPremium) {
                postSideEffect(SoundscapesSideEffect.OpenPaywall)
            } else {
                postSideEffect(SoundscapesSideEffect.OpenScene(sceneId))
            }
        }
    }
}

data class SoundscapesState(
    val isPremium: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val quickFilter: SoundscapeQuickFilter? = null,
    val scenes: List<Scene> = emptyList(),
    val categorySections: List<SoundscapeCategorySection> = emptyList(),
    val defaultScenes: List<Scene> = emptyList(),
    val displaySections: List<SoundscapeCategorySection> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    val popularScenes: List<Scene> = emptyList(),
    val playlists: List<SoundscapePlaylist> = emptyList(),
    val presets: List<SoundscapePreset> = emptyList(),
    val downloadedScenes: List<Scene> = emptyList(),
    val myScenes: List<Scene> = emptyList(),
    val miniPlayer: MiniPlayerUi = MiniPlayerUi(),
    val activeSceneId: Int? = null,
    val activeSceneImageUrl: String? = null,
    val isPlaybackActive: Boolean = false,
)

data class MiniPlayerUi(
    val isVisible: Boolean = false,
    val title: String = "",
    val isPlaying: Boolean = false,
)

sealed class SoundscapesSideEffect {
    data class OpenScene(val sceneId: Int) : SoundscapesSideEffect()
    data class OpenPlaylist(val playlistId: Int) : SoundscapesSideEffect()
    data object OpenPaywall : SoundscapesSideEffect()
}
