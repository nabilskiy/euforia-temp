package digital.euforia.app.data.repository

import digital.euforia.app.data.db.dao.FeedbackFormDao
import digital.euforia.app.data.db.entity.FeedbackFormWithQuestions
import digital.euforia.app.data.model.NetworkFeedbackForm
import digital.euforia.app.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackFormRepository @Inject constructor(
    private val feedbackFormDao: FeedbackFormDao,
) {

    fun getFormByAliasFlow(alias: String): Flow<FeedbackFormWithQuestions?> =
        feedbackFormDao.getFormWithQuestionsByAliasFlow(alias)

    fun getFormByIdFlow(id: Int): Flow<FeedbackFormWithQuestions?> =
        feedbackFormDao.getFormWithQuestionsByIdFlow(id)

    fun getFormByAlias(alias: String): FeedbackFormWithQuestions? =
        feedbackFormDao.getFormWithQuestionsByAlias(alias)

    fun getFormById(id: Int): FeedbackFormWithQuestions? =
        feedbackFormDao.getFormWithQuestionsById(id)

    /**
     * Save or replace an entire feedback form graph from the network.
     */
    suspend fun upsertFromNetwork(network: NetworkFeedbackForm) {
        val formEntity = network.toEntity()
        val questionEntities = network.items.map { it.toEntity(network.id) }
        val optionEntities = network.items.flatMap { q ->
            (q.items ?: emptyList()).map { it.toEntity(q.id) }
        }
        feedbackFormDao.replaceForm(
            form = formEntity,
            questions = questionEntities,
            options = optionEntities,
        )
    }
}
