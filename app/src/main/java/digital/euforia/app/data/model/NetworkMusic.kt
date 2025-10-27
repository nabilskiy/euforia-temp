package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.Music

@JsonClass(generateAdapter = true)
data class NetworkMusic(
    @field:Json(name = "class") val type: String,          // "music"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "type") val contentType: String,     // e.g. "scene"
    @field:Json(name = "category_id") val categoryId: Int,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "file_url") val fileUrl: String,
    @field:Json(name = "image_url") val imageUrl: String,
    @field:Json(name = "file") val file: NetworkFile?
)

fun NetworkMusic.toEntity(): Music = Music(
    id = id,
    type = type,
    contentType = contentType,
    alias = alias,
    categoryId = categoryId,
    name = name,
    description = description,
    fileUrl = fileUrl,
    imageUrl = imageUrl,
    file = file?.toEntity(),
)
