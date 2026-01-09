package digital.euforia.app.domain.usecase.feedback

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.FeedbackFormWithQuestions
import digital.euforia.app.data.repository.FeedbackFormRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFeedbackFormUseCase @Inject constructor(
    private val repository: FeedbackFormRepository,
    private val configFetcher: EuforiaRemoteConfigFetcher
) {
    /**
     * Returns a feedback form with all related questions and options from the local DB.
     * Preferred selection is by [id] when provided; otherwise by [alias] (default: "feedback").
     * This use case is one-shot (does not return Flow), similar to GetFAQCategoriesUseCase.
     */
    suspend operator fun invoke(): FeedbackFormWithQuestions? {
        return withContext(Dispatchers.IO) {
            val id = configFetcher.getFeedbackFormId()
            if (id != null) {
                repository.getFormById(id.toInt())
            } else null
        }
    }
}
