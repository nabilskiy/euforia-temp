/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.repository

import digital.euforia.app.data.api.PexelsApi
import digital.euforia.app.data.api.UnsplashApi
import digital.euforia.app.data.model.BackgroundMediaItem
import digital.euforia.app.data.model.toBackgroundMediaItem
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundImportRepository @Inject constructor(
    private val unsplashApi: UnsplashApi,
    private val pexelsApi: PexelsApi,
) {
    suspend fun searchUnsplashPhotos(query: String): ResultWrapper<List<BackgroundMediaItem>> =
        withContext(Dispatchers.IO) {
            val normalized = query.trim()
            if (normalized.isBlank()) {
                unsplashApi.listPhotos().mapCatching { photos ->
                    photos.mapNotNull { it.toBackgroundMediaItem() }
                }
            } else {
                unsplashApi.searchPhotos(query = normalized).mapCatching { body ->
                    body.results.orEmpty().mapNotNull { it.toBackgroundMediaItem() }
                }
            }
        }

    suspend fun searchPexels(query: String, videos: Boolean): ResultWrapper<List<BackgroundMediaItem>> =
        withContext(Dispatchers.IO) {
            val normalized = query.trim()
            if (videos) {
                if (normalized.isBlank()) {
                    pexelsApi.popularVideos().mapCatching { body ->
                        body.videos.orEmpty().mapNotNull { it.toBackgroundMediaItem() }
                    }
                } else {
                    pexelsApi.searchVideos(query = normalized).mapCatching { body ->
                        body.videos.orEmpty().mapNotNull { it.toBackgroundMediaItem() }
                    }
                }
            } else {
                if (normalized.isBlank()) {
                    pexelsApi.curatedPhotos().mapCatching { body ->
                        body.photos.orEmpty().mapNotNull { it.toBackgroundMediaItem() }
                    }
                } else {
                    pexelsApi.searchPhotos(query = normalized).mapCatching { body ->
                        body.photos.orEmpty().mapNotNull { it.toBackgroundMediaItem() }
                    }
                }
            }
        }
}
