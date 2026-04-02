/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.SoundscapePlaylist

@JsonClass(generateAdapter = true)
data class NetworkPlaylist(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "image_preview_url") val imageUrl: String?,
    @field:Json(name = "pro") val pro: Boolean = false,
    @field:Json(name = "scene_ids") val sceneIds: List<Int> = emptyList(),
)

fun NetworkPlaylist.toEntity(): SoundscapePlaylist = SoundscapePlaylist(
    id = id,
    alias = alias,
    name = name,
    description = description,
    imageUrl = imageUrl,
    pro = pro,
    sceneIds = sceneIds,
)

