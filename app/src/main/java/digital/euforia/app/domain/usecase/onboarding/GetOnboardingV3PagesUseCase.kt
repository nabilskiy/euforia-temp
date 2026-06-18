package digital.euforia.app.domain.usecase.onboarding

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.ui.onboardingV3.OnboardingV3Page
import digital.euforia.app.ui.onboardingV3.defaultOnboardingV3Pages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetOnboardingV3PagesUseCase @Inject constructor(
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
) {

    suspend operator fun invoke(): List<OnboardingV3Page> = withContext(Dispatchers.IO) {
        defaultOnboardingV3Pages.filter { page ->
            when (page) {
                OnboardingV3Page.GoalFeedbackPage -> true // runtime: only if goal selected (VM)
                else -> remoteConfigFetcher.getIntroStepShow(page.stepId)
            }
        }
    }
}
