package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.RateConfig
import digital.euforia.app.domain.model.config.RatePeriodConfig

@JsonClass(generateAdapter = true)
data class NetworkRateConfig(
    @field:Json(name = "morning") val morning: NetworkRatePeriodConfig,
    @field:Json(name = "daytime") val daytime: NetworkRatePeriodConfig,
    @field:Json(name = "evening") val evening: NetworkRatePeriodConfig
) {
    fun toDomain() = RateConfig(
        morning = morning.toDomain(),
        daytime = daytime.toDomain(),
        evening = evening.toDomain()
    )
}

@JsonClass(generateAdapter = true)
data class NetworkRatePeriodConfig(
    @field:Json(name = "isEnabled") val isEnabled: Boolean,
    @field:Json(name = "eventCount") val eventCount: Int,
    @field:Json(name = "timeInterval") val timeInterval: Int,
    @field:Json(name = "timeIntervalAfterCancel") val timeIntervalAfterCancel: Int
) {
    fun toDomain() = RatePeriodConfig(
        isEnabled = isEnabled,
        eventCount = eventCount,
        timeInterval = timeInterval,
        timeIntervalAfterCancel = timeIntervalAfterCancel
    )
}
