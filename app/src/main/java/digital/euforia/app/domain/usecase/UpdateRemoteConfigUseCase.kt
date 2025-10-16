package digital.euforia.app.domain.usecase

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UpdateRemoteConfigUseCase @Inject constructor(
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
) {
    suspend operator fun invoke(initListener : (() -> Unit)) {
        withContext(Dispatchers.IO) {
            remoteConfigFetcher.initOnColdStartWithTimeout {
                initListener.invoke()
                Timber.d("Remote config fetched")
            }
        }
    }
}