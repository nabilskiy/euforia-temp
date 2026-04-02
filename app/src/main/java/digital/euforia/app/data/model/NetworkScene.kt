/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.Scene

@JsonClass(generateAdapter = true)
data class NetworkScene(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "class") val modelClass: String? = null,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "subtitle") val subtitle: String? = null,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "keywords") val keywords: String? = null,
    @field:Json(name = "image_url") val imageUrl: String?,
    @field:Json(name = "image_preview_url") val imagePreviewUrl: String? = null,
    @field:Json(name = "image_cover_url") val imageCoverUrl: String? = null,
    @field:Json(name = "video_url") val videoUrl: String?,
    @field:Json(name = "video") val video: NetworkVideoFile? = null,
    @field:Json(name = "music_id") val musicId: Int?,
    @field:Json(name = "scene_musics") val sceneMusics: List<NetworkSceneMusic> = emptyList(),
    @field:Json(name = "scene_sounds") val sceneSounds: List<NetworkSceneSound> = emptyList(),
    @field:Json(name = "pro") val pro: Boolean = false,
    @field:Json(name = "published_at") val publishedAt: Long? = null,
    @field:Json(name = "category_id") val categoryId: Int? = null,
)

fun NetworkScene.toEntity(resolvedCategoryId: Int? = null): Scene = Scene(
    id = id,
    alias = alias,
    name = name,
    description = description,
    imageUrl = imageUrl,
    imagePreviewUrl = imagePreviewUrl,
    videoUrl = videoUrl,
    musicId = musicId ?: sceneMusics.firstOrNull()?.music?.id,
    pro = pro,
    publishedAt = publishedAt,
    categoryId = resolvedCategoryId ?: categoryId,
)

@JsonClass(generateAdapter = true)
data class NetworkSceneMusic(
    @field:Json(name = "class") val modelClass: String? = null,
    @field:Json(name = "music_file_url") val musicFileUrl: String? = null,
    @field:Json(name = "volume") val volume: Int? = null,
    @field:Json(name = "timeout") val timeout: Int? = null,
    @field:Json(name = "interval") val interval: Int? = null,
    @field:Json(name = "music") val music: NetworkMusic? = null,
)

@JsonClass(generateAdapter = true)
data class NetworkSceneSound(
    @field:Json(name = "class") val modelClass: String? = null,
    @field:Json(name = "sound_file_url") val soundFileUrl: String? = null,
    @field:Json(name = "volume") val volume: Int? = null,
    @field:Json(name = "timeout") val timeout: Int? = null,
    @field:Json(name = "interval") val interval: Int? = null,
    @field:Json(name = "pos_x") val posX: Int? = null,
    @field:Json(name = "pos_y") val posY: Int? = null,
    @field:Json(name = "sound") val sound: NetworkSound? = null,
)
