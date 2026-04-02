package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.File

@JsonClass(generateAdapter = true)
data class NetworkFile(
    @field:Json(name = "class") val type: String? = null,
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "ext") val ext: String? = null,
    @field:Json(name = "size") val size: Int? = null,
    @field:Json(name = "duration") val duration: Int? = null,
)

fun NetworkFile.toEntity(): File = File(
    id = requireNotNull(id) { "file id is required" },
    type = type.orEmpty(),
    url = url.orEmpty(),
    ext = ext.orEmpty(),
    size = size ?: 0,
    duration = duration ?: 0,
)