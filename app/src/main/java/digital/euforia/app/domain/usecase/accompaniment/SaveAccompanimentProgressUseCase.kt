package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.data.repository.AccompanimentItemRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import kotlin.time.ExperimentalTime

class SaveAccompanimentProgressUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val repository: AccompanimentItemRepository,
    private val configFetcher: EuforiaRemoteConfigFetcher
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(
        accompanimentWithItems: AccompanimentWithItems,
        playbackProgress: Float,
        timeOfDay: TimeOfDay
    ) {
        withContext(Dispatchers.IO) {
            val isDemo = profilePreferences.getIsDemo()
            val completionConfig = configFetcher.getDemoUnlockDayConfig()
            val accompanimentItem = accompanimentWithItems.items.first { it.timeOfDay == timeOfDay }

            if (playbackProgress >= completionConfig.completionItemPercentage) {
                when (timeOfDay) {
                    TimeOfDay.DAYTIME -> {
                        val completedCount = completionConfig.completedCount
                        val currentPhrase =
                            accompanimentWithItems.accompaniment.phrases.firstOrNull { it.id !in accompanimentItem.viewedPhraseId }
                        if (currentPhrase != null) {
                            val completedPhrases =
                                accompanimentItem.viewedPhraseId + currentPhrase.id
                            val isCompleted = if (!accompanimentItem.isCompleted) {
                                completedPhrases.size >= completedCount
                            } else {
                                true
                            }

                            val updatedAccompanimentItem = accompanimentItem.copy(
                                viewedPhraseId = completedPhrases,
                                isCompleted = isCompleted,
                                viewCount = accompanimentItem.viewCount + 1,
                                viewedAt = if (isCompleted) Instant.now()
                                    .toEpochMilli() else accompanimentItem.viewedAt
                            )

                            Timber.tag("SavingProgress").d(updatedAccompanimentItem.toString())
                            repository.update(updatedAccompanimentItem)
//                                updateAccompanimentItemUseCase.invoke(updateAccompanimentItemUseCase)
                        } else {
                            val updatedAccompanimentItem = accompanimentItem.copy(
                                viewedPhraseId = emptyList(),
                            )
                            repository.update(updatedAccompanimentItem)
                        }
                    }

                    else -> {
                        val updatedItem = if (accompanimentItem.isCompleted) {
                            accompanimentItem.copy(
                                viewCount = accompanimentItem.viewCount + 1
                            )
                        } else {
                            accompanimentItem.copy(
                                isCompleted = true,
                                viewCount = accompanimentItem.viewCount + 1,
                                viewedAt = Instant.now().toEpochMilli()
                            )

                        }
                        Timber.tag("SavingProgress").d(updatedItem.toString())
                        repository.update(updatedItem)
                    }
                }
            }
        }
    }
}