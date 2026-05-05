/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.service.soundscapes.SoundscapeAssetType
import digital.euforia.app.service.soundscapes.SoundscapeOfflineManifest
import digital.euforia.app.service.soundscapes.SoundscapeAudioCacheManager
import digital.euforia.app.ui.soundscapes.scene.copySceneIdFromPresetId
import digital.euforia.app.ui.soundscapes.scene.presetIdFromDownloadId
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
            repository.getDownloadsFlow(),
            repository.getScenesFlow(),
            repository.getPresetsFlow(),
        ) { downloads, scenes, presets ->
            val scenesById = scenes.associateBy { it.id }
            val presetsById = presets.associateBy { it.id }
            downloads.map { item ->
                val scene = scenesById[item.sceneId]
                val preset = presetIdFromDownloadId(item.id)?.let(presetsById::get)
                val manifest = SoundscapeOfflineManifest.fromJsonOrNull(item.localPath)
                val imageUrl = manifest?.firstLocalUrlByType(SoundscapeAssetType.IMAGE_PREVIEW)
                    ?: manifest?.firstLocalUrlByType(SoundscapeAssetType.IMAGE_BACKGROUND)
                    ?: scene?.imagePreviewUrl?.takeIf { it.isNotBlank() }
                    ?: scene?.imageUrl?.takeIf { it.isNotBlank() }
                SoundscapeDownloadCard(
                    download = item,
                    scene = scene,
                    title = preset?.name?.takeIf { it.isNotBlank() }
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

class GetReadyDownloadedScenesFlowUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    operator fun invoke(): Flow<List<Scene>> {
        return combine(
            repository.getDownloadsFlow(),
            repository.getScenesFlow(),
            repository.getPresetsFlow(),
        ) { downloads, scenes, presets ->
            val scenesById = scenes.associateBy { it.id }
            val presetsById = presets.associateBy { it.id }
            downloads
                .asSequence()
                .filter { it.status == SoundscapeDownloadItem.STATUS_READY }
                .mapNotNull { item ->
                    val presetId = presetIdFromDownloadId(item.id) ?: return@mapNotNull null
                    val preset = presetsById[presetId] ?: return@mapNotNull null
                    val original = scenesById[preset.sceneId] ?: return@mapNotNull null
                    original.copy(
                        id = copySceneIdFromPresetId(preset.id),
                        name = preset.name.ifBlank { original.name },
                    )
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
                repository.deletePreset(presetId)
                repository.deleteLocalSceneState(copySceneIdFromPresetId(presetId))
            }
        }
        repository.clearDownloads()
    }
}

