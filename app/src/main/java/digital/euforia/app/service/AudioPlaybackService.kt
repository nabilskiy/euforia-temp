package digital.euforia.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Service that owns a single main ExoPlayer instance for accompaniment playback
 * and an optional secondary instance for sound effects.
 *
 * Start playback by starting the service with the corresponding ACTION and url EXTRA.
 */
@AndroidEntryPoint
class AudioPlaybackService : Service() {

    private var mainPlayer: ExoPlayer? = null
    private var sfxPlayer: ExoPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_MAIN -> {
                val url = intent.getStringExtra(EXTRA_URL)
                if (!url.isNullOrBlank()) playMain(url)
            }
            ACTION_PLAY_SFX -> {
                val url = intent.getStringExtra(EXTRA_URL)
                if (!url.isNullOrBlank()) playSfx(url)
            }
            ACTION_STOP_ALL -> stopAll()
            ACTION_RELEASE -> {
                releaseAll()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun ensureMainPlayer(): ExoPlayer {
        val existing = mainPlayer
        if (existing != null) return existing
        val player = buildPlayer(this)
        mainPlayer = player
        return player
    }

    private fun ensureSfxPlayer(): ExoPlayer {
        val existing = sfxPlayer
        if (existing != null) return existing
        val player = buildPlayer(this)
        sfxPlayer = player
        return player
    }

    private fun playMain(url: String) {
        val player = ensureMainPlayer()
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    private fun playSfx(url: String) {
        val player = ensureSfxPlayer()
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    private fun stopAll() {
        mainPlayer?.run {
            playWhenReady = false
            stop()
        }
        sfxPlayer?.run {
            playWhenReady = false
            stop()
        }
    }

    private fun releaseAll() {
        mainPlayer?.release()
        mainPlayer = null
        sfxPlayer?.release()
        sfxPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAll()
    }

    companion object Companion {
        const val ACTION_PLAY_MAIN = "digital.euforia.app.service.action.PLAY_MAIN"
        const val ACTION_PLAY_SFX = "digital.euforia.app.service.action.PLAY_SFX"
        const val ACTION_STOP_ALL = "digital.euforia.app.service.action.STOP_ALL"
        const val ACTION_RELEASE = "digital.euforia.app.service.action.RELEASE"
        const val EXTRA_URL = "digital.euforia.app.service.extra.URL"

        private fun buildPlayer(context: Context): ExoPlayer {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build()
            return ExoPlayer.Builder(context)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build()
        }

        fun playMainIntent(context: Context, url: String): Intent =
            Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_PLAY_MAIN
                putExtra(EXTRA_URL, url)
            }

        fun playSfxIntent(context: Context, url: String): Intent =
            Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_PLAY_SFX
                putExtra(EXTRA_URL, url)
            }

        fun stopAllIntent(context: Context): Intent =
            Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_STOP_ALL
            }

        fun releaseIntent(context: Context): Intent =
            Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_RELEASE
            }
    }
}
