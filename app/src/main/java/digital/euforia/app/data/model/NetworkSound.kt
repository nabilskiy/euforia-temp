package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkSound(
    @field:Json(name = "class") val type: String,
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "category_id") val categoryId: Int,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "color") val color: String?,
    @field:Json(name = "continuous") val continuous: Boolean,
    @field:Json(name = "min_repeat_delay") val minRepeatDelay: Int,
    @field:Json(name = "max_repeat_delay") val maxRepeatDelay: Int,
    @field:Json(name = "file_url") val fileUrl: String,
    @field:Json(name = "image_url") val imageUrl: String,
    @field:Json(name = "file") val file: NetworkFile?
)