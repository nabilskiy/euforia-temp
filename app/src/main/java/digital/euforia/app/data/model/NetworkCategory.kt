package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkCategory(
    @field:Json(name = "class") val type: String,
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "meditations") val meditations: List<NetworkMeditation> = emptyList(),
    @field:Json(name = "exercises") val exercises: List<NetworkExercise> = emptyList(),
    @field:Json(name = "articles") val articles: List<NetworkArticle> = emptyList(),
    @field:Json(name = "scenes") val scenes: List<NetworkScene> = emptyList(),
    @field:Json(name = "position") val position: Int? = null,
)