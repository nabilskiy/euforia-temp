/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.db.entity.SavedSoundscape
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SceneCategory
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState
import digital.euforia.app.data.repository.SoundscapesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

const val SOUNDSCAPE_SECTION_FALLBACK_ALL = -2
const val SOUNDSCAPE_SECTION_UNCATEGORIZED = -1
const val SOUNDSCAPE_SECTION_DEFAULT_PLAYLIST = -3
const val SOUNDSCAPE_SECTION_MY = -4

data class SoundscapeCategorySection(
    val categoryId: Int,
    val title: String,
    val scenes: List<Scene>,
)

data class SoundscapesCatalog(
    val scenes: List<Scene>,
    val playlists: List<SoundscapePlaylist>,
    val categorySections: List<SoundscapeCategorySection>,
    val savedSoundscapes: List<SavedSoundscape> = emptyList(),
)

internal fun buildSoundscapeCategorySections(
    categories: List<SceneCategory>,
    scenes: List<Scene>,
): List<SoundscapeCategorySection> {
    if (scenes.isEmpty()) return emptyList()
    if (categories.isEmpty()) {
        return listOf(SoundscapeCategorySection(categoryId = SOUNDSCAPE_SECTION_FALLBACK_ALL, title = "", scenes = scenes))
    }
    val byCategory = scenes.groupBy { it.categoryId }
    val ordered = categories.map { cat ->
        SoundscapeCategorySection(
            categoryId = cat.id,
            title = cat.name,
            scenes = byCategory[cat.id].orEmpty(),
        )
    }
    val uncategorized = scenes.filter { it.categoryId == null }
    val result = ordered.filter { it.scenes.isNotEmpty() }.toMutableList()
    if (uncategorized.isNotEmpty()) {
        result.add(SoundscapeCategorySection(categoryId = SOUNDSCAPE_SECTION_UNCATEGORIZED, title = "", scenes = uncategorized))
    }
    return result
}

private data class SoundscapeCatalogInputs(
    val categories: List<SceneCategory>,
    val scenes: List<Scene>,
    val playlists: List<SoundscapePlaylist>,
    val localBySceneId: Map<Int, SoundscapeSceneLocalState>,
    val presets: List<SoundscapePreset>,
)

class GetSoundscapesCatalogFlowUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    operator fun invoke(): Flow<SoundscapesCatalog> = flow {
        repository.ensureSavedSoundscapesSeeded()
        emit(Unit)
    }.flatMapLatest {
        combine(
            combine(
                repository.getSceneCategoriesFlow(),
                repository.getScenesFlow(),
                repository.getPlaylistsFlow(),
                repository.getLocalSceneStatesFlow(),
                repository.getPresetsFlow(),
            ) { c, s, p, l, pr ->
                SoundscapeCatalogInputs(c, s, p, l, pr)
            },
            repository.getSavedSoundscapesFlow(),
        ) { inputs, savedSoundscapes ->
            val presetBaseIds = inputs.presets.map { it.sceneId }.toSet()
            val mergedScenes = mergeSoundscapeScenesWithLocalBackgroundExcludingPresetBases(
                inputs.scenes,
                inputs.localBySceneId,
                presetBaseIds,
            )
            SoundscapesCatalog(
                scenes = mergedScenes,
                playlists = inputs.playlists,
                categorySections = buildSoundscapeCategorySections(inputs.categories, mergedScenes),
                savedSoundscapes = savedSoundscapes,
            )
        }
    }
}
