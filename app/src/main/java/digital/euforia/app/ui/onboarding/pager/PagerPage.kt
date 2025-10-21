package digital.euforia.app.ui.onboarding.pager

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import digital.euforia.app.ui.onboarding.OnboardingPage
import digital.euforia.app.ui.onboarding.OnboardingState
import digital.euforia.app.ui.onboarding.OnboardingViewModel

/**
 * Helper function to check if a specific page is currently opened.
 *
 * @param state The current onboarding state
 * @param currentPosition The position of the page being rendered
 * @return True if the current page matches the specified page type and position
 */
private fun OnboardingPage.isOpened(
    state: OnboardingState,
    currentPosition: Int,
): Boolean {
    return state.currentPage.pageType == this && state.currentPage.position == currentPosition
}

@Composable
fun PagerPage(
    viewModel: OnboardingViewModel,
    state: OnboardingState,
    currentPosition: Int,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
) {
    val pageType = state.pages[currentPosition]
    when (pageType) {
//        OnboardingPages.IntroPage -> IntroPage()
        OnboardingPage.GenderPage -> GenderPage(viewModel, state.selectedGender)
        OnboardingPage.LanguagePage -> LanguagePage(
            viewModel = viewModel,
            languages = state.languages,
            selectedLanguage = state.selectedLanguage,
            isPlaying = state.isPlayingVoiceSample
        )

        OnboardingPage.NamePage -> NamePage(
            name = state.name,
            focusManager = focusManager,
            keyboardController = keyboardController,
            isPageOpened = { pageType.isOpened(state, currentPosition) },
            onNameUpdated = viewModel::onNameUpdated
        )

        OnboardingPage.GoalPage -> GoalsPage(viewModel, state.goals, state.selectedGoals)
        OnboardingPage.SamplesPage -> SamplesPage(
            viewModel = viewModel,
            playbackState = state.samplePlaybackState,
            isPageOpened = { pageType.isOpened(state, currentPosition) },
        )

        OnboardingPage.InterestsPage -> InterestsPage(
            viewModel = viewModel,
            interests = state.interests,
            selectedInterests = state.selectedInterests,
        )

        OnboardingPage.NotificationsPage -> NotificationsPage(
            viewModel = viewModel,
            isPageOpened = { pageType.isOpened(state, currentPosition) }
        )

        OnboardingPage.EmailPage -> EmailPage(
            email = state.email,
            isValid = state.isNextEnabled,
            focusManager = focusManager,
            keyboardController = keyboardController,
            isPageOpened = { pageType.isOpened(state, currentPosition) },
            onEmailUpdated = viewModel::onEmailUpdated
        )

        OnboardingPage.PreparePage -> PreparePage()
        OnboardingPage.PlayerPage -> PlayerPage()
    }
}
