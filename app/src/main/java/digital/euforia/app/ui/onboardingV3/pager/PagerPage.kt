package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import digital.euforia.app.R
import digital.euforia.app.ui.onboarding.pager.EmailPage
import digital.euforia.app.ui.onboardingV3.IntroAnswerKeys
import digital.euforia.app.ui.onboardingV3.OnboardingV3Page
import digital.euforia.app.ui.onboardingV3.OnboardingV3State
import digital.euforia.app.ui.onboardingV3.OnboardingV3ViewModel
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun PagerPage(
    viewModel: OnboardingV3ViewModel,
    state: OnboardingV3State,
    position: Int,
    activePosition: Int = state.currentPage.position,
    listTopPadding: Dp,
) {
    val pageType = state.pages.getOrNull(position) ?: return
    val isActive = { position == activePosition }
    val localizedRes = LocalLocalizedRes.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    when (pageType) {
        OnboardingV3Page.StartPage -> StartPage(viewModel, isPageActive = isActive())
        OnboardingV3Page.About1Page -> AboutPage(
            config = AboutPageConfig(
                titleRes = R.string.intro_v3_about_1_title,
                bodyRes = R.string.intro_v3_about_1_body,
                boldPartRes = R.string.intro_v3_about_1_bold,
                voiceRes = R.raw.snd_intro_audio_session_female,
                centerColor = Color(0xFFD69618),
                edgeColor = Color(0xFF3D2A0A),
                iconType = AboutIconType.Headphones,
            ),
            isPageActive = isActive(),
        )
        OnboardingV3Page.About2Page -> AboutPage(
            config = AboutPageConfig(
                titleRes = R.string.intro_v3_about_2_title,
                bodyRes = R.string.intro_v3_about_2_body,
                boldPartRes = R.string.intro_v3_about_2_bold,
                voiceRes = R.raw.snd_intro_meditation_female,
                centerColor = Color(0xFF395BD3),
                edgeColor = Color(0xFF0D1533),
                iconType = AboutIconType.Meditation,
            ),
            isPageActive = isActive(),
        )
        OnboardingV3Page.About3Page -> AboutPage(
            config = AboutPageConfig(
                titleRes = R.string.intro_v3_about_3_title,
                bodyRes = R.string.intro_v3_about_3_body,
                boldPartRes = R.string.intro_v3_about_3_bold,
                voiceRes = R.raw.snd_intro_soundscape_female,
                centerColor = Color(0xFFB1385F),
                edgeColor = Color(0xFF2A0A14),
                iconType = AboutIconType.Soundscapes,
            ),
            isPageActive = isActive(),
        )
        OnboardingV3Page.GoalsPage -> GoalsPage(
            viewModel = viewModel,
            goals = state.goals,
            selectedGoalId = state.selectedGoalId,
            topPadding = listTopPadding,
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
            topPadding = listTopPadding,
            isPageActive = isActive(),
        )
        OnboardingV3Page.DailyCommitmentPage -> SingleChoiceQuestionPage(
            answers = state.dailyCommitments,
            selectedId = state.introAnswers[IntroAnswerKeys.DAILY_COMMITMENT]?.identifier,
            onAnswerSelected = { viewModel.onAnswerSelected(IntroAnswerKeys.DAILY_COMMITMENT, it) },
            topPadding = listTopPadding,
            isPageActive = isActive(),
        )
        OnboardingV3Page.TimePage -> SingleChoiceQuestionPage(
            answers = state.timeOptions,
            selectedId = state.introAnswers[IntroAnswerKeys.TIME]?.identifier,
            onAnswerSelected = { viewModel.onAnswerSelected(IntroAnswerKeys.TIME, it) },
            topPadding = listTopPadding,
            isPageActive = isActive(),
        )
        OnboardingV3Page.ScenesPreviewPage -> ScenesPreviewPage(
            isPageActive = isActive(),
            onBackClick = viewModel::onPreviousPage,
            onNextClick = viewModel::onNextPage,
        )
        OnboardingV3Page.ScenesPage -> ScenesGridPage(
            scenes = state.scenes,
            selectedScenes = state.selectedScenes,
            onSceneSelected = viewModel::onSceneSelected,
            topPadding = listTopPadding,
            isPageActive = isActive(),
        )
        OnboardingV3Page.FeedbackLoop2Page -> FeedbackLoopPage(
            title = localizedRes.string(R.string.intro_v3_feedback_loop_2_title),
            text = localizedRes.string(R.string.intro_v3_feedback_loop_2_text),
            glowColor = Color(0xFF1A3D6B),
            isPageActive = isActive(),
        )
        OnboardingV3Page.DisclaimerPage -> DisclaimerPage(isPageActive = isActive())
        OnboardingV3Page.AgePage -> AgePage(
            selectedAge = state.age,
            isPageActive = isActive(),
            onAgeSelected = viewModel::onAgeSelected,
        )
        OnboardingV3Page.SummaryPage -> SummaryPage(
            selectedGoal = state.goals.find { it.identifier == state.selectedGoalId },
            selectedProgram = state.introAnswers[IntroAnswerKeys.PROGRAMS],
            selectedScenes = state.selectedScenes,
            age = state.age,
            isPageActive = isActive(),
            onNextClick = viewModel::onNextPage,
        )
        OnboardingV3Page.SocialProofPage -> SocialProofPage(isPageActive = isActive())
        OnboardingV3Page.EmailPage -> EmailPage(
            email = state.email,
            isValid = state.isNextEnabled,
            focusManager = focusManager,
            keyboardController = keyboardController,
            isPageOpened = isActive,
            onEmailUpdated = viewModel::onEmailUpdated,
        )
        OnboardingV3Page.NamePage -> NameV3Page(
            name = state.name,
            isPageActive = isActive(),
            onNameUpdated = viewModel::onNameUpdated,
        )
        OnboardingV3Page.NotificationsSetupPage -> NotificationsSetupV3Page(
            settings = state.notificationSettings,
            timeOfDayConfig = state.notificationTimeOfDayConfig,
            isPageActive = isActive(),
            onToggle = viewModel::onNotificationToggled,
            onTimeChanged = viewModel::onNotificationTimeChanged,
        )
        OnboardingV3Page.LoadingPage -> LoadingV3Page(
            isPageActive = isActive(),
            onFinished = viewModel::onNextPage,
        )
        OnboardingV3Page.PaywallPage -> PaywallV3Page(
            isPageActive = isActive(),
            onNextClick = viewModel::onNextPage,
        )
        OnboardingV3Page.FirstExperiencePage -> FirstExperiencePage(
            isPageActive = isActive(),
            selectedPreviewType = state.selectedPreviewType,
            reminderDayOffset = state.firstExperienceReminderDayOffset,
            reminderHour = state.firstExperienceReminderHour,
            reminderMinute = state.firstExperienceReminderMinute,
            reminderScheduledAt = state.firstExperienceReminderScheduledAt,
            isReminderScheduled = state.isFirstExperienceReminderScheduled,
            onPreviewTypeSelected = viewModel::onPreviewTypeSelected,
            onReminderTimeChanged = viewModel::onFirstExperienceReminderTimeChanged,
            onReminderScheduleClick = viewModel::onFirstExperienceReminderScheduled,
            onReminderProceedClick = viewModel::onFirstExperienceProceedAfterReminder,
            onReminderCancelClick = viewModel::onFirstExperienceReminderCancelled,
            onPlayNowClick = viewModel::onFirstExperiencePlayNow,
        )
        OnboardingV3Page.RatePage -> RateV3Page(
            isPageActive = isActive(),
            onSubmit = viewModel::onRateSubmitted,
        )
        OnboardingV3Page.KeepExploringPage -> KeepExploringV3Page(
            isPageActive = isActive(),
            onFinished = viewModel::onNextPage,
        )
        else -> PlaceholderPage(page = pageType, isPageActive = isActive())
    }
}
