package digital.euforia.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.onboarding.Gender
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.domain.model.onboarding.Interest
import digital.euforia.app.domain.model.onboarding.Language
import digital.euforia.app.domain.model.onboarding.getByTag
import digital.euforia.app.domain.model.onboarding.getVoiceRes
import digital.euforia.app.domain.usecase.onboarding.GetAppLanguageUseCase
import digital.euforia.app.domain.usecase.onboarding.GetGoalsUseCase
import digital.euforia.app.domain.usecase.onboarding.GetInterestsUseCase
import digital.euforia.app.domain.usecase.onboarding.GetLanguageOptionsUseCase
import digital.euforia.app.domain.usecase.onboarding.GetOnboardingPagesUseCase
import digital.euforia.app.domain.usecase.onboarding.ValidateEmailUseCase
import digital.euforia.app.ui.util.MediaPlayerHelper
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    val getOnboardingPagesUseCase: GetOnboardingPagesUseCase,
    val getLanguageOptionsUseCase: GetLanguageOptionsUseCase,
    val getGoalsUseCase: GetGoalsUseCase,
    val getInterestsUseCase: GetInterestsUseCase,
    val getAppLanguageUseCase: GetAppLanguageUseCase,
    val validateEmailUseCase: ValidateEmailUseCase,
    @ApplicationContext val context: android.content.Context,
) : ViewModel(),
    ContainerHost<OnboardingState, OnboardingSideEffect> {
    override val container = container<OnboardingState, OnboardingSideEffect>(
        initialState = OnboardingState(),
        onCreate = {
            observeLanguage()
            initOnboardingState()
            observerNotificationPermission()
        }
    )

    private var samplesPlaybackJob: Job? = null

    private fun observeLanguage() {
        viewModelScope.launch {
            appPreferences.getLanguageFlow().collectLatest { languageTag ->
                val languageOption = getByTag(languageTag)
                reduceState { copy(selectedLanguage = languageOption) }
            }
        }
    }

    private fun observerNotificationPermission() = viewModelScope.launch {
        appPreferences.isNotificationPermissionGrantedFlow().collectLatest { granted ->
            intent {
                val pages = getOnboardingPagesUseCase.invoke(granted)
                reduce { state.copy(pages = pages) }
            }
        }
    }

    private fun initOnboardingState() {
        viewModelScope.launch {
            intent {
                val pages = getOnboardingPagesUseCase.invoke(state.notificationPermissionGranted)
                val languageOptions = getLanguageOptionsUseCase.invoke()
                val languageTag = getAppLanguageUseCase.invoke()
                val goals = getGoalsUseCase.invoke(languageTag)
                val interests = getInterestsUseCase.invoke(languageTag)
                reduce {
                    state.copy(
                        pages = pages,
                        languages = languageOptions,
                        goals = goals,
                        interests = interests,
                    )
                }
            }
        }
    }

    fun onPageUpdated(position: Int) {
        val state = container.stateFlow.value
        val pageType = state.pages.getOrNull(position) ?: defaultOnboardingPages.first()

//        intent { reduce { state.copy(currentPage = state.currentPage.copy(position, pageType)) } }
//        updateCurrentPage(CurrentPage(position, pageType))
    }

    fun onNextPage(skipPage: Boolean = false) {
        isNextPageAllowed(
            onAllowed = {
                intent {
//        val currentState = container.stateFlow.value
                    val nextPagePosition =
                        if (!skipPage) state.currentPage.position + 1 else state.currentPage.position + 2
//            val nextPagePosition = state.currentPage.position + 1

                    if (nextPagePosition < state.pages.size) {
                        val newCurrentPage = state.currentPage.copy(
                            position = nextPagePosition,
                            pageType = state.pages[nextPagePosition]
                        )
                        if (newCurrentPage.pageType == OnboardingPage.LanguagePage) {
                            playVoiceSample()
                        } else if (newCurrentPage.pageType == OnboardingPage.SamplesPage) {
                            playSoundSamples()
                        }
                        applyNextButtonVisibility(newCurrentPage)
                        reduce { state.copy(currentPage = newCurrentPage) }
//            updateCurrentPage(newCurrentPage)
                    } else {
//            navigateToHome()
                    }
                }
            },
            onBlocked = {}
        )

    }

    private fun isNextPageAllowed(
        onAllowed: () -> Unit,
        onBlocked: () -> Unit
    ) {
        intent {
            if (state.currentPage.pageType is OnboardingPage.EmailPage) {
                state.email?.let { email ->
                    val isEmailValid = validateEmailUseCase.invoke(email)
                    reduce { state.copy(isNextEnabled = isEmailValid) }
                    if (isEmailValid) {
                        onAllowed()
                    } else onBlocked()
                } ?: run {
                    reduce { state.copy(isNextEnabled = false) }
                    onBlocked()
                }
            } else {
                onAllowed()
            }
        }
    }

    fun onPreviousPage() {
        intent {
//            val currentState = container.stateFlow.value
            val previousPagePosition = state.currentPage.position - 1

            if (previousPagePosition >= 0) {
                val newCurrentPage = state.currentPage.copy(
                    position = previousPagePosition,
                    pageType = state.pages[previousPagePosition]
                )
                applyNextButtonVisibility(newCurrentPage)
                reduce { state.copy(currentPage = newCurrentPage) }
//                updateCurrentPage(newCurrentPage)
            }
        }
    }

    fun onNotificationPermissionGranted() {
        intent {
            appPreferences.setNotificationPermissionGranted(true)
//            if (state.currentPage == OnboardingPage.NotificationsPage) {
            if (state.currentPage.pageType == OnboardingPage.NotificationsPage) {
                reduce {
                    state.copy(
                        currentPage = CurrentPage(
                            position = state.currentPage.position,
                            pageType = OnboardingPage.EmailPage
                        )
                    )
                }
            }
//            }
//            val state = container.stateFlow.value
//            val filteredPages = state.pages.filter { it != OnboardingPage.NotificationsPage }
//            reduce { state.copy(pages = filteredPages, notificationPermissionGranted = true) }
        }
    }

    fun onGenderSelected(gender: Gender) {
        val state = container.stateFlow.value
        if (state.selectedGender == gender) return
        reduceState { copy(selectedGender = gender) }
    }

    fun onLanguageSelected(language: Language) {
        val state = container.stateFlow.value
        if (state.selectedLanguage == language) return
        viewModelScope.launch {
            appPreferences.setLanguage(language.tag)
            // Update the application locale to load localized resources
//            val localeList = LocaleListCompat.forLanguageTags(language.tag)
//            AppCompatDelegate.setApplicationLocales(localeList)
            playVoiceSample()
            // Create a locale from the language tag and pass it to GetGoalsUseCase
            val goals = getGoalsUseCase.invoke(language.tag)
            val interests = getInterestsUseCase.invoke(language.tag)
            reduceState { copy(selectedLanguage = language, goals = goals, interests = interests) }
        }
    }

    private fun applyNextButtonVisibility(currentPage: CurrentPage) {
        intent {
            val isVisible = when (currentPage.pageType) {
                OnboardingPage.GoalPage -> state.selectedGoals.isNotEmpty()
                OnboardingPage.InterestsPage -> state.selectedInterests.isNotEmpty()
                else -> true
            }
            val isEnabled = when (currentPage.pageType) {
                else -> true
            }
            reduce { state.copy(isNextButtonVisible = isVisible, isNextEnabled = isEnabled) }
        }
    }

    fun onNameUpdated(name: String?) {
        reduceState { copy(name = name) }
    }

    fun onEmailUpdated(email: String?) {
        reduceState { copy(email = email, isNextEnabled = true) }
    }

    fun onGoalToggled(index: Int) {
        val state = container.stateFlow.value
        val newGoals = state.selectedGoals.toMutableSet()
        if (newGoals.contains(index)) {
            newGoals.remove(index)
        } else {
            newGoals.add(index)
        }
        val isNextVisible = newGoals.isNotEmpty()
        reduceState { copy(selectedGoals = newGoals, isNextButtonVisible = isNextVisible) }
    }

    fun onInterestToggled(index: Int) {
        val state = container.stateFlow.value
        val newInterests = state.selectedInterests.toMutableSet()
        if (newInterests.contains(index)) {
            newInterests.remove(index)
        } else {
            if (newInterests.size < 3) newInterests.add(index)
        }
        val isNextVisible = newInterests.isNotEmpty()
        reduceState { copy(selectedInterests = newInterests, isNextButtonVisible = isNextVisible) }
    }

    private fun playVoiceSample() {
        val state = container.stateFlow.value
        state.selectedLanguage.getVoiceRes(state.selectedGender)?.let {
            reduceState { copy(isPlayingVoiceSample = true) }
            MediaPlayerHelper.play(
                context = context,
                soundRes = it,
                onCompletion = { reduceState { copy(isPlayingVoiceSample = false) } })
        } ?: run {
            reduceState { copy(isPlayingVoiceSample = false) }
            MediaPlayerHelper.release()
        }
    }

    fun onSampleSelected(soundRes: Int) {
        MediaPlayerHelper.release()
        reduceState { copy(isPlayingVoiceSample = true) }
        MediaPlayerHelper.play(
            context = context,
            soundRes = soundRes,
            onCompletion = { reduceState { copy(isPlayingVoiceSample = false) } })
    }

    fun playSoundSamples() {
        samplesPlaybackJob?.cancel()
        updateSamplePlaybackState(SamplePlaybackState(isPlaying = true, currentSampleIndex = 0))
        var soundResIndex = 0
        samplesPlaybackJob = viewModelScope.launch {
            while (isActive) {
                MediaPlayerHelper.release()
                MediaPlayerHelper.play(
                    context = context,
                    soundRes = audioResList[soundResIndex],
                    onCompletion = {
                        updateSamplePlaybackState(SamplePlaybackState())
                        samplesPlaybackJob?.cancel()
                        samplesPlaybackJob = null
                    })
                delay(5000L)
                val state = container.stateFlow.value
                if (state.currentPage.pageType != OnboardingPage.SamplesPage) {
                    MediaPlayerHelper.release()
                    cancel()
                } else {
                    if (soundResIndex == audioResList.lastIndex) {
                        soundResIndex = 0
                    } else soundResIndex++
                }
                updateSamplePlaybackState(SamplePlaybackState(true, soundResIndex))
            }
        }

    }

//    fun updateCurrentPage(currentPage: CurrentPage) {
//        reduceState { copy(currentPage = currentPage) }
//    }

    fun updateSamplePlaybackState(playbackState: SamplePlaybackState) {
        reduceState { copy(samplePlaybackState = playbackState) }
    }

    override fun onCleared() {
        MediaPlayerHelper.release()
        super.onCleared()
    }
}

data class OnboardingState(
    val pages: List<OnboardingPage> = emptyList(),
    val languages: List<Language> = emptyList(),
    val selectedLanguage: Language = Language.EN,
    val selectedGender: Gender = Gender.FEMALE,
    val goals: List<Goal> = emptyList(),
    val selectedGoals: Set<Int> = emptySet(),
    val interests: List<Interest> = emptyList(),
    val selectedInterests: Set<Int> = emptySet(),
    val currentPage: CurrentPage = CurrentPage(),
    val isPlayingVoiceSample: Boolean = false,
    val samplePlaybackState: SamplePlaybackState = SamplePlaybackState(),
//    val isPlayingSoundSample: Boolean = false,
    val playingSampleIndex: Int = 0,
    val email: String? = null,
    val name: String? = null,
    val notificationPermissionGranted: Boolean = false,
    val isNextEnabled: Boolean = true,
    val isNextButtonVisible: Boolean = true,
)

data class CurrentPage(
    val position: Int = 0,
    val pageType: OnboardingPage = OnboardingPage.GenderPage
)

data class SamplePlaybackState(
    val isPlaying: Boolean = false,
    val currentSampleIndex: Int = 0,
)

sealed class OnboardingSideEffect {
    data object NavigateHome : OnboardingSideEffect()
    data object NavigatePaywall : OnboardingSideEffect()
}

private val audioResList: List<Int> = listOf(
    R.raw.bgm_intro_scene_1,
    R.raw.bgm_intro_scene_2,
    R.raw.bgm_intro_scene_3
)
