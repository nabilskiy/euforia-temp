package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.File

@JsonClass(generateAdapter = true)
data class NetworkFile(
    @field:Json(name = "class") val type: String,
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "ext") val ext: String,
    @field:Json(name = "size") val size: Int,
    @field:Json(name = "duration") val duration: Int
)

fun NetworkFile.toEntity(): File = File(
    id = id,
    type = type,
    url = url,
    ext = ext,
    size = size,
    duration = duration,
)