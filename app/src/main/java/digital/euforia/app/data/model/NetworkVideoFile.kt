package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkVideoFile(
    @field:Json(name = "class") val type: String,
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "ext") val ext: String,
    @field:Json(name = "size") val size: Long?,
    @field:Json(name = "width") val width: Int?,
    @field:Json(name = "height") val height: Int?,
    @field:Json(name = "ratio") val ratio: Double?,
    @field:Json(name = "duration") val duration: Int?,
    @field:Json(name = "video_urls") val videoUrls: NetworkVideoUrls?
)

@JsonClass(generateAdapter = true)
data class NetworkVideoUrls(
    @field:Json(name = "hls") val hls: String?,
    @field:Json(name = "dash") val dash: String?,
)
