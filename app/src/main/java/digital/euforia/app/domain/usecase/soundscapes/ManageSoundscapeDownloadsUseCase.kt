/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.db.entity.SavedSoundscape
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.db.entity.SoundscapePreset
import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.data.soundscapes.toLocalStateForEditor
import digital.euforia.app.domain.soundscapes.copySceneIdFromPresetId
import digital.euforia.app.domain.soundscapes.presetIdFromDownloadId
import digital.euforia.app.service.soundscapes.SoundscapeAssetType
import digital.euforia.app.service.soundscapes.SoundscapeAudioCacheManager
import digital.euforia.app.service.soundscapes.SoundscapeOfflineManifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SoundscapeDownloadCard(
    val download: SoundscapeDownloadItem,
    val scene: Scene?,
    val title: String,
    val imageUrl: String?,
    val openSceneId: Int,
    val presetId: Int?,
)

private data class DownloadMergeInputs(
    val downloads: List<SoundscapeDownloadItem>,
    val scenes: List<Scene>,
    val presets: List<SoundscapePreset>,
    val localBySceneId: Map<Int, SoundscapeSceneLocalState>,
)

class GetSoundscapeDownloadsFlowUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    operator fun invoke(): Flow<List<SoundscapeDownloadItem>> = repository.getDownloadsFlow()
}

class GetSoundscapeDownloadCardsFlowUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    operator fun invoke(): Flow<List<SoundscapeDownloadCard>> {
        return combine(
            combine(
                repository.getDownloadsFlow(),
                repository.getScenesFlow(),
                repository.getPresetsFlow(),
                repository.getLocalSceneStatesFlow(),
            ) { downloads, scenes, presets, localBySceneId ->
                DownloadMergeInputs(downloads, scenes, presets, localBySceneId)
            },
            repository.getSavedSoundscapesFlow(),
        ) { slice, savedRows ->
            val downloads = slice.downloads
            val scenes = slice.scenes
            val presets = slice.presets
            val localBySceneId = slice.localBySceneId
            val scenesById = scenes
                .map { it.mergeSoundscapeSceneLocalBackground(localBySceneId[it.id]) }
                .associateBy { it.id }
            val presetsById = presets.associateBy { it.id }
            val savedByPresetId = savedRows.associateBy { it.id }
            downloads.map { item ->
                val scene = scenesById[item.sceneId]
                val preset = presetIdFromDownloadId(item.id)?.let(presetsById::get)
                val saved = preset?.id?.let(savedByPresetId::get)
                val manifest = SoundscapeOfflineManifest.fromJsonOrNull(item.localPath)
                val imageUrl = resolveDownloadCardImageUrl(
                    item = item,
                    scene = scene,
                    preset = preset,
                    saved = saved,
                    manifest = manifest,
                    localBySceneId = localBySceneId,
                )
                SoundscapeDownloadCard(
                    download = item,
                    scene = scene,
                    title = saved?.name?.takeIf { it.isNotBlank() }
                        ?: preset?.name?.takeIf { it.isNotBlank() }
                        ?: scene?.name?.takeIf { it.isNotBlank() }
                        ?: item.title,
                    imageUrl = imageUrl,
                    openSceneId = preset?.let { copySceneIdFromPresetId(it.id) } ?: item.sceneId,
                    presetId = preset?.id,
                )
            }
        }
    }
}

private fun resolveDownloadCardImageUrl(
    item: SoundscapeDownloadItem,
    scene: Scene?,
    preset: SoundscapePreset?,
    saved: SavedSoundscape?,
    manifest: SoundscapeOfflineManifest?,
    localBySceneId: Map<Int, SoundscapeSceneLocalState>,
): String? {
    // Prefer saved payload (custom image or video first frame) so the row matches catalog / mini player
    // before falling back to manifest paths (which may still reflect stock previews in edge cases).
    if (preset != null && saved != null) {
        val copyId = copySceneIdFromPresetId(preset.id)
        val fromSaved = saved.toLocalStateForEditor(copyId)
        if (fromSaved != null && scene != null) {
            val merged = scene.mergeSoundscapeSceneLocalBackground(fromSaved)
            merged.imagePreviewUrl?.takeIf { it.isNotBlank() }?.let { return it }
            merged.imageUrl?.takeIf { it.isNotBlank() }?.let { return it }
        }
    }
    manifest?.firstLocalUrlByType(SoundscapeAssetType.IMAGE_PREVIEW)?.takeIf { it.isNotBlank() }?.let { return it }
    manifest?.firstLocalUrlByType(SoundscapeAssetType.IMAGE_BACKGROUND)?.takeIf { it.isNotBlank() }?.let { return it }
    val stock = scene?.mergeSoundscapeSceneLocalBackground(localBySceneId[item.sceneId])
    return stock?.imagePreviewUrl?.takeIf { it.isNotBlank() }
        ?: stock?.imageUrl?.takeIf { it.isNotBlank() }
}

class GetReadyDownloadedScenesFlowUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    operator fun invoke(): Flow<List<Scene>> {
        return combine(
            combine(
                repository.getDownloadsFlow(),
                repository.getScenesFlow(),
                repository.getPresetsFlow(),
                repository.getLocalSceneStatesFlow(),
            ) { downloads, scenes, presets, localBySceneId ->
                DownloadMergeInputs(downloads, scenes, presets, localBySceneId)
            },
            repository.getSavedSoundscapesFlow(),
        ) { slice, savedRows ->
            val downloads = slice.downloads
            val scenes = slice.scenes
            val presets = slice.presets
            val localBySceneId = slice.localBySceneId
            val scenesById = scenes
                .map { it.mergeSoundscapeSceneLocalBackground(localBySceneId[it.id]) }
                .associateBy { it.id }
            val presetsById = presets.associateBy { it.id }
            val savedByPresetId = savedRows.associateBy { it.id }
            downloads
                .asSequence()
                .filter { it.status == SoundscapeDownloadItem.STATUS_READY }
                .mapNotNull { item ->
                    val presetId = presetIdFromDownloadId(item.id) ?: return@mapNotNull null
                    val preset = presetsById[presetId] ?: return@mapNotNull null
                    val original = scenesById[preset.sceneId] ?: return@mapNotNull null
                    val saved = savedByPresetId[presetId]
                    val copyId = copySceneIdFromPresetId(preset.id)
                    val fromSaved = saved?.toLocalStateForEditor(copyId)
                    val card = original.copy(
                        id = copyId,
                        name = saved?.name?.ifBlank { preset.name } ?: preset.name.ifBlank { original.name },
                    ).mergeSoundscapeSceneLocalBackground(fromSaved)
                    card
                }
                .toList()
        }
    }
}

class QueueSoundscapeDownloadUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke(item: SoundscapeDownloadItem) = repository.queueDownload(item)
}

class RetrySoundscapeDownloadUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke(item: SoundscapeDownloadItem) = repository.updateDownload(
        item.copy(
            status = SoundscapeDownloadItem.STATUS_QUEUED,
            progress = 0,
            updatedAt = System.currentTimeMillis()
        )
    )
}

class DeleteSoundscapeDownloadUseCase @Inject constructor(
    private val repository: SoundscapesRepository,
    private val audioCacheManager: SoundscapeAudioCacheManager,
) {
    suspend operator fun invoke(id: String) {
        val item = repository.getDownload(id)
        val presetId = item?.id?.let(::presetIdFromDownloadId)
        val manifest = SoundscapeOfflineManifest.fromJsonOrNull(item?.localPath)
        if (manifest != null) {
            audioCacheManager.deleteByManifest(manifest)
        }
        repository.deleteDownload(id)
        if (presetId != null) {
            repository.deleteSavedSoundscape(presetId)
            repository.deletePreset(presetId)
            repository.deleteLocalSceneState(copySceneIdFromPresetId(presetId))
        }
    }
}

class ClearSoundscapeDownloadsUseCase @Inject constructor(
    private val repository: SoundscapesRepository,
    private val audioCacheManager: SoundscapeAudioCacheManager,
) {
    suspend operator fun invoke() {
        val snapshot = repository.getDownloadsSnapshot()
        snapshot.forEach { item ->
            val manifest = SoundscapeOfflineManifest.fromJsonOrNull(item.localPath)
            if (manifest != null) {
                audioCacheManager.deleteByManifest(manifest)
            }
            val presetId = presetIdFromDownloadId(item.id)
            if (presetId != null) {
                repository.deleteSavedSoundscape(presetId)
                repository.deletePreset(presetId)
                repository.deleteLocalSceneState(copySceneIdFromPresetId(presetId))
            }
        }
        repository.clearDownloads()
    }
}

