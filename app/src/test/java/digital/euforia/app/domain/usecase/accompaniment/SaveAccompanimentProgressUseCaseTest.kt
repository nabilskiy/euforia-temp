package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.*
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.DemoUnlockDayConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SaveAccompanimentProgressUseCaseTest {

    private val appPreferences = mockk<AppPreferences>(relaxed = true)
    private val profilePreferences = mockk<ProfilePreferences>(relaxed = true)
    private val repository = mockk<AccompanimentItemRepository>(relaxed = true)
    private val accompanimentRepository = mockk<AccompanimentRepository>(relaxed = true)
    private val configFetcher = mockk<EuforiaRemoteConfigFetcher>(relaxed = true)

    private lateinit var useCase: SaveAccompanimentProgressUseCase

    @Before
    fun setUp() {
        useCase = SaveAccompanimentProgressUseCase(
            appPreferences,
            profilePreferences,
            repository,
            accompanimentRepository,
            configFetcher
        )
    }

    @Test
    fun `should complete MORNING item when progress is enough`() = runBlocking {
        val timeOfDay = TimeOfDay.MORNING
        val accompaniment = createAccompaniment()
        val item = AccompanimentItem(id = 1, accompanimentId = 1, timeOfDay = timeOfDay, isCompleted = false)
        val accompanimentWithItems = AccompanimentWithItems(accompaniment, listOf(item))
        val completionConfig = DemoUnlockDayConfig(completionItemPercentage = 0.8f)

        coEvery { configFetcher.getDemoUnlockDayConfig() } returns completionConfig
        coEvery { profilePreferences.getIsDemo() } returns false

        useCase(accompanimentWithItems, 0.9f, timeOfDay)

        coVerify {
            repository.update(match { it.isCompleted && it.timeOfDay == timeOfDay })
        }
    }

    @Test
    fun `should complete DAYTIME item when phrases are completed`() = runBlocking {
        val timeOfDay = TimeOfDay.DAYTIME
        val phrases = listOf(
            Phrase(id = 1, type = "type", name = "name", daytimeTitle = "title", description = "desc", musicFileUrl = null, maleAudioUrl = null, femaleAudioUrl = null, music = null),
            Phrase(id = 2, type = "type", name = "name", daytimeTitle = "title", description = "desc", musicFileUrl = null, maleAudioUrl = null, femaleAudioUrl = null, music = null)
        )
        val accompaniment = createAccompaniment().copy(phrases = phrases)
        val item = AccompanimentItem(id = 1, accompanimentId = 1, timeOfDay = timeOfDay, isCompleted = false, viewedPhraseId = listOf(1))
        val accompanimentWithItems = AccompanimentWithItems(accompaniment, listOf(item))
        val completionConfig = DemoUnlockDayConfig(completedCount = 2, completionItemPercentage = 0.8f)

        coEvery { configFetcher.getDemoUnlockDayConfig() } returns completionConfig
        coEvery { profilePreferences.getIsDemo() } returns false

        useCase(accompanimentWithItems, 0.9f, timeOfDay)

        coVerify {
            repository.update(match { it.isCompleted && it.viewedPhraseId == listOf(1, 2) })
        }
    }

    @Test
    fun `DAYTIME item should NOT complete if all phrases are already viewed but item is not completed`() = runBlocking {
        // This is the bug I suspect.
        val timeOfDay = TimeOfDay.DAYTIME
        val phrases = listOf(
            Phrase(id = 1, type = "type", name = "name", daytimeTitle = "title", description = "desc", musicFileUrl = null, maleAudioUrl = null, femaleAudioUrl = null, music = null)
        )
        val accompaniment = createAccompaniment().copy(phrases = phrases)
        val item = AccompanimentItem(id = 1, accompanimentId = 1, timeOfDay = timeOfDay, isCompleted = false, viewedPhraseId = listOf(1))
        val accompanimentWithItems = AccompanimentWithItems(accompaniment, listOf(item))
        val completionConfig = DemoUnlockDayConfig(completedCount = 1, completionItemPercentage = 0.8f)

        coEvery { configFetcher.getDemoUnlockDayConfig() } returns completionConfig
        coEvery { profilePreferences.getIsDemo() } returns false

        useCase(accompanimentWithItems, 0.9f, timeOfDay)

        // It should be completed now, but with current logic it might just reset viewedPhraseId
        coVerify {
            repository.update(match { it.isCompleted })
        }
    }

    private fun createAccompaniment() = Accompaniment(
        id = 1,
        type = "type",
        demo = false,
        name = "name",
        morningTitle = "morning",
        daytimeTitle = "daytime",
        eveningTitle = "evening",
        description = "desc",
        morningImageUrl = null,
        daytimeImageUrl = null,
        eveningImageUrl = null,
        morningMusicUrl = null,
        daytimeMusicUrl = null,
        eveningMusicUrl = null,
        morningMaleAudioUrl = null,
        morningFemaleAudioUrl = null,
        daytimeMaleAudioUrl = null,
        daytimeFemaleAudioUrl = null,
        eveningMaleAudioUrl = null,
        eveningFemaleAudioUrl = null,
        morningMusic = null,
        daytimeMusic = null,
        eveningMusic = null,
        morningMaleAudio = null,
        morningFemaleAudio = null,
        daytimeMaleAudio = null,
        daytimeFemaleAudio = null,
        eveningMaleAudio = null,
        eveningFemaleAudio = null,
        morningContent = "morning",
        daytimeContent = "daytime",
        eveningContent = "evening",
        phrases = emptyList(),
        publishedAt = 0L
    )
}
