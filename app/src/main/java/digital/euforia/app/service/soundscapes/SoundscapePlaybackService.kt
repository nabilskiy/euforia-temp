/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import digital.euforia.app.App
import digital.euforia.app.R
import digital.euforia.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
@UnstableApi
class SoundscapePlaybackService : MediaSessionService() {
    @Inject
    lateinit var playbackController: SoundscapePlaybackController

    @Inject
    lateinit var navigationEvents: SoundscapeNavigationEvents

    @Inject
    lateinit var soundsManager: SoundscapeSoundsManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var musicPlayer: ExoPlayer? = null
    private var syncingFromPlaybackState = false
    private var lastMediaId: String? = null
    private var lastSessionStreamUrl: String? = null
    private var lastMusicUrl: String? = null
    private var sleepTimerJob: Job? = null
    private var sleepTimerTickJob: Job? = null
    private var musicPauseFadeJob: Job? = null
    private var sleepDeadlineElapsedMs: Long? = null
    private var sleepTimerSeconds: Int? = null
    private val lastRepeatRemainingSecondByLayer = mutableMapOf<String, Long?>()

    override fun onCreate() {
        super.onCreate()
        // Session player is metadata-only (real mix is in SoundscapeSoundsManager). It must not
        // request exclusive audio focus or it will pause/stop all layer ExoPlayers on each sync.
        val exo = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(false)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus */ false
            )
            .build()
        exo.volume = 0f
        exo.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (syncingFromPlaybackState) return
                // Metadata-only session player must not drive global playback: navigation, focus, system UI,
                // etc. can toggle ExoPlayer without user intent. Only mirror changes we trust as real transport
                // commands (local request + remote/session: notification, headset, MediaSession).
                val trustedTransportReason =
                    reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST ||
                        reason == Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE
                if (!trustedTransportReason) {
                    val desiredPlaying = playbackController.playback.value.isPlaying
                    if (exo.playWhenReady != desiredPlaying) {
                        syncingFromPlaybackState = true
                        try {
                            exo.playWhenReady = desiredPlaying
                        } finally {
                            syncingFromPlaybackState = false
                        }
                    }
                    return
                }
                val current = playbackController.playback.value.isPlaying
                if (current != playWhenReady) {
                    playbackController.setPlaying(playWhenReady)
                }
            }
        })
        player = exo
        musicPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(buildCachedMediaSourceFactory())
            .setHandleAudioBecomingNoisy(false)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus */ false
            )
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
            }
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.app_name)
            .build()
        setMediaNotificationProvider(notificationProvider)
        mediaSession = MediaSession.Builder(this, exo)
            .setId("soundscape_playback_session")
            .setSessionActivity(mainActivityIntent())
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val base = super.onConnect(session, controller)
                    val available = base.availableSessionCommands
                        .buildUpon()
                        .add(Commands.NEXT_SCENE)
                        .add(Commands.PREV_SCENE)
                        .build()
                    return MediaSession.ConnectionResult.accept(
                        available,
                        base.availablePlayerCommands
                    )
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: android.os.Bundle
                ): ListenableFuture<SessionResult> {
                    return when (customCommand.customAction) {
                        Commands.NEXT_SCENE.customAction -> {
                            playbackController.nextSceneId()?.let(navigationEvents::openScene)
                            Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }

                        Commands.PREV_SCENE.customAction -> {
                            playbackController.prevSceneId()?.let(navigationEvents::openScene)
                            Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }

                        else -> super.onCustomCommand(session, controller, customCommand, args)
                    }
                }

            })
            .build()
        observeJob = serviceScope.launch {
            playbackController.playback.collectLatest { playback ->
                if (playback.sceneId == null) {
                    cancelSleepTimer()
                    lastMediaId = null
                    lastSessionStreamUrl = null
                    lastMusicUrl = null
                    lastRepeatRemainingSecondByLayer.clear()
                    soundsManager.releaseAll()
                    musicPlayer?.stop()
                    musicPlayer?.clearMediaItems()
                    stopSelf()
                    return@collectLatest
                }
                if (playback.stopWithFadeOut) {
                    cancelSleepTimer()
                    lastRepeatRemainingSecondByLayer.clear()
                    fadeOutAndStopMusic(durationMs = 10_000L)
                    soundsManager.fadeOutAndReleaseAll(durationMs = 10_000L)
                    playbackController.stop()
                    return@collectLatest
                }
                soundsManager.render(playback) { layerKey, remainingMs ->
                    val bucketSec = remainingMs?.coerceAtLeast(0L)?.div(1000L)
                    val previousBucketSec = lastRepeatRemainingSecondByLayer[layerKey]
                    if (previousBucketSec != bucketSec) {
                        lastRepeatRemainingSecondByLayer[layerKey] = bucketSec
                        playbackController.updateLayerRepeatRemaining(layerKey, remainingMs)
                        if (bucketSec != null) {
                            Timber.tag("SOUNDSCAPES_DEBUG").v(
                                "repeat_remaining layer=%s sec=%s",
                                layerKey,
                                bucketSec
                            )
                        }
                    }
                }
                syncMusicPlayback(playback)
                syncSleepTimer(playback)
                updateSessionMediaItem(exo, playback)
                syncingFromPlaybackState = true
                try {
                    if (exo.playWhenReady != playback.isPlaying) {
                        exo.playWhenReady = playback.isPlaying
                    }
                } finally {
                    syncingFromPlaybackState = false
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        cancelSleepTimer()
        musicPauseFadeJob?.cancel()
        observeJob?.cancel()
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        soundsManager.releaseAll()
        lastRepeatRemainingSecondByLayer.clear()
        musicPlayer?.release()
        musicPlayer = null
        super.onDestroy()
    }

    private fun syncSleepTimer(playback: SoundscapePlaybackState) {
        val seconds = playback.timerSeconds
        if (seconds == null || seconds <= 0) {
            cancelSleepTimer()
            return
        }
        val expectedDeadline = if (sleepTimerSeconds == seconds && sleepDeadlineElapsedMs != null) {
            sleepDeadlineElapsedMs!!
        } else {
            SystemClock.elapsedRealtime() + seconds * 1000L
        }
        if (sleepTimerJob != null && sleepDeadlineElapsedMs == expectedDeadline) return
        sleepTimerSeconds = seconds
        sleepDeadlineElapsedMs = expectedDeadline
        playbackController.updateTimerRemaining(secondsUntilDeadline(expectedDeadline))
        sleepTimerJob?.cancel()
        sleepTimerJob = serviceScope.launch {
            val delayMs = (expectedDeadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            delay(delayMs)
            playbackController.setPlaying(false)
            playbackController.setTimer(null)
        }
        sleepTimerTickJob?.cancel()
        sleepTimerTickJob = serviceScope.launch {
            while (true) {
                val left = secondsUntilDeadline(expectedDeadline)
                playbackController.updateTimerRemaining(left)
                if (left <= 0) break
                delay(1000L)
            }
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerTickJob?.cancel()
        sleepTimerJob = null
        sleepTimerTickJob = null
        sleepDeadlineElapsedMs = null
        sleepTimerSeconds = null
        playbackController.updateTimerRemaining(null)
    }

    private fun secondsUntilDeadline(deadlineElapsedMs: Long): Int {
        val remainingMs = (deadlineElapsedMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        return ((remainingMs + 999L) / 1000L).toInt()
    }



    private suspend fun fadeOutAndStopMusic(durationMs: Long = 320L) {
        val p = musicPlayer ?: return
        val startVolume = p.volume.coerceIn(0f, 1f)
        if (startVolume <= 0.0001f) {
            p.stop()
            p.clearMediaItems()
            return
        }
        val steps = 10
        val stepDelayMs = (durationMs / steps).coerceAtLeast(1L)
        for (step in 1..steps) {
            val t = step / steps.toFloat()
            val volume = startVolume * (1f - t)
            p.volume = volume.coerceIn(0f, 1f)
            delay(stepDelayMs)
        }
        p.stop()
        p.clearMediaItems()
        p.volume = startVolume
    }

    private suspend fun fadeOutAndPauseMusic(
        player: ExoPlayer,
        restoreVolume: Float,
        durationMs: Long = 1000L
    ) {
        val startVolume = player.volume.coerceIn(0f, 1f)
        if (startVolume <= 0.0001f) {
            player.pause()
            player.playWhenReady = false
            player.volume = restoreVolume
            return
        }
        val steps = 10
        val stepDelayMs = (durationMs / steps).coerceAtLeast(1L)
        for (step in 1..steps) {
            val t = step / steps.toFloat()
            val volume = startVolume * (1f - t)
            player.volume = volume.coerceIn(0f, 1f)
            delay(stepDelayMs)
        }
        player.pause()
        player.playWhenReady = false
        player.volume = restoreVolume
    }

    private fun syncMusicPlayback(playback: SoundscapePlaybackState) {
        val p = musicPlayer ?: return
        val musicUrl = playback.sceneMusicUrl?.takeIf(String::isNotBlank)
        if (musicUrl.isNullOrBlank()) {
            musicPauseFadeJob?.cancel()
            lastMusicUrl = null
            p.stop()
            p.clearMediaItems()
            return
        }
        if (lastMusicUrl != musicUrl || p.mediaItemCount == 0) {
            p.setMediaItem(MediaItem.fromUri(musicUrl))
            p.prepare()
            lastMusicUrl = musicUrl
        }
        val finalMusicVolume = (playback.musicVolume * playback.sceneMusicVolumeFactor).coerceIn(0f, 1f)
        android.util.Log.d(
            "SOUNDSCAPES_AUDIO",
            "music_volume base=${playback.musicVolume} factor=${playback.sceneMusicVolumeFactor} final=$finalMusicVolume playing=${playback.isPlaying}"
        )
        if (playback.isPlaying) {
            musicPauseFadeJob?.cancel()
            p.volume = finalMusicVolume
            p.play()
        } else {
            if ((p.playWhenReady || p.isPlaying) && (musicPauseFadeJob?.isActive != true)) {
                musicPauseFadeJob = serviceScope.launch {
                    fadeOutAndPauseMusic(
                        player = p,
                        restoreVolume = finalMusicVolume,
                        durationMs = 1000L
                    )
                }
            } else if (!p.playWhenReady && !p.isPlaying) {
                p.volume = finalMusicVolume
            }
        }
    }

    private fun updateSessionMediaItem(exo: ExoPlayer, playback: SoundscapePlaybackState) {
        val sceneId = playback.sceneId ?: return
        val mediaId = "soundscape:$sceneId"
        val artworkUri = playback.sceneImageUrl?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        val sessionStreamUrl = playback.layers
            .firstNotNullOfOrNull { it.audioUrl?.takeIf(String::isNotBlank) }
        val metadata = MediaMetadata.Builder()
            .setTitle(playback.sceneTitle.ifBlank { getString(R.string.scenes_title) })
            .setArtist(getString(R.string.scenes_title))
            .setArtworkUri(artworkUri)
            .build()
        if (sessionStreamUrl.isNullOrBlank()) {
            exo.stop()
            exo.clearMediaItems()
            lastMediaId = mediaId
            lastSessionStreamUrl = null
            return
        }
        val shouldReplaceItem =
            lastMediaId != mediaId ||
                exo.mediaItemCount == 0 ||
                lastSessionStreamUrl != sessionStreamUrl
        if (shouldReplaceItem) {
            exo.setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .setUri(Uri.parse(sessionStreamUrl))
                    .setMediaMetadata(metadata)
                    .build()
            )
            lastMediaId = mediaId
            lastSessionStreamUrl = sessionStreamUrl
        }
        if (exo.playbackState == Player.STATE_IDLE) {
            exo.prepare()
        }
    }

    private fun mainActivityIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun buildCachedMediaSourceFactory(): DefaultMediaSourceFactory {
        val cacheFactory = CacheDataSource.Factory()
            .setCache(App.getExoCache(this))
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(this))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        return DefaultMediaSourceFactory(cacheFactory)
    }

    companion object {
        private const val CHANNEL_ID = "soundscapes_playback"
    }

    object Commands {
        val NEXT_SCENE = SessionCommand("soundscape_next_scene", android.os.Bundle.EMPTY)
        val PREV_SCENE = SessionCommand("soundscape_prev_scene", android.os.Bundle.EMPTY)
    }
}

