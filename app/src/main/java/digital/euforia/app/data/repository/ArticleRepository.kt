package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.data.db.entity.Article
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: EuforiaApi,
    private val articleDao: ArticleDao,
) {
    suspend fun getArticleContent(articleId: Int): ResultWrapper<String> {
        return api.getArticleContent(articleId).map { responseBody ->
            responseBody.string()
        }
    }

    suspend fun getById(articleId: Int): ResultWrapper<Article> {
        return withContext(Dispatchers.IO) {
            val localArticle = articleDao.getById(articleId)
            if (localArticle != null) {
                ResultWrapper.Success(localArticle)
            } else {
                api.getArticle(articleId)
                    .map { networkArticles ->
                        networkArticles.toEntity().also { articleDao.upsert(it) }
                    }
            }
        }
    }
}