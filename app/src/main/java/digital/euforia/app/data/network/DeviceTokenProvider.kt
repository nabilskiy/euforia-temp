package digital.euforia.app.data.network

import digital.euforia.app.BuildConfig
import digital.euforia.app.data.util.md5
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.di.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class DeviceTokenProvider @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationCoroutineScope private val scope: CoroutineScope
) : TokenProvider() {


    override suspend fun initToken() {
        appPreferences.getDeviceTokenFlow().collectLatest { deviceToken ->
            token = deviceToken
        }
    }
}

class TokensProvider @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
//    @ApplicationCoroutineScope private val scope: CoroutineScope
) {
    // Use nullable vars with safe defaults to avoid UninitializedPropertyAccessException
    // on first app launch before tokens are generated.
    var deviceToken: String? = null
    var authToken: String? = null
    var isPremium: Boolean? = null
    var isInitialized: Boolean = false

    suspend fun initTokens() {
//        scope.launch {
        appPreferences.getDeviceTokenFlow().collectLatest { it ->
            deviceToken = it
            authToken = "${it}${BuildConfig.API_KEY}".md5
            isInitialized = true
        }
        profilePreferences.getIsPremiumFlow().collectLatest { isPremiumValue ->
            isPremium = isPremiumValue
        }
//        }
    }

    suspend fun initDeviceToken() {
        val token = appPreferences.initDeviceToken()
        deviceToken = token
        authToken = "${token}${BuildConfig.API_KEY}".md5
        isInitialized = true
    }

    /**
     * Returns a device token, generating and persisting it if necessary.
     * Safe to call from any thread; will block briefly on first access only.
     */
    fun getDeviceTokenBlocking(): String? {
        if (deviceToken.isNullOrBlank()) {
            // Generate and cache the token synchronously on first access
            kotlinx.coroutines.runBlocking {
                initDeviceToken()
            }
        }
        return deviceToken
    }

    /**
     * Returns the auth token (md5(deviceToken+API_KEY)). Ensures device token exists.
     * Safe to call from any thread; will block briefly on first access only.
     */
    fun getAuthTokenBlocking(): String? {
        if (authToken.isNullOrBlank()) {
            // Ensure device token is initialized; this also sets authToken
            kotlinx.coroutines.runBlocking {
                if (deviceToken.isNullOrBlank()) {
                    initDeviceToken()
                } else {
                    val token = deviceToken
                    authToken = "${token}${BuildConfig.API_KEY}".md5
                    isInitialized = true
                }
            }
        }
        return authToken
    }

    fun getIsPremiumBlocking(): Boolean {
//        return kotlinx.coroutines.runBlocking {
//            profilePreferences.getIsPremium()
//        }
        if (isPremium == null) {
            // Ensure isPremium is initialized
            kotlinx.coroutines.runBlocking {
                val isPremiumValue = profilePreferences.getIsPremium()
                isPremium = isPremiumValue
            }
        }
        return isPremium ?: false
    }
}

//    suspend fun getDeviceToken(): String {
//        return scope.async { appPreferences.getDeviceToken() }.await()
//    }
//
//    suspend fun getAuthToken(): String? {
//        return scope.async {
//            val token = appPreferences.getDeviceToken()
//            "$token${BuildConfig.API_KEY}".md5
//        }.await()
//    }
//}
