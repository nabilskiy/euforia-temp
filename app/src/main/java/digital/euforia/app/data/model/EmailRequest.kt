package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmailRequest(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "details") val details: String
)
