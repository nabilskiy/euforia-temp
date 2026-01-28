package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.repository.ArticleRepository
import digital.euforia.app.data.repository.ExerciseRepository
import digital.euforia.app.data.repository.MeditationRepository
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPublicationInfoUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val publicationInfoMapper: PublicationInfoMapper,
) {
    suspend operator fun invoke(
        publicationType: PublicationType,
        id: Int
    ): ResultWrapper<PublicationInfo> {
        return withContext(Dispatchers.IO) {
            when (publicationType) {
                PublicationType.ARTICLE -> api.getArticle(id).map { networkArticle ->
                    publicationInfoMapper.fromNetworkArticle(networkArticle)
                }

                PublicationType.EXERCISE -> api.getExercise(id).map { networkExercise ->
                    publicationInfoMapper.fromNetworkExercise(networkExercise)
                }

                PublicationType.MEDITATION -> api.getMeditation(id).map { networkMeditation ->
                    publicationInfoMapper.fromNetworkMeditation(networkMeditation)
                }
            }
        }
    }
}