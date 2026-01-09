package digital.euforia.app.ui.plan

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.data.util.combine
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.BannerConfig
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.domain.model.plan.RankedPackage
import digital.euforia.app.domain.model.plan.ExtraPackage
import digital.euforia.app.domain.model.plan.demoDailyTasks
import digital.euforia.app.domain.model.plan.premiumDailyTasks
import digital.euforia.app.domain.model.toEventParam
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentWithItemsFlowUseCase
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentsWithItemsUseCase
import digital.euforia.app.domain.usecase.accompaniment.SyncAccompanimentsUseCase
import digital.euforia.app.domain.usecase.app_settings.GetAppSettingsUseCase
import digital.euforia.app.domain.usecase.network.CheckInternetConnectionUseCase
import digital.euforia.app.domain.usecase.plan.CheckTaskCompletionUseCase
import digital.euforia.app.domain.usecase.plan.ComputeContinuousDaysUseCase
import digital.euforia.app.domain.usecase.plan.GetBannerConfigUseCase
import digital.euforia.app.domain.usecase.plan.GetExtraPackageFlowUseCase
import digital.euforia.app.domain.usecase.program.GetTopProgramsFlowUseCase
import digital.euforia.app.domain.usecase.resources.SyncResourcesUseCase
import digital.euforia.app.domain.usecase.translation.GetTranslationUseCase
import digital.euforia.app.ui.util.getCurrentTimeOfDay
import digital.euforia.app.ui.util.logTag
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.ErrorViewState
import digital.euforia.app.ui.util.widget.mapToErrorViewState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.plus

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getAccompanimentWithItemsFlowUseCase: GetAccompanimentWithItemsFlowUseCase,
    private val getTopPackagesFlowUseCase: GetTopProgramsFlowUseCase,
    private val getExtraPackageFlowUseCase: GetExtraPackageFlowUseCase,
    private val getTranslationUseCase: GetTranslationUseCase,
    private val getBannerConfigUseCase: GetBannerConfigUseCase,
    private val computeContinuousDaysUseCase: ComputeContinuousDaysUseCase,
    private val config: EuforiaRemoteConfigFetcher,
    private val accompanimentRepository: AccompanimentRepository,
    private val syncAccompanimentsUseCase: SyncAccompanimentsUseCase,
    private val syncResourcesUseCase: SyncResourcesUseCase,
    private val checkTaskCompletionUseCase: CheckTaskCompletionUseCase,
//    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val getAccompanimentsWithItemsUseCase: GetAccompanimentsWithItemsUseCase,
    val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<PlanState, PlanSideEffect> {
    override val container = container<PlanState, PlanSideEffect>(
        initialState = PlanState(),
        onCreate = {
            analyticSender.todayShow()
//            checkNetwork()
            applySettings()
            observeStates()
            initialLoading()
            applyTranslations()
        }
    )

    private fun initialLoading() {
        viewModelScope.launch {
            startLoading()
            loadAppSettings()
            loadAccompaniments()
            loadResources()
        }
    }

//    fun checkNetwork() {
//        intent {
//            val isNetworkAvailable = checkInternetConnectionUseCase.invoke()
//            reduce {
//                state.copy(
//                    networkAvailable = isNetworkAvailable
//                )
//            }
//        }
//    }

    private fun startLoading() {
        reduceState { copy(isLoading = true, isAccompanimentLoading = true, errorState = null) }
    }


    private suspend fun loadAppSettings() {
//        viewModelScope.launch {
        getAppSettingsUseCase.invoke().onSuccess {
            reduceState {
                copy(
                    todayOffset = todayOffset.copy(
                        daysBefore = it?.accompanimentsOffsetBefore ?: 2,
                        daysAfter = it?.accompanimentsOffsetAfter ?: 2
                    )
                )
            }
        }.onFailure {
            Timber.tag(logTag()).d(it, "Failed to load app settings")
            reduceState { copy(errorState = it.mapToErrorViewState()) }
        }
//        }
    }

    private suspend fun loadAccompaniments() {
//        viewModelScope.launch {
        val completedDays = accompanimentRepository.getCompletedAccompanimentsCount()
        val isDemo = profilePreferences.getIsDemo()
        val isPremium = profilePreferences.getIsPremium()
        syncAccompanimentsUseCase.invoke(isDemo).onSuccess {
            observeStates()
        }.onFailure { error ->
            reduceState { copy(errorState = error.mapToErrorViewState()) }
            Timber.tag(logTag()).d(error, "Failed to load accompaniments")
        }.onFinish {
            reduceState { copy(isAccompanimentLoading = false) }
        }
//        }
    }

    private suspend fun loadResources() {
        syncResourcesUseCase.invoke().onFailure { error ->
            Timber.tag(logTag()).d(error, "Failed to load resources")
            reduceState { copy(errorState = error.mapToErrorViewState()) }
        }.onFinish { reduceState { copy(isLoading = false, isRefreshing = false) } }
    }

    private fun applySettings() {
        intent {
            val timeOfDayConfig = config.getTimeOfDayConfig() ?: defaultTimeOfDayConfig()
            val currentTimeOfDay = getCurrentTimeOfDay(timeOfDayConfig)
            val continuousDays = computeContinuousDaysUseCase.invoke()
//            val settings = getAppSettingsUseCase.invoke()
            val videoCoverUrl = config.getTodayIntroVideoCoverUrl()
//            val todayOffset = settings?.run {
//                state.todayOffset.copy(
//                    daysBefore = settings.accompanimentsOffsetBefore,
//                    daysAfter = settings.accompanimentsOffsetAfter
//                )
//            } ?: state.todayOffset
            reduce {
                state.copy(
//                    todayOffset = todayOffset,
                    timeOfDay = currentTimeOfDay,
                    timeOfDayConfig = timeOfDayConfig,
                    continuousDays = continuousDays,
                    todayVideoCoverUrl = videoCoverUrl
                )
            }
        }
    }

    private fun applyTranslations() {
        intent {
            val bannerConfig = getBannerConfigUseCase()
            reduce {
                state.copy(
                    bannerConfig = bannerConfig
                )
            }
        }
    }

    private fun observeStates() {
        viewModelScope.launch {
            val completedDaysFlow = accompanimentRepository.getCompletedAccompanimentsCountFlow()
//            val completedDaysFlow = appPreferences.getCompletedDaysFlow()
            val completedDailyTasksFlow = appPreferences.getCompletedDailyTasksFlow()
            val isPremiumFlow = profilePreferences.getIsPremiumFlow()
            val isDemoFlow = profilePreferences.getIsDemoFlow()
            val accompanimentWithItemsFlow = isDemoFlow.flatMapLatest { isDemo ->
                getAccompanimentWithItemsFlowUseCase.invoke()
            }
            val topPackagesFlow = getTopPackagesFlowUseCase.invoke()
            val extraPackageFlow = getExtraPackageFlowUseCase.invoke()

            combine(
                completedDaysFlow,
                completedDailyTasksFlow,
                isPremiumFlow,
                isDemoFlow,
                accompanimentWithItemsFlow,
                topPackagesFlow,
                extraPackageFlow
            ) { completedDays, completedDailyTask, isPremium, isDemo, accompanimentsWithItems, topPackages, extraPackage ->

                Timber.tag("CompletedDays")
                    .d("completedDays: $completedDays, completedDailyTask: $completedDailyTask, isPremium: $isPremium, isDemo: $isDemo")
                val state = container.stateFlow.value

                val dayItems = accompanimentsWithItems.mapIndexed { index, accompanimentWithItems ->
                    val todayBefore = state.todayOffset.daysBefore
                    val isToday = computeIsToday(index, isDemo, completedDays, todayBefore)
                    val lockState = computeLockState(
                        index = index,
                        isDemo = isDemo,
                        isPremium = isPremium,
                        completedDays = completedDays,
                        freeDemoDays = state.freeDemoDays,
                        todayOffsetBefore = todayBefore
                    )
                    accompanimentWithItems.toDayUi(
                        isToday = isToday,
                        lockState = lockState,
                        timeOfDay = state.timeOfDay,
                        isPremium = isPremium,
                        dayIndex = index,
                        todayOffsetBefore = todayBefore
                    )
                }.let { list ->
                    if (isDemo && !isPremium) list + createSubscriptionDayUi() else list
                }

                val todaySelectedIndex = when {
                    isDemo -> completedDays
                    else -> state.todayOffset.daysBefore
                }
                val completedDailyTasks = checkTaskCompletionUseCase.invoke()
                state.copy(
                    isLoading = false,
                    selectedDayIndex = todaySelectedIndex,
                    completedDays = completedDays,
                    isPremium = isPremium,
                    isDemo = isDemo,
                    days = dayItems,
                    completedDailyTasks = completedDailyTasks,
                    dailyTasks = if (isDemo) demoDailyTasks() else premiumDailyTasks(),
                    topPackages = topPackages,
                    extraPackage = extraPackage,
                )
            }.distinctUntilChanged()
                .collectLatest { updatedState ->
                    reduceState { updatedState }
                }
        }
    }

    fun onDaySelected(day: Int) {
        intent {
            if (day < 0 || day >= state.days.size) return@intent
            reduceState { copy(selectedDayIndex = day) }
        }
    }

    fun onDayTimeItemClick(item: DayTimeItemUi) {
        intent {
            if (state.isDemo) {
                analyticSender.todayDemoTimeOfDayClick(
                    item.item
                        .timeOfDay.toEventParam()
                )
            } else {
                analyticSender.todayTimeOfDayClick(
                    item.item
                        .timeOfDay.toEventParam()
                )
            }

            val accompaniment = state.days.getOrNull(state.selectedDayIndex)?.accompaniment
                ?: return@intent
            val title = when (item.item.timeOfDay) {
                TimeOfDay.MORNING -> accompaniment.morningTitle
                TimeOfDay.DAYTIME -> accompaniment.daytimeTitle
                TimeOfDay.EVENING -> accompaniment.eveningTitle
            }

            if (item.state == DayTimeItemUi.State.LOCKED) return@intent
            val accompanimentItem = item.item
//            val audioUrl = accompanimentItem.audioUrl ?: return@intent
            postSideEffect(
                PlanSideEffect.NavigateAudioPlayer(
                    accompanimentId = item.item.accompanimentId,
                    timeOfDay = item.item.timeOfDay
                )
            )
        }
    }

    fun onSkipDemo() {
        intent {
            viewModelScope.launch {
                syncAccompanimentsUseCase.invoke(false)
                profilePreferences.setIsDemo(false)
//                accompanimentRepository.syncAccompaniments(false)
            }
        }
    }

    fun onFinishWeek() {
        intent {
            if (state.isDemo) {
                if (state.isPremium) {
                    postSideEffect(PlanSideEffect.ShowSkipWeekDialog)
                } else {
                    postSideEffect(PlanSideEffect.NavigateFinishWeekScreen)
                }
            }
        }
    }

    fun onRetryClicked() {
        viewModelScope.launch {
            initialLoading()
//            checkNetwork()
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            reduceState { copy(isRefreshing = true) }
            initialLoading()
        }
    }

    fun onDownloadClicked() {
        intent { postSideEffect(PlanSideEffect.NavigateDownloads) }
    }
}

data class PlanState(
    val days: List<DayUi> = emptyList(),
    val completedDays: Int = 0,
    val selectedDayIndex: Int = 0,
    val isPremium: Boolean = false,
    val isDemo: Boolean = true,
    val freeDemoDays: Int = 3,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val timeOfDayConfig: TimeOfDayConfig = defaultTimeOfDayConfig(),
    val todayOffset: TodayOffset = TodayOffset(),
    val completedDailyTasks: Int = 1,
    val dailyTasks: List<DailyTask> = demoDailyTasks(),
    val topPackages: List<RankedPackage> = emptyList(),
    val extraPackage: ExtraPackage? = null,
    val continuousDays: Int = 1,
    val bannerConfig: BannerConfig? = null,
    val todayVideoCoverUrl: String? = null,
//    val networkAvailable: Boolean = true,
    val isLoading: Boolean = true,
    val isAccompanimentLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorState: ErrorViewState? = null,
)

data class TodayOffset(
    val daysBefore: Int = 2,
    val daysAfter: Int = 2,
)

data class DayUi(
    val accompaniment: Accompaniment? = null,
    val items: List<DayTimeItemUi> = emptyList(),
    val isToday: Boolean = false,
    val lockState: LockState = LockState.UNLOCKED,
    val isSubscriptionDay: Boolean = false,
) {
    @Keep
    enum class LockState {
        UNLOCKED,
        LOCKED_BY_PREMIUM,
        LOCKED_BY_PREV_DAY,
    }

    fun isLockedByPremium() = lockState == LockState.LOCKED_BY_PREMIUM
    fun isLockedByPrevDay() = lockState == LockState.LOCKED_BY_PREV_DAY
}

fun createSubscriptionDayUi(): DayUi {
    return DayUi(
        isSubscriptionDay = true,
        lockState = DayUi.LockState.LOCKED_BY_PREMIUM
    )
}

data class DayTimeItemUi(
    val item: AccompanimentItem,
    val state: State = State.AVAILABLE
) {
    @Keep
    enum class State {
        AVAILABLE,
        COMPLETED,
        LOCKED,
        SCHEDULED
    }
}


sealed class PlanSideEffect {
    data class NavigateAudioPlayer(
        val accompanimentId: Int,
        val timeOfDay: TimeOfDay
    ) : PlanSideEffect()

    data object ShowSkipWeekDialog : PlanSideEffect()
    data object NavigateFinishWeekScreen : PlanSideEffect()
    data object NavigateDownloads : PlanSideEffect()
}