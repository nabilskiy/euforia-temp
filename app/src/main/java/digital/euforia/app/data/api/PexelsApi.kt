/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.api

import digital.euforia.app.data.model.PexelsPhotosResponse
import digital.euforia.app.data.model.PexelsVideosResponse
import digital.euforia.app.domain.util.ResultWrapper
import retrofit2.http.GET
import retrofit2.http.Query

interface PexelsApi {
    @GET("v1/curated")
    suspend fun curatedPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
    ): ResultWrapper<PexelsPhotosResponse>

    @GET("v1/search")
    suspend fun searchPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("query") query: String,
    ): ResultWrapper<PexelsPhotosResponse>

    @GET("videos/popular")
    suspend fun popularVideos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
    ): ResultWrapper<PexelsVideosResponse>

    @GET("videos/search")
    suspend fun searchVideos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("query") query: String,
    ): ResultWrapper<PexelsVideosResponse>
}
