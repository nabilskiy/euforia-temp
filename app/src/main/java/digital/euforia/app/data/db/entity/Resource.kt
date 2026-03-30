package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "resources")
data class Resource(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "author_id") val authorId: Int?,
    @ColumnInfo(name = "pro") val pro: Boolean,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "subtitle") val subtitle: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "keywords") val keywords: String?,
    @ColumnInfo(name = "published_at") val publishedAt: Long?,
    @ColumnInfo(name = "file_url") val fileUrl: String?,
    @ColumnInfo(name = "preview_url") val previewUrl: String?,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
    @ColumnInfo(name = "class_id") val classId: Int?,
    @ColumnInfo(name = "class_alias") val classAlias: String?,
    @Embedded(prefix = "entity_") val entity: ResourceEntity?,
    @ColumnInfo(name = "options") val options: String?,
    @ColumnInfo(name = "min_app_version") val minAppVersion: Int?,
    @Embedded(prefix = "file_info_") val file: ResourceFile?,
    @Embedded(prefix = "preview_info_") val preview: ResourceFile?,
) {
    companion object {
        const val CLASS_ALIAS_VOICE_AVATAR = "voice_avatar"
        const val CLASS_ALIAS_VOICE_MUSIC = "voice_music"
        const val CLASS_ALIAS_MEDITATION_BACKGROUND = "meditation_background"
    }
}

@JsonClass(generateAdapter = true)
data class ResourceEntity(
    @ColumnInfo(name = "videoUrl") val videoUrl: String?,
    @ColumnInfo(name = "imageUrl") val imageUrl: String?,
    @ColumnInfo(name = "musicUrl") val musicUrl: String?,
    @ColumnInfo(name = "maxVolume") val maxVolume: Float?,
)

data class ResourceFile(
    @ColumnInfo(name = "type") val type: String?,
    @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "ext") val ext: String?,
    @ColumnInfo(name = "size") val size: Long?,
    @ColumnInfo(name = "width") val width: Int?,
    @ColumnInfo(name = "height") val height: Int?,
    @ColumnInfo(name = "ratio") val ratio: Double?,
    @ColumnInfo(name = "duration") val duration: Int?,
    @Embedded(prefix = "urls_") val videoUrls: VideoUrls?,
)