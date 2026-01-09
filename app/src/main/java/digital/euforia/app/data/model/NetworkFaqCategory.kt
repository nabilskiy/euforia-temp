package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.FaqCategory

@JsonClass(generateAdapter = true)
data class NetworkFaqCategory(
    @field:Json(name = "class") val type: String, // "faq_category"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "alias") val alias: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "description") val description: String?,
)

fun NetworkFaqCategory.toEntity(): FaqCategory = FaqCategory(
    id = id,
    type = type,
    alias = alias,
    name = name,
    description = description,
)
