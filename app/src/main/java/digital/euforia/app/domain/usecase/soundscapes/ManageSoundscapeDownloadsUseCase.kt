/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.repository.SoundscapesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SoundscapeDownloadCard(
    val download: SoundscapeDownloadItem,
    val scene: Scene?,
    val title: String,
    val imageUrl: String?,
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
            repository.getScenesFlow()
        ) { downloads, scenes ->
            val scenesById = scenes.associateBy { it.id }
            downloads.map { item ->
                val scene = scenesById[item.sceneId]
                val imageUrl = scene?.imagePreviewUrl?.takeIf { it.isNotBlank() }
                    ?: scene?.imageUrl?.takeIf { it.isNotBlank() }
                SoundscapeDownloadCard(
                    download = item,
                    scene = scene,
                    title = scene?.name?.takeIf { it.isNotBlank() } ?: item.title,
                    imageUrl = imageUrl
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
            repository.getScenesFlow()
        ) { downloads, scenes ->
            val readyIds = downloads
                .asSequence()
                .filter { it.status == SoundscapeDownloadItem.STATUS_READY }
                .map { it.sceneId }
                .toSet()
            scenes.filter { it.id in readyIds }
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
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteDownload(id)
}

class ClearSoundscapeDownloadsUseCase @Inject constructor(
    private val repository: SoundscapesRepository
) {
    suspend operator fun invoke() = repository.clearDownloads()
}

