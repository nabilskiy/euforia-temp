package digital.euforia.app.data.api

import digital.euforia.app.data.model.EmailRequest
import digital.euforia.app.data.model.FeedbackRequest
import digital.euforia.app.data.model.FormAnswerRequest
import digital.euforia.app.data.model.NetworkAccompaniment
import digital.euforia.app.data.model.NetworkArticle
import digital.euforia.app.data.model.NetworkCategory
import digital.euforia.app.data.model.NetworkCompose
import digital.euforia.app.data.model.NetworkExercise
import digital.euforia.app.data.model.NetworkFaqCategory
import digital.euforia.app.data.model.NetworkFaqItem
import digital.euforia.app.data.model.NetworkFeedbackForm
import digital.euforia.app.data.model.NetworkMeditation
import digital.euforia.app.data.model.NetworkMusic
import digital.euforia.app.data.model.NetworkMusicCategory
import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.data.model.NetworkPlaylist
import digital.euforia.app.data.model.NetworkPlaylistDetails
import digital.euforia.app.data.model.NetworkResource
import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.model.NetworkSearchResults
import digital.euforia.app.data.model.NetworkSettings
import digital.euforia.app.data.model.NetworkSound
import digital.euforia.app.data.model.NetworkSoundCategory
import digital.euforia.app.domain.util.ResultWrapper
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EuforiaApi {
    @GET("exercises")
    suspend fun exercises(): Response<List<String>>

    @GET("articles")
    suspend fun articles(): Response<List<String>>

    /**
     * Matches iOS `getSounds(pagination: PerPagePagination(perPage: 500, initialPage: 1))`.
     */
    @GET("sounds")
    suspend fun sounds(
        @Query("per-page") perPage: Int = 500,
        @Query("page") page: Int = 1,
    ): ResultWrapper<List<NetworkSound>>

    @GET("sounds/{id}")
    suspend fun sound(
        @Path("id") id: Int
    ): ResultWrapper<NetworkSound>

    /**
     * Matches iOS `getSoundCategories(pagination: PerPagePagination(perPage: 100, initialPage: 1))`.
     */
    @GET("categories/sound")
    suspend fun getSoundCategories(
        @Query("per-page") perPage: Int = 100,
        @Query("page") page: Int = 1,
    ): ResultWrapper<List<NetworkSoundCategory>>

    @GET("categories/sound/{id}")
    suspend fun getSoundCategory(
        @Path("id") id: Int
    ): ResultWrapper<NetworkSoundCategory>

    /**
     * Matches iOS `getMusics(pagination: PerPagePagination(perPage: 500, initialPage: 1))`.
     */
    @GET("music")
    suspend fun music(
        @Query("per-page") perPage: Int = 500,
        @Query("page") page: Int = 1,
    ): ResultWrapper<List<NetworkMusic>>

    @GET("music/{id}")
    suspend fun getMusic(
        @Path("id") id: Int
    ): ResultWrapper<NetworkMusic>

    /**
     * Matches iOS `getMusicCategories(pagination: PerPagePagination(perPage: 100, initialPage: 1))`.
     */
    @GET("categories/music")
    suspend fun getMusicCategories(
        @Query("per-page") perPage: Int = 100,
        @Query("page") page: Int = 1,
    ): ResultWrapper<List<NetworkMusicCategory>>

    @GET("categories/music/{id}")
    suspend fun getMusicCategory(
        @Path("id") id: Int
    ): ResultWrapper<NetworkMusicCategory>

    @GET("categories/scene")
    suspend fun getSceneCategories(): ResultWrapper<List<NetworkCategory>>

    @GET("scenes")
    suspend fun scenes(
        @Query("per-page") perPage: Int? = 500
    ): ResultWrapper<List<NetworkScene>>

    @GET("scenes/{id}")
    suspend fun scene(
        @Path("id") id: Int
    ): ResultWrapper<NetworkScene>

    @GET("playlists")
    suspend fun playlists(): ResultWrapper<List<NetworkPlaylist>>

    @GET("playlists/{id}")
    suspend fun playlist(
        @Path("id") id: Int
    ): ResultWrapper<NetworkPlaylistDetails>

    @GET("accompaniments/week")
    suspend fun getAccompanimentsPerWeek(
        @Query("demo") demo: Int
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

    @POST("forms/{id}")
    suspend fun submitForm(
        @Path("id") id: String,
        @Body body: List<FormAnswerRequest>
    ): ResultWrapper<Unit>

    @POST("feedback/create")
    suspend fun submitFeedback(
        @Body body: FeedbackRequest
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

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("meditations") searchMeditations: Int = 1,
        @Query("exercises") searchExercises: Int = 1,
        @Query("articles") searchArticles: Int = 1,
        @Query("scenes") searchScenes: Int = 0,
        @Query("limit") limit: Int = 50,
        @Query("author_id") authorId: Int? = null,
        @Query("pro") pro: String = "all",
        @Query("active") active: String = "active",
    ): ResultWrapper<NetworkSearchResults>

    @POST("compose")
    suspend fun compose(
        @Body body: RequestBody,
    ): ResultWrapper<List<NetworkCompose>>

    @POST("feedback")
    suspend fun sendFeedback(
        @Body body: FeedbackRequest
    ): ResultWrapper<Unit>

    @POST("email/create")
    suspend fun email(
        @Body body: EmailRequest
    ): ResultWrapper<Unit>

    @FormUrlEncoded
    @POST("email/create")
    suspend fun sendEmail(
        @Field("email") email: String
    ): ResultWrapper<Unit>
}