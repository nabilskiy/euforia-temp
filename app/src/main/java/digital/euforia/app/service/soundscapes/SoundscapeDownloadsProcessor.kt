/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.di.ApplicationCoroutineScopeIO
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.domain.util.retry
import digital.euforia.app.ui.soundscapes.scene.copySceneIdFromPresetId
import digital.euforia.app.ui.soundscapes.scene.presetIdFromDownloadId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundscapeDownloadsProcessor @Inject constructor(
    @ApplicationCoroutineScopeIO private val scope: CoroutineScope,
    private val repository: SoundscapesRepository,
    private val audioCacheManager: SoundscapeAudioCacheManager,
    private val analyticSender: AnalyticSender,
) {
    @Volatile
    private var started = false
    private var queueObserverJob: Job? = null
    private var drainJob: Job? = null

    fun start() {
        if (started) return
        started = true
        queueObserverJob = scope.launch {
            repository.getDownloadsFlow().collect { items ->
                val hasQueued = items.any { it.status == SoundscapeDownloadItem.STATUS_QUEUED }
                if (hasQueued && (drainJob?.isActive != true)) {
                    drainJob = scope.launch {
                        drainQueue()
                    }
                }
            }
        }
    }

    private suspend fun drainQueue() {
        while (true) {
            val current = repository.getDownloadsFlow().firstOrNull().orEmpty()
            val next = current.firstOrNull { it.status == SoundscapeDownloadItem.STATUS_QUEUED } ?: break
            processItem(next)
        }
    }

    private suspend fun processItem(item: SoundscapeDownloadItem) {
        val startedItem = item.copy(
            status = SoundscapeDownloadItem.STATUS_DOWNLOADING,
            progress = 0,
            updatedAt = System.currentTimeMillis()
        )
        repository.updateDownload(startedItem)

        try {
            val scene = repository.getSceneDetails(item.sceneId).dataOrNull
            val localStateSceneId = presetIdFromDownloadId(item.id)?.let(::copySceneIdFromPresetId) ?: item.sceneId
            val localState = repository.getLocalSceneState(localStateSceneId)
            val assetsToDownload = buildOfflineAssetRefs(
                scene = scene,
                localLayerSoundIds = parseLocalLayerSoundIds(localState?.layersJson),
                localSelectedMusicUrl = localState?.selectedMusicUrl,
                localSoundFileUrlsById = repository.getAllSounds()
                    .associate { sound -> sound.id to sound.fileUrl.takeIf { it.isNotBlank() } }
            )

            if (assetsToDownload.isEmpty()) {
                repository.updateDownload(
                    startedItem.copy(
                        status = SoundscapeDownloadItem.STATUS_READY,
                        progress = 100,
                        localPath = SoundscapeOfflineManifest(sceneId = item.sceneId, assets = emptyList()).toJson(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                analyticSender.soundscapeDownloadReady(item.sceneId)
                return
            }

            val downloadedAssets = mutableListOf<SoundscapeOfflineAsset>()
            assetsToDownload.forEachIndexed { index, asset ->
                val result = retry(retries = 2, delayMillis = 500L) {
                    runCatching {
                        audioCacheManager.ensureCached(asset.url, asset.type)
                    }.fold(
                        onSuccess = { ResultWrapper.success(it) },
                        onFailure = { ResultWrapper.failure(it) }
                    )
                }
                if (!result.isSuccess) {
                    throw result.throwableOrNull ?: IllegalStateException("Download failed")
                }
                downloadedAssets += SoundscapeOfflineAsset(
                    type = asset.type,
                    remoteUrl = asset.url,
                    localUrl = result.dataOrNull.orEmpty()
                )
                val progress = (((index + 1).toFloat() / assetsToDownload.size.toFloat()) * 100f).toInt().coerceIn(0, 100)
                repository.updateDownload(
                    startedItem.copy(
                        status = SoundscapeDownloadItem.STATUS_DOWNLOADING,
                        progress = progress,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            repository.updateDownload(
                startedItem.copy(
                    status = SoundscapeDownloadItem.STATUS_READY,
                    progress = 100,
                    localPath = SoundscapeOfflineManifest(
                        sceneId = item.sceneId,
                        assets = downloadedAssets
                    ).toJson(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            analyticSender.soundscapeDownloadReady(item.sceneId)
        } catch (t: Throwable) {
            Timber.tag("SOUNDSCAPES_DOWNLOADS").e(t, "Failed to download scene %s", item.sceneId)
            analyticSender.soundscapeDownloadFailed(item.sceneId, t.javaClass.simpleName)
            repository.updateDownload(
                startedItem.copy(
                    status = SoundscapeDownloadItem.STATUS_FAILED,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}

internal data class OfflineAssetRef(
    val type: SoundscapeAssetType,
    val url: String,
)

internal fun buildOfflineAssetRefs(
    scene: NetworkScene?,
    localLayerSoundIds: List<Int> = emptyList(),
    localSelectedMusicUrl: String? = null,
    localSoundFileUrlsById: Map<Int, String?> = emptyMap(),
): List<OfflineAssetRef> {
    val soundAssets = scene?.sceneSounds
        ?.mapNotNull { soundItem ->
            soundItem.soundFileUrl?.takeIf { it.isNotBlank() }
                ?: soundItem.sound?.fileUrl?.takeIf { it.isNotBlank() }
                ?: soundItem.sound?.file?.url?.takeIf { it.isNotBlank() }
        }
        .orEmpty()
        .map { OfflineAssetRef(type = SoundscapeAssetType.SOUND, url = it) }
    val localEditedSoundAssets = localLayerSoundIds
        .mapNotNull { soundId -> localSoundFileUrlsById[soundId]?.takeIf { it.isNotBlank() } }
        .map { OfflineAssetRef(type = SoundscapeAssetType.SOUND, url = it) }
    val musicAssets = scene?.sceneMusics
        ?.mapNotNull { music ->
            music.musicFileUrl?.takeIf { it.isNotBlank() }
                ?: music.music?.fileUrl?.takeIf { it.isNotBlank() }
                ?: music.music?.file?.url?.takeIf { it.isNotBlank() }
        }.orEmpty()
        .map { OfflineAssetRef(type = SoundscapeAssetType.MUSIC, url = it) }
    val localEditedMusicAssets = listOfNotNull(
        localSelectedMusicUrl?.takeIf { it.isNotBlank() }?.let {
            OfflineAssetRef(type = SoundscapeAssetType.MUSIC, url = it)
        }
    )
    val videoAssets = listOfNotNull(
        scene?.video?.url?.takeIf { it.isNotBlank() },
        scene?.videoUrl?.takeIf { it.isNotBlank() }
    ).map { OfflineAssetRef(type = SoundscapeAssetType.VIDEO, url = it) }
    val previewAssets = listOfNotNull(
        scene?.imagePreviewUrl?.takeIf { it.isNotBlank() },
        scene?.imageCoverUrl?.takeIf { it.isNotBlank() }
    ).map { OfflineAssetRef(type = SoundscapeAssetType.IMAGE_PREVIEW, url = it) }
    val backgroundAssets = listOfNotNull(
        scene?.imageUrl?.takeIf { it.isNotBlank() }
    ).map { OfflineAssetRef(type = SoundscapeAssetType.IMAGE_BACKGROUND, url = it) }
    return (
        soundAssets +
            localEditedSoundAssets +
            musicAssets +
            localEditedMusicAssets +
            videoAssets +
            previewAssets +
            backgroundAssets
        )
        .distinctBy { "${it.type}:${it.url}" }
}

private fun parseLocalLayerSoundIds(layersJson: String?): List<Int> {
    if (layersJson.isNullOrBlank()) return emptyList()
    return layersJson.split("|")
        .mapNotNull { item ->
            val idPart = item.substringBefore(":", missingDelimiterValue = "").trim()
            idPart.toIntOrNull()
        }
}
