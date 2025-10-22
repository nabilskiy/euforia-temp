package digital.euforia.app.domain.usecase.translation

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncTranslationsUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val configFetcher: EuforiaRemoteConfigFetcher
) {
    suspend operator fun invoke(): Unit {
        withContext(Dispatchers.IO) {
            val json = configFetcher.getStringsJson()
            appPreferences.setTranslationsJson(json)
        }
    }
}