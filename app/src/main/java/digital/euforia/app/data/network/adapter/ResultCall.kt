package digital.euforia.app.data.network.adapter

import digital.euforia.app.data.network.NetworkNotAvailableException
import digital.euforia.app.domain.util.ResultWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

internal class ResultCall<T>(proxy: Call<T>, private val isResponse: Boolean) :
    CallDelegate<T, Any>(proxy) {

    override fun enqueue(callback: Callback<Any>) {
        proxy.enqueue(ResultCallback(this, callback, isResponse))
    }

    override fun clone(): Call<Any> = ResultCall(proxy.clone(), isResponse)

    private class ResultCallback<T>(
        private val proxy: ResultCall<T>,
        private val callback: Callback<in Any>,
        private val isResponse: Boolean
    ) : Callback<T> {

        @Suppress("UNCHECKED_CAST")
        override fun onResponse(call: Call<T>, response: Response<T>) {
            val result = if (response.isSuccessful) {
                if (isResponse) {
                    ResultWrapper.success(response)
                } else {
                    ResultWrapper.success(response.body() as T)
                }
            } else {
                ResultWrapper.failure(HttpException(response))
            }
            callback.onResponse(proxy, Response.success<Any>(result))
        }

        override fun onFailure(call: Call<T>, error: Throwable) {
            when (error) {
                is CancellationException -> callback.onFailure(proxy, error)
                is UnknownHostException -> {
                    callback.onResponse(
                        proxy, Response.success<Any>(ResultWrapper.failure(
                            NetworkNotAvailableException()
                        ))
                    )
                }
                else -> {
                    val result = ResultWrapper.failure(error)
                    callback.onResponse(proxy, Response.success<Any>(result))
                }
            }
        }
    }
}