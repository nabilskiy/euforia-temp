/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.service.soundscapes

import org.json.JSONArray
import org.json.JSONObject

enum class SoundscapeAssetType {
    SOUND,
    MUSIC,
    VIDEO,
    IMAGE_PREVIEW,
    IMAGE_BACKGROUND,
    SOUND_ICON,
}

data class SoundscapeOfflineAsset(
    val type: SoundscapeAssetType,
    val remoteUrl: String,
    val localUrl: String,
)

data class SoundscapeOfflineManifest(
    val sceneId: Int,
    val assets: List<SoundscapeOfflineAsset>,
) {
    fun findLocalUrl(remoteUrl: String?, type: SoundscapeAssetType? = null): String? {
        if (remoteUrl.isNullOrBlank()) return null
        return assets.firstOrNull { asset ->
            asset.remoteUrl == remoteUrl && (type == null || asset.type == type)
        }?.localUrl
    }

    fun firstLocalUrlByType(type: SoundscapeAssetType): String? {
        return assets.firstOrNull { it.type == type }?.localUrl
    }

    fun toJson(): String {
        val root = JSONObject()
        root.put("sceneId", sceneId)
        val arr = JSONArray()
        assets.forEach { asset ->
            arr.put(
                JSONObject()
                    .put("type", asset.type.name)
                    .put("remoteUrl", asset.remoteUrl)
                    .put("localUrl", asset.localUrl)
            )
        }
        root.put("assets", arr)
        return root.toString()
    }

    companion object {
        fun fromJsonOrNull(value: String?): SoundscapeOfflineManifest? {
            if (value.isNullOrBlank()) return null
            return runCatching {
                val root = JSONObject(value)
                val sceneId = root.optInt("sceneId", 0)
                val arr = root.optJSONArray("assets") ?: JSONArray()
                val assets = buildList {
                    for (index in 0 until arr.length()) {
                        val item = arr.optJSONObject(index) ?: continue
                        val type = item.optString("type")
                            .takeIf { it.isNotBlank() }
                            ?.let { runCatching { SoundscapeAssetType.valueOf(it) }.getOrNull() }
                            ?: continue
                        val remoteUrl = item.optString("remoteUrl")
                        val localUrl = item.optString("localUrl")
                        if (remoteUrl.isBlank() || localUrl.isBlank()) continue
                        add(
                            SoundscapeOfflineAsset(
                                type = type,
                                remoteUrl = remoteUrl,
                                localUrl = localUrl,
                            )
                        )
                    }
                }
                if (assets.isEmpty()) null else SoundscapeOfflineManifest(sceneId = sceneId, assets = assets)
            }.getOrNull()
        }
    }
}

