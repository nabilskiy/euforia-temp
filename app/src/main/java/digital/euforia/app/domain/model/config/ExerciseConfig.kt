package digital.euforia.app.domain.model.config

import digital.euforia.app.data.config.NetworkExerciseConfig
import digital.euforia.app.data.config.NetworkExerciseData
import digital.euforia.app.data.config.NetworkExerciseEntity

data class ExerciseConfig(
    val type: ExerciseBlockType,
    val entityId: Int? = null,
    val entityIds: List<Int>? = null,
    val entity: ExerciseEntity? = null,
    val data: ExerciseData? = null
)

data class ExerciseEntity(
    val imageUrl: String? = null,
    val actionUrl: String? = null
)

data class ExerciseData(
    val title: String? = null,
    val description: String? = null,
    val maxItems: Int? = null
)

enum class ExerciseBlockType {
    EXERCISE_LIST,
    BANNER,
    EXERCISE_CATEGORY,
    EXERCISE,
    DIVIDER,
    EXERCISE_PREMIUM,
    UNKNOWN
}

fun NetworkExerciseConfig.toDomain(): ExerciseConfig {
    return ExerciseConfig(
        type = type.toExerciseBlockType(),
        entityId = entityId,
        entityIds = entityIds,
        entity = entity?.toDomain(),
        data = data?.toDomain()
    )
}

fun NetworkExerciseEntity.toDomain(): ExerciseEntity {
    return ExerciseEntity(
        imageUrl = imageUrl,
        actionUrl = actionUrl
    )
}

fun NetworkExerciseData.toDomain(): ExerciseData {
    return ExerciseData(
        title = title,
        description = description,
        maxItems = maxItems
    )
}

fun String.toExerciseBlockType(): ExerciseBlockType = when (this) {
    "exercise_list" -> ExerciseBlockType.EXERCISE_LIST
    "banner" -> ExerciseBlockType.BANNER
    "exercise_category" -> ExerciseBlockType.EXERCISE_CATEGORY
    "exercise" -> ExerciseBlockType.EXERCISE
    "divider" -> ExerciseBlockType.DIVIDER
    "exercise_premium" -> ExerciseBlockType.EXERCISE_PREMIUM
    else -> ExerciseBlockType.UNKNOWN
}