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

class GetPublicationInfosUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val publicationInfoMapper: PublicationInfoMapper
) {

    suspend operator fun invoke(
        publicationType: PublicationType,
        ids: List<Int>
    ): ResultWrapper<List<PublicationInfo>> {
        val idParam = ids.joinToString(",")
        return withContext(Dispatchers.IO) {
            when (publicationType) {
                PublicationType.ARTICLE -> api.getArticles(idParam).map { networkArticles ->
                    networkArticles.map { networkArticle ->
                        publicationInfoMapper.fromNetworkArticle(networkArticle)
                    }
                }

                PublicationType.EXERCISE -> api.getExercises(idParam).map { networkExercises ->
                    networkExercises.map { networkExercise ->
                        publicationInfoMapper.fromNetworkExercise(networkExercise)
                    }
                }

                PublicationType.MEDITATION -> api.getMeditations(idParam)
                    .map { networkMeditations ->
                        networkMeditations.map { networkMeditation ->
                            publicationInfoMapper.fromNetworkMeditation(networkMeditation)
                        }
                    }
            }
        }
    }
}