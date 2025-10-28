package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.Resource

@JsonClass(generateAdapter = true)
data class NetworkResource(
    @field:Json(name = "class") val type: String, // "resource"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "author_id") val authorId: Int?,
    @field:Json(name = "pro") val pro: Boolean,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "subtitle") val subtitle: String?,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "keywords") val keywords: String?,
    @field:Json(name = "published_at") val publishedAt: Long?,
    @field:Json(name = "file_url") val fileUrl: String?,
    @field:Json(name = "file") val file: NetworkResourceFile?,
    @field:Json(name = "preview_url") val previewUrl: String?,
    @field:Json(name = "preview") val preview: NetworkResourceFile?,
    @field:Json(name = "category_id") val categoryId: Int?,
    @field:Json(name = "class_id") val classId: Int?,
    @field:Json(name = "class_alias") val classAlias: String?,
    @field:Json(name = "entity") val entity: String?,
    @field:Json(name = "options") val options: String?,
    @field:Json(name = "min_app_version") val minAppVersion: Int?,
)

fun NetworkResource.toEntity(): Resource = Resource(
    id = id,
    type = type,
    alias = alias,
    authorId = authorId,
    pro = pro,
    name = name,
    subtitle = subtitle,
    description = description,
    keywords = keywords,
    publishedAt = publishedAt,
    fileUrl = fileUrl,
    previewUrl = previewUrl,
    categoryId = categoryId,
    classId = classId,
    classAlias = classAlias,
    entity = entity,
    options = options,
    minAppVersion = minAppVersion,
    file = file?.toEntity(),
    preview = preview?.toEntity(),
)

fun NetworkResourceFile.toEntity(): digital.euforia.app.data.db.entity.ResourceFile =
    digital.euforia.app.data.db.entity.ResourceFile(
        type = type,
        id = id,
        url = url,
        ext = ext,
        size = size,
        width = width,
        height = height,
        ratio = ratio,
        duration = duration,
        videoUrls = videoUrls?.toEntity(),
    )

@JsonClass(generateAdapter = true)
data class NetworkResourceFile(
    @field:Json(name = "class") val type: String, // "file"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "ext") val ext: String,
    @field:Json(name = "size") val size: Long?,
    @field:Json(name = "width") val width: Int?,
    @field:Json(name = "height") val height: Int?,
    @field:Json(name = "ratio") val ratio: Double?,
    @field:Json(name = "duration") val duration: Int?,
    @field:Json(name = "video_urls") val videoUrls: NetworkVideoUrls?,
)
