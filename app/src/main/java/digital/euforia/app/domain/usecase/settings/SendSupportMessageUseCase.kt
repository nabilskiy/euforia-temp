package digital.euforia.app.domain.usecase.settings

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.model.FeedbackRequest
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendSupportMessageUseCase @Inject constructor(private val api: EuforiaApi) {
    suspend operator fun invoke(
        message: String,
        email: String
    ): ResultWrapper<Unit> {
        return withContext(Dispatchers.IO) {
            api.sendFeedback(
                FeedbackRequest(
                    email = email,
                    type = "support",
                    name = "",
                    message = message,
                    details = ""
                )
            )
        }
    }
}