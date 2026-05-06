/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Normalized item for soundscape scene background import (Unsplash / Pexels). */
data class BackgroundMediaItem(
    val id: String,
    val author: String,
    val previewUrl: String,
    val mediaUrl: String,
    val isVideo: Boolean,
    val durationSec: Int? = null,
)

// region Unsplash

@JsonClass(generateAdapter = true)
data class UnsplashSearchResponse(
    val results: List<UnsplashPhoto>? = null,
)

@JsonClass(generateAdapter = true)
data class UnsplashPhoto(
    val id: String,
    val user: UnsplashUser?,
    val urls: UnsplashUrls?,
)

@JsonClass(generateAdapter = true)
data class UnsplashUser(
    val name: String? = null,
)

@JsonClass(generateAdapter = true)
data class UnsplashUrls(
    val small: String? = null,
    val full: String? = null,
)

// endregion

// region Pexels

@JsonClass(generateAdapter = true)
data class PexelsPhotosResponse(
    val photos: List<PexelsPhoto>? = null,
)

@JsonClass(generateAdapter = true)
data class PexelsPhoto(
    val id: Long,
    val photographer: String? = null,
    val src: PexelsPhotoSrc? = null,
)

@JsonClass(generateAdapter = true)
data class PexelsPhotoSrc(
    val medium: String? = null,
    @Json(name = "large2x") val large2x: String? = null,
    val original: String? = null,
)

@JsonClass(generateAdapter = true)
data class PexelsVideosResponse(
    val videos: List<PexelsVideo>? = null,
)

@JsonClass(generateAdapter = true)
data class PexelsVideo(
    val id: Long,
    val user: PexelsNamedUser? = null,
    val image: String? = null,
    val duration: Int? = null,
    @Json(name = "video_files") val videoFiles: List<PexelsVideoFile>? = null,
)

@JsonClass(generateAdapter = true)
data class PexelsNamedUser(
    val name: String? = null,
)

@JsonClass(generateAdapter = true)
data class PexelsVideoFile(
    val quality: String? = null,
    val link: String? = null,
)

// endregion
