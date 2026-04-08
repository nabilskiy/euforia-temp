/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.MusicCategory
import digital.euforia.app.data.db.entity.Music

@JsonClass(generateAdapter = true)
data class NetworkMusic(
    @field:Json(name = "class") val type: String? = null,
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "alias") val alias: String? = null,
    @field:Json(name = "type") val contentType: String? = null,
    @field:Json(name = "category_id") val categoryId: Int? = null,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "file_url") val fileUrl: String? = null,
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "file") val file: NetworkFile? = null,
)

@JsonClass(generateAdapter = true)
data class NetworkMusicCategory(
    @field:Json(name = "class") val type: String? = null,
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "alias") val alias: String? = null,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "position") val position: Int? = null,
)

fun NetworkMusic.toEntity(): Music = Music(
    id = requireNotNull(id) { "music id is required" },
    type = type ?: "music",
    contentType = contentType.orEmpty(),
    alias = alias.orEmpty(),
    categoryId = categoryId ?: 0,
    name = name.orEmpty(),
    description = description,
    fileUrl = fileUrl.orEmpty(),
    imageUrl = imageUrl.orEmpty(),
    file = file?.toEntity(),
)

fun NetworkMusicCategory.toEntity(index: Int): MusicCategory = MusicCategory(
    id = requireNotNull(id) { "music category id is required" },
    type = type ?: "category",
    alias = alias.orEmpty(),
    name = name.orEmpty(),
    description = description,
    position = position ?: index,
)
