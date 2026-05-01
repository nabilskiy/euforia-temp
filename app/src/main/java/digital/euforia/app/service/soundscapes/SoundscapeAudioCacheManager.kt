/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class PreparedAudioUrl(
    val remoteUrl: String,
    val playbackUrl: String,
    val fromCache: Boolean,
)

@Singleton
class SoundscapeAudioCacheManager @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val okHttpClient: OkHttpClient,
) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val inFlightMutex = Mutex()
    private val inFlight = LinkedHashMap<String, kotlinx.coroutines.Deferred<String>>()

    private val soundscapeDirectory: File by lazy {
        File(context.filesDir, "soundscapes").apply { mkdirs() }
    }

    suspend fun prepareSceneLayers(
        layers: List<SoundscapeLayerState>,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): List<SoundscapeLayerState> = coroutineScope {
        val urls = layers.mapNotNull { it.audioUrl?.takeIf { url -> url.isNotBlank() } }.distinct()
        if (urls.isEmpty()) {
            onProgress(0, 0)
            return@coroutineScope layers
        }
        var completed = 0
        val total = urls.size
        onProgress(completed, total)
        val resolved = urls.map { url ->
            async {
                val localOrRemote = runCatching { ensureCached(url) }.getOrDefault(url)
                val done = synchronized(this@SoundscapeAudioCacheManager) {
                    completed += 1
                    completed
                }
                onProgress(done, total)
                url to localOrRemote
            }
        }.awaitAll().toMap()
        layers.map { layer ->
            val remote = layer.audioUrl
            val local = remote?.let { resolved[it] }
            if (local != null) layer.copy(audioUrl = local) else layer
        }
    }

    suspend fun ensureCached(url: String): String = ensureCached(url, SoundscapeAssetType.SOUND)

    suspend fun ensureCached(url: String, type: SoundscapeAssetType): String {
        if (url.isBlank() || isLocalUrl(url)) return url
        if (shouldBypassFileCaching(url, type)) return url
        val existing = findExistingFile(url, type)
        if (existing != null) return existing.toURI().toString()
        val key = "${type.name}:$url"
        val deferred = inFlightMutex.withLock {
            inFlight[key] ?: coroutineScope {
                async(ioDispatcher) { downloadToCache(url, type) }
            }.also { inFlight[key] = it }
        }
        return try {
            deferred.await()
        } finally {
            inFlightMutex.withLock {
                if (inFlight[key] === deferred) {
                    inFlight.remove(key)
                }
            }
        }
    }

    suspend fun resolvePlaybackUrl(url: String?): String? = resolvePlaybackUrl(url, SoundscapeAssetType.SOUND)

    suspend fun resolvePlaybackUrl(url: String?, type: SoundscapeAssetType): String? {
        if (url.isNullOrBlank() || isLocalUrl(url)) return url
        if (shouldBypassFileCaching(url, type)) return url
        val existing = findExistingFile(url, type) ?: return url
        return existing.toURI().toString()
    }

    suspend fun resolveForPlayback(url: String?): PreparedAudioUrl? {
        if (url.isNullOrBlank()) return null
        val playback = if (isLocalUrl(url)) {
            url
        } else {
            val existing = findExistingFile(url, SoundscapeAssetType.SOUND)
            existing?.toURI()?.toString() ?: url
        }
        return PreparedAudioUrl(
            remoteUrl = url,
            playbackUrl = playback,
            fromCache = playback != url
        )
    }

    suspend fun resolveForPlayback(url: String?, type: SoundscapeAssetType): PreparedAudioUrl? {
        if (url.isNullOrBlank()) return null
        val playback = if (isLocalUrl(url)) {
            url
        } else {
            if (shouldBypassFileCaching(url, type)) return PreparedAudioUrl(
                remoteUrl = url,
                playbackUrl = url,
                fromCache = false
            )
            val existing = findExistingFile(url, type)
            existing?.toURI()?.toString() ?: url
        }
        return PreparedAudioUrl(
            remoteUrl = url,
            playbackUrl = playback,
            fromCache = playback != url
        )
    }

    suspend fun deleteCached(url: String, type: SoundscapeAssetType): Boolean = withContext(ioDispatcher) {
        val file = findExistingFile(url, type) ?: return@withContext false
        file.delete()
    }

    suspend fun deleteByManifest(manifest: SoundscapeOfflineManifest) {
        withContext(ioDispatcher) {
            manifest.assets.forEach { asset ->
                runCatching {
                    val uri = java.net.URI(asset.localUrl)
                    if (uri.scheme.equals("file", ignoreCase = true)) {
                        File(uri).delete()
                    } else {
                        findExistingFile(asset.remoteUrl, asset.type)?.delete()
                    }
                }.onFailure {
                    findExistingFile(asset.remoteUrl, asset.type)?.delete()
                }
            }
        }
    }

    private suspend fun downloadToCache(url: String, type: SoundscapeAssetType): String = withContext(ioDispatcher) {
        val target = fileForUrl(url, type)
        target.parentFile?.mkdirs()
        if (target.exists()) {
            return@withContext target.toURI().toString()
        }
        val tmp = File(target.absolutePath + ".tmp")
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to download asset: ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("Empty response body")
            tmp.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
        if (!tmp.renameTo(target)) {
            tmp.copyTo(target, overwrite = true)
            tmp.delete()
        }
        target.toURI().toString()
    }

    private fun findExistingFile(url: String, type: SoundscapeAssetType): File? {
        val file = fileForUrl(url, type)
        return file.takeIf { it.exists() }
    }

    private fun fileForUrl(url: String, type: SoundscapeAssetType): File {
        val ext = extractExtension(url)
        val digest = sha256(url)
        val fileName = if (ext.isNotBlank()) "$digest.$ext" else digest
        return File(directoryForType(type), fileName)
    }

    private fun directoryForType(type: SoundscapeAssetType): File {
        val child = when (type) {
            SoundscapeAssetType.SOUND -> "sounds"
            SoundscapeAssetType.MUSIC -> "music"
            SoundscapeAssetType.VIDEO -> "video"
            SoundscapeAssetType.IMAGE_PREVIEW -> "images/preview"
            SoundscapeAssetType.IMAGE_BACKGROUND -> "images/background"
        }
        return File(soundscapeDirectory, child).apply { mkdirs() }
    }

    private fun extractExtension(url: String): String {
        return runCatching {
            val path = java.net.URI(url).path ?: return@runCatching ""
            val tail = path.substringAfterLast('/', "")
            if (!tail.contains('.')) "" else tail.substringAfterLast('.').lowercase()
        }.getOrDefault("")
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }

    private fun isLocalUrl(url: String): Boolean {
        return url.startsWith("file:/") || runCatching {
            val uri = java.net.URI(url)
            uri.scheme.equals("content", ignoreCase = true)
        }.getOrDefault(false)
    }

    private fun shouldBypassFileCaching(url: String, type: SoundscapeAssetType): Boolean {
        if (type != SoundscapeAssetType.VIDEO && type != SoundscapeAssetType.MUSIC) return false
        val path = runCatching { java.net.URI(url).path.orEmpty().lowercase() }.getOrDefault(url.lowercase())
        return path.endsWith(".m3u8") || path.endsWith(".mpd")
    }
}
