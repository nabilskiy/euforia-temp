package digital.euforia.app.ui.plan.firstweek

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.domain.model.toEventParam
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentWithItemsFlowUseCase
import digital.euforia.app.domain.usecase.accompaniment.GetDemoAccompanimentWithItemsFlowUseCase
import digital.euforia.app.ui.plan.DayTimeItemUi
import digital.euforia.app.ui.plan.DayUi
import digital.euforia.app.ui.plan.PlanSideEffect
import digital.euforia.app.ui.plan.computeIsToday
import digital.euforia.app.ui.plan.computeLockState
import digital.euforia.app.ui.plan.createSubscriptionDayUi
import digital.euforia.app.ui.plan.toDayUi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FirstWeekViewModel @Inject constructor(
    private val getAccompanimentWithItemsFlowUseCase: GetDemoAccompanimentWithItemsFlowUseCase,
    private val profilePreferences: ProfilePreferences,
    val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<FirstWeekState, FirstWeekSideEffect> {
    override val container = container<FirstWeekState, FirstWeekSideEffect>(
        initialState = FirstWeekState(),
        onCreate = {
            analyticSender.demoPeriodShow()
            loadAccompaniments()
        }
    )

    private fun loadAccompaniments() {
        viewModelScope.launch {
            val accompanimentWithItemsFlow = getAccompanimentWithItemsFlowUseCase.invoke()
            val isPremium = profilePreferences.getIsPremium()
            accompanimentWithItemsFlow.collectLatest { accompanimentWithItems ->
                val state = container.stateFlow.value


                val dayItems = accompanimentWithItems.mapIndexed { index, accompanimentWithItems ->
                    val lockState = DayUi.LockState.UNLOCKED
                    accompanimentWithItems.toDayUi(
                        isToday = false,
                        lockState = lockState,
                        timeOfDay = state.timeOfDay,
                        forceUnlock = true,
                        isPremium = isPremium
                    )
                }
                intent {
                    reduce {
                        state.copy(
                            days = dayItems,
                        )
                    }
                }
            }
        }
    }


    fun onDayTimeItemClick(item: DayTimeItemUi) {
        intent {
            val day = state.days.firstOrNull { dayUi ->
                dayUi.items.contains(item)
            }
            day?.let {
                analyticSender.demoPeriodTimeOfDayClick(
                    item.item.timeOfDay.toEventParam(),
                    state.days.indexOf(day)
                )
            }
            if (item.state == DayTimeItemUi.State.LOCKED) return@intent
            val accompanimentItem = item.item
//            val audioUrl = accompanimentItem.audioUrl ?: return@intent
            postSideEffect(
                FirstWeekSideEffect.NavigateAudioPlayer(
                    accompanimentId = item.item.accompanimentId,
                    timeOfDay = item.item.timeOfDay
                )
            )
        }
    }
}

data class FirstWeekState(
    val days: List<DayUi> = emptyList(),
    val errorMessage: String? = null,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val timeOfDayConfig: TimeOfDayConfig = defaultTimeOfDayConfig(),
)

sealed class FirstWeekSideEffect {
    data class NavigateAudioPlayer(
        val accompanimentId: Int,
        val timeOfDay: TimeOfDay
    ) : FirstWeekSideEffect()
}