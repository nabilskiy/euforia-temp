package digital.euforia.app.data.network.adapter

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Response

internal abstract class CallDelegate<In, Out>(protected val proxy: Call<In>) : Call<Out> {

    override fun execute(): Response<Out> = throw NotImplementedError()

    override fun cancel() = proxy.cancel()

    override fun request(): Request = proxy.request()

    override fun isExecuted() = proxy.isExecuted

    override fun isCanceled() = proxy.isCanceled

    override fun timeout(): Timeout = proxy.timeout()
}