package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import digital.euforia.app.ui.programs.ProgramUi

/** Aggregation for reading full package with its children */
data class PackageWithChildren(
    @Embedded val pkg: Package,
    @Relation(parentColumn = "id", entityColumn = "main_package_id")
    val meditations: List<Meditation>,
    @Relation(parentColumn = "id", entityColumn = "main_package_id")
    val exercises: List<Exercise>,
    @Relation(parentColumn = "id", entityColumn = "main_package_id")
    val articles: List<Article>,
)

data class PackageWithMeditations(
    @Embedded val pkg: Package,
    @Relation(parentColumn = "id", entityColumn = "main_package_id")
    val meditations: List<Meditation>,
)

@Entity(tableName = "packages")
data class Package(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "pro") val pro: Boolean,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "author_id") val authorId: Int?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "subtitle") val subtitle: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "keywords") val keywords: String?,

    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "image_preview_url") val imagePreviewUrl: String?,
    @ColumnInfo(name = "image_cover_url") val imageCoverUrl: String?,
    @ColumnInfo(name = "video_cover_url") val videoCoverUrl: String?,

    @ColumnInfo(name = "color_1") val color1: String?,
    @ColumnInfo(name = "color_2") val color2: String?,
    @ColumnInfo(name = "color_3") val color3: String?,

    @ColumnInfo(name = "published_at") val publishedAt: Long?,
    @ColumnInfo(name = "publications_count") val publicationsCount: Int = 0,
) {

    companion object Companion {
        val TOP_PACKAGES_IDS = listOf("3", "1", "19")
    }
}

@Entity(tableName = "meditations")
data class Meditation(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "author_id") val authorId: Int?,
    @ColumnInfo(name = "main_category_id") val mainCategoryId: Int?,
    @ColumnInfo(name = "main_package_id") val mainPackageId: Int?,
    @ColumnInfo(name = "pro") val pro: Boolean,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "subtitle") val subtitle: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "keywords") val keywords: String?,

    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "image_preview_url") val imagePreviewUrl: String?,
    @ColumnInfo(name = "image_cover_url") val imageCoverUrl: String?,

    @ColumnInfo(name = "music_file_url") val musicFileUrl: String?,

    @ColumnInfo(name = "color_1") val color1: String?,
    @ColumnInfo(name = "color_2") val color2: String?,
    @ColumnInfo(name = "color_3") val color3: String?,

    @ColumnInfo(name = "published_at") val publishedAt: Long?,

    @Embedded(prefix = "music_") val music: File?,

    @ColumnInfo(name = "audio_url") val audioUrl: String?,
    @ColumnInfo(name = "video_url") val videoUrl: String?,
    @ColumnInfo(name = "audio_preview_url") val audioPreviewUrl: String?,
    @ColumnInfo(name = "video_preview_url") val videoPreviewUrl: String?,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean = false,

    @Embedded(prefix = "audio_file_") val audio: File?,
    @Embedded(prefix = "video_file_") val video: File?,
    @ColumnInfo(name = "related_package_ids") val relatedPackageIds: List<Int> = emptyList(),
) {

    fun computeDurationMinutes(): Int {
        return (audio?.duration ?: 0).div(60).coerceAtLeast(1)
    }
}

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "author_id") val authorId: Int?,
    @ColumnInfo(name = "main_category_id") val mainCategoryId: Int?,
    @ColumnInfo(name = "main_package_id") val mainPackageId: Int?,
    @ColumnInfo(name = "pro") val pro: Boolean,

    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "subtitle") val subtitle: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "keywords") val keywords: String?,

    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "image_preview_url") val imagePreviewUrl: String?,
    @ColumnInfo(name = "image_cover_url") val imageCoverUrl: String?,

    @ColumnInfo(name = "music_file_url") val musicFileUrl: String?,

    @ColumnInfo(name = "color_1") val color1: String?,
    @ColumnInfo(name = "color_2") val color2: String?,
    @ColumnInfo(name = "color_3") val color3: String?,

    @ColumnInfo(name = "published_at") val publishedAt: Long?,

    @Embedded(prefix = "music_") val music: File?,

    @ColumnInfo(name = "audio_url") val audioUrl: String?,
    @ColumnInfo(name = "video_url") val videoUrl: String?,
    @ColumnInfo(name = "audio_preview_url") val audioPreviewUrl: String?,
    @ColumnInfo(name = "video_preview_url") val videoPreviewUrl: String?,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean = false,

    @Embedded(prefix = "audio_file_") val audio: File?,
    @Embedded(prefix = "video_file_") val video: VideoFile?,
    @ColumnInfo(name = "related_package_ids") val relatedPackageIds: List<Int> = emptyList(),
) {

    fun computeDurationMinutes(): Int {
        return (video?.duration ?: audio?.duration ?: 0).div(60).coerceAtLeast(1)
    }
}

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "author_id") val authorId: Int?,
    @ColumnInfo(name = "main_category_id") val mainCategoryId: Int?,
    @ColumnInfo(name = "main_package_id") val mainPackageId: Int?,
    @ColumnInfo(name = "pro") val pro: Boolean,

    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "subtitle") val subtitle: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "keywords") val keywords: String?,

    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "image_preview_url") val imagePreviewUrl: String?,
    @ColumnInfo(name = "image_cover_url") val imageCoverUrl: String?,

    @ColumnInfo(name = "music_file_url") val musicFileUrl: String?,

    @ColumnInfo(name = "color_1") val color1: String?,
    @ColumnInfo(name = "color_2") val color2: String?,
    @ColumnInfo(name = "color_3") val color3: String?,

    @ColumnInfo(name = "published_at") val publishedAt: Long?,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean = false,

    @Embedded(prefix = "music_") val music: File?,

    @ColumnInfo(name = "content_length") val contentLength: Int?,
    @ColumnInfo(name = "related_package_ids") val relatedPackageIds: List<Int> = emptyList(),
) {

    fun computeDurationMinutes(): Int {
        return (contentLength?.div(16.2))?.toInt()?.div(60)?.coerceAtLeast(1) ?: 0
    }
}

/** Plain value objects for embedding video details in Exercise */
data class VideoFile(
    @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "ext") val ext: String?,
    @ColumnInfo(name = "size") val size: Long?,
    @ColumnInfo(name = "width") val width: Int?,
    @ColumnInfo(name = "height") val height: Int?,
    @ColumnInfo(name = "ratio") val ratio: Double?,
    @ColumnInfo(name = "duration") val duration: Int?,
    @Embedded(prefix = "urls_") val videoUrls: VideoUrls?,
)

data class VideoUrls(
    @ColumnInfo(name = "hls") val hls: String?,
    @ColumnInfo(name = "dash") val dash: String?,
)