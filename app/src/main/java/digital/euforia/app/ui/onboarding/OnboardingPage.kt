package digital.euforia.app.ui.onboarding

import digital.euforia.app.R

sealed class OnboardingPage(
    val titleRes: Int? = null,
    val subtitleRes: Int? = null,
    val isSkippable: Boolean = false,
    val isBackAllowed: Boolean = true
) {
    data object GenderPage :
        OnboardingPage(
            titleRes = R.string.intro_gender_step_title,
            isBackAllowed = false
        )

    data object LanguagePage : OnboardingPage(titleRes = R.string.intro_language_step_title)
    data object NamePage :
        OnboardingPage(
            titleRes = R.string.intro_name_step_title,
            isSkippable = true
        )

    data object GoalPage :
        OnboardingPage(
            titleRes = R.string.intro_goals_step_title,
            isSkippable = true
        )

    data object SamplesPage : OnboardingPage()
    data object InterestsPage :
        OnboardingPage(
            titleRes = R.string.intro_interests_step_title,
            subtitleRes = R.string.intro_interests_selected_count,
            isSkippable = true
        )

    data object NotificationsPage :
        OnboardingPage(
            titleRes = R.string.intro_notifications_step_title,
            subtitleRes = R.string.intro_notifications_step_subtitle,
            isSkippable = true
        )

    data object EmailPage : OnboardingPage(
        titleRes = R.string.intro_email_step_title,
        isSkippable = true
    )

    data object PreparePage : OnboardingPage()
    data object PlayerPage : OnboardingPage()
}

val defaultOnboardingPages = listOf(
    OnboardingPage.GenderPage,
    OnboardingPage.LanguagePage,
    OnboardingPage.NamePage,
    OnboardingPage.GoalPage,
    OnboardingPage.SamplesPage,
    OnboardingPage.InterestsPage,
    OnboardingPage.NotificationsPage,
    OnboardingPage.EmailPage,
    OnboardingPage.PreparePage,
    OnboardingPage.PlayerPage
)