package digital.euforia.app.data.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlin.collections.orEmpty

fun flattenToStringMap(input: Map<String, Any?>, prefix: String = ""): Map<String, String> {
    val out = mutableMapOf<String, String>()
    input.forEach { (k, v) ->
        val key = if (prefix.isEmpty()) k else "$prefix.$k"
        when (v) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                out += flattenToStringMap(v as Map<String, Any?>, key)
            }
            is List<*> -> {
                // За потреби: або серіалізувати у JSON, або робити join з роздільником
                out[key] = v.joinToString(",") { it?.toString().orEmpty() }
            }
            null -> { /* пропускаємо або кладемо "" */ }
            else -> out[key] = v.toString()
        }
    }
    return out
}

fun parseJsonToFlatMap(json: String, moshi: Moshi = Moshi.Builder().build()): Map<String, String> {
    val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    val adapter = moshi.adapter<Map<String, Any?>>(type)
    val root = adapter.fromJson(json).orEmpty()
    return flattenToStringMap(root)
}