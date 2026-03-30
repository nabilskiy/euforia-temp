package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.RateAppConfig

@JsonClass(generateAdapter = true)
class NetworkRateAppConfig(
    @field:Json(name = "isEnabled") val isEnabled: Boolean,
    @field:Json(name = "launchCount") val launchCount: Int,
    @field:Json(name = "timeInterval") val timeInterval: Long
) {
    fun toDomain() = RateAppConfig(
        isEnabled = isEnabled,
        launchCount = launchCount,
        timeInterval = timeInterval
    )
}
