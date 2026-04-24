package digital.euforia.app.domain.usecase

import android.net.Uri
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.ui.navigation.HomeDestination
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ParseDeepLinkUseCaseTest {
    private val accompanimentRepository = mockk<AccompanimentRepository>(relaxed = true)
    private val profilePreferences = mockk<ProfilePreferences>(relaxed = true)
    private val useCase = ParseDeepLinkUseCase(accompanimentRepository, profilePreferences)

    @Test
    fun `scenes root deep link opens soundscapes root`() = runBlocking {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "euforia"
        every { uri.host } returns "scenes"
        every { uri.pathSegments } returns emptyList()
        every { uri.getQueryParameter(any()) } returns null
        val destination = useCase(uri)
        assertEquals(HomeDestination.Soundscapes, destination)
    }

    @Test
    fun `scene deep link opens scene screen`() = runBlocking {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "euforia"
        every { uri.host } returns "scenes"
        every { uri.pathSegments } returns listOf("123")
        every { uri.getQueryParameter(any()) } returns null
        val destination = useCase(uri)
        assertEquals(HomeDestination.SoundscapesScene(sceneId = 123), destination)
    }

    @Test
    fun `playlist deep link opens playlist screen`() = runBlocking {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "euforia"
        every { uri.host } returns "playlists"
        every { uri.pathSegments } returns listOf("77")
        every { uri.getQueryParameter(any()) } returns null
        val destination = useCase(uri)
        assertEquals(HomeDestination.SoundscapesPlaylist(playlistId = 77), destination)
    }
}

