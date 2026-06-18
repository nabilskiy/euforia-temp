package digital.euforia.app.ui.onboardingV3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.domain.model.onboarding.IntroAnswerItem
import digital.euforia.app.domain.usecase.onboarding.GetAppLanguageUseCase
import digital.euforia.app.domain.usecase.onboarding.GetGoalsUseCase
import digital.euforia.app.domain.usecase.onboarding.GetIntroAnswersUseCase
import digital.euforia.app.domain.usecase.onboarding.GetOnboardingV3PagesUseCase
import digital.euforia.app.ui.util.BackgroundPlayerHelper
import digital.euforia.app.ui.util.openWebLink
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class OnboardingV3ViewModel @Inject constructor(
    private val getOnboardingV3PagesUseCase: GetOnboardingV3PagesUseCase,
    private val getGoalsUseCase: GetGoalsUseCase,
    private val getIntroAnswersUseCase: GetIntroAnswersUseCase,
    private val getAppLanguageUseCase: GetAppLanguageUseCase,
    val appPreferences: AppPreferences,
    val profilePreferences: ProfilePreferences,
    private val analyticSender: AnalyticSender,
    @ApplicationContext private val context: android.content.Context,
) : ViewModel(), ContainerHost<OnboardingV3State, OnboardingV3SideEffect> {

    override val container = container<OnboardingV3State, OnboardingV3SideEffect>(
        initialState = OnboardingV3State(),
        onCreate = {
            analyticSender.introShow()
            BackgroundPlayerHelper.playLooping(context, R.raw.bgm_intro)
            loadPages()
        }
    )

    private fun loadPages() {
        viewModelScope.launch {
            val languageTag = getAppLanguageUseCase()
            val goals = getGoalsUseCase(languageTag)
            val programs = getIntroAnswersUseCase(R.raw.programs, languageTag)
            val dailyCommitments = getIntroAnswersUseCase(R.raw.intro_daily_commitment, languageTag)
            val timeOptions = getIntroAnswersUseCase(R.raw.intro_time, languageTag)
            val scenes = getIntroAnswersUseCase(R.raw.intro_scenes, languageTag)
            val notificationSettings = loadNotificationSettings()
            intent {
                val pages = getOnboardingV3PagesUseCase()
                reduce {
                    applyChromeForPage(
                        state.copy(
                            pages = pages,
                            goals = goals,
                            programs = programs,
                            dailyCommitments = dailyCommitments,
                            timeOptions = timeOptions,
                            scenes = scenes,
                            notificationSettings = notificationSettings,
                            currentPage = state.currentPage.copy(
                                pageType = pages.firstOrNull() ?: OnboardingV3Page.StartPage,
                            ),
                        ),
                    )
                }
            }
        }
    }

    private suspend fun loadNotificationSettings(): List<OnboardingV3NotificationSetting> {
        val morningTime = appPreferences.getMorningNotificationTime()
        val daytimeTime = appPreferences.getDayNotificationTime()
        val eveningTime = appPreferences.getEveningNotificationTime()
        return listOf(
            OnboardingV3NotificationSetting(
                slot = OnboardingV3NotificationSlot.Morning,
                enabled = appPreferences.isMorningNotificationEnabled(),
                hour = morningTime.first.takeIf { it != 7 } ?: 8,
                minute = morningTime.second,
            ),
            OnboardingV3NotificationSetting(
                slot = OnboardingV3NotificationSlot.Daytime,
                enabled = appPreferences.isDayNotificationEnabled(),
                hour = daytimeTime.first.takeIf { it != 12 } ?: 13,
                minute = daytimeTime.second,
            ),
            OnboardingV3NotificationSetting(
                slot = OnboardingV3NotificationSlot.Evening,
                enabled = appPreferences.isEveningNotificationEnabled(),
                hour = eveningTime.first,
                minute = eveningTime.second,
            ),
        )
    }

    fun onGoalSelected(goalId: String) {
        intent {
            reduce {
                applyChromeForPage(state.copy(selectedGoalId = goalId))
            }
        }
    }

    fun onAnswerSelected(key: String, answer: IntroAnswerItem) {
        intent {
            reduce {
                applyChromeForPage(
                    state.copy(introAnswers = state.introAnswers + (key to answer)),
                )
            }
        }
    }

    fun onSceneSelected(scene: IntroAnswerItem) {
        intent {
            val selected = state.selectedScenes
            val nextSelected = if (selected.any { it.identifier == scene.identifier }) {
                selected.filterNot { it.identifier == scene.identifier }
            } else if (selected.size < 3) {
                selected + scene
            } else {
                selected
            }
            reduce { applyChromeForPage(state.copy(selectedScenes = nextSelected)) }
        }
    }

    fun onNameUpdated(name: String) {
        intent {
            val trimmed = name.take(30)
            reduce { applyChromeForPage(state.copy(name = trimmed)) }
            viewModelScope.launch {
                profilePreferences.setName(trimmed)
                if (trimmed.isNotBlank()) analyticSender.setName(trimmed)
            }
        }
    }

    fun onNotificationToggled(slot: OnboardingV3NotificationSlot, enabled: Boolean) {
        intent {
            val updated = state.notificationSettings.map {
                if (it.slot == slot) it.copy(enabled = enabled) else it
            }
            reduce { applyChromeForPage(state.copy(notificationSettings = updated)) }
            saveNotificationSettings(updated)
        }
    }

    fun saveNotificationSettings() {
        intent {
            saveNotificationSettings(state.notificationSettings)
        }
    }

    private fun saveNotificationSettings(settings: List<OnboardingV3NotificationSetting>) {
        viewModelScope.launch {
            settings.forEach { setting ->
                when (setting.slot) {
                    OnboardingV3NotificationSlot.Morning -> {
                        appPreferences.setMorningNotificationEnabled(setting.enabled)
                        appPreferences.setMorningNotificationTime(setting.hour, setting.minute)
                    }
                    OnboardingV3NotificationSlot.Daytime -> {
                        appPreferences.setDayNotificationEnabled(setting.enabled)
                        appPreferences.setDayNotificationTime(setting.hour, setting.minute)
                    }
                    OnboardingV3NotificationSlot.Evening -> {
                        appPreferences.setEveningNotificationEnabled(setting.enabled)
                        appPreferences.setEveningNotificationTime(setting.hour, setting.minute)
                    }
                }
            }
        }
    }

    fun onNotificationPermissionGranted() {
        viewModelScope.launch {
            appPreferences.setNotificationPermissionGranted(true)
        }
    }

    fun onPageUpdated(position: Int) {
        val page = container.stateFlow.value.pages.getOrNull(position) ?: return
        analyticSender.introStepShow(page.stepId)
    }

    fun onTermsClicked() {
        analyticSender.introTermsClick()
        openWebLink(context, context.getString(R.string.link_terms))
    }

    fun onPrivacyClicked() {
        analyticSender.introTermsClick()
        openWebLink(context, context.getString(R.string.link_privacy))
    }

    fun onStartFlowCompleted() {
        onNextPage()
    }

    fun onNextPage() {
        intent {
            var nextPosition = state.currentPage.position + 1
            while (nextPosition < state.pages.size) {
                val nextType = state.pages[nextPosition]
                if (!canShow(nextType, state)) {
                    nextPosition++
                    continue
                }
                val newPage = state.currentPage.copy(position = nextPosition, pageType = nextType)
                reduce { applyChromeForPage(state.copy(currentPage = newPage)) }
                return@intent
            }
            appPreferences.setOnboardingCompleted(true)
            postSideEffect(OnboardingV3SideEffect.NavigateAudioPlayer())
        }
    }

    fun onSkipPage() {
        intent {
            val page = state.currentPage.pageType
            if (!page.isSkippable) return@intent
            analyticSender.introStepSkip(page.stepId)
            onNextPage()
        }
    }

    fun onPreviousPage() {
        intent {
            val prevPosition = state.currentPage.position - 1
            if (prevPosition < 0) return@intent
            val prevType = state.pages[prevPosition]
            val newPage = state.currentPage.copy(position = prevPosition, pageType = prevType)
            reduce { applyChromeForPage(state.copy(currentPage = newPage)) }
        }
    }

    private fun canShow(page: OnboardingV3Page, state: OnboardingV3State): Boolean {
        return when (page) {
            OnboardingV3Page.GoalFeedbackPage -> state.selectedGoalId != null
            else -> true
        }
    }

    private fun applyChromeForPage(state: OnboardingV3State): OnboardingV3State {
        val page = state.currentPage.pageType
        val isNextEnabled = when (page) {
            OnboardingV3Page.GoalsPage -> state.selectedGoalId != null
            OnboardingV3Page.ProgramsPage -> state.introAnswers.containsKey(IntroAnswerKeys.PROGRAMS)
            OnboardingV3Page.DailyCommitmentPage -> state.introAnswers.containsKey(IntroAnswerKeys.DAILY_COMMITMENT)
            OnboardingV3Page.TimePage -> state.introAnswers.containsKey(IntroAnswerKeys.TIME)
            OnboardingV3Page.ScenesPage -> state.selectedScenes.isNotEmpty()
            else -> true
        }
        return state.copy(
            isShellChromeVisible = !page.hidesShellChrome,
            isNextButtonVisible = !page.hidesShellChrome && page != OnboardingV3Page.StartPage,
            isNextEnabled = isNextEnabled,
        )
    }

    fun onPause() {
        BackgroundPlayerHelper.pauseWithFade()
    }

    fun onResume() {
        BackgroundPlayerHelper.resumeWithFade(context, R.raw.bgm_intro)
    }
}

data class OnboardingV3State(
    val pages: List<OnboardingV3Page> = defaultOnboardingV3Pages,
    val goals: List<Goal> = emptyList(),
    val programs: List<IntroAnswerItem> = emptyList(),
    val dailyCommitments: List<IntroAnswerItem> = emptyList(),
    val timeOptions: List<IntroAnswerItem> = emptyList(),
    val scenes: List<IntroAnswerItem> = emptyList(),
    val selectedScenes: List<IntroAnswerItem> = emptyList(),
    val notificationSettings: List<OnboardingV3NotificationSetting> = defaultOnboardingV3NotificationSettings,
    val introAnswers: Map<String, IntroAnswerItem> = emptyMap(),
    val currentPage: OnboardingV3CurrentPage = OnboardingV3CurrentPage(),
    val isNextEnabled: Boolean = true,
    val isNextButtonVisible: Boolean = false,
    val isShellChromeVisible: Boolean = false,
    val selectedGoalId: String? = null,
    val name: String? = null,
    val email: String? = null,
)

enum class OnboardingV3NotificationSlot {
    Morning,
    Daytime,
    Evening,
}

data class OnboardingV3NotificationSetting(
    val slot: OnboardingV3NotificationSlot,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
)

val defaultOnboardingV3NotificationSettings = listOf(
    OnboardingV3NotificationSetting(OnboardingV3NotificationSlot.Morning, enabled = true, hour = 8, minute = 0),
    OnboardingV3NotificationSetting(OnboardingV3NotificationSlot.Daytime, enabled = true, hour = 13, minute = 0),
    OnboardingV3NotificationSetting(OnboardingV3NotificationSlot.Evening, enabled = true, hour = 20, minute = 0),
)

data class OnboardingV3CurrentPage(
    val position: Int = 0,
    val pageType: OnboardingV3Page = OnboardingV3Page.StartPage,
)

sealed class OnboardingV3SideEffect {
    data class NavigateAudioPlayer(
        val accompanimentId: Int = 67,
        val timeOfDay: TimeOfDay = TimeOfDay.EVENING,
    ) : OnboardingV3SideEffect()
}
