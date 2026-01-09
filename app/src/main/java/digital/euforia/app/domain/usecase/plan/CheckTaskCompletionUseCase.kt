package digital.euforia.app.domain.usecase.plan

import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import javax.inject.Inject

class CheckTaskCompletionUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
) {
    suspend operator fun invoke(): Int {
        val isDemo = profilePreferences.getIsDemo()

        if (isDemo) {
            var completed = 1
            val completedDays = appPreferences.getCompletedDays()
            if (completedDays > 0) {
                completed += completed + 1
            }
            if (completedDays >= 7) {
                completed += completed + 1
            }
            return completed
        } else {
            return 1
        }
    }
}