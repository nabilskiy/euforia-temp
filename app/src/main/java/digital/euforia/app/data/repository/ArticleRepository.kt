package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.db.dao.ArticleDao
import digital.euforia.app.data.model.toEntity
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: EuforiaApi,
    private val articleDao: ArticleDao,
    private val publicationInfoMapper: PublicationInfoMapper,

    ) {
    suspend fun getArticleContent(articleId: Int): ResultWrapper<String> {
        return api.getArticleContent(articleId).map { responseBody ->
            responseBody.string()
        }
    }

    suspend fun getArticleById(articleId: Int): ResultWrapper<PublicationInfo> {
        return withContext(Dispatchers.IO) {
            val localArticle = articleDao.getById(articleId)
            if (localArticle != null) {
                ResultWrapper.Success(publicationInfoMapper.fromArticle(localArticle))
            } else {
                api.getArticles(ids = articleId.toString())
                    .map { networkArticles ->
                        articleDao.upsertAll(networkArticles.map {
                            it.toEntity()
                        })
                        publicationInfoMapper.fromNetworkArticle(networkArticles.first())
                    }
            }
        }
    }
}