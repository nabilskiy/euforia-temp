package digital.euforia.app.ui.onboardingV3

import digital.euforia.app.R

sealed class OnboardingV3Page(
    val stepId: String,
    val titleRes: Int? = null,
    val isSkippable: Boolean = false,
    val isBackAllowed: Boolean = true,
    val isQuestion: Boolean = false,
    val hidesShellChrome: Boolean = false,
) {
    data object StartPage : OnboardingV3Page(
        stepId = "start",
        isBackAllowed = false,
        hidesShellChrome = true,
    )

    data object About1Page : OnboardingV3Page(
        stepId = "about_1",
        titleRes = R.string.intro_v3_about_1_title,
        isBackAllowed = true,
        hidesShellChrome = false,
    )

    data object About2Page : OnboardingV3Page(
        stepId = "about_2",
        titleRes = R.string.intro_v3_about_2_title,
    )

    data object About3Page : OnboardingV3Page(
        stepId = "about_3",
        titleRes = R.string.intro_v3_about_3_title,
    )

    data object GoalsPage : OnboardingV3Page(
        stepId = "goals",
        titleRes = R.string.intro_goals_step_title,
        isSkippable = true,
        isQuestion = true,
    )

    data object GoalFeedbackPage : OnboardingV3Page(
        stepId = "goal_feedback",
        isSkippable = true,
    )

    data object FeedbackLoop1Page : OnboardingV3Page(
        stepId = "feedback_loop_1",
        isSkippable = true,
    )

    data object ProgramsPage : OnboardingV3Page(
        stepId = "programs",
        titleRes = R.string.intro_v3_programs_title,
        isSkippable = true,
        isQuestion = true,
    )

    data object DailyCommitmentPage : OnboardingV3Page(
        stepId = "daily_commitment",
        titleRes = R.string.intro_v3_daily_commitment_title,
        isSkippable = true,
        isQuestion = true,
    )

    data object TimePage : OnboardingV3Page(
        stepId = "time",
        titleRes = R.string.intro_v3_time_title,
        isSkippable = true,
        isQuestion = true,
    )

    data object ScenesPreviewPage : OnboardingV3Page(
        stepId = "scenes_preview",
        hidesShellChrome = true,
    )

    data object ScenesPage : OnboardingV3Page(
        stepId = "scenes",
        titleRes = R.string.intro_interests_step_title,
        isSkippable = true,
        isQuestion = true,
    )

    data object FeedbackLoop2Page : OnboardingV3Page(
        stepId = "feedback_loop_2",
        isSkippable = true,
    )

    data object DisclaimerPage : OnboardingV3Page(
        stepId = "disclaimer",
    )

    data object AgePage : OnboardingV3Page(
        stepId = "age",
        titleRes = R.string.intro_v3_age_title,
        isQuestion = true,
    )

    data object GenderPage : OnboardingV3Page(
        stepId = "gender",
        titleRes = R.string.intro_gender_step_title,
        isQuestion = true,
    )

    data object SummaryPage : OnboardingV3Page(
        stepId = "summary",
        hidesShellChrome = true,
    )

    data object SocialProofPage : OnboardingV3Page(
        stepId = "social_proof",
    )

    data object EmailPage : OnboardingV3Page(
        stepId = "email",
        titleRes = R.string.intro_email_step_title,
        isSkippable = true,
        isQuestion = true,
    )

    data object NamePage : OnboardingV3Page(
        stepId = "name",
        titleRes = R.string.intro_name_step_title,
        isSkippable = true,
        isQuestion = true,
    )

    data object NotificationsSetupPage : OnboardingV3Page(
        stepId = "notifications_setup",
        titleRes = R.string.intro_v3_notifications_setup_title,
        isSkippable = true,
    )

    data object LoadingPage : OnboardingV3Page(
        stepId = "loading",
        isBackAllowed = false,
        hidesShellChrome = true,
    )

    data object FirstExperiencePage : OnboardingV3Page(
        stepId = "first_experience",
        isBackAllowed = false,
        hidesShellChrome = true,
    )

    data object RatePage : OnboardingV3Page(
        stepId = "rate",
        isBackAllowed = false,
        hidesShellChrome = true,
    )

    data object PaywallPage : OnboardingV3Page(
        stepId = "paywall",
        isBackAllowed = false,
        hidesShellChrome = true,
    )
}

/** iOS IntroVariant == 3 order. */
val defaultOnboardingV3Pages = listOf(
    OnboardingV3Page.StartPage,
    OnboardingV3Page.About1Page,
    OnboardingV3Page.About2Page,
    OnboardingV3Page.About3Page,
    OnboardingV3Page.GoalsPage,
    OnboardingV3Page.GoalFeedbackPage,
    OnboardingV3Page.FeedbackLoop1Page,
    OnboardingV3Page.ProgramsPage,
    OnboardingV3Page.DailyCommitmentPage,
    OnboardingV3Page.TimePage,
    OnboardingV3Page.ScenesPreviewPage,
    OnboardingV3Page.ScenesPage,
    OnboardingV3Page.FeedbackLoop2Page,
    OnboardingV3Page.DisclaimerPage,
    OnboardingV3Page.AgePage,
    OnboardingV3Page.GenderPage,
    OnboardingV3Page.SummaryPage,
    OnboardingV3Page.SocialProofPage,
    OnboardingV3Page.EmailPage,
    OnboardingV3Page.NamePage,
    OnboardingV3Page.NotificationsSetupPage,
    OnboardingV3Page.LoadingPage,
    OnboardingV3Page.FirstExperiencePage,
    OnboardingV3Page.RatePage,
    OnboardingV3Page.PaywallPage,
)
