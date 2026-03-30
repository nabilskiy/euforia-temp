package digital.euforia.app.data.config

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.internal.ConfigFetchHandler.DEFAULT_MINIMUM_FETCH_INTERVAL_IN_SECONDS
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import digital.euforia.app.R
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.domain.util.runCatchingWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await


abstract class FirebaseRemoteConfigFetcher(
    private val moshi: Moshi,
) {
    protected val remoteConfig
        get() = Firebase.remoteConfig

    private var isFetchingOnColdStart = false

    private val _fetchedAndActivatedFlow = MutableStateFlow(false)
    val fetchedAndActivatedFlow = _fetchedAndActivatedFlow.asStateFlow()

    private var initListener: (() -> Unit)? = null

    private val _abTestFlow = MutableStateFlow<Map<String, String>?>(null)
    val abTestFlow = _abTestFlow.asStateFlow()

    suspend fun initOnColdStartWithTimeout(
        timeout: Long = INITIAL_MAX_DELAY,
        initListener: () -> Unit
    ) {
        this.initListener = initListener

        // Start the actual fetch
        initOnColdStart()
    }

    suspend fun initOnColdStart() {
        isFetchingOnColdStart = true
        runCatching { // Preventing failure when no internet: "The client had an error while calling the backend"
            with(remoteConfig) {
                fetch(0).await()
                activate().await()
                _fetchedAndActivatedFlow.value = true
                notifyAndForget()
                isFetchingOnColdStart = false
                setFetchTimeoutForHotStart()
                onConfigsUpdated()
            }
        }
    }

    suspend fun waitUntilFetchedAndActivated() {
        fetchedAndActivatedFlow.first { it }
    }

    private fun notifyAndForget() {
        initListener?.invoke()
        initListener = null
    }

    suspend fun onHotStart() {
        if (!isFetchingOnColdStart) {
            remoteConfig.fetchAndActivate().await()
            onConfigsUpdated()
        }
    }

    open fun onConfigsUpdated() {
        val abTests = getAbTests()
        if (abTests.isNotEmpty()) {
            _abTestFlow.value = abTests
        }
    }

    private fun setFetchTimeoutForHotStart() {
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                fetchTimeoutInSeconds = DEFAULT_MINIMUM_FETCH_INTERVAL_IN_SECONDS
            }
        )
    }

    fun getAbTests(): Map<String, String> {
        return remoteConfig.all
            .filter { it.key.startsWith(KEY_PREFIX_AB_TEST) }
            .map { it.key to it.value.asString() }
            .toMap()
    }

    fun getCertificatesConfig(): List<String> {
        return getListConfig(
            key = KEY_AWS_CERTIFICATE_CONFIG,
        ).dataOrNull ?: emptyList()
    }

    protected fun <T, R> getConfig(
        key: String,
        moshiClazz: Class<T>,
        map: (T) -> R
    ): ResultWrapper<R> {
        return runCatchingWrapper {
            val json = remoteConfig.getString(key)
            moshi.adapter(moshiClazz)
                .fromJson(json)
                .let(::checkNotNull)
                .let(map::invoke)
        }.onFailure {
            logError(TAG, it)
        }
    }

    protected fun getListConfig(key: String): ResultWrapper<List<String>> {
        val listMyData = Types.newParameterizedType(MutableList::class.java, String::class.java)
        return runCatchingWrapper {
            val json = remoteConfig.getString(key)
            if (json.isBlank()) return ResultWrapper.success(emptyList())
            moshi.adapter<List<String>>(listMyData)
                .fromJson(json)
                .let(::checkNotNull)
        }.onFailure { logError(TAG, it) }
    }

    protected fun getBooleanConfig(key: String): ResultWrapper<Boolean> {
        return runCatchingWrapper {
            remoteConfig.getBoolean(key)
        }.onFailure { logError(TAG, it) }
    }

    abstract fun logError(tag: String, e: Throwable)

    suspend fun reset() {
        remoteConfig.reset().await()
    }

    companion object {
        const val TAG = "FirebaseRemoteConfig"
        private const val INITIAL_MAX_DELAY: Long = 2000L
        private const val KEY_AWS_CERTIFICATE_CONFIG = "aws_certificate_config"
        private const val KEY_PREFIX_AB_TEST = "ab_test"
    }
}
