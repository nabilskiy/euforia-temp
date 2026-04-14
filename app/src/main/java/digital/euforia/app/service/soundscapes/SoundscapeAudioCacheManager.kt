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

    private val soundsDirectory: File by lazy {
        File(context.filesDir, "soundscapes/sounds").apply { mkdirs() }
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

    suspend fun ensureCached(url: String): String {
        if (url.isBlank() || isLocalUrl(url)) return url
        val existing = findExistingFile(url)
        if (existing != null) return existing.toURI().toString()
        val deferred = inFlightMutex.withLock {
            inFlight[url] ?: coroutineScope {
                async(ioDispatcher) { downloadToCache(url) }
            }.also { inFlight[url] = it }
        }
        return try {
            deferred.await()
        } finally {
            inFlightMutex.withLock {
                if (inFlight[url] === deferred) {
                    inFlight.remove(url)
                }
            }
        }
    }

    suspend fun resolvePlaybackUrl(url: String?): String? {
        if (url.isNullOrBlank() || isLocalUrl(url)) return url
        val existing = findExistingFile(url) ?: return url
        return existing.toURI().toString()
    }

    suspend fun resolveForPlayback(url: String?): PreparedAudioUrl? {
        if (url.isNullOrBlank()) return null
        val playback = if (isLocalUrl(url)) {
            url
        } else {
            val existing = findExistingFile(url)
            existing?.toURI()?.toString() ?: url
        }
        return PreparedAudioUrl(
            remoteUrl = url,
            playbackUrl = playback,
            fromCache = playback != url
        )
    }

    private suspend fun downloadToCache(url: String): String = withContext(ioDispatcher) {
        soundsDirectory.mkdirs()
        val target = fileForUrl(url)
        if (target.exists()) {
            return@withContext target.toURI().toString()
        }
        val tmp = File(target.absolutePath + ".tmp")
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to download sound: ${response.code}")
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

    private fun findExistingFile(url: String): File? {
        val file = fileForUrl(url)
        return file.takeIf { it.exists() }
    }

    private fun fileForUrl(url: String): File {
        val ext = extractExtension(url)
        val digest = sha256(url)
        val fileName = if (ext.isNotBlank()) "$digest.$ext" else digest
        return File(soundsDirectory, fileName)
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
}
