package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.model.FeedbackRequest
import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendRatingCommentUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val profilePreferences: ProfilePreferences,
) {

    suspend operator fun invoke(
        accompanimentWithItems: AccompanimentWithItems,
        timeOfDay: TimeOfDay,
        title: String,
        phraseId: Int,
        rating: Int,
        isDemo: Boolean,
        comment: String?,
    ) {
        withContext(Dispatchers.IO) {
            val length = comment?.trim()?.length ?: 0
            if (length >= 5) {
                val currentItem =
                    accompanimentWithItems.items.firstOrNull { it.timeOfDay == timeOfDay }
                val accompanimentItem = currentItem ?: return@withContext
                val phraseId =
                    if (timeOfDay == TimeOfDay.DAYTIME) accompanimentItem.viewedPhraseId.last() else null
                val details = "rating $rating\n" +
                        "accompaniment_id ${accompanimentItem.id}\n" +
                        "accompaniment_title $title" +
                        "phrase_id $phraseId\n" +
                        "time_of_day $timeOfDay\n" +
                        "is_demo $isDemo"
                api.sendFeedback(
                    FeedbackRequest(
                        type = "rate",
                        message = comment.orEmpty(),
                        details = details,
                        email = profilePreferences.getEmail().orEmpty(),
                        name = profilePreferences.getName().orEmpty(),
                    )
                )
            }
        }
    }
}