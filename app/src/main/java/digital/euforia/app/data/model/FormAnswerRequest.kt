package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FormAnswerRequest(
    @field:Json(name = "key") val key: String,
    @field:Json(name = "answer") val answer: Any
)
