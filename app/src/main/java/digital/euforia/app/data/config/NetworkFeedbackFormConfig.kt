package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.FeedbackFormConfig

@JsonClass(generateAdapter = true)
class NetworkFeedbackFormConfig(
    @field:Json(name = "isEnabled") val isEnabled: Boolean,
    @field:Json(name = "launchCount") val launchCount: Int,
    @field:Json(name = "timeInterval") val timeInterval: Int
) {
    fun toDomain() = FeedbackFormConfig(
        isEnabled = isEnabled,
        launchCount = launchCount,
        timeInterval = timeInterval
    )
}
