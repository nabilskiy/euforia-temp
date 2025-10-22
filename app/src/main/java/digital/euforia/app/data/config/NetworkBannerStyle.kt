package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.StyleConfig

@JsonClass(generateAdapter = true)
class NetworkBannerStyle(
    @field:Json(name = "subtitleColor") val subtitleColor: String,
    @field:Json(name = "subtitleSize") val subtitleSize: Int,
    @field:Json(name = "textAlignment") val textAlignment: String,
    @field:Json(name = "ratio") val ratio: Float?
) {
    fun toStyle() = StyleConfig(
        subtitleColor = subtitleColor,
        subtitleSize = subtitleSize,
        textAlignment = textAlignment,
        ratio = ratio
    )
}