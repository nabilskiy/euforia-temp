/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.SoundscapeSound
import digital.euforia.app.data.db.entity.SoundscapeSoundCategory

@JsonClass(generateAdapter = true)
data class NetworkSound(
    @field:Json(name = "class") val type: String? = null,
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "alias") val alias: String? = null,
    @field:Json(name = "category_id") val categoryId: Int? = null,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "color") val color: String? = null,
    @field:Json(name = "continuous") val continuous: Boolean? = null,
    @field:Json(name = "min_repeat_delay") val minRepeatDelay: Int? = null,
    @field:Json(name = "max_repeat_delay") val maxRepeatDelay: Int? = null,
    @field:Json(name = "file_url") val fileUrl: String? = null,
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "file") val file: NetworkFile? = null,
)

@JsonClass(generateAdapter = true)
data class NetworkSoundCategory(
    @field:Json(name = "class") val type: String? = null,
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "alias") val alias: String? = null,
    @field:Json(name = "name") val name: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "position") val position: Int? = null,
)

fun NetworkSound.toEntity(): SoundscapeSound = SoundscapeSound(
    id = requireNotNull(id) { "sound id is required" },
    type = type ?: "sound",
    alias = alias.orEmpty(),
    categoryId = categoryId ?: 0,
    name = name.orEmpty(),
    description = description,
    color = color,
    continuous = continuous ?: false,
    minRepeatDelay = minRepeatDelay,
    maxRepeatDelay = maxRepeatDelay,
    fileUrl = fileUrl?.takeIf { it.isNotBlank() }
        ?: file?.url?.takeIf { it.isNotBlank() }
        ?: "",
    imageUrl = imageUrl.orEmpty(),
    file = file?.toEntity(),
)

fun NetworkSoundCategory.toEntity(index: Int): SoundscapeSoundCategory = SoundscapeSoundCategory(
    id = requireNotNull(id) { "sound category id is required" },
    type = type ?: "category",
    alias = alias.orEmpty(),
    name = name.orEmpty(),
    description = description,
    position = position ?: index,
)