package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import digital.euforia.app.R
import digital.euforia.app.ui.onboardingV3.IntroAnswerKeys
import digital.euforia.app.ui.onboardingV3.OnboardingV3Page
import digital.euforia.app.ui.onboardingV3.OnboardingV3State
import digital.euforia.app.ui.onboardingV3.OnboardingV3ViewModel
import digital.euforia.app.ui.util.LocalLocalizedRes

private fun OnboardingV3Page.isActive(state: OnboardingV3State, position: Int): Boolean {
    return state.currentPage.pageType == this && state.currentPage.position == position
}

@Composable
fun PagerPage(
    viewModel: OnboardingV3ViewModel,
    state: OnboardingV3State,
    position: Int,
) {
    val pageType = state.pages.getOrNull(position) ?: return
    val isActive = { pageType.isActive(state, position) }
    val localizedRes = LocalLocalizedRes.current

    when (pageType) {
        OnboardingV3Page.StartPage -> StartPage(viewModel, isPageActive = isActive())
        OnboardingV3Page.About1Page -> AboutPage(
            config = AboutPageConfig(
                titleRes = R.string.intro_v3_about_1_title,
                bodyRes = R.string.intro_v3_about_1_body,
                boldPartRes = R.string.intro_v3_about_1_bold,
                centerColor = Color(0xFFD69618),
                edgeColor = Color(0xFF3D2A0A),
                iconEmoji = "🎧",
            ),
            isPageActive = isActive(),
        )
        OnboardingV3Page.About2Page -> AboutPage(
            config = AboutPageConfig(
                titleRes = R.string.intro_v3_about_2_title,
                bodyRes = R.string.intro_v3_about_2_body,
                boldPartRes = R.string.intro_v3_about_2_bold,
                centerColor = Color(0xFF395BD3),
                edgeColor = Color(0xFF0D1533),
                iconEmoji = "🧘",
            ),
            isPageActive = isActive(),
        )
        OnboardingV3Page.About3Page -> AboutPage(
            config = AboutPageConfig(
                titleRes = R.string.intro_v3_about_3_title,
                bodyRes = R.string.intro_v3_about_3_body,
                boldPartRes = R.string.intro_v3_about_3_bold,
                centerColor = Color(0xFFB1385F),
                edgeColor = Color(0xFF2A0A14),
                iconEmoji = "🌿",
            ),
            isPageActive = isActive(),
        )
        OnboardingV3Page.GoalsPage -> GoalsPage(
            viewModel = viewModel,
            goals = state.goals,
            selectedGoalId = state.selectedGoalId,
            isPageActive = isActive(),
        )
        OnboardingV3Page.GoalFeedbackPage -> GoalFeedbackPage(
            selectedGoal = state.goals.find { it.identifier == state.selectedGoalId },
            isPageActive = isActive(),
        )
        OnboardingV3Page.FeedbackLoop1Page -> FeedbackLoopPage(
            title = localizedRes.string(R.string.intro_v3_feedback_loop_1_title),
            text = localizedRes.string(R.string.intro_v3_feedback_loop_1_text),
            glowColor = Color(0xFF002269),
            isPageActive = isActive(),
        )
        OnboardingV3Page.ProgramsPage -> SingleChoiceQuestionPage(
            answers = state.programs,
            selectedId = state.introAnswers[IntroAnswerKeys.PROGRAMS]?.identifier,
            onAnswerSelected = { viewModel.onAnswerSelected(IntroAnswerKeys.PROGRAMS, it) },
            isPageActive = isActive(),
        )
        OnboardingV3Page.DailyCommitmentPage -> SingleChoiceQuestionPage(
            answers = state.dailyCommitments,
            selectedId = state.introAnswers[IntroAnswerKeys.DAILY_COMMITMENT]?.identifier,
            onAnswerSelected = { viewModel.onAnswerSelected(IntroAnswerKeys.DAILY_COMMITMENT, it) },
            isPageActive = isActive(),
        )
        OnboardingV3Page.TimePage -> SingleChoiceQuestionPage(
            answers = state.timeOptions,
            selectedId = state.introAnswers[IntroAnswerKeys.TIME]?.identifier,
            onAnswerSelected = { viewModel.onAnswerSelected(IntroAnswerKeys.TIME, it) },
            isPageActive = isActive(),
        )
        OnboardingV3Page.FeedbackLoop2Page -> FeedbackLoopPage(
            title = localizedRes.string(R.string.intro_v3_feedback_loop_2_title),
            text = localizedRes.string(R.string.intro_v3_feedback_loop_2_text),
            glowColor = Color(0xFF1A3D6B),
            isPageActive = isActive(),
        )
        else -> PlaceholderPage(page = pageType, isPageActive = isActive())
    }
}
