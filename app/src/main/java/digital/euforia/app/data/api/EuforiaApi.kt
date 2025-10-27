package digital.euforia.app.data.api

import digital.euforia.app.data.model.NetworkAccompaniment
import digital.euforia.app.data.model.NetworkMusic
import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.data.model.NetworkSettings
import digital.euforia.app.data.model.NetworkSound
import digital.euforia.app.domain.util.ResultWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface EuforiaApi {
    @GET("exercises")
    suspend fun exercises(): Response<List<String>>

    @GET("articles")
    suspend fun articles(): Response<List<String>>

    @GET("sounds")
    suspend fun sounds(): ResultWrapper<List<NetworkSound>>

    @GET("music")
    suspend fun music(): ResultWrapper<List<NetworkMusic>>

    @GET("accompaniments/week")
    suspend fun getAccompanimentsPerWeek(
        @Query("demo") demo: Boolean
    ): ResultWrapper<List<NetworkAccompaniment>>

    @GET("accompaniments/today")
    suspend fun getTodayAccompaniments(
        @Query("demo") demo: Boolean = true
    ): ResultWrapper<NetworkAccompaniment>

    suspend fun settings(): ResultWrapper<NetworkSettings>

    @GET("packages")
    suspend fun getPackages(
        @Query("id") id: String,
        @Query("per-page") perPage: Int? = null
    ): ResultWrapper<NetworkPackage>
}