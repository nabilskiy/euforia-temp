package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.mapper.program.toProgramUi
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.ProgramUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetProgramDetailsUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val publicationInfoMapper: PublicationInfoMapper,
) {

    suspend operator fun invoke(id: Int): ResultWrapper<ProgramDetailsResult> {
        return withContext(Dispatchers.IO) {
            api.getPackage(id).map { networkPackage ->
                val program = networkPackage.toProgramUi()
                val articlesList = networkPackage.articles.map { networkArticle ->
                    publicationInfoMapper.fromNetworkArticle(networkArticle)
                }
                val exercisesList = networkPackage.exercises.map { networkExercise ->
                    publicationInfoMapper.fromNetworkExercise(networkExercise)
                }
                val meditationsList = networkPackage.meditations.map { networkMeditation ->
                    publicationInfoMapper.fromNetworkMeditation(networkMeditation)
                }
                ProgramDetailsResult(
                    program = program,
                    exercisesList = exercisesList,
                    articlesList = articlesList,
                    meditationsList = meditationsList,
                )
            }
        }
    }
}


data class ProgramDetailsResult(
    val program: ProgramUi,
    val exercisesList: List<PublicationInfo>,
    val articlesList: List<PublicationInfo>,
    val meditationsList: List<PublicationInfo>,
)
