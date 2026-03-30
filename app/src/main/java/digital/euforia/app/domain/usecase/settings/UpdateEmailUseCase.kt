package digital.euforia.app.domain.usecase.settings

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateEmailUseCase @Inject constructor(
    private val api: EuforiaApi,
    private val profilePreferences: ProfilePreferences
) {

    suspend operator fun invoke(email: String): ResultWrapper<Unit> {
        return withContext(Dispatchers.IO) {
            profilePreferences.setEmail(email)
            api.sendEmail(email)
        }
    }
}