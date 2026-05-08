package digital.euforia.app.ui.util

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import digital.euforia.app.service.soundscapes.pauseActiveSoundscapeIfNeeded
import kotlinx.coroutines.*

object SoundPlayer {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()

    fun init(context: Context, soundResList: List<Int>) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(1) // кількість одночасних звуків
            .build()

        // завантажуємо всі звуки
        soundResList.forEach { resId ->
            val id = soundPool?.load(context, resId, 1) ?: 0
            soundMap[resId] = id
        }
    }

    fun play(soundRes: Int) {
        val id = soundMap[soundRes] ?: return
        soundPool?.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}


object MediaPlayerHelper {

    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context, soundRes: Int, onCompletion: (() -> Unit)? = null) {
        mediaPlayer?.release()
        pauseActiveSoundscapeIfNeeded(context)
        mediaPlayer = MediaPlayer.create(context, soundRes).apply {
            setOnCompletionListener {
                onCompletion?.invoke()
                this@MediaPlayerHelper.release() // Use helper's release to clear the reference
            }
            start()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private var fadeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: IllegalStateException) {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: IllegalStateException) {
            mediaPlayer = null
            false
        }
    }

    fun resume() {
        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: IllegalStateException) {
            mediaPlayer = null
        }
    }

    fun stopWithFade(durationMs: Long = 1000, onComplete: (() -> Unit)? = null) {
        try {
            if (mediaPlayer == null || mediaPlayer?.isPlaying == false) {
                onComplete?.invoke()
                return
            }
        } catch (e: IllegalStateException) {
            mediaPlayer = null
            onComplete?.invoke()
            return
        }
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val steps = 20
            val delayMs = durationMs / steps
            val initialVolume = 1.0f
            for (i in steps downTo 0) {
                val volume = initialVolume * (i.toFloat() / steps)
                try {
                    mediaPlayer?.setVolume(volume, volume)
                } catch (e: Exception) {
                    break
                }
                delay(delayMs)
            }
            release()
            onComplete?.invoke()
        }
    }
}

object BackgroundPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null
    private var fadeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var targetVolume = 0.3f
    private var isFadingOut = false

    fun playLooping(context: Context, soundRes: Int) {
        try {
            if (mediaPlayer?.isPlaying == true) return
        } catch (e: IllegalStateException) {
            mediaPlayer = null
        }
        mediaPlayer?.release()
        pauseActiveSoundscapeIfNeeded(context)
        mediaPlayer = MediaPlayer.create(context, soundRes).apply {
            setVolume(targetVolume, targetVolume)
            isLooping = true
            start()
        }
    }

    fun pauseWithFade(durationMs: Long = 1000) {
        try {
            if (mediaPlayer == null || mediaPlayer?.isPlaying == false || isFadingOut) return
        } catch (e: IllegalStateException) {
            mediaPlayer = null
            return
        }
        isFadingOut = true
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val startVolume = targetVolume
            val steps = 20
            val delayMs = durationMs / steps
            for (i in steps downTo 0) {
                val volume = startVolume * (i.toFloat() / steps)
                mediaPlayer?.setVolume(volume, volume)
                delay(delayMs)
            }
            mediaPlayer?.pause()
            isFadingOut = false
        }
    }

    fun resumeWithFade(context: Context, soundRes: Int, durationMs: Long = 1000) {
        if (mediaPlayer == null) {
            playLooping(context, soundRes)
        }
        try {
            if (mediaPlayer?.isPlaying == true && !isFadingOut) return
        } catch (e: IllegalStateException) {
            mediaPlayer = null
            playLooping(context, soundRes)
        }
        
        isFadingOut = false
        fadeJob?.cancel()
        fadeJob = scope.launch {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.setVolume(0f, 0f)
                mediaPlayer?.start()
            }
            val startVolume = 0f
            val steps = 20
            val delayMs = durationMs / steps
            for (i in 0..steps) {
                val volume = targetVolume * (i.toFloat() / steps)
                mediaPlayer?.setVolume(volume, volume)
                delay(delayMs)
            }
        }
    }

    fun setVolume(volume: Float) {
        targetVolume = volume
        mediaPlayer?.setVolume(volume, volume)
    }

    fun stop() {
        fadeJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isFadingOut = false
    }
}