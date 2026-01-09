package digital.euforia.app.domain.usecase.feedback

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.repository.FeedbackFormRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SyncFeedbackFormUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val repository: FeedbackFormRepository,
    private val remoteConfig: EuforiaRemoteConfigFetcher,
) {
    /**
     * Fetch feedback form id or alias from Remote Config and sync it into local DB.
     * If the RC value is absent/blank, falls back to alias "feedback".
     */
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        try {
            val id = remoteConfig.getFeedbackFormId()

            val result = api.getForm(id = id)
            result
                .onSuccess { form ->
                    if (form != null) {
                        repository.upsertFromNetwork(form)
                        Timber.d("Feedback form synced: id=${form.id}, alias=${form.alias}")
                    } else {
                        Timber.w("Feedback form response is null (id=$id)")
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to sync feedback form (id=$id)")
                }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error while syncing feedback form")
        }
    }
}
