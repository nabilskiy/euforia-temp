package digital.euforia.app.data.api

import digital.euforia.app.data.model.NetworkAccompaniment
import digital.euforia.app.data.model.NetworkArticle
import digital.euforia.app.data.model.NetworkCategory
import digital.euforia.app.data.model.NetworkExercise
import digital.euforia.app.data.model.NetworkFaqCategory
import digital.euforia.app.data.model.NetworkFaqItem
import digital.euforia.app.data.model.NetworkFeedbackForm
import digital.euforia.app.data.model.NetworkMeditation
import digital.euforia.app.data.model.NetworkMusic
import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.data.model.NetworkResource
import digital.euforia.app.data.model.NetworkSettings
import digital.euforia.app.data.model.NetworkSound
import digital.euforia.app.domain.util.ResultWrapper
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

    @GET("settings")
    suspend fun settings(): ResultWrapper<NetworkSettings>

    @GET("packages")
    suspend fun getPackages(
        @Query("id") id: String? = null,
        @Query("per-page") perPage: Int? = null
    ): ResultWrapper<NetworkPackage?>

    @GET("packages")
    suspend fun getAllPackages(
        @Query("id") id: String? = null,
        @Query("per-page") perPage: Int? = null
    ): ResultWrapper<List<NetworkPackage>>

    @GET("resources")
    suspend fun getResources(
        @Query("class_alias") classAlias: String? = null,
        @Query("per-page") perPage: Int? = null
    ): ResultWrapper<List<NetworkResource>>

    /**
     * Returns list of FAQ categories.
     */
    @GET("faq-categories")
    suspend fun getFaqCategories(): ResultWrapper<List<NetworkFaqCategory>>

    @GET("faq")
    suspend fun getFaqItems(
        @Query("type") type: String
    ): ResultWrapper<List<NetworkFaqItem>>

    /**
     * Returns a feedback form by id or alias.
     */
    @GET("forms")
    suspend fun getForm(
        @Query("id") id: String? = null,
    ): ResultWrapper<NetworkFeedbackForm?>

    @POST("feedback/create")
    suspend fun submitFeedback(
        @Query("type") type: String,
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("message") message: String,
        @Query("details") details: String,
    ): ResultWrapper<Unit>

    @POST("subscription")
    suspend fun subscription(
        @Body body: RequestBody,
    ): ResultWrapper<Any>

    @GET("meditations")
    suspend fun getMeditations(
        @Query("ids") ids: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("package_id") packageId: Int? = null,
        @Query("q") q: String? = null,
    ): ResultWrapper<List<NetworkMeditation>>

    @GET("articles")
    suspend fun getArticles(
        @Query("ids") ids: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("q") q: String? = null,
    ): ResultWrapper<List<NetworkArticle>>

    @GET("exercises")
    suspend fun getExercises(
        @Query("ids") ids: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("q") q: String? = null,
        @Query("pro") pro: String? = null, // all, pro, free
    ): ResultWrapper<List<NetworkExercise>>

    @GET("articles-content/{id}")
    suspend fun getArticleContent(
        @Path("id") id: Int
    ): ResultWrapper<ResponseBody>

    @GET("exercises/{id}")
    suspend fun getExercise(
        @Path("id") id: Int
    ): ResultWrapper<NetworkExercise>

    @GET("meditations/{id}")
    suspend fun getMeditation(
        @Path("id") id: Int
    ): ResultWrapper<NetworkMeditation>

    @GET("articles/{id}")
    suspend fun getArticle(
        @Path("id") id: Int
    ): ResultWrapper<NetworkArticle>

    @GET("packages/{id}")
    suspend fun getPackage(
        @Path("id") id: Int
    ): ResultWrapper<NetworkPackage>

    @GET("categories/{type}/{id}")
    suspend fun getCategory(
        @Path("type") type: String,
        @Path("id") id: Int
    ): ResultWrapper<NetworkCategory>
}