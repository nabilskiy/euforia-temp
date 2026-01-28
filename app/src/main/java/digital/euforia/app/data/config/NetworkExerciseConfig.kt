package digital.euforia.app.data.config

import com.squareup.moshi.Json

data class NetworkExerciseConfig(
    @field:Json(name = "type")
    val type: String,

    @field:Json(name = "entityId")
    val entityId: Int? = null,

    @field:Json(name = "entityIds")
    val entityIds: List<Int>? = null,

    @field:Json(name = "entity")
    val entity: NetworkExerciseEntity? = null,

    @field:Json(name = "data")
    val data: NetworkExerciseData? = null,

    @field:Json(name = "filters")
    val filters: Any? = null
)

data class NetworkExerciseEntity(
    @field:Json(name = "img_url")
    val imageUrl: String? = null,

    @field:Json(name = "action_url")
    val actionUrl: String? = null
)

data class NetworkExerciseData(
    @field:Json(name = "title")
    val title: String? = null,

    @field:Json(name = "description")
    val description: String? = null,

    @field:Json(name = "maxItems")
    val maxItems: Int? = null
)
