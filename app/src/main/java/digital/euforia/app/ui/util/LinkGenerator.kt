/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */
package digital.euforia.app.ui.util

object LinkGenerator {
    private const val BASE_URL = "https://euforia.digital"

    fun buildAudioSceneShareUrl(sceneId: Int): String? {
        if (sceneId <= 0) return null
        return "$BASE_URL/scenes/$sceneId"
    }
}
