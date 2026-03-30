package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents one compose block item returned by /compose API.
 *
 * Example JSON item:
 * { "key": "random2", "result": { ... } }
 * or { "key": "scenes_list", "result": [ { ... }, { ... } ] }
 * or { "key": "scene", "result": null }
 */
@JsonClass(generateAdapter = true)
data class NetworkCompose(
    @field:Json(name = "key") val key: String,
    // Moshi will decode JSON objects as Map<*, *>, arrays as List<*>, and null as null.
    @field:Json(name = "result") val result: Any?
)