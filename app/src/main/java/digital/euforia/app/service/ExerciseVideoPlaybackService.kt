package digital.euforia.app.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionError
import dagger.hilt.android.AndroidEntryPoint
import digital.euforia.app.R
import digital.euforia.app.service.soundscapes.beginSoundscapeInterruption
import digital.euforia.app.service.soundscapes.endSoundscapeInterruption
import digital.euforia.app.ui.MainActivity
import timber.log.Timber

private const val EXERCISE_SERVICE_MAIN_TOKEN = "exercise_service_main"

@AndroidEntryPoint
@UnstableApi
class ExerciseVideoPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var sfxPlayer: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        val exo = createPlayer()
        // Sync SFX playback with main player state
        exo.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Timber.tag("PUBLICATION_PLAYBACK").d("Service: onIsPlayingChanged=$isPlaying")
                if (isPlaying) {
                    beginSoundscapeInterruption(this@ExerciseVideoPlaybackService, EXERCISE_SERVICE_MAIN_TOKEN)
                } else {
                    endSoundscapeInterruption(this@ExerciseVideoPlaybackService, EXERCISE_SERVICE_MAIN_TOKEN)
                }
                sfxPlayer?.let { sp ->
                    if (isPlaying) sp.play() else sp.pause()
                }
                super.onIsPlayingChanged(isPlaying)
            }

            override fun onPlaybackStateChanged(state: Int) {
                Timber.tag("PUBLICATION_PLAYBACK").d("Service: onPlaybackStateChanged=$state")
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Timber.tag("PUBLICATION_PLAYBACK").e(error, "Service: onPlayerError")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Timber.tag("PUBLICATION_PLAYBACK").d("Service: onMediaItemTransition=${mediaItem?.mediaId}, reason=$reason")
            }
        })
        player = exo
        setMediaNotificationProvider(createNotificationProvider())
        mediaSession = createMediaSession(createSessionPlayer(exo))
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        endSoundscapeInterruption(this, EXERCISE_SERVICE_MAIN_TOKEN)
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        sfxPlayer?.release()
        sfxPlayer = null
        super.onDestroy()
    }

    object Commands {
        val STOP_SERVICE = SessionCommand("exercise_stop_service", android.os.Bundle.EMPTY)
        val PLAY_SFX = SessionCommand("play_sfx", android.os.Bundle.EMPTY)
        val STOP_SFX = SessionCommand("stop_sfx", android.os.Bundle.EMPTY)
        val PAUSE_SFX = SessionCommand("pause_sfx", android.os.Bundle.EMPTY)
        val RESUME_SFX = SessionCommand("resume_sfx", android.os.Bundle.EMPTY)
        val VOLUME_SFX = SessionCommand("volume_sfx", android.os.Bundle.EMPTY)
    }

    private fun createPlayer(): ExoPlayer =
        ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()

    private fun createNotificationProvider(): DefaultMediaNotificationProvider =
        DefaultMediaNotificationProvider.Builder(this)
            .setChannelId("exercise_video_playback")
            .setChannelName(R.string.app_name)
            .build()

    private fun createSessionPlayer(exo: ExoPlayer): Player =
        object : ForwardingPlayer(exo) {
            override fun getAvailableCommands(): Player.Commands =
                super.getAvailableCommands()
                    .buildUpon()
                    .remove(COMMAND_SEEK_TO_PREVIOUS)
                    .remove(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .remove(COMMAND_SEEK_TO_NEXT)
                    .remove(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .build()

            override fun isCommandAvailable(command: Int): Boolean =
                when (command) {
                    COMMAND_SEEK_TO_PREVIOUS,
                    COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                    COMMAND_SEEK_TO_NEXT,
                    COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> false
                    else -> super.isCommandAvailable(command)
                }

//            override fun canSeekToPrevious(): Boolean = false
//
//            override fun canSeekToNext(): Boolean = false
//
//            override fun canSeekToPreviousMediaItem(): Boolean = false
//
//            override fun canSeekToNextMediaItem(): Boolean = false
        }

    private fun createMediaSession(sessionPlayer: Player): MediaSession {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return MediaSession.Builder(this, sessionPlayer)
            .setId("exercise_video_session")
            .setSessionActivity(pendingIntent)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val base = super.onConnect(session, controller)
                    val available = base.availableSessionCommands
                        .buildUpon()
                        .add(Commands.STOP_SERVICE)
                        .add(Commands.PLAY_SFX)
                        .add(Commands.STOP_SFX)
                        .add(Commands.PAUSE_SFX)
                        .add(Commands.RESUME_SFX)
                        .add(Commands.VOLUME_SFX)
                        .build()
                    val playerCommands = base.availablePlayerCommands.buildUpon()
                        .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .remove(Player.COMMAND_SEEK_TO_NEXT)
                        .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .build()
                    return MediaSession.ConnectionResult.accept(
                        available,
                        playerCommands
                    )
                }

                override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
                    super.onPostConnect(session, controller)
                    Timber.tag("PUBLICATION_PLAYBACK").d("Service: onPostConnect from ${controller.packageName}")
                }

                override fun onDisconnected(session: MediaSession, controller: MediaSession.ControllerInfo) {
                    super.onDisconnected(session, controller)
                    Timber.tag("PUBLICATION_PLAYBACK").d("Service: onDisconnected from ${controller.packageName}")
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: android.os.Bundle
                ): com.google.common.util.concurrent.ListenableFuture<SessionResult> {
                    return when (customCommand.customAction) {
                        Commands.STOP_SERVICE.customAction -> {
                            Timber.tag("PUBLICATION_PLAYBACK").d("Service: received STOP_SERVICE")
                            player?.let { p ->
                                p.playWhenReady = false
                                p.stop()
                                p.clearMediaItems()
                            }
                            endSoundscapeInterruption(this@ExerciseVideoPlaybackService, EXERCISE_SERVICE_MAIN_TOKEN)
                            stopSelf()
                            com.google.common.util.concurrent.Futures.immediateFuture(
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            )
                        }
                        Commands.PLAY_SFX.customAction -> {
                            val url = args.getString("url")
                            val volume = args.getFloat("volume", 1f).coerceIn(0f, 1f)
                            if (url.isNullOrBlank()) {
                                return com.google.common.util.concurrent.Futures.immediateFuture(
                                    SessionResult(SessionError.ERROR_BAD_VALUE)
                                )
                            }
                            val sp = ensureSfxPlayer()
                            sp.volume = volume
                            sp.setMediaItem(MediaItem.fromUri(url))
                            sp.prepare()
                            sp.playWhenReady = player?.playWhenReady == true
                            sp.addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(state: Int) {
                                    if (state == Player.STATE_ENDED) {
                                        sp.clearMediaItems()
                                    }
                                }
                            })
                            com.google.common.util.concurrent.Futures.immediateFuture(
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            )
                        }
                        Commands.STOP_SFX.customAction -> {
                            sfxPlayer?.let { sp ->
                                sp.playWhenReady = false
                                sp.stop()
                                sp.clearMediaItems()
                            }
                            com.google.common.util.concurrent.Futures.immediateFuture(
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            )
                        }
                        Commands.PAUSE_SFX.customAction -> {
                            sfxPlayer?.pause()
                            com.google.common.util.concurrent.Futures.immediateFuture(
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            )
                        }
                        Commands.RESUME_SFX.customAction -> {
                            sfxPlayer?.play()
                            com.google.common.util.concurrent.Futures.immediateFuture(
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            )
                        }
                        Commands.VOLUME_SFX.customAction -> {
                            val volume = args.getFloat("volume", 1f).coerceIn(0f, 1f)
                            sfxPlayer?.volume = volume
                            com.google.common.util.concurrent.Futures.immediateFuture(
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            )
                        }
                        else -> super.onCustomCommand(session, controller, customCommand, args)
                    }
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
}
