package digital.euforia.app.service.soundscapes

import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.model.NetworkSceneMusic
import digital.euforia.app.data.model.NetworkSceneSound
import digital.euforia.app.data.model.NetworkVideoFile
import org.junit.Assert.assertEquals
import org.junit.Test

class SoundscapeDownloadsProcessorTest {
    @Test
    fun `buildOfflineAssetUrls includes sounds music and video deduplicated`() {
        val scene = NetworkScene(
            id = 1,
            alias = "a",
            name = "Scene",
            description = null,
            imageUrl = null,
            videoUrl = "https://cdn/video.mp4",
            musicId = null,
            sceneMusics = listOf(NetworkSceneMusic(musicFileUrl = "https://cdn/music.mp3")),
            sceneSounds = listOf(
                NetworkSceneSound(soundFileUrl = "https://cdn/s1.mp3"),
                NetworkSceneSound(soundFileUrl = "https://cdn/s1.mp3"),
            ),
            video = NetworkVideoFile(url = "https://cdn/video.mp4")
        )

        val urls = buildOfflineAssetUrls(scene)
        assertEquals(
            listOf("https://cdn/s1.mp3", "https://cdn/music.mp3", "https://cdn/video.mp4"),
            urls
        )
    }
}

