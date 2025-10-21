package digital.euforia.app.ui.plan

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.data.util.combine
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.domain.model.plan.RankedPackage
import digital.euforia.app.domain.model.plan.demoDailyTasks
import digital.euforia.app.domain.model.plan.premiumDailyTasks
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentWithItemsFlowUseCase
import digital.euforia.app.domain.usecase.app_settings.GetAppSettingsUseCase
import digital.euforia.app.domain.usecase.plan.ComputeContinuousDaysUseCase
import digital.euforia.app.domain.usecase.program.GetTopProgramsFlowUseCase
import digital.euforia.app.ui.util.getCurrentTimeOfDay
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getAccompanimentWithItemsFlowUseCase: GetAccompanimentWithItemsFlowUseCase,
    private val getTopPackagesFlowUseCase: GetTopProgramsFlowUseCase,
    private val computeContinuousDaysUseCase: ComputeContinuousDaysUseCase,
    private val config: EuforiaRemoteConfigFetcher

) : ViewModel(), ContainerHost<PlanState, PlanSideEffect> {
    override val container = container<PlanState, PlanSideEffect>(
        initialState = PlanState(),
        onCreate = {
            applySettings()
            observeStates()
        }
    )

    private fun applySettings() {
        intent {
            val timeOfDayConfig = config.getTimeOfDayConfig() ?: defaultTimeOfDayConfig()
            val currentTimeOfDay = getCurrentTimeOfDay(timeOfDayConfig)
            val continuousDays = computeContinuousDaysUseCase.invoke()
            val settings = getAppSettingsUseCase.invoke()
            val todayOffset = settings?.run {
                state.todayOffset.copy(
                    daysBefore = settings.accompanimentsOffsetBefore,
                    daysAfter = settings.accompanimentsOffsetAfter
                )
            } ?: state.todayOffset
            reduce {
                state.copy(
                    todayOffset = todayOffset,
                    timeOfDay = currentTimeOfDay,
                    timeOfDayConfig = timeOfDayConfig,
                    continuousDays = continuousDays,
                )
            }
        }
    }

    private fun observeStates() {
        viewModelScope.launch {
            val completedDaysFlow = appPreferences.getCompletedDaysFlow()
            val completedDailyTasksFlow = appPreferences.getCompletedDailyTasksFlow()
            val isPremiumFlow = profilePreferences.getIsPremiumFlow()
            val isDemoFlow = profilePreferences.getIsDemoFlow()
            val accompanimentWithItemsFlow = getAccompanimentWithItemsFlowUseCase.invoke()
            val topPackagesFlow = getTopPackagesFlowUseCase.invoke()

            combine(
                completedDaysFlow,
                completedDailyTasksFlow,
                isPremiumFlow,
                isDemoFlow,
                accompanimentWithItemsFlow,
                topPackagesFlow
            ) { completedDays, completedDailyTasks, isPremium, isDemo, accompanimentsWithItems, topPackages ->
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
                    )
                }
                state.copy(
                    completedDays = completedDays,
                    isPremium = isPremium,
                    isDemo = isDemo,
                    days = dayItems,
                    completedDailyTasks = completedDailyTasks,
                    dailyTasks = if (isDemo) demoDailyTasks() else premiumDailyTasks(),
                    topPackages = topPackages
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
    val continuousDays: Int = 1,
)

data class TodayOffset(
    val daysBefore: Int = 2,
    val daysAfter: Int = 2,
)

data class DayUi(
    val accompaniment: Accompaniment,
    val items: List<DayTimeItemUi>,
    val isToday: Boolean,
    val lockState: LockState,
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

data class DayTimeItemUi(
    val item: AccompanimentItem,
    val state: State = State.AVAILABLE
) {
    enum class State {
        AVAILABLE,
        COMPLETED,
        LOCKED,
        SCHEDULED
    }
}


sealed class PlanSideEffect {}