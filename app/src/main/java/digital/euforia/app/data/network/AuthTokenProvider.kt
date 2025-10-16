package digital.euforia.app.data.network

import digital.euforia.app.BuildConfig
import digital.euforia.app.data.md5
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.di.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthTokenProvider @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationCoroutineScope private val scope: CoroutineScope
) : TokenProvider() {



    override suspend fun initToken() {
        // Auth token is md5(deviceToken + API_KEY). Keep it updated whenever device token changes.
//        scope.launch {
            appPreferences.getDeviceTokenFlow().collectLatest { deviceToken ->
                token = "${deviceToken}${BuildConfig.API_KEY}".md5
            }
//        }
    }
}