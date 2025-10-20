package digital.euforia.app.data.network

import digital.euforia.app.BuildConfig
import digital.euforia.app.data.util.md5
import digital.euforia.app.data.store.AppPreferences
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
//    @ApplicationCoroutineScope private val scope: CoroutineScope
) {
    lateinit var deviceToken: String
    lateinit var authToken: String

    suspend fun initTokens() {
//        scope.launch {
        appPreferences.getDeviceTokenFlow().collectLatest { it ->
            deviceToken = it
            authToken = "${deviceToken}${BuildConfig.API_KEY}".md5
        }
//        }
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
}
