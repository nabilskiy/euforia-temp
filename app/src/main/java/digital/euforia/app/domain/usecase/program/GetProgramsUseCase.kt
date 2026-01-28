package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.mapper.program.toProgramUi
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.config.BlockType
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.ProgramUi
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetProgramsUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val publicationInfoMapper: PublicationInfoMapper,
    private val configFetcher: EuforiaRemoteConfigFetcher
) {
    suspend operator fun invoke(): ResultWrapper<ProgramsResult> {
        val exercises = mutableListOf<PublicationInfo>()
        val articles = mutableListOf<PublicationInfo>()
        val programsConfig = configFetcher.getProgramsConfig()
        val articlesConfig = programsConfig.firstOrNull { it.type == BlockType.ARTICLE_LIST }
        val exercisesConfig = programsConfig.firstOrNull { it.type == BlockType.EXERCISE_LIST }
        val maxArticles = exercisesConfig?.data?.maxItems ?: 12
        val maxExercises = articlesConfig?.data?.maxItems ?: 12

        return withContext(Dispatchers.IO) {
            api.getAllPackages().map { packages ->
                ProgramsResult(
                    programList = packages.map { pkg ->
                        exercises.addAll(pkg.exercises.map { networkExercise ->
                            publicationInfoMapper.fromNetworkExercise(networkExercise)
                        })
                        articles.addAll(pkg.articles.map { networkArticle ->
                            publicationInfoMapper.fromNetworkArticle(networkArticle)
                        })
                        pkg.toProgramUi()
                    },
                    exercisesList = exercises.distinctBy { it.id }.shuffled().take(maxExercises),
                    articlesList = articles.distinctBy { it.id }.shuffled().take(maxArticles),
                )
            }
        }
    }
}

data class ProgramsResult(
    val programList: List<ProgramUi>,
    val exercisesList: List<PublicationInfo>,
    val articlesList: List<PublicationInfo>,
)
