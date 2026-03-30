package digital.euforia.app.data.network

import android.os.Build
import digital.euforia.app.BuildConfig
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.util.toInt
import digital.euforia.app.di.ApplicationCoroutineScopeDefault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.Locale
import java.util.TimeZone

class HeadersInterceptor(
//    @DeviceToken private val deviceTokenProvider: DeviceTokenProvider,
//    @AuthToken private val authTokenProvider : AuthTokenProvider,
    private val tokensProvider: TokensProvider,
//    private val scope: CoroutineScope,
//    private val profileCache: ProfileCache,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader(ACCEPT_LANGUAGE, Locale.getDefault().language)
            .addHeader(APP_OS, ANDROID)
            .addHeader(APP_DEVICE, ANDROID)
            .addHeader(APP_VERSION, "${BuildConfig.VERSION_CODE}")
            .addHeader(APP_VERSION_NAME, BuildConfig.VERSION_NAME)
//            .addHeader(APP_GEN_TOKEN, authTokenProvider.token ?: "")
//            .addHeader(APP_SANDBOX, "${BuildConfig.IS_SANDBOX.toInt()}")
//            .addHeader(APP_SUBS_TOKEN, "mnjdaafflakhjgeefjnjefnb.AO-J1OxOepG-I34Qq2wiXAGkBvEXszYsvjv1_Wu0GrUhw9VWk_lzXB_aYsIi21bQiL_9yvyGiJYgQZiIj5c0QrVuZRmICyXKqQ")
            .addDeviceToken()
            .addAuthToken()
            .addTimezone()
//            .addSubsValid()
//            .addHeader(APP_SUBS_TOKEN, PreferencesManager.readSubsToken(this))
            .addHeader(USER_AGENT, USER_AGENT_VALUE + Build.MODEL + Build.ID)
            .build()

        return chain.proceed(request)
    }

    private fun addDeviceTokenHeader(builder: Request.Builder): Request.Builder {
        val token = tokensProvider.deviceToken
        if (!token.isNullOrBlank()) {
            builder.addHeader(APP_DEVICE_TOKEN, token)
        }
        return builder
    }

    private fun Request.Builder.addDeviceToken(): Request.Builder {
        // Ensure we have a concrete value on demand (may block briefly on first launch)
        val token = tokensProvider.getDeviceTokenBlocking()
        if (!token.isNullOrBlank()) {
            addHeader(APP_DEVICE_TOKEN, token)
        }
        return this
    }

    private fun Request.Builder.addAuthToken(): Request.Builder {
        // Ensure we have a concrete value on demand (may block briefly on first launch)
        val token = tokensProvider.getAuthTokenBlocking()
        if (!token.isNullOrBlank()) {
            addHeader(APP_GEN_TOKEN, token)
        }
        return this
    }

    private fun Request.Builder.addSubsValid(): Request.Builder {
        val isValid = tokensProvider.getIsPremiumBlocking()
        addHeader(AUTH_SUBS_VALID, isValid.toString())
        return this
    }

//    private fun Request.Builder.addFirebaseToken(): Request.Builder {
//        val token = Firebase.
//        if (!token.isNullOrBlank()) {
//            addHeader(FIREBASE_APP_CHECK, token)
//        }
//        return this
//    }

    private fun Request.Builder.addTimezone(): Request.Builder {
        // Build a GMT±HH:MM string without relying on java.time (to avoid crashes on older devices)
        val tz = TimeZone.getDefault()
        val offsetMillis =
            tz.rawOffset + if (tz.inDaylightTime(java.util.Date())) tz.dstSavings else 0
        val sign = if (offsetMillis >= 0) "+" else "-"
        val totalMinutes = kotlin.math.abs(offsetMillis / 60000)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val headerValue = String.format("GMT%s%02d:%02d", sign, hours, minutes)
        addHeader(APP_TIMEZONE, headerValue)
        return this
    }

//    private fun Request.Builder.addAuthorizationToken(value: String?): Request.Builder {
//        if (!value.isNullOrBlank()) {
//            addHeader(AUTHORISATION, BEARER + value)
//        }
//        return this
//    }

    private companion object {
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val APP_OS = "App-OS"
        const val ANDROID = "android"
        const val APP_VERSION = "App-Version"
        const val APP_VERSION_NAME = "App-Version-Name"
        const val APP_GEN_TOKEN = "App-Gen-Token"
        const val APP_DEVICE_TOKEN = "App-Device-Token"
        const val APP_DEVICE = "App-Device"
        const val APP_SUBS_TOKEN = "App-Subs-Token"
        const val APP_TIMEZONE = "App-Timezone"
        const val APP_SANDBOX = "App-Sandbox"
        const val USER_AGENT = "User-Agent" // can merge with NConst if desired
        const val FIREBASE_APP_CHECK = "X-Firebase-AppCheck"
        const val USER_AGENT_VALUE = "euforia-android-"
        const val AUTH_SUBS_VALID = "App-Subs-Valid"
    }
}