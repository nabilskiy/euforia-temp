/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scenes")
data class Scene(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "image_preview_url") val imagePreviewUrl: String?,
    @ColumnInfo(name = "video_url") val videoUrl: String?,
    @ColumnInfo(name = "music_id") val musicId: Int?,
    @ColumnInfo(name = "pro") val pro: Boolean,
    @ColumnInfo(name = "published_at") val publishedAt: Long?,
    @ColumnInfo(name = "category_id") val categoryId: Int? = null,
)

@Entity(tableName = "soundscape_playlists")
data class SoundscapePlaylist(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "pro") val pro: Boolean,
    @ColumnInfo(name = "scene_ids") val sceneIds: List<Int>,
)

@Entity(tableName = "soundscape_presets")
data class SoundscapePreset(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "scene_id") val sceneId: Int,
    @ColumnInfo(name = "layers_json") val layersJson: String,
    @ColumnInfo(name = "music_volume") val musicVolume: Float = 1f,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "soundscape_download_items")
data class SoundscapeDownloadItem(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "scene_id") val sceneId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "local_path") val localPath: String? = null,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "progress") val progress: Int = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val STATUS_NOT_DOWNLOADED = "NOT_DOWNLOADED"
        const val STATUS_QUEUED = "QUEUED"
        const val STATUS_DOWNLOADING = "DOWNLOADING"
        const val STATUS_READY = "READY"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_EXPIRED = "EXPIRED"
    }
}

