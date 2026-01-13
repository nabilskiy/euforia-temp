package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.domain.util.ResultWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: EuforiaApi,
    private val articleDao: ArticleDao
) {
    suspend fun getArticleContent(articleId: Int): ResultWrapper<String> {
        return api.getArticleContent(articleId).map { responseBody ->
            responseBody.string()
        }
    }
}