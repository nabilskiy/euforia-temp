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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
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
            val urls = buildOfflineAssetUrls(scene)

            if (urls.isEmpty()) {
                repository.updateDownload(
                    startedItem.copy(
                        status = SoundscapeDownloadItem.STATUS_READY,
                        progress = 100,
                        localPath = "scene_${item.sceneId}|assets=${urls.size}",
                        updatedAt = System.currentTimeMillis()
                    )
                )
                analyticSender.soundscapeDownloadReady(item.sceneId)
                return
            }

            urls.forEachIndexed { index, url ->
                val result = retry(retries = 2, delayMillis = 500L) {
                    runCatching {
                        audioCacheManager.ensureCached(url)
                    }.fold(
                        onSuccess = { ResultWrapper.success(it) },
                        onFailure = { ResultWrapper.failure(it) }
                    )
                }
                if (!result.isSuccess) {
                    throw result.throwableOrNull ?: IllegalStateException("Download failed")
                }
                val progress = (((index + 1).toFloat() / urls.size.toFloat()) * 100f).toInt().coerceIn(0, 100)
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
                    localPath = "scene_${item.sceneId}|assets=${urls.size}",
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

internal fun buildOfflineAssetUrls(scene: NetworkScene?): List<String> {
    val soundUrls = scene?.sceneSounds
        ?.mapNotNull { soundItem ->
            soundItem.soundFileUrl?.takeIf { it.isNotBlank() }
                ?: soundItem.sound?.fileUrl?.takeIf { it.isNotBlank() }
                ?: soundItem.sound?.file?.url?.takeIf { it.isNotBlank() }
        }.orEmpty()
    val musicUrls = scene?.sceneMusics
        ?.mapNotNull { m ->
            m.musicFileUrl?.takeIf { it.isNotBlank() }
                ?: m.music?.fileUrl?.takeIf { it.isNotBlank() }
                ?: m.music?.file?.url?.takeIf { it.isNotBlank() }
        }.orEmpty()
    val backgroundUrls = listOfNotNull(
        scene?.video?.url?.takeIf { it.isNotBlank() },
        scene?.videoUrl?.takeIf { it.isNotBlank() }
    )
    return (soundUrls + musicUrls + backgroundUrls).distinct()
}
