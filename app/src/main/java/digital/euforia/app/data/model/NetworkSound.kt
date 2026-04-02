package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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