package digital.euforia.app.domain.usecase.settings

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.model.FormAnswerRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubmitFormUseCase @Inject constructor(
    private val api: EuforiaApi
) {

    suspend operator fun invoke(
        formId: String,
        answers: List<FormAnswerRequest>
    ) = withContext(Dispatchers.IO) { api.submitForm(formId, answers) }
}