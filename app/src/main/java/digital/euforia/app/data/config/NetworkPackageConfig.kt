package digital.euforia.app.data.config

import com.squareup.moshi.Json

data class NetworkPackageConfig(
    @field:Json(name = "type")
    val type: String,

    @field:Json(name = "data")
    val data: NetworkBlockData? = null
)

data class NetworkBlockData(
    @field:Json(name = "title")
    val title: String? = null,

    @field:Json(name = "description")
    val description: String? = null,

    @field:Json(name = "maxItems")
    val maxItems: Int? = null,

    @field:Json(name = "headerAction")
    val headerAction: String? = null
)