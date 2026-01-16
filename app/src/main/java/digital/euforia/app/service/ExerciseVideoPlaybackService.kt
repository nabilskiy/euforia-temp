package digital.euforia.app.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import dagger.hilt.android.AndroidEntryPoint
import digital.euforia.app.R

@AndroidEntryPoint
@UnstableApi
class ExerciseVideoPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val exo = createPlayer()
        player = exo
        setMediaNotificationProvider(createNotificationProvider())
        mediaSession = createMediaSession(exo)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        super.onDestroy()
    }

    object Commands {
        val STOP_SERVICE = SessionCommand("exercise_stop_service", android.os.Bundle.EMPTY)
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

    private fun createMediaSession(exo: ExoPlayer): MediaSession =
        MediaSession.Builder(this, exo)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val base = super.onConnect(session, controller)
                    val available = base.availableSessionCommands
                        .buildUpon()
                        .add(Commands.STOP_SERVICE)
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
                ): com.google.common.util.concurrent.ListenableFuture<SessionResult> {
                    return when (customCommand.customAction) {
                        Commands.STOP_SERVICE.customAction -> {
                            player?.let { p ->
                                p.playWhenReady = false
                                p.stop()
                                p.clearMediaItems()
                            }
                            stopSelf()
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
