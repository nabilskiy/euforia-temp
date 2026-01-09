package digital.euforia.app.ui.util

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool

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
        mediaPlayer = MediaPlayer.create(context, soundRes).apply {
            setOnCompletionListener {
                onCompletion?.invoke()
                release() // автоматично очищаємо після відтворення
            }
            start()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

object BackgroundPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playLooping(context: Context, soundRes: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundRes).apply {
            setVolume(0.3f, 0.3f)
            isLooping = true
            start()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}