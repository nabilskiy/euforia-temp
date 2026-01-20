package digital.euforia.app.domain.usecase.program

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
    private val articleRepository: ArticleRepository,
    private val exerciseRepository: ExerciseRepository,
    private val meditationRepository: MeditationRepository,
    private val publicationInfoMapper: PublicationInfoMapper,
) {
    suspend operator fun invoke(
        publicationType: PublicationType,
        id: Int
    ): ResultWrapper<PublicationInfo> {
        return withContext(Dispatchers.IO) {
            when (publicationType) {
                PublicationType.ARTICLE -> articleRepository.getById(id)
                    .map { publicationInfoMapper.fromArticle(it) }

                PublicationType.EXERCISE -> exerciseRepository.getById(id).map {
                    publicationInfoMapper.fromExercise(it)
                }

                PublicationType.MEDITATION -> meditationRepository.getById(id).map {
                    publicationInfoMapper.fromMeditation(it)
                }
            }
        }
    }
}