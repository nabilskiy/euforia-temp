package digital.euforia.app.ui.onboardingV3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.BuildConfig
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.store.FirstExperienceReminderPreferences
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.domain.model.config.isDaytimeRange
import digital.euforia.app.domain.model.config.isEveningRange
import digital.euforia.app.domain.model.config.isMorningRange
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.domain.model.onboarding.IntroAnswerItem
import digital.euforia.app.domain.usecase.UpdateNotificationsUseCase
import digital.euforia.app.domain.usecase.onboarding.GetAppLanguageUseCase
import digital.euforia.app.domain.usecase.onboarding.GetGoalsUseCase
import digital.euforia.app.domain.usecase.onboarding.GetIntroAnswersUseCase
import digital.euforia.app.domain.usecase.onboarding.GetOnboardingV3PagesUseCase
import digital.euforia.app.domain.usecase.onboarding.SendIntroRateFeedbackUseCase
import digital.euforia.app.domain.usecase.onboarding.ValidateEmailUseCase
import digital.euforia.app.service.notifications.NotificationScheduler
import digital.euforia.app.ui.util.BackgroundPlayerHelper
import digital.euforia.app.ui.util.getCurrentTimeOfDay
import digital.euforia.app.ui.util.openWebLink
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class OnboardingV3ViewModel @Inject constructor(
    private val getOnboardingV3PagesUseCase: GetOnboardingV3PagesUseCase,
    private val getGoalsUseCase: GetGoalsUseCase,
    private val getIntroAnswersUseCase: GetIntroAnswersUseCase,
    private val getAppLanguageUseCase: GetAppLanguageUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val sendIntroRateFeedbackUseCase: SendIntroRateFeedbackUseCase,
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
    private val accompanimentRepository: AccompanimentRepository,
    private val updateNotificationsUseCase: UpdateNotificationsUseCase,
    private val notificationScheduler: NotificationScheduler,
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
            val timeOfDayConfig = remoteConfigFetcher.getTimeOfDayConfig() ?: defaultTimeOfDayConfig()
            val notificationSettings = loadNotificationSettings(timeOfDayConfig)
            val introSkipDelaySeconds = remoteConfigFetcher.getIntroSkipDelaySeconds()
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
                            introSkipDelaySeconds = introSkipDelaySeconds,
                            notificationTimeOfDayConfig = timeOfDayConfig,
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

    private suspend fun loadNotificationSettings(
        timeOfDayConfig: TimeOfDayConfig,
    ): List<OnboardingV3NotificationSetting> {
        val defaultConfig = defaultTimeOfDayConfig()
        val morningTime = appPreferences.getMorningNotificationTime()
        val daytimeTime = appPreferences.getDayNotificationTime()
        val eveningTime = appPreferences.getEveningNotificationTime()
        return listOf(
            OnboardingV3NotificationSetting(
                slot = OnboardingV3NotificationSlot.Morning,
                enabled = appPreferences.isMorningNotificationEnabled(),
                hour = morningTime.first.takeIf { it != defaultConfig.morningBegin }
                    ?: timeOfDayConfig.morningNotification,
                minute = morningTime.second,
            ),
            OnboardingV3NotificationSetting(
                slot = OnboardingV3NotificationSlot.Daytime,
                enabled = appPreferences.isDayNotificationEnabled(),
                hour = daytimeTime.first.takeIf { it != defaultConfig.daytimeBegin }
                    ?: timeOfDayConfig.daytimeNotification,
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

    fun onPreviewTypeSelected(type: OnboardingV3PreviewType) {
        intent {
            reduce { state.copy(selectedPreviewType = type) }
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

    fun onAgeSelected(age: Int) {
        intent {
            reduce { applyChromeForPage(state.copy(age = age)) }
        }
    }

    fun onNotificationToggled(slot: OnboardingV3NotificationSlot, enabled: Boolean) {
        intent {
            val updated = state.notificationSettings.map {
                if (it.slot == slot) it.copy(enabled = enabled) else it
            }
            reduce { applyChromeForPage(state.copy(notificationSettings = updated)) }
        }
    }

    fun onNotificationTimeChanged(slot: OnboardingV3NotificationSlot, time: Pair<Int, Int>) {
        intent {
            if (!state.notificationTimeOfDayConfig.isAllowedNotificationHour(slot, time.first)) {
                return@intent
            }
            val updated = state.notificationSettings.map {
                if (it.slot == slot) {
                    it.copy(hour = time.first, minute = time.second)
                } else {
                    it
                }
            }
            reduce { applyChromeForPage(state.copy(notificationSettings = updated)) }
        }
    }

    fun onEmailUpdated(email: String?) {
        intent {
            reduce { applyChromeForPage(state.copy(email = email, isNextEnabled = true)) }
        }
    }

    fun onNotificationsSetupConfirmed(hasNotificationPermission: Boolean) {
        val settings = container.stateFlow.value.notificationSettings
        viewModelScope.launch {
            persistNotificationSettings(settings)
            if (hasNotificationPermission) {
                appPreferences.setNotificationPermissionGranted(true)
            }
            onNextPage()
        }
    }

    private suspend fun persistNotificationSettings(settings: List<OnboardingV3NotificationSetting>) {
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
            if (state.currentPage.pageType == OnboardingV3Page.EmailPage) {
                val email = state.email.orEmpty()
                if (email.isNotBlank()) {
                    val isEmailValid = validateEmailUseCase(email)
                    if (!isEmailValid) {
                        reduce { state.copy(isNextEnabled = false) }
                        return@intent
                    }
                    profilePreferences.setEmail(email)
                }
            }

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
            if (appPreferences.isNotificationPermissionGranted()) {
                updateNotificationsUseCase()
            }
            postSideEffect(OnboardingV3SideEffect.NavigateHome)
        }
    }

    fun onFirstExperiencePlayNow() {
        intent {
            appPreferences.clearFirstExperienceReminder()
            notificationScheduler.cancelFirstExperienceReminder()
            when (state.selectedPreviewType) {
                OnboardingV3PreviewType.Accompaniment -> {
                    val effect = resolvePreviewAudioSideEffect()
                    if (effect != null) {
                        postSideEffect(effect)
                    } else {
                        Timber.tag(TAG).w("Unable to resolve onboarding preview audio session")
                    }
                }
                OnboardingV3PreviewType.Meditation -> {
                    val meditationId = state.introAnswers[IntroAnswerKeys.PROGRAMS]?.entityId
                        ?: remoteConfigFetcher.getIntroDefaultMeditationId()
                    if (meditationId != null) {
                        postSideEffect(OnboardingV3SideEffect.NavigatePreviewMeditation(meditationId))
                    } else {
                        Timber.tag(TAG).w("Unable to resolve onboarding preview meditation")
                    }
                }
                OnboardingV3PreviewType.Soundscape -> {
                    val sceneId = state.selectedScenes.firstNotNullOfOrNull { it.entityId }
                        ?: state.introAnswers[IntroAnswerKeys.SCENES]?.entityId
                        ?: remoteConfigFetcher.getIntroDefaultSceneId()
                    if (sceneId != null) {
                        postSideEffect(
                            OnboardingV3SideEffect.NavigatePreviewSoundscape(
                                sceneId = sceneId,
                                introSceneTimerSeconds = remoteConfigFetcher.getIntroSceneTimerSeconds(),
                            ),
                        )
                    } else {
                        Timber.tag(TAG).w("Unable to resolve onboarding preview soundscape")
                    }
                }
            }
        }
    }

    fun onRateSubmitted(rating: Int, comment: String) {
        intent {
            val previewType = state.selectedPreviewType
            val entityId = resolvePreviewEntityId(previewType)
            analyticSender.introRate(
                rating = rating,
                entityId = entityId,
                contentType = previewType.analyticsValue,
            )
            sendIntroRateFeedbackUseCase(
                rating = rating,
                comment = comment,
                previewType = previewType,
                entityId = entityId,
            )
            onNextPage()
        }
    }

    fun onFirstExperienceReminderTimeChanged(dayOffset: Int, hour: Int, minute: Int) {
        intent {
            reduce {
                state.copy(
                    firstExperienceReminderDayOffset = dayOffset.coerceIn(0, 1),
                    firstExperienceReminderHour = hour.coerceIn(0, 23),
                    firstExperienceReminderMinute = minute.coerceIn(0, 59),
                )
            }
        }
    }

    fun onFirstExperienceReminderScheduled(hasNotificationPermission: Boolean) {
        intent {
            val payload = resolveFirstExperienceReminderPayload()
            if (payload == null) {
                Timber.tag(TAG).w("Unable to resolve first experience reminder payload")
                return@intent
            }
            val triggerAt = computeFirstExperienceReminderTriggerAt(
                dayOffset = state.firstExperienceReminderDayOffset,
                hour = state.firstExperienceReminderHour,
                minute = state.firstExperienceReminderMinute,
            )
            val triggerAtMillis = triggerAt.toInstant().toEpochMilli()

            appPreferences.saveFirstExperienceReminder(
                FirstExperienceReminderPreferences(
                    previewType = state.selectedPreviewType.name,
                    entityId = payload.entityId,
                    accompanimentId = payload.accompanimentId,
                    timeOfDay = payload.timeOfDay?.name,
                    sceneTimerSeconds = payload.sceneTimerSeconds,
                    imageUrl = payload.imageUrl,
                    triggerAtMillis = triggerAtMillis,
                ),
            )
            if (hasNotificationPermission) {
                appPreferences.setNotificationPermissionGranted(true)
            }
            notificationScheduler.scheduleFirstExperienceReminder(
                triggerAtMillis = triggerAtMillis,
                previewType = state.selectedPreviewType.name,
                entityId = payload.entityId,
                accompanimentId = payload.accompanimentId,
                timeOfDay = payload.timeOfDay?.name,
                sceneTimerSeconds = payload.sceneTimerSeconds,
                imageUrl = payload.imageUrl,
            )
            reduce {
                state.copy(
                    firstExperienceReminderDayOffset = if (triggerAt.toLocalDate() == ZonedDateTime.now().toLocalDate()) {
                        0
                    } else {
                        1
                    },
                    firstExperienceReminderHour = triggerAt.hour,
                    firstExperienceReminderMinute = triggerAt.minute,
                    firstExperienceReminderScheduledAt = triggerAtMillis,
                    isFirstExperienceReminderScheduled = true,
                )
            }
        }
    }

    fun onFirstExperienceReminderCancelled() {
        intent {
            appPreferences.clearFirstExperienceReminder()
            notificationScheduler.cancelFirstExperienceReminder()
            reduce {
                state.copy(
                    firstExperienceReminderScheduledAt = null,
                    isFirstExperienceReminderScheduled = false,
                )
            }
        }
    }

    fun onFirstExperienceProceedAfterReminder() {
        intent {
            appPreferences.setOnboardingCompleted(true)
            if (appPreferences.isNotificationPermissionGranted()) {
                updateNotificationsUseCase()
            }
            postSideEffect(OnboardingV3SideEffect.NavigateHome)
        }
    }

    private suspend fun resolveFirstExperienceReminderPayload(): FirstExperienceReminderPayload? {
        return when (container.stateFlow.value.selectedPreviewType) {
            OnboardingV3PreviewType.Accompaniment -> {
                val effect = resolvePreviewAudioSideEffect() ?: return null
                FirstExperienceReminderPayload(
                    accompanimentId = effect.accompanimentId,
                    timeOfDay = effect.timeOfDay,
                )
            }
            OnboardingV3PreviewType.Meditation -> {
                val currentState = container.stateFlow.value
                val meditationId = currentState.introAnswers[IntroAnswerKeys.PROGRAMS]?.entityId
                    ?: remoteConfigFetcher.getIntroDefaultMeditationId()
                    ?: return null
                FirstExperienceReminderPayload(
                    entityId = meditationId,
                    imageUrl = currentState.introAnswers[IntroAnswerKeys.PROGRAMS]?.imageUrl,
                )
            }
            OnboardingV3PreviewType.Soundscape -> {
                val currentState = container.stateFlow.value
                val scene = currentState.selectedScenes.firstOrNull { it.entityId != null }
                    ?: currentState.introAnswers[IntroAnswerKeys.SCENES]
                val sceneId = scene?.entityId
                    ?: remoteConfigFetcher.getIntroDefaultSceneId()
                    ?: return null
                FirstExperienceReminderPayload(
                    entityId = sceneId,
                    sceneTimerSeconds = remoteConfigFetcher.getIntroSceneTimerSeconds(),
                    imageUrl = scene?.imageUrl,
                )
            }
        }
    }

    private fun resolvePreviewEntityId(type: OnboardingV3PreviewType): Int? {
        val currentState = container.stateFlow.value
        return when (type) {
            OnboardingV3PreviewType.Accompaniment -> null
            OnboardingV3PreviewType.Meditation -> {
                currentState.introAnswers[IntroAnswerKeys.PROGRAMS]?.entityId
                    ?: remoteConfigFetcher.getIntroDefaultMeditationId()
            }
            OnboardingV3PreviewType.Soundscape -> {
                currentState.selectedScenes.firstNotNullOfOrNull { it.entityId }
                    ?: currentState.introAnswers[IntroAnswerKeys.SCENES]?.entityId
                    ?: remoteConfigFetcher.getIntroDefaultSceneId()
            }
        }
    }

    private fun computeFirstExperienceReminderTriggerAt(
        dayOffset: Int,
        hour: Int,
        minute: Int,
    ): ZonedDateTime {
        val now = ZonedDateTime.now()
        var candidate = now
            .plusDays(dayOffset.toLong())
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
        if (candidate.isBefore(now)) {
            candidate = candidate.plusDays(1)
        }

        val minDelayMinutes = if (BuildConfig.DEBUG) DEBUG_MIN_REMINDER_DELAY_MINUTES else RELEASE_MIN_REMINDER_DELAY_MINUTES
        val minimum = now.plusMinutes(minDelayMinutes).withSecond(0).withNano(0)
        val maximum = now.plusHours(MAX_REMINDER_DELAY_HOURS)
        return when {
            candidate.isBefore(minimum) -> minimum
            candidate.isAfter(maximum) -> maximum
            else -> candidate
        }
    }

    private suspend fun resolvePreviewAudioSideEffect(): OnboardingV3SideEffect.NavigatePreviewAudio? {
        val timeOfDayConfig = remoteConfigFetcher.getTimeOfDayConfig() ?: defaultTimeOfDayConfig()
        val timeOfDay = getCurrentTimeOfDay(timeOfDayConfig)
        val requestDemo = profilePreferences.getIsDemo() || !profilePreferences.getIsPremium()
        val accompanimentId = accompanimentRepository
            .getAccompanimentWithItems(isDemo = requestDemo)
            .dataOrNull
            ?.firstOrNull()
            ?.accompaniment
            ?.id
            ?: return null

        return OnboardingV3SideEffect.NavigatePreviewAudio(
            accompanimentId = accompanimentId,
            timeOfDay = timeOfDay,
        )
    }

    fun onPreviewCompleted() {
        intent {
            val ratePosition = state.pages.indexOf(OnboardingV3Page.RatePage)
            if (ratePosition >= 0) {
                val newPage = state.currentPage.copy(
                    position = ratePosition,
                    pageType = OnboardingV3Page.RatePage,
                )
                reduce { applyChromeForPage(state.copy(currentPage = newPage)) }
            } else {
                onNextPage()
            }
        }
    }

    fun onSkipPage() {
        intent {
            val page = state.currentPage.pageType
            if (!page.canSkipLikeIos()) return@intent
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

private fun OnboardingV3Page.canSkipLikeIos(): Boolean {
    return when (this) {
        OnboardingV3Page.StartPage,
        OnboardingV3Page.About1Page,
        OnboardingV3Page.About2Page,
        OnboardingV3Page.About3Page,
        OnboardingV3Page.GenderPage,
        OnboardingV3Page.SummaryPage,
        OnboardingV3Page.LoadingPage,
        OnboardingV3Page.FirstExperiencePage,
        OnboardingV3Page.RatePage -> false

        else -> !hidesShellChrome
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
    val introSkipDelaySeconds: Double = 0.0,
    val notificationTimeOfDayConfig: TimeOfDayConfig = defaultTimeOfDayConfig(),
    val notificationSettings: List<OnboardingV3NotificationSetting> = defaultOnboardingV3NotificationSettings,
    val introAnswers: Map<String, IntroAnswerItem> = emptyMap(),
    val currentPage: OnboardingV3CurrentPage = OnboardingV3CurrentPage(),
    val isNextEnabled: Boolean = true,
    val isNextButtonVisible: Boolean = false,
    val isShellChromeVisible: Boolean = false,
    val selectedGoalId: String? = null,
    val age: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val selectedPreviewType: OnboardingV3PreviewType = OnboardingV3PreviewType.Accompaniment,
    val firstExperienceReminderDayOffset: Int = defaultFirstExperienceReminderTime().dayOffset,
    val firstExperienceReminderHour: Int = defaultFirstExperienceReminderTime().hour,
    val firstExperienceReminderMinute: Int = defaultFirstExperienceReminderTime().minute,
    val firstExperienceReminderScheduledAt: Long? = null,
    val isFirstExperienceReminderScheduled: Boolean = false,
)

enum class OnboardingV3PreviewType {
    Accompaniment,
    Meditation,
    Soundscape,
}

private val OnboardingV3PreviewType.analyticsValue: String
    get() = when (this) {
        OnboardingV3PreviewType.Accompaniment -> "accompaniment"
        OnboardingV3PreviewType.Meditation -> "meditation"
        OnboardingV3PreviewType.Soundscape -> "soundscape"
    }

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

fun TimeOfDayConfig.isAllowedNotificationHour(
    slot: OnboardingV3NotificationSlot,
    hour: Int,
): Boolean = when (slot) {
    OnboardingV3NotificationSlot.Morning -> isMorningRange(hour)
    OnboardingV3NotificationSlot.Daytime -> isDaytimeRange(hour)
    OnboardingV3NotificationSlot.Evening -> isEveningRange(hour)
}

data class OnboardingV3CurrentPage(
    val position: Int = 0,
    val pageType: OnboardingV3Page = OnboardingV3Page.StartPage,
)

private data class FirstExperienceReminderPayload(
    val entityId: Int? = null,
    val accompanimentId: Int? = null,
    val timeOfDay: TimeOfDay? = null,
    val sceneTimerSeconds: Int = 600,
    val imageUrl: String? = null,
)

private data class DefaultFirstExperienceReminderTime(
    val dayOffset: Int,
    val hour: Int,
    val minute: Int,
)

private fun defaultFirstExperienceReminderTime(): DefaultFirstExperienceReminderTime {
    val now = ZonedDateTime.now()
    val defaultTime = now.plusMinutes(DEFAULT_REMINDER_DELAY_MINUTES).withSecond(0).withNano(0)
    return DefaultFirstExperienceReminderTime(
        dayOffset = if (defaultTime.toLocalDate() == now.toLocalDate()) 0 else 1,
        hour = defaultTime.hour,
        minute = defaultTime.minute,
    )
}

sealed class OnboardingV3SideEffect {
    data object NavigateHome : OnboardingV3SideEffect()

    data class NavigateAudioPlayer(
        val accompanimentId: Int = 67,
        val timeOfDay: TimeOfDay = TimeOfDay.EVENING,
    ) : OnboardingV3SideEffect()

    data class NavigatePreviewAudio(
        val accompanimentId: Int,
        val timeOfDay: TimeOfDay,
    ) : OnboardingV3SideEffect()

    data class NavigatePreviewMeditation(
        val meditationId: Int,
    ) : OnboardingV3SideEffect()

    data class NavigatePreviewSoundscape(
        val sceneId: Int,
        val introSceneTimerSeconds: Int,
    ) : OnboardingV3SideEffect()
}

private const val TAG = "OnboardingV3"
private const val DEFAULT_REMINDER_DELAY_MINUTES = 20L
private const val DEBUG_MIN_REMINDER_DELAY_MINUTES = 2L
private const val RELEASE_MIN_REMINDER_DELAY_MINUTES = 15L
private const val MAX_REMINDER_DELAY_HOURS = 48L
