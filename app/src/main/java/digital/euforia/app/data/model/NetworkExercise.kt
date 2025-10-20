package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkExercise(
    @field:Json(name = "class") val type: String, // "exercise"
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
    @field:Json(name = "video") val video: NetworkVideoFile?,
)

fun NetworkVideoFile.toEntity(): digital.euforia.app.data.db.entity.VideoFile =
    digital.euforia.app.data.db.entity.VideoFile(
        id = id,
        type = type,
        url = url,
        ext = ext,
        size = size,
        width = width,
        height = height,
        ratio = ratio,
        duration = duration,
        videoUrls = videoUrls?.toEntity(),
    )

fun NetworkVideoUrls.toEntity(): digital.euforia.app.data.db.entity.VideoUrls =
    digital.euforia.app.data.db.entity.VideoUrls(
        hls = hls,
        dash = dash,
    )

fun NetworkExercise.toEntity(): digital.euforia.app.data.db.entity.Exercise =
    digital.euforia.app.data.db.entity.Exercise(
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
