package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.TimeOfDayConfig

@JsonClass(generateAdapter = true)
class NetworkTimeOfDayConfig(
    @field:Json(name="morningBegin") val morningBegin: Int,
    @field:Json(name="morningNotification") val morningNotification: Int,
    @field:Json(name="daytimeBegin") val daytimeBegin: Int,
    @field:Json(name="daytimeNotification") val daytimeNotification: Int,
    @field:Json(name="eveningBegin") val eveningBegin: Int,
    @field:Json(name="eveningNotification") val eveningNotification: Int
) {
    fun toTimeOfDayConfig() = TimeOfDayConfig(
        morningBegin = morningBegin,
        morningNotification = morningNotification,
        daytimeBegin = daytimeBegin,
        daytimeNotification = daytimeNotification,
        eveningBegin = eveningBegin,
        eveningNotification = eveningNotification
    )
}