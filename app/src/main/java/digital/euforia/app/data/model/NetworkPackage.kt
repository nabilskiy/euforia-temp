package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkPackage(
    @field:Json(name = "class") val type: String, // "package"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "pro") val pro: Boolean,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "author_id") val authorId: Int?,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "subtitle") val subtitle: String?,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "keywords") val keywords: String?,

    @field:Json(name = "image_url") val imageUrl: String?,
    @field:Json(name = "image_preview_url") val imagePreviewUrl: String?,
    @field:Json(name = "image_cover_url") val imageCoverUrl: String?,
    @field:Json(name = "video_cover_url") val videoCoverUrl: String?,

    @field:Json(name = "color_1") val color1: String?,
    @field:Json(name = "color_2") val color2: String?,
    @field:Json(name = "color_3") val color3: String?,

    @field:Json(name = "published_at") val publishedAt: Long?,

    @field:Json(name = "meditations") val meditations: List<NetworkMeditation> = emptyList(),
    @field:Json(name = "exercises") val exercises: List<NetworkExercise> = emptyList(),
    @field:Json(name = "articles") val articles: List<NetworkArticle> = emptyList(),
)

fun NetworkPackage.toEntity(): digital.euforia.app.data.db.entity.PackageEntity =
    digital.euforia.app.data.db.entity.PackageEntity(
        id = id,
        type = type,
        pro = pro,
        alias = alias,
        authorId = authorId,
        name = name,
        subtitle = subtitle,
        description = description,
        keywords = keywords,
        imageUrl = imageUrl,
        imagePreviewUrl = imagePreviewUrl,
        imageCoverUrl = imageCoverUrl,
        videoCoverUrl = videoCoverUrl,
        color1 = color1,
        color2 = color2,
        color3 = color3,
        publishedAt = publishedAt,
    )
