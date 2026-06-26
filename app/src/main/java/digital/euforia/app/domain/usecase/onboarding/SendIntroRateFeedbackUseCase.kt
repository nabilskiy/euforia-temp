package digital.euforia.app.domain.usecase.onboarding

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.model.FeedbackRequest
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.ui.onboardingV3.OnboardingV3PreviewType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.ceil

class SendIntroRateFeedbackUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val profilePreferences: ProfilePreferences,
) {
    suspend operator fun invoke(
        rating: Int,
        comment: String,
        previewType: OnboardingV3PreviewType,
        entityId: Int?,
    ) = withContext(Dispatchers.IO) {
        val message = comment.trim()
        if (message.length < 5) return@withContext

        val details = """
            rating: ${ceil(rating.toDouble()).toInt()}
            emotion: -
            entity_id: ${entityId ?: 0}
            content_type: ${previewType.rawValue}
        """.trimIndent()

        runCatching {
            api.sendFeedback(
                FeedbackRequest(
                    type = "rate",
                    message = message,
                    details = details,
                    email = profilePreferences.getEmail().orEmpty(),
                    name = profilePreferences.getName().orEmpty(),
                ),
            )
        }.onFailure {
            Timber.d(it, "Error sending intro rate feedback")
        }
    }
}

private val OnboardingV3PreviewType.rawValue: String
    get() = when (this) {
        OnboardingV3PreviewType.Accompaniment -> "accompaniment"
        OnboardingV3PreviewType.Meditation -> "meditation"
        OnboardingV3PreviewType.Soundscape -> "soundscape"
    }
