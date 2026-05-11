/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState

/**
 * Applies per-scene editor state ([SoundscapeSceneLocalState]) onto catalog [Scene] rows so list UIs,
 * playlists, and download cards match the iOS model where `AudioScene` carries both video URL and
 * generated preview URLs after a background change.
 */
fun Scene.mergeSoundscapeSceneLocalBackground(local: SoundscapeSceneLocalState?): Scene {
    if (local == null) return this
    val img = local.backgroundImageUrl?.takeIf { it.isNotBlank() }
    val vid = local.backgroundVideoUrl?.takeIf { it.isNotBlank() }
    val src = local.backgroundSource?.takeIf { it.isNotBlank() }
    if (img == null && vid == null && src == null) return this

    val isImageOnly = src == "image" || src == "local_image"
    val nextVideo = when {
        isImageOnly -> null
        vid != null -> vid
        else -> this.videoUrl
    }
    // Custom image: sharp URL for both fields. Custom video: first frame in both for catalog / mini / downloads.
    val nextImagePreview = img ?: this.imagePreviewUrl
    val nextImageUrl = img ?: this.imageUrl
    return copy(
        imagePreviewUrl = nextImagePreview,
        imageUrl = nextImageUrl,
        videoUrl = nextVideo,
    )
}

fun mergeSoundscapeScenesWithLocalBackground(
    scenes: List<Scene>,
    localBySceneId: Map<Int, SoundscapeSceneLocalState>,
): List<Scene> = scenes.map { scene ->
    scene.mergeSoundscapeSceneLocalBackground(localBySceneId[scene.id])
}

/**
 * Omits local rows keyed by a catalog [Scene.id] that is still used as [SoundscapePreset.sceneId],
 * so preset-only editor state (previously stored on the stock id) does not repaint catalog cards.
 */
fun mergeSoundscapeScenesWithLocalBackgroundExcludingPresetBases(
    scenes: List<Scene>,
    localBySceneId: Map<Int, SoundscapeSceneLocalState>,
    presetBaseCatalogSceneIds: Set<Int>,
): List<Scene> = scenes.map { scene ->
    val local = localBySceneId[scene.id].takeUnless { scene.id in presetBaseCatalogSceneIds }
    scene.mergeSoundscapeSceneLocalBackground(local)
}
