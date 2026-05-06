/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.model

fun UnsplashPhoto.toBackgroundMediaItem(): BackgroundMediaItem? {
    val thumb = urls?.small.orEmpty()
    val full = urls?.full.orEmpty()
    if (id.isBlank() || thumb.isBlank() || full.isBlank()) return null
    return BackgroundMediaItem(
        id = id,
        author = user?.name.orEmpty(),
        previewUrl = thumb,
        mediaUrl = full,
        isVideo = false,
    )
}

fun PexelsPhoto.toBackgroundMediaItem(): BackgroundMediaItem? {
    val preview = src?.medium.orEmpty()
    val full = src?.large2x?.takeIf { it.isNotBlank() } ?: src?.original.orEmpty()
    val idStr = id.toString()
    if (idStr.isBlank() || preview.isBlank() || full.isBlank()) return null
    return BackgroundMediaItem(
        id = idStr,
        author = photographer.orEmpty(),
        previewUrl = preview,
        mediaUrl = full,
        isVideo = false,
    )
}

fun PexelsVideo.toBackgroundMediaItem(): BackgroundMediaItem? {
    val preview = image.orEmpty()
    val videoUrl = pickBestVideoUrl(videoFiles)
    val idStr = id.toString()
    if (preview.isBlank() || videoUrl.isNullOrBlank()) return null
    val durationSec = duration?.takeIf { it > 0 }
    return BackgroundMediaItem(
        id = idStr,
        author = user?.name.orEmpty(),
        previewUrl = preview,
        mediaUrl = videoUrl,
        isVideo = true,
        durationSec = durationSec,
    )
}

private fun pickBestVideoUrl(files: List<PexelsVideoFile>?): String? {
    if (files.isNullOrEmpty()) return null
    var fallback: String? = null
    for (f in files) {
        val link = f.link?.takeIf { it.isNotBlank() } ?: continue
        if (fallback == null) fallback = link
        val q = f.quality
        if (q == "hd" || q == "fhd") {
            return link
        }
    }
    return fallback
}
