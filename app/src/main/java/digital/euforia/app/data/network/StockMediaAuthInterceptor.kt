/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.network

import digital.euforia.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds provider-specific auth headers for stock media APIs.
 * Must not reuse [HeadersInterceptor] (Euforia app headers are inappropriate for third-party hosts).
 */
class StockMediaAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        when (request.url.host) {
            HOST_UNSPLASH -> {
                builder.header("Accept-Version", "v1")
                builder.header("Authorization", "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}")
            }
            HOST_PEXELS -> {
                builder.header("Authorization", BuildConfig.PEXELS_API_KEY)
            }
        }
        return chain.proceed(builder.build())
    }

    private companion object {
        const val HOST_UNSPLASH = "api.unsplash.com"
        const val HOST_PEXELS = "api.pexels.com"
    }
}
