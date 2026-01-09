package digital.euforia.app.domain.usecase.plan

import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckFirstWeekCompletion @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            val isDemo = profilePreferences.getIsDemo()
            if (isDemo) {

            }
        }
    }
}