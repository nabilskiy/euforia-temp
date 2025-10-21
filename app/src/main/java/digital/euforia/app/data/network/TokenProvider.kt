package digital.euforia.app.data.network

import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.di.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Qualifier

abstract class TokenProvider {
    var token: String? = null
//    fun getToken(): String? = token

    abstract suspend fun initToken()
}

//class DeviceTokenProvider @Inject constructor(
//    private val appPreferences: AppPreferences,
//    @ApplicationCoroutineScope private val scope: CoroutineScope
//) : TokenProvider() {
//    override fun initToken() {
//        scope.launch {
//            appPreferences.getDeviceTokenFlow().collectLatest {
//                token = it
//            }
//        }
//    }
//}

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class DeviceToken

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class AuthToken