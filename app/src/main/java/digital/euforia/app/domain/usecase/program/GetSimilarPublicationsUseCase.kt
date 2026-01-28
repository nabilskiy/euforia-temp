package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSimilarPublicationsUseCase @Inject constructor(
    private val publicationInfoMapper: PublicationInfoMapper,
    private val api: EuforiaApi
) {
    suspend operator fun invoke(
        publicationType: PublicationType,
        categoryId: Int,
        excludePublicationId: Int,
        limit: Int = 30
    ): ResultWrapper<List<PublicationInfo>> {
        return withContext(Dispatchers.IO) {
            api.getCategory(
                type = publicationType.name.lowercase(),
                id = categoryId
            ).map { networkCategory ->

                when (publicationType) {
                    PublicationType.EXERCISE -> {
                        networkCategory.exercises
                            .map { networkExercise ->
                                publicationInfoMapper.fromNetworkExercise(networkExercise)
                            }
                    }

                    PublicationType.ARTICLE -> {
                        networkCategory.articles
                            .map { networkArticle ->
                                publicationInfoMapper.fromNetworkArticle(networkArticle)
                            }
                    }

                    PublicationType.MEDITATION -> {
                        networkCategory.meditations
                            .map { networkMeditation ->
                                publicationInfoMapper.fromNetworkMeditation(networkMeditation)
                            }
                    }
                }
                    .filter { it.id != excludePublicationId }
                    .shuffled()
                    .take(limit)

            }
        }
    }
}