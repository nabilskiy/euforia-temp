/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */
package digital.euforia.app.data.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType
import digital.euforia.app.domain.model.config.ScenePlayerConfig

internal object SoundscapesRemoteConfigParser {
    fun parseScenesDefaultIds(
        json: String,
        moshi: Moshi,
        onError: (Throwable) -> Unit,
    ): List<Int> {
        if (json.isBlank()) return emptyList()
        return try {
            val listType = newParameterizedType(MutableList::class.java, Int::class.javaObjectType)
            moshi.adapter<List<Int>>(listType).fromJson(json).orEmpty()
        } catch (t: Throwable) {
            onError(t)
            emptyList()
        }
    }

    fun parseScenePlayerConfig(
        json: String,
        moshi: Moshi,
        onError: (Throwable) -> Unit,
    ): ScenePlayerConfig {
        if (json.isBlank()) return ScenePlayerConfig()
        return try {
            val mapType = newParameterizedType(
                Map::class.java,
                String::class.java,
                Boolean::class.javaObjectType
            )
            val map = moshi.adapter<Map<String, Boolean>>(mapType).fromJson(json).orEmpty()
            ScenePlayerConfig(
                isParallaxEnabled = map["isParallaxEnabled"] ?: true,
                ambientMode = map["ambientMode"] ?: false,
            )
        } catch (t: Throwable) {
            onError(t)
            ScenePlayerConfig()
        }
    }
}

