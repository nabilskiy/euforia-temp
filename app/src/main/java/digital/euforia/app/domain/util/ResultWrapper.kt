@file:OptIn(ExperimentalContracts::class)
package digital.euforia.app.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

sealed class ResultWrapper<out T : Any?> : Serializable {

    inline val dataOrNull: T?
        get() = when (this) {
            is Success -> data
            else -> null
        }

    inline val dataOrThrow: T
        get() = when (this) {
            is Failure -> throw throwable
            is Success -> data
        }

    inline val throwableOrNull: Throwable?
        get() = when (this) {
            is Failure -> throwable
            else -> null
        }

    inline val isSuccess: Boolean
        get() = this is Success

    inline fun <R : Any?> map(transform: (data: T) -> R): ResultWrapper<R> {
        contract {
            callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Success -> Success(transform.invoke(data))
            is Failure -> this
        }
    }

    @Suppress("TooGenericExceptionCaught")
    inline fun <R : Any?> mapCatching(transform: (data: T) -> R): ResultWrapper<R> {
        contract {
            callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
        }

        return try {
            map(transform)
        } catch (t: Throwable) {
            Failure(t)
        }
    }

    inline fun <R : Any?> flatMap(transform: (data: T) -> ResultWrapper<R>): ResultWrapper<R> {
        contract {
            callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Failure -> this
            is Success -> transform.invoke(data)
        }
    }

    inline fun <R> fold(
        onSuccess: (data: T) -> R,
        onFailure: (Throwable) -> R
    ): R {
        contract {
            callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
            callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Success -> onSuccess.invoke(data)
            is Failure -> onFailure.invoke(throwable)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun onSuccess(block: (data: T) -> Unit): ResultWrapper<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        if (this is Success) {
            block.invoke(data)
        }
        return this
    }

    @OptIn(ExperimentalContracts::class)
    inline fun onFailure(block: (Throwable) -> Unit): ResultWrapper<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        if (this is Failure) {
            block.invoke(throwable)
        }
        return this
    }

    inline fun onFinish(block: () -> Unit): ResultWrapper<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        block.invoke()
        return this
    }

    companion object {
        fun <T> success(value: T): ResultWrapper<T> =
            Success(value)

        fun failure(throwable: Throwable): ResultWrapper<Nothing> =
            Failure(throwable)
    }

    data class Success<T>(val data: T) : ResultWrapper<T>()

    data class Failure(val throwable: Throwable) : ResultWrapper<Nothing>() {
        init {
            // Rethrow on CancellationException
            if (throwable is CancellationException) {
                throw throwable
            }
        }
    }
}

fun <T> Flow<ResultWrapper<T>>.onSuccess(block: (data: T) -> Unit): Flow<ResultWrapper<T>> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return this.map { result ->
        if (result is ResultWrapper.Success) {
            block.invoke(result.data)
        }
        result
    }
}

fun <T> Flow<ResultWrapper<T>>.onFailure(block: (Throwable) -> Unit): Flow<ResultWrapper<T>> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return this.map { result ->
        if (result is ResultWrapper.Failure) {
            block.invoke(result.throwable)
        }
        result
    }
}

@Suppress("TooGenericExceptionCaught")
inline fun <R : Any?> runCatchingWrapper(block: () -> R): ResultWrapper<R> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        ResultWrapper.success(block.invoke())
    } catch (t: Throwable) {
        ResultWrapper.failure(t)
    }
}

/**
 * Retry block of code [retries] times with progressive [delayMillis] delay between attempts.
 * Delay between attempts is calculated as [delayMillis] * (attempt + 1)
 * If all attempts fail, return last result
 *
 * @param retries number of attempts to retry
 * @param delayMillis delay between attempts
 * @param block block of code to retry
 */
suspend fun <T> retry(
    retries: Int = 3,
    delayMillis: Long = 1000L,
    block: suspend () -> ResultWrapper<T>,
): ResultWrapper<T> {
    repeat(retries) { attempt ->
        val result = block()
        if (result.isSuccess) {
            return result
        }
        delay(delayMillis * (attempt + 1))
    }
    return block()
}