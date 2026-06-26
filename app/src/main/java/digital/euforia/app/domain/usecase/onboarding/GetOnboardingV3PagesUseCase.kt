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
                OnboardingV3Page.EmailPage -> remoteConfigFetcher.getIntroStepShow(page.stepId, default = false)
                OnboardingV3Page.LoadingPage,
                OnboardingV3Page.PaywallPage,
                OnboardingV3Page.KeepExploringPage -> true // matches iOS: new terminal steps bypass step_show RC
                else -> remoteConfigFetcher.getIntroStepShow(page.stepId)
            }
        }
    }
}
