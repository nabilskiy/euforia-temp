package digital.euforia.app.data.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.domain.model.config.CriticalUpdateConfig

@JsonClass(generateAdapter = true)
class NetworkCriticalUpdateConfig(
    @field:Json(name = "needUpdate") val needUpdate: Boolean,
    @field:Json(name = "cancelable") val cancelable: Boolean,
    @field:Json(name = "minVersionCode") val minVersionCode: Int
) {
    fun toDomain() = CriticalUpdateConfig(
        needUpdate = needUpdate,
        cancelable = cancelable,
        minVersionCode = minVersionCode
    )
}
