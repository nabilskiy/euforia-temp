package digital.euforia.app.domain.usecase.onboarding

import android.os.Build
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.ui.onboarding.OnboardingPage
import digital.euforia.app.ui.onboarding.defaultOnboardingPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetOnboardingPagesUseCase @Inject constructor(
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
    private val appPreferences: AppPreferences
) {

    suspend operator fun invoke(permissionGranted: Boolean): List<OnboardingPage> {
        return withContext(Dispatchers.IO) {
            try {
                buildList {
                    add(OnboardingPage.GenderPage)
                    if (remoteConfigFetcher.getIntroLangStepShow()) add(OnboardingPage.LanguagePage)
                    if (remoteConfigFetcher.getIntroNameStepShow()) add(OnboardingPage.NamePage)
                    if (remoteConfigFetcher.getIntroGoalsStepShow()) add(OnboardingPage.GoalPage)
                    add(OnboardingPage.SamplesPage)
                    add(OnboardingPage.InterestsPage)
                    if (remoteConfigFetcher.getIntroNotificationsStepShow()
                        && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !appPreferences.isNotificationPermissionGranted())
                    ) {
                        add(OnboardingPage.NotificationsPage)
                    }
                    if (remoteConfigFetcher.getIntroEmailStepShow()) add(OnboardingPage.EmailPage)

                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching onboarding pages from remote config")
                defaultOnboardingPages
            }
        }
    }
}
