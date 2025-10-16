package digital.euforia.app.data.network.adapter

import digital.euforia.app.domain.util.ResultWrapper
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java || returnType !is ParameterizedType) return null

        // Call<*>
        val callInnerType = getParameterUpperBound(0, returnType)
        if (getRawType(callInnerType) != ResultWrapper::class.java) return null

        // Call<ResultWrapper<Nothing> or Call<ResultWrapper<*>
        if (callInnerType !is ParameterizedType) {
            return ResultCallAdapter<Nothing>(Nothing::class.java, false)
        }

        // Call<ResultWrapper<Any?>
        val resultInnerType = getParameterUpperBound(0, callInnerType)
        var isResponse = false
        val realResponseType = if (getRawType(resultInnerType) == Response::class.java) {
            // Call<ResultWrapper<Response<Any?>>
            require(resultInnerType is ParameterizedType) {
                "Response must be parameterized as Response<Foo> or Response<? extends Foo>"
            }
            isResponse = true
            getParameterUpperBound(0, resultInnerType)
        } else {
            // Call<ResultWrapper<Any?>
            resultInnerType
        }

        return ResultCallAdapter<Any?>(realResponseType, isResponse)
    }

    private class ResultCallAdapter<R>(
        private val type: Type,
        private val isResponse: Boolean
    ) : CallAdapter<R, Any> {

        override fun responseType(): Type = type

        override fun adapt(call: Call<R>): Any = ResultCall(call, isResponse)
    }
}