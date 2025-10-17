package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkMeditation(
    @field:Json(name = "class") val type: String, // "meditation"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "author_id") val authorId: Int?,
    @field:Json(name = "main_category_id") val mainCategoryId: Int?,
    @field:Json(name = "main_package_id") val mainPackageId: Int?,
    @field:Json(name = "pro") val pro: Boolean,

    @field:Json(name = "name") val name: String,
    @field:Json(name = "subtitle") val subtitle: String?,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "keywords") val keywords: String?,

    @field:Json(name = "image_url") val imageUrl: String?,
    @field:Json(name = "image_preview_url") val imagePreviewUrl: String?,
    @field:Json(name = "image_cover_url") val imageCoverUrl: String?,

    @field:Json(name = "music_file_url") val musicFileUrl: String?,

    @field:Json(name = "color_1") val color1: String?,
    @field:Json(name = "color_2") val color2: String?,
    @field:Json(name = "color_3") val color3: String?,

    @field:Json(name = "published_at") val publishedAt: Long?,

    @field:Json(name = "music") val music: NetworkFile?,

    @field:Json(name = "audio_url") val audioUrl: String?,
    @field:Json(name = "video_url") val videoUrl: String?,
    @field:Json(name = "audio_preview_url") val audioPreviewUrl: String?,
    @field:Json(name = "video_preview_url") val videoPreviewUrl: String?,

    @field:Json(name = "audio") val audio: NetworkFile?,
    @field:Json(name = "video") val video: NetworkFile?,
)

fun NetworkMeditation.toEntity(): digital.euforia.app.data.db.entity.MeditationEntity =
    digital.euforia.app.data.db.entity.MeditationEntity(
        id = id,
        type = type,
        alias = alias,
        authorId = authorId,
        mainCategoryId = mainCategoryId,
        mainPackageId = mainPackageId,
        pro = pro,
        name = name,
        subtitle = subtitle,
        description = description,
        keywords = keywords,
        imageUrl = imageUrl,
        imagePreviewUrl = imagePreviewUrl,
        imageCoverUrl = imageCoverUrl,
        musicFileUrl = musicFileUrl,
        color1 = color1,
        color2 = color2,
        color3 = color3,
        publishedAt = publishedAt,
        music = music?.toEntity(),
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        audioPreviewUrl = audioPreviewUrl,
        videoPreviewUrl = videoPreviewUrl,
        audio = audio?.toEntity(),
        video = video?.toEntity(),
    )
