/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

private const val COPY_SCENE_ID_OFFSET = 1_000_000
private const val DOWNLOAD_COPY_PREFIX = "copy_"

fun copySceneIdFromPresetId(presetId: Int): Int = -(COPY_SCENE_ID_OFFSET + presetId)

fun presetIdFromCopySceneId(sceneId: Int): Int? {
    if (sceneId >= 0) return null
    val raw = -sceneId - COPY_SCENE_ID_OFFSET
    return raw.takeIf { it > 0 }
}

fun isCopySceneId(sceneId: Int): Boolean = presetIdFromCopySceneId(sceneId) != null

fun copyDownloadIdFromPresetId(presetId: Int): String = "$DOWNLOAD_COPY_PREFIX$presetId"

fun presetIdFromDownloadId(downloadId: String): Int? {
    if (!downloadId.startsWith(DOWNLOAD_COPY_PREFIX)) return null
    return downloadId.removePrefix(DOWNLOAD_COPY_PREFIX).toIntOrNull()
}
