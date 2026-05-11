/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

import digital.euforia.app.domain.soundscapes.copyDownloadIdFromPresetId as domainCopyDownloadIdFromPresetId
import digital.euforia.app.domain.soundscapes.copySceneIdFromPresetId as domainCopySceneIdFromPresetId
import digital.euforia.app.domain.soundscapes.isCopySceneId as domainIsCopySceneId
import digital.euforia.app.domain.soundscapes.presetIdFromCopySceneId as domainPresetIdFromCopySceneId
import digital.euforia.app.domain.soundscapes.presetIdFromDownloadId as domainPresetIdFromDownloadId

fun copySceneIdFromPresetId(presetId: Int): Int = domainCopySceneIdFromPresetId(presetId)

fun presetIdFromCopySceneId(sceneId: Int): Int? = domainPresetIdFromCopySceneId(sceneId)

fun isCopySceneId(sceneId: Int): Boolean = domainIsCopySceneId(sceneId)

fun copyDownloadIdFromPresetId(presetId: Int): String = domainCopyDownloadIdFromPresetId(presetId)

fun presetIdFromDownloadId(downloadId: String): Int? = domainPresetIdFromDownloadId(downloadId)
