package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.DemoUnlockDayConfig

@JsonClass(generateAdapter = true)
class NetworkDemoUnlockDayConfig(
    @field:Json(name = "completedCount") val completedCount: Int,
    @field:Json(name = "completionItemPercentage") val completionItemPercentage: Float
) {
    fun toDemoUnlockDayConfig() = DemoUnlockDayConfig(
        completedCount = completedCount,
        completionItemPercentage = completionItemPercentage
    )
}