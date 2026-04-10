/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapePresetsFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.GetSoundscapesCatalogFlowUseCase
import digital.euforia.app.domain.usecase.soundscapes.SoundscapeCategorySection
import digital.euforia.app.domain.usecase.soundscapes.SaveSoundscapePresetUseCase
import digital.euforia.app.domain.usecase.soundscapes.SyncSoundscapesCatalogUseCase
import digital.euforia.app.service.soundscapes.SoundscapeLayerState
import digital.euforia.app.service.soundscapes.SoundscapePlaybackController
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.flow.collectLatest
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

@HiltViewModel
class SoundscapesViewModel @Inject constructor(
    private val syncSoundscapesCatalogUseCase: SyncSoundscapesCatalogUseCase,
    private val getSoundscapesCatalogFlowUseCase: GetSoundscapesCatalogFlowUseCase,
    private val getSoundscapePresetsFlowUseCase: GetSoundscapePresetsFlowUseCase,
    private val saveSoundscapePresetUseCase: SaveSoundscapePresetUseCase,
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val playbackController: SoundscapePlaybackController,
    private val analyticSender: AnalyticSender,
) : ViewModel(), ContainerHost<SoundscapesState, SoundscapesSideEffect> {

    override val container = container<SoundscapesState, SoundscapesSideEffect>(
        initialState = SoundscapesState(),
        onCreate = {
            observeCatalog()
            observePresets()
            observePlayback()
            refresh()
        }
    )

    fun refresh() {
        intent {
            reduce { state.copy(isLoading = true) }
            syncSoundscapesCatalogUseCase.invoke()
                .onFailure { reduce { state.copy(error = it.message ?: "Sync failed") } }
                .onFinish { reduce { state.copy(isLoading = false) } }
        }
    }

    fun onSearchQueryChanged(query: String) {
        reduceState {
            copy(
                query = query,
                displaySections = applyFiltersToSections(categorySections, query, quickFilter)
            )
        }
    }

    fun onQuickFilterSelected(filter: SoundscapeQuickFilter?) {
        reduceState {
            copy(
                quickFilter = filter,
                displaySections = applyFiltersToSections(categorySections, query, filter)
            )
        }
    }

    fun onSceneClick(scene: Scene) {
        intent {
            val isPremium = profilePreferences.getIsPremium()
            if (scene.pro && !isPremium) {
                postSideEffect(SoundscapesSideEffect.OpenPaywall)
            } else {
                postSideEffect(SoundscapesSideEffect.OpenScene(scene.id))
            }
        }
    }

    fun onPlaylistClick(playlistId: Int) {
        intent { postSideEffect(SoundscapesSideEffect.OpenPlaylist(playlistId)) }
    }

    fun onQuickPlay(scene: Scene) {
        playbackController.start(
            sceneId = scene.id,
            sceneTitle = scene.name,
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
                reduce {
                    val nextSections = catalog.categorySections
                    state.copy(
                        scenes = catalog.scenes,
                        categorySections = nextSections,
                        playlists = catalog.playlists,
                        displaySections = applyFiltersToSections(
                            nextSections,
                            state.query,
                            state.quickFilter
                        )
                    )
                }
            }
        }
    }

    private fun observePresets() {
        intent {
            getSoundscapePresetsFlowUseCase().collectLatest { presets ->
                reduce { state.copy(presets = presets) }
            }
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
                        )
                    )
                }
            }
        }
    }

    fun onMiniPlayerClick() {
        val sceneId = playbackController.playback.value.sceneId ?: return
        intent {
            val scene = state.scenes.firstOrNull { it.id == sceneId }
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val quickFilter: SoundscapeQuickFilter? = null,
    val scenes: List<Scene> = emptyList(),
    val categorySections: List<SoundscapeCategorySection> = emptyList(),
    val displaySections: List<SoundscapeCategorySection> = emptyList(),
    val playlists: List<digital.euforia.app.data.db.entity.SoundscapePlaylist> = emptyList(),
    val presets: List<SoundscapePreset> = emptyList(),
    val miniPlayer: MiniPlayerUi = MiniPlayerUi(),
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
