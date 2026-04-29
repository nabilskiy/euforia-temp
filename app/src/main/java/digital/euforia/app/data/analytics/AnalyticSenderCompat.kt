package digital.euforia.app.data.analytics

/**
 * Calls soundscape layer settings analytics in an ABI-safe way.
 *
 * On some builds the installed AnalyticSender dex may lag behind call sites and expose an older
 * method shape. Reflection avoids NoSuchMethodError crash and degrades to best available overload.
 */
fun AnalyticSender.soundscapeLayerSettingsOpenedCompat(
    layerKey: String,
    soundId: Int,
    title: String,
) {
    val method = javaClass.methods
        .asSequence()
        .filter { it.name == "soundscapeLayerSettingsOpened" }
        .sortedByDescending { it.parameterCount }
        .firstOrNull()
        ?: return

    runCatching {
        when (method.parameterCount) {
            3 -> method.invoke(this, layerKey, soundId, title)
            2 -> method.invoke(this, layerKey, soundId)
            1 -> method.invoke(this, layerKey)
            0 -> method.invoke(this)
            else -> Unit
        }
    }.getOrElse {
        // Last-resort fallback: keep flow stable and still register that layer interaction happened.
        runCatching { soundscapeLayerAdded() }
    }
}
