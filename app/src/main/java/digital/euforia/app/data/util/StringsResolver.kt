package digital.euforia.app.data.util

import android.content.Context

class StringsResolver(
    private val context: Context,
    private val kv: () -> Map<String, String>  // постачальник актуальної мапи
) {
    fun getRaw(key: String): String? =
        kv()[key] ?: getFromResources(key)

    fun get(key: String, placeholders: Map<String, String> = emptyMap()): String {
        val base = getRaw(key) ?: key // якщо нічого — повертаємо ключ (щоб видно було)
        return base.fillPlaceholders(placeholders)
    }

    private fun getFromResources(key: String): String? {
        // Можна зберігати ті самі "rc" ключі у strings.xml
        val id = context.resources.getIdentifier(key, "string", context.packageName)
        return if (id != 0) context.getString(id) else null
    }
}

fun String.fillPlaceholders(map: Map<String, String>): String {
    var out = this
    map.forEach { (k, v) ->
        out = out.replace("%$k%", v)
            .replace("\${$k}", v)         // підтримка ${plan.period}
    }
    return out
}