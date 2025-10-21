package digital.euforia.app.data.model.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class NetworkAppSettingsConfig(
    @Json(name = "allow_languages") val allowedLanguages: List<String>,
) {
    fun toAppSettingsConfig() = digital.euforia.app.domain.model.config.AppSettingsConfig(
        allowedLanguages = allowedLanguages,
    )
}