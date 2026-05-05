/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.App
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundscapeSoundsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        const val MAX_SOUND_LAYERS = 12
    }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val players = LinkedHashMap<String, ExoPlayer>()
    private val playerUrls = LinkedHashMap<String, String>()
    private val fadeGeneration = LinkedHashMap<String, Int>()
    private val layerRepeatSec = LinkedHashMap<String, Int>()
    private val delayedStartsAtMs = LinkedHashMap<String, Long>()
    private val delayedStartRunnables = LinkedHashMap<String, Runnable>()
    private var currentSceneId: Int? = null
    private var isScenePlaying: Boolean = false
    private var countdownTickerActive = false
    private var repeatRemainingUpdater: (String, Long?) -> Unit = { _, _ -> }

    fun render(
        playback: SoundscapePlaybackState,
        onRepeatRemainingChanged: (layerKey: String, remainingMs: Long?) -> Unit = { _, _ -> }
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { render(playback, onRepeatRemainingChanged) }
            return
        }
        repeatRemainingUpdater = onRepeatRemainingChanged
        if (playback.sceneId == null) {
            releaseAll()
            return
        }
        isScenePlaying = playback.isPlaying
        if (currentSceneId != playback.sceneId) {
            releaseAll()
            currentSceneId = playback.sceneId
        }

        val layersWithAudio = playback.layers
            .filter { !it.audioUrl.isNullOrBlank() }
            .take(MAX_SOUND_LAYERS)
        val keepIds = layersWithAudio.map { it.instanceKey }.toSet()

        // Release removed layers.
        val toRemove = players.keys.filter { it !in keepIds }
        toRemove.forEach { id ->
            fadeGeneration.remove(id)
            playerUrls.remove(id)
            layerRepeatSec.remove(id)
            cancelDelayedStart(id)
            repeatRemainingUpdater(id, null)
            players.remove(id)?.release()
        }

        // Create/update players for active layers.
        layersWithAudio.forEach { layer ->
            val layerUrl = layer.audioUrl.orEmpty()
            val existing = players[layer.instanceKey]
            val currentUrl = playerUrls[layer.instanceKey]
            val player = if (existing == null || currentUrl != layerUrl) {
                existing?.release()
                createPlayer(layer.instanceKey, layerUrl).also {
                    players[layer.instanceKey] = it
                    playerUrls[layer.instanceKey] = layerUrl
                }
            } else existing
            val repeatSec = if (layer.isContinuous) 0 else layer.repeatIntervalSec.coerceAtLeast(0)
            val previousRepeatSec = layerRepeatSec[layer.instanceKey]
            layerRepeatSec[layer.instanceKey] = repeatSec
            val shouldWaitForDelayedStart = delayedStartsAtMs[layer.instanceKey]?.let { it > System.currentTimeMillis() } == true
            val targetVolume = if (layer.muted) 0f else layer.volume.coerceIn(0f, 1f)
            Timber.tag("SOUNDSCAPES_AUDIO").d(
                "layer_render key=%s soundId=%s title=%s muted=%s targetVolume=%.2f hasPlayer=%s",
                layer.instanceKey,
                layer.id,
                layer.title,
                layer.muted,
                targetVolume,
                existing != null
            )
            if (playback.isPlaying) {
                cancelFade(layer.instanceKey)
                player.volume = targetVolume
                if (repeatSec == 0) {
                    cancelDelayedStart(layer.instanceKey)
                    repeatRemainingUpdater(layer.instanceKey, null)
                    player.repeatMode = Player.REPEAT_MODE_ALL
                    player.playWhenReady = true
                    player.play()
                } else {
                    player.repeatMode = Player.REPEAT_MODE_OFF
                    if (shouldWaitForDelayedStart && previousRepeatSec != null && previousRepeatSec != repeatSec) {
                        // iOS parity: when interval is changed while waiting, restart countdown from full new interval.
                        scheduleDelayedReplayFromNow(layer.instanceKey, player, repeatSec)
                    } else if (!shouldWaitForDelayedStart) {
                        player.playWhenReady = true
                        player.play()
                    }
                }
            } else {
                cancelDelayedStart(layer.instanceKey)
                repeatRemainingUpdater(layer.instanceKey, null)
                startFadeOut(layer.instanceKey, player)
            }
        }
        updateRepeatCountdowns()
        Timber.tag("SOUNDSCAPES_AUDIO").d(
            "render scene=%s layers=%s players=%s playing=%s",
            playback.sceneId,
            layersWithAudio.size,
            players.size,
            playback.isPlaying
        )
    }



    suspend fun fadeOutAndReleaseAll(durationMs: Long = 320L) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            releaseAll()
            return
        }
        val currentPlayers = players.values.toList()
        if (currentPlayers.isEmpty()) {
            releaseAll()
            return
        }
        players.keys.forEach { key ->
            fadeGeneration[key] = (fadeGeneration[key] ?: 0) + 1
        }
        val startVolumes = currentPlayers.map { it.volume.coerceIn(0f, 1f) }
        val steps = 10
        val stepDelayMs = (durationMs / steps).coerceAtLeast(1L)
        for (step in 1..steps) {
            val t = step / steps.toFloat()
            currentPlayers.forEachIndexed { idx, player ->
                val v = startVolumes[idx] * (1f - t)
                player.volume = v.coerceIn(0f, 1f)
            }
            delay(stepDelayMs)
        }
        releaseAll()
    }

    fun releaseAll() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { releaseAll() }
            return
        }
        players.values.forEach { it.release() }
        players.clear()
        playerUrls.clear()
        fadeGeneration.clear()
        layerRepeatSec.clear()
        delayedStartRunnables.keys.toList().forEach(::cancelDelayedStart)
        countdownTickerActive = false
        currentSceneId = null
        isScenePlaying = false
    }

    private fun createPlayer(layerKey: String, audioUrl: String): ExoPlayer {
        val cache = App.getExoCache(context)
        val upstream = DefaultDataSource.Factory(context)
        val cacheFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstream)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
            .build().apply {
            // Multiple layer players must NOT each handle audio focus: they would compete and
            // pause/duck each other whenever play() or volume-driven re-render runs.
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus */ false
            )
            setMediaItem(MediaItem.fromUri(audioUrl))
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
            volume = 0.6f
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        handleLayerEnded(layerKey, this@apply)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Timber.tag("SOUNDSCAPES_AUDIO").w(error, "Layer player error key=%s", layerKey)
                }
            })
        }.also {
            Timber.tag("SOUNDSCAPES_AUDIO").d("Layer player created for %s", audioUrl)
        }
    }

    private fun cancelFade(layerId: String) {
        fadeGeneration[layerId] = (fadeGeneration[layerId] ?: 0) + 1
    }

    private fun startFadeOut(layerId: String, player: ExoPlayer) {
        val gen = (fadeGeneration[layerId] ?: 0) + 1
        fadeGeneration[layerId] = gen
        val startVolume = player.volume.coerceIn(0f, 1f)
        if (startVolume <= 0.0001f) {
            player.pause()
            player.playWhenReady = false
            return
        }
        val steps = 10
        val stepDelayMs = 100L
        for (step in 1..steps) {
            mainHandler.postDelayed({
                if (fadeGeneration[layerId] != gen) return@postDelayed
                val t = step / steps.toFloat()
                val v = startVolume * (1f - t)
                player.volume = v.coerceIn(0f, 1f)
                if (step == steps) {
                    player.pause()
                    player.playWhenReady = false
                }
            }, step * stepDelayMs)
        }
    }

    private fun handleLayerEnded(layerKey: String, player: ExoPlayer) {
        val repeatSec = layerRepeatSec[layerKey] ?: 0
        if (repeatSec <= 0 || !isScenePlaying) {
            player.seekTo(0L)
            player.playWhenReady = isScenePlaying
            if (isScenePlaying) player.play()
            return
        }
        scheduleDelayedReplayFromNow(layerKey, player, repeatSec)
    }

    private fun scheduleDelayedReplayFromNow(layerKey: String, player: ExoPlayer, repeatSec: Int) {
        val delayMs = repeatSec * 1000L
        val startAtMs = System.currentTimeMillis() + delayMs
        cancelDelayedStart(layerKey)
        delayedStartsAtMs[layerKey] = startAtMs
        repeatRemainingUpdater(layerKey, delayMs)
        val runnable = Runnable {
            delayedStartRunnables.remove(layerKey)
            delayedStartsAtMs.remove(layerKey)
            if (!isScenePlaying) return@Runnable
            val currentPlayer = players[layerKey] ?: return@Runnable
            currentPlayer.seekTo(0L)
            currentPlayer.playWhenReady = true
            currentPlayer.play()
            repeatRemainingUpdater(layerKey, null)
        }
        delayedStartRunnables[layerKey] = runnable
        mainHandler.postDelayed(runnable, delayMs)
        ensureCountdownTicker()
    }

    private fun cancelDelayedStart(layerKey: String) {
        delayedStartRunnables.remove(layerKey)?.let(mainHandler::removeCallbacks)
        delayedStartsAtMs.remove(layerKey)
    }

    private fun updateRepeatCountdowns() {
        val now = System.currentTimeMillis()
        val keys = layerRepeatSec.keys.toList()
        keys.forEach { key ->
            val remaining = delayedStartsAtMs[key]?.minus(now)?.coerceAtLeast(0L)
            repeatRemainingUpdater(key, remaining)
        }
    }

    private fun ensureCountdownTicker() {
        if (countdownTickerActive) return
        countdownTickerActive = true
        mainHandler.post(object : Runnable {
            override fun run() {
                if (!countdownTickerActive) return
                updateRepeatCountdowns()
                val hasActive = delayedStartsAtMs.values.any { it > System.currentTimeMillis() }
                if (hasActive) {
                    mainHandler.postDelayed(this, 100L)
                } else {
                    countdownTickerActive = false
                }
            }
        })
    }
}
