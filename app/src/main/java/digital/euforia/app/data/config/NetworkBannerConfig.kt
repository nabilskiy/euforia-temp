package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.BannerConfig

@JsonClass(generateAdapter = true)
class NetworkBannerConfig(
    @field:Json(name = "subtitle") val subtitle: String,
    @field:Json(name = "img_url") val imgUrl: String,
    @field:Json(name = "action_url") val actionUrl: String,
    @field:Json(name = "style") val style: NetworkBannerStyle,
    @field:Json(name = "predicate") val predicate: String,
    @field:Json(name = "isFullWidth") val isFullWidth: Boolean
) {
    fun toBannerConfig() = BannerConfig(
        subtitle = subtitle,
        imgUrl = imgUrl,
        actionUrl = actionUrl,
        style = style.toStyle(),
        predicate = predicate,
        isFullWidth = isFullWidth
    )
}