/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import digital.euforia.app.R
import digital.euforia.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@UnstableApi
class SoundscapePlaybackService : MediaSessionService() {
    @Inject
    lateinit var playbackController: SoundscapePlaybackController

    @Inject
    lateinit var navigationEvents: SoundscapeNavigationEvents

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var syncingFromPlaybackState = false
    private var lastMediaId: String? = null

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
            // Session ExoPlayer uses a non-playable placeholder item; isPlaying may not toggle.
            // playWhenReady still reflects transport from MediaController / notification controls.
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (syncingFromPlaybackState) return
                val current = playbackController.playback.value.isPlaying
                if (current != playWhenReady) {
                    playbackController.setPlaying(playWhenReady)
                }
            }
        })
        player = exo
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
                    stopSelf()
                    return@collectLatest
                }
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
        observeJob?.cancel()
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun updateSessionMediaItem(exo: ExoPlayer, playback: SoundscapePlaybackState) {
        val sceneId = playback.sceneId ?: return
        val mediaId = "soundscape:$sceneId"
        val artworkUri = playback.sceneImageUrl?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        val metadata = MediaMetadata.Builder()
            .setTitle(playback.sceneTitle.ifBlank { getString(R.string.scenes_title) })
            .setArtist(getString(R.string.scenes_title))
            .setArtworkUri(artworkUri)
            .build()
        if (lastMediaId != mediaId || exo.mediaItemCount == 0) {
            exo.setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .setUri(Uri.EMPTY)
                    .setMediaMetadata(metadata)
                    .build()
            )
            lastMediaId = mediaId
        } else {
            exo.replaceMediaItem(
                0,
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .setUri(Uri.EMPTY)
                    .setMediaMetadata(metadata)
                    .build()
            )
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

    companion object {
        private const val CHANNEL_ID = "soundscapes_playback"
    }

    object Commands {
        val NEXT_SCENE = SessionCommand("soundscape_next_scene", android.os.Bundle.EMPTY)
        val PREV_SCENE = SessionCommand("soundscape_prev_scene", android.os.Bundle.EMPTY)
    }
}

