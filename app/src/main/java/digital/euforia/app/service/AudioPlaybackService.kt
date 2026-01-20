package digital.euforia.app.service

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.SessionError
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import digital.euforia.app.R

/**
 * MediaSessionService hosting a main ExoPlayer instance and exposing a MediaSession
 * so UI can control playback via MediaController. Additionally, it owns a secondary
 * ExoPlayer dedicated to short sound effects that can play concurrently with the main
 * player without stealing audio focus.
 */
@AndroidEntryPoint
@UnstableApi
class AudioPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var sfxPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    object Commands {
        val PLAY_SFX = SessionCommand("play_sfx", Bundle.EMPTY)
        val STOP_SFX = SessionCommand("stop_sfx", Bundle.EMPTY)
        val PAUSE_SFX = SessionCommand("pause_sfx", Bundle.EMPTY)
        val RESUME_SFX = SessionCommand("resume_sfx", Bundle.EMPTY)
        val VOLUME_SFX = SessionCommand("volume_sfx", Bundle.EMPTY)
    }

    override fun onCreate() {
        super.onCreate()
        val exo = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()
        exo.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                sfxPlayer?.let {
                    if (!isPlaying) {
                        it.pause()
                    } else {
                        it.play()
                    }
                }
                super.onIsPlayingChanged(isPlaying)
            }
        })
        exo.volume = 1.0f
        player = exo

        // Provide media style notification so the system can promote us to a foreground service on playback
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId("media_playback")
            .setChannelName(R.string.app_name)
            .build()
        setMediaNotificationProvider(notificationProvider)

        mediaSession = MediaSession.Builder(this, exo)
            .setId("audio_playback_session")
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val base = super.onConnect(session, controller)
                    val available = base.availableSessionCommands
                        .buildUpon()
                        .add(Commands.PLAY_SFX)
                        .add(Commands.STOP_SFX)
                        .add(Commands.PAUSE_SFX)
                        .add(Commands.RESUME_SFX)
                        .add(Commands.VOLUME_SFX)
                        .build()
                    return MediaSession.ConnectionResult.accept(
                        available, base.availablePlayerCommands
                    )
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    when (customCommand.customAction) {
                        "play_sfx" -> {
                            val url = args.getString("url") ?: return Futures.immediateFuture(
                                SessionResult(SessionError.ERROR_BAD_VALUE)
                            )
                            val volume = args.getFloat("volume", 1f).coerceIn(0f, 1f)
                            val p = ensureSfxPlayer()
                            p.volume = volume
                            p.setMediaItem(MediaItem.fromUri(url))
                            p.prepare()
                            if (exo.isPlaying) p.playWhenReady = true
                            // optional: release after completion
                            p.addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(state: Int) {
                                    if (state == Player.STATE_ENDED) {
                                        p.clearMediaItems()
                                    }
                                }
                            })
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }

                        "stop_sfx" -> {
                            sfxPlayer?.stop()
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }

                        "volume_sfx" -> {
                            val volume = args.getFloat("volume", 1f).coerceIn(0f, 1f)
                            sfxPlayer?.volume = volume
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }

                        "pause_sfx" -> {
                            sfxPlayer?.pause()
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }

                        "resume_sfx" -> {
                            sfxPlayer?.play()
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }
                    }
                    return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
                }
            })
            .build()
    }

    private fun ensureSfxPlayer(): ExoPlayer {
        val existing = sfxPlayer
        if (existing != null) return existing
        val attrs = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION)
            .setUsage(C.USAGE_ASSISTANCE_SONIFICATION)
            .build()
        val p = ExoPlayer.Builder(this)
            .setAudioAttributes(attrs, /* handleAudioFocus= */ false)
            .setHandleAudioBecomingNoisy(true)
            .build()
        sfxPlayer = p
        return p
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val res = super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY_SFX -> {
                val url = intent.getStringExtra(EXTRA_URL)
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 1f).coerceIn(0f, 1f)
                if (!url.isNullOrBlank()) {
                    val p = ensureSfxPlayer()
                    p.volume = volume
                    p.setMediaItem(MediaItem.fromUri(url))
                    p.prepare()
                    p.playWhenReady = true
                }
            }

            ACTION_STOP_SFX -> {
                sfxPlayer?.let { sp ->
                    sp.playWhenReady = false
                    sp.stop()
                }
            }
        }
        return res
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Stop service if nothing is playing on the main player
        val p = player
        if (p == null || !p.playWhenReady) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        sfxPlayer?.release()
        sfxPlayer = null
        super.onDestroy()
    }

    companion object Companion {
        const val ACTION_PLAY_SFX = "digital.euforia.app.service.action.PLAY_SFX"
        const val ACTION_STOP_SFX = "digital.euforia.app.service.action.STOP_SFX"
        const val ACTION_VOLUME_SFX = "digital.euforia.app.service.action.PAUSE_SFX"
        const val ACTION_PAUSE_SFX = "digital.euforia.app.service.action.PAUSE_SFX"
        const val ACTION_RESUME_SFX = "digital.euforia.app.service.action.RESUME_SFX"
        const val EXTRA_URL = "digital.euforia.app.service.extra.URL"
        const val EXTRA_VOLUME = "digital.euforia.app.service.extra.VOLUME"
    }
}