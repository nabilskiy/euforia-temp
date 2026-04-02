package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkVideoFile(
    @field:Json(name = "class") val type: String? = null,
    @field:Json(name = "id") val id: Int? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "ext") val ext: String? = null,
    @field:Json(name = "size") val size: Long? = null,
    @field:Json(name = "width") val width: Int? = null,
    @field:Json(name = "height") val height: Int? = null,
    @field:Json(name = "ratio") val ratio: Double? = null,
    @field:Json(name = "duration") val duration: Int? = null,
    @field:Json(name = "video_urls") val videoUrls: NetworkVideoUrls? = null,
)

@JsonClass(generateAdapter = true)
data class NetworkVideoUrls(
    @field:Json(name = "hls") val hls: String?,
    @field:Json(name = "dash") val dash: String?,
)
