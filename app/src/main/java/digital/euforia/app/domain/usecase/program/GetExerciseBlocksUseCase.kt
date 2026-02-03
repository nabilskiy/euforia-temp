package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.model.config.ExerciseBlockType
import digital.euforia.app.domain.model.config.ExerciseConfig
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.publication.PublicationType
import javax.inject.Inject

class GetExerciseBlocksUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val configFetcher: EuforiaRemoteConfigFetcher,
    private val publicationInfoMapper: PublicationInfoMapper
) {

    suspend operator fun invoke(): ResultWrapper<List<ExerciseUiBlock>> {
        val config = configFetcher.getExercisesConfig()
        val blocksList = config.mapNotNull { exerciseConfig ->
            when (exerciseConfig.type) {
                ExerciseBlockType.EXERCISE_LIST -> processListItem(exerciseConfig)
                ExerciseBlockType.BANNER -> processBanner(exerciseConfig)
                ExerciseBlockType.EXERCISE_CATEGORY -> processExerciseCategory(exerciseConfig)
                ExerciseBlockType.EXERCISE -> processExercise(exerciseConfig)
                ExerciseBlockType.DIVIDER -> ExerciseUiBlock.Divider
                ExerciseBlockType.EXERCISE_PREMIUM -> processPremiumExercises(exerciseConfig)
                else -> null
            }
        }
        return if (blocksList.isNotEmpty()) {
            ResultWrapper.Success(blocksList)
        } else {
            ResultWrapper.Failure(Exception("No exercise blocks found"))
        }
    }

    private suspend fun processListItem(config: ExerciseConfig): ExerciseUiBlock.ExerciseList? {
        if (config.entityIds.isNullOrEmpty()) return null
        val idsParam = config.entityIds.joinToString(",")
        val networkExercises = api.getExercises(ids = idsParam).dataOrNull
        val publicationInfosList = networkExercises?.map { networkExercise ->
            publicationInfoMapper.fromNetworkExercise(networkExercise)
        }
        return if (publicationInfosList.isNullOrEmpty()) {
            null
        } else {
            ExerciseUiBlock.ExerciseList(
                publicationInfoList = publicationInfosList,
                title = config.data?.title.orEmpty(),
                description = config.data?.description.orEmpty(),
                maxItems = config.data?.maxItems ?: 3
            )
        }
    }

    private suspend fun processBanner(config: ExerciseConfig): ExerciseUiBlock.Banner? {
        if (config.entity == null) return null
        val actionUrl = config.entity.actionUrl ?: return null
        if (actionUrl.contains("ids=")) {
            val ids = actionUrl
                .substringAfter("ids=")
                .split(",")
                .mapNotNull { it.toIntOrNull() }
            return ExerciseUiBlock.Banner(
                imageUrl = config.entity.imageUrl.orEmpty(),
                ids = ids,
                id = null,
                actionUrl = config.entity.actionUrl
            )
        } else {
            val id = actionUrl
                .substringAfterLast("/")
                .toIntOrNull()
            return ExerciseUiBlock.Banner(
                imageUrl = config.entity.imageUrl.orEmpty(),
                ids = emptyList(),
                id = id,
                actionUrl = config.entity.actionUrl
            )
        }
    }

    private suspend fun processExerciseCategory(config: ExerciseConfig): ExerciseUiBlock.Category? {
        return config.entityId?.let { categoryId ->
            val networkCategory = api.getCategory(
                type = PublicationType.EXERCISE.name.lowercase(),
                id = categoryId
            ).dataOrNull

            val publicationInfosList = networkCategory?.exercises?.map { networkExercise ->
                publicationInfoMapper.fromNetworkExercise(networkExercise)
            }

            return if (publicationInfosList.isNullOrEmpty()) {
                null
            } else {
                ExerciseUiBlock.Category(
                    title = config.data?.title.orEmpty(),
                    publicationInfoList = publicationInfosList
                )
            }
        }
    }

    private suspend fun processExercise(config: ExerciseConfig): ExerciseUiBlock.Exercise? {
        return config.entityId?.let { exerciseId ->
            val networkExercise = api.getExercise(exerciseId).dataOrNull
            if (networkExercise != null) {
                val publicationInfo = publicationInfoMapper.fromNetworkExercise(networkExercise)
                ExerciseUiBlock.Exercise(
                    publicationInfo = publicationInfo,
                    title = config.data?.title.orEmpty(),
                    description = config.data?.description.orEmpty()
                )
            } else {
                null
            }
        }
    }

    private suspend fun processPremiumExercises(config: ExerciseConfig): ExerciseUiBlock.ExercisePremium? {
        if (config.entityIds.isNullOrEmpty()) return null
        val idsParam = config.entityIds.joinToString(",")
        val networkExercises = api.getExercises(ids = idsParam, pro = "pro").dataOrNull
        val publicationInfosList = networkExercises?.map { networkExercise ->
            publicationInfoMapper.fromNetworkExercise(networkExercise)
        }
        return if (publicationInfosList.isNullOrEmpty()) {
            null
        } else {
            ExerciseUiBlock.ExercisePremium(
                publicationInfoList = publicationInfosList,
                title = config.data?.title.orEmpty(),
                maxItems = config.data?.maxItems ?: 3
            )
        }
    }
}

sealed class ExerciseUiBlock {
    data class ExerciseList(
        val publicationInfoList: List<PublicationInfo>,
        val title: String,
        val description: String,
        val maxItems: Int
    ) : ExerciseUiBlock()

    data class Banner(
        val imageUrl: String,
        val ids: List<Int>,
        val id: Int?,
        val actionUrl: String?
    ) : ExerciseUiBlock()

    data class Category(
        val title: String,
        val publicationInfoList: List<PublicationInfo>,
    ) : ExerciseUiBlock()

    data class Exercise(
        val publicationInfo: PublicationInfo,
        val title: String,
        val description: String
    ) : ExerciseUiBlock()

    data object Divider : ExerciseUiBlock()

    data class ExercisePremium(
        val publicationInfoList: List<PublicationInfo>,
        val title: String,
        val maxItems: Int
    ) : ExerciseUiBlock()

    data class ExerciseGroup(
        val id: Int,
        val title: String,
        val items: List<ExerciseUiBlock>
    ) : ExerciseUiBlock()
}