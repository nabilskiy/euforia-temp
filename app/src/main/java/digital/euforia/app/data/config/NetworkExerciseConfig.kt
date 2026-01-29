package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkExerciseConfig(
    @Json(name = "type")
    val type: String,

    @Json(name = "entityId")
    val entityId: Int? = null,

    @Json(name = "entityIds")
    val entityIds: List<Int>? = null,

    @Json(name = "entity")
    val entity: NetworkExerciseEntity? = null,

    @Json(name = "data")
    val data: NetworkExerciseData? = null,

    @Json(name = "filters")
    val filters: Any? = null
)

@JsonClass(generateAdapter = true)
data class NetworkExerciseEntity(
    @Json(name = "img_url")
    val imageUrl: String? = null,

    @Json(name = "action_url")
    val actionUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkExerciseData(
    @Json(name = "title")
    val title: String? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "maxItems")
    val maxItems: Int? = null
)
