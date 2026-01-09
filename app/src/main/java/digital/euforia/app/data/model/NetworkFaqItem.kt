package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.FaqItem

@JsonClass(generateAdapter = true)
data class NetworkFaqItem(
    @field:Json(name = "class") val type: String, // "faq"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "category_id") val categoryId: Int,
    // Server uses "type" to pass category alias for convenience
    @field:Json(name = "type") val categoryAlias: String,
    @field:Json(name = "question") val question: String,
    @field:Json(name = "answer") val answer: String,
    @field:Json(name = "date") val date: Long?,
)

fun NetworkFaqItem.toEntity(): FaqItem = FaqItem(
    id = id,
    type = type,
    categoryId = categoryId,
    categoryAlias = categoryAlias,
    question = question,
    answer = answer,
    date = date,
)
