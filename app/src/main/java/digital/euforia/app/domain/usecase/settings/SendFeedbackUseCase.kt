package digital.euforia.app.domain.usecase.settings

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.model.FeedbackRequest
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.settings.FeedbackType
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SendFeedbackUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val profilePreferences: ProfilePreferences,
) {
    suspend operator fun invoke(
        type: FeedbackType,
        message: String? = null,
        details: String? = null,
        userEmail: String? = null
    ) {
        withContext(Dispatchers.IO) {
            try {
                val name = profilePreferences.getName()
                val email = userEmail ?: profilePreferences.getEmail()
                api.submitFeedback(
                    FeedbackRequest(
                        type = type.typeName,
                        message = message.orEmpty(),
                        details = details.orEmpty(),
                        name = name.orEmpty(),
                        email = email.orEmpty()
                    )
                )

            } catch (e: Exception) {
                Timber.d(e, "Error sending feedback")
            }
        }
    }
}