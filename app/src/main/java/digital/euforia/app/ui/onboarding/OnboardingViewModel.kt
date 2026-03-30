package digital.euforia.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
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
import digital.euforia.app.ui.util.openWebLink
import digital.euforia.app.ui.util.BackgroundPlayerHelper
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    val profilePreferences: ProfilePreferences,
    val getOnboardingPagesUseCase: GetOnboardingPagesUseCase,
    val getLanguageOptionsUseCase: GetLanguageOptionsUseCase,
    val getGoalsUseCase: GetGoalsUseCase,
    val getInterestsUseCase: GetInterestsUseCase,
    val getAppLanguageUseCase: GetAppLanguageUseCase,
    val validateEmailUseCase: ValidateEmailUseCase,
    val analyticSender: AnalyticSender,
    @ApplicationContext val context: android.content.Context,
) : ViewModel(),
    ContainerHost<OnboardingState, OnboardingSideEffect> {
    override val container = container<OnboardingState, OnboardingSideEffect>(
        initialState = OnboardingState(),
        onCreate = {
            observeLanguage()
            initOnboardingState()
            observerNotificationPermission()

//            BackgroundPlayerHelper.playLooping(
//                context = context,
//                soundRes = R.raw.bgm_intro
//            )
            BackgroundPlayerHelper.playLooping(context, R.raw.bgm_intro)
            analyticSender.introShow()
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

    fun onTermsClicked() {
        analyticSender.introTermsClick()
        openUrl(context.getString(R.string.link_terms))
    }

    fun onPrivacyClicked() {
        analyticSender.introTermsClick()
        openUrl(context.getString(R.string.link_privacy))
    }

    private fun openUrl(url: String) {
        openWebLink(context, url)
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
        logPageOpened(pageType)
    }

    fun onSkipPage() {
        intent {
            if (state.currentPage.pageType == OnboardingPage.SamplesPage) {
                MediaPlayerHelper.stopWithFade(1000L)
            }
            logSkipPage(state.currentPage)
            val nextPagePosition = state.currentPage.position + 1

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
            } else {
                // Onboarding finished
                val name = state.name.orEmpty()
                profilePreferences.setName(name)
                if (name.isNotEmpty()) {
                    analyticSender.setName(name)
                }
                validateEmailUseCase.invoke(state.email.orEmpty()).let { isEmailValid ->
                    if (isEmailValid) profilePreferences.setEmail(state.email.orEmpty())
                }
                profilePreferences.setGender(state.selectedGender)
                appPreferences.setOnboardingCompleted(true)
                postSideEffect(OnboardingSideEffect.NavigateAudioPlayer())
            }
        }
    }

    fun onNextPage(skipPage: Boolean = false) {
        isNextPageAllowed(
            onAllowed = {
                intent {
                    if (state.currentPage.pageType == OnboardingPage.SamplesPage) {
                        MediaPlayerHelper.stopWithFade(1000L)
                    }
                    logNextPage(state.currentPage)
                    val nextPagePosition =
                        if (!skipPage) state.currentPage.position + 1 else state.currentPage.position + 2

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
                    } else {
                        // Onboarding finished
                        val name = state.name.orEmpty()
                        profilePreferences.setName(name)
                        if (name.isNotEmpty()) {
                            analyticSender.setName(name)
                        }
                        profilePreferences.setEmail(state.email.orEmpty())
                        profilePreferences.setGender(state.selectedGender)
                        appPreferences.setOnboardingCompleted(true)
                        postSideEffect(OnboardingSideEffect.NavigateAudioPlayer())
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
                if (state.email.isNullOrEmpty()) {
                    onAllowed()
                } else {
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
                }
            } else {
                onAllowed()
            }
        }
    }

    fun onPreviousPage() {
        intent {
            if (state.currentPage.pageType == OnboardingPage.SamplesPage) {
                MediaPlayerHelper.stopWithFade(1000L)
            }
//            val currentState = container.stateFlow.value
            val previousPagePosition = state.currentPage.position - 1

            if (previousPagePosition >= 0) {
                val newCurrentPage = state.currentPage.copy(
                    position = previousPagePosition,
                    pageType = state.pages[previousPagePosition]
                )
                if (newCurrentPage.pageType == OnboardingPage.LanguagePage) {
                    playVoiceSample()
                } else if (newCurrentPage.pageType == OnboardingPage.SamplesPage) {
                    playSoundSamples()
                }
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
            BackgroundPlayerHelper.pauseWithFade()
            reduceState { copy(isPlayingVoiceSample = true) }
            MediaPlayerHelper.play(
                context = context,
                soundRes = it,
                onCompletion = {
                    reduceState { copy(isPlayingVoiceSample = false) }
                    BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
                })
        } ?: run {
            reduceState { copy(isPlayingVoiceSample = false) }
            MediaPlayerHelper.release()
            BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
        }
    }

    fun onSampleSelected(soundRes: Int) {
        BackgroundPlayerHelper.pauseWithFade()
        MediaPlayerHelper.release()
        reduceState { copy(isPlayingVoiceSample = true) }
        MediaPlayerHelper.play(
            context = context,
            soundRes = soundRes,
            onCompletion = {
                reduceState { copy(isPlayingVoiceSample = false) }
                BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
            })
    }

    fun playSoundSamples() {
        samplesPlaybackJob?.cancel()
        BackgroundPlayerHelper.pauseWithFade()
        updateSamplePlaybackState(SamplePlaybackState(isPlaying = true, currentSampleIndex = 0))
        var soundResIndex = 0
        samplesPlaybackJob = viewModelScope.launch {
            while (isActive) {
                if (!MediaPlayerHelper.isPlaying()) {
                    MediaPlayerHelper.release()
                    MediaPlayerHelper.play(
                        context = context,
                        soundRes = audioResList[soundResIndex],
                        onCompletion = {
                            updateSamplePlaybackState(SamplePlaybackState())
                            samplesPlaybackJob?.cancel()
                            samplesPlaybackJob = null
                            BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
                        })
                }
                
                var elapsed = 0L
                while (elapsed < 5000L && isActive) {
                    if (MediaPlayerHelper.isPlaying()) {
                        elapsed += 100L
                    }
                    delay(100L)
                }
                
                if (!isActive) break

                val state = container.stateFlow.value
                if (state.currentPage.pageType != OnboardingPage.SamplesPage) {
                    MediaPlayerHelper.stopWithFade(1000L)
                    BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
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

    fun onPause() {
        MediaPlayerHelper.pause()
        BackgroundPlayerHelper.pauseWithFade()
    }

    fun onResume() {
        val state = container.stateFlow.value
        if (state.currentPage.pageType == OnboardingPage.SamplesPage) {
            MediaPlayerHelper.resume()
        }
        BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
    }

    override fun onCleared() {
        MediaPlayerHelper.release()
        BackgroundPlayerHelper.stop()
        super.onCleared()
    }

    private fun logPageOpened(page: OnboardingPage) {
        when (page) {
            OnboardingPage.GenderPage -> analyticSender.introGenderShow()
            OnboardingPage.LanguagePage -> analyticSender.introLangShow()
            OnboardingPage.NamePage -> analyticSender.introNameShow()
            OnboardingPage.NotificationsPage -> analyticSender.introNotificationsShow()
            OnboardingPage.EmailPage -> analyticSender.introEmailShow()
            OnboardingPage.InterestsPage -> analyticSender.introInterestsShow()
            OnboardingPage.GoalPage -> analyticSender.introGoalsShow()
            OnboardingPage.SamplesPage -> analyticSender.introScenesShow()
//            OnboardingPage.GoalPage -> analyticSender.introGoalShow()
//            OnboardingPage.SamplesPage -> analyticSender.introSampleShow()
//            OnboardingPage.EmailPage -> analyticSender.introEmailShow()
            else -> {}
        }
    }

    private fun logNextPage(currentPage: CurrentPage) {
        when (currentPage.pageType) {
            OnboardingPage.GenderPage -> analyticSender.introGenderNext()
            OnboardingPage.LanguagePage -> analyticSender.introLangNext()
            OnboardingPage.NamePage -> analyticSender.introNameNext()
            OnboardingPage.NotificationsPage -> analyticSender.introNotificationsNext()
            OnboardingPage.EmailPage -> analyticSender.introEmailNext()
            OnboardingPage.InterestsPage -> analyticSender.introInterestsNext()
            OnboardingPage.GoalPage -> analyticSender.introGoalsShow()
            OnboardingPage.SamplesPage -> analyticSender.introScenesNext()
            else -> {}
        }
    }

    private fun logSkipPage(currentPage: CurrentPage) {
        when (currentPage.pageType) {
            OnboardingPage.NamePage -> analyticSender.introNameSkip()
            OnboardingPage.NotificationsPage -> analyticSender.introNotificationsSkip()
            OnboardingPage.EmailPage -> analyticSender.introEmailSkip()
            OnboardingPage.InterestsPage -> analyticSender.introInterestsSkip()
            OnboardingPage.GoalPage -> analyticSender.introGoalsSkip()
            else -> {}
        }
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
    data class NavigateAudioPlayer(
        val accompanimentId: Int = 67,
        val timeOfDay: TimeOfDay = TimeOfDay.EVENING
    ) : OnboardingSideEffect()
}

private val audioResList: List<Int> = listOf(
    R.raw.bgm_intro_scene_1,
    R.raw.bgm_intro_scene_2,
    R.raw.bgm_intro_scene_3
)
