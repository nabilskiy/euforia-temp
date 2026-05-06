/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.api

import digital.euforia.app.data.model.UnsplashPhoto
import digital.euforia.app.data.model.UnsplashSearchResponse
import digital.euforia.app.domain.util.ResultWrapper
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    @GET("photos")
    suspend fun listPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("order_by") orderBy: String = "popular",
    ): ResultWrapper<List<UnsplashPhoto>>

    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("query") query: String,
    ): ResultWrapper<UnsplashSearchResponse>
}
