package digital.euforia.app.ui.settings.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.domain.model.config.isDaytimeRange
import digital.euforia.app.domain.model.config.isEveningRange
import digital.euforia.app.domain.model.config.isMorningRange
import digital.euforia.app.domain.usecase.UpdateNotificationsUseCase
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val configFetcher: EuforiaRemoteConfigFetcher,
    private val updateNotificationsUseCase: UpdateNotificationsUseCase
) : ViewModel(),
    ContainerHost<NotificationsState, NotificationsSideEffect> {
    override val container = container<NotificationsState, NotificationsSideEffect>(
        initialState = NotificationsState(),
        onCreate = {
            loadSettings()
        }
    )

    private fun loadSettings() {
        viewModelScope.launch {
            val timeOfDayConfig = configFetcher.getTimeOfDayConfig() ?: defaultTimeOfDayConfig()
            intent {
                val isMorningEnabled = appPreferences.isMorningNotificationEnabled()
                val isDayEnabled = appPreferences.isDayNotificationEnabled()
                val isEveningEnabled = appPreferences.isEveningNotificationEnabled()
                val (morningHour, morningMinute) = appPreferences.getMorningNotificationTime()
                val (dayHour, dayMinute) = appPreferences.getDayNotificationTime()
                val (eveningHour, eveningMinute) = appPreferences.getEveningNotificationTime()
                reduce {
                    state.copy(
                        timeOfDayConfig = timeOfDayConfig,
                        isMorningNotificationEnabled = isMorningEnabled,
                        isDayNotificationEnabled = isDayEnabled,
                        isEveningNotificationEnabled = isEveningEnabled,
                        morningTime = morningHour to morningMinute,
                        dayTime = dayHour to dayMinute,
                        eveningTime = eveningHour to eveningMinute)
                }
            }
        }
    }

    fun onMorningNotificationToggled(enabled: Boolean) {
        intent {
            reduce {
                state.copy(isMorningNotificationEnabled = enabled)
            }
        }
    }

    fun onDayNotificationToggled(enabled: Boolean) {
        intent {
            reduce {
                state.copy(isDayNotificationEnabled = enabled)
            }
        }
    }

    fun onEveningNotificationToggled(enabled: Boolean) {
        intent {
            reduce {
                state.copy(isEveningNotificationEnabled = enabled)
            }
        }
    }

    fun onMorningTimeChanged(time: Pair<Int, Int>) {
        intent {
            if (state.timeOfDayConfig.isMorningRange(time.first)) {
                reduce { state.copy(morningTime = time) }
            }
        }
    }

    fun onDayTimeChanged(time: Pair<Int, Int>) {
        intent {
            if (state.timeOfDayConfig.isDaytimeRange(time.first)) {
                reduce { state.copy(dayTime = time) }
            }
        }
    }

    fun onEveningTimeChanged(time: Pair<Int, Int>) {
        intent {
            if (state.timeOfDayConfig.isEveningRange(time.first))
                reduce { state.copy(eveningTime = time) }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val state = container.stateFlow.value
            appPreferences.setMorningNotificationEnabled(state.isMorningNotificationEnabled)
            appPreferences.setDayNotificationEnabled(state.isDayNotificationEnabled)
            appPreferences.setEveningNotificationEnabled(state.isEveningNotificationEnabled)
            val (morningHour, morningMinute) = state.morningTime
            appPreferences.setMorningNotificationTime(morningHour, morningMinute)
            val (dayHour, dayMinute) = state.dayTime
            appPreferences.setDayNotificationTime(dayHour, dayMinute)
            val (eveningHour, eveningMinute) = state.eveningTime
            appPreferences.setEveningNotificationTime(eveningHour, eveningMinute)
            updateNotificationsUseCase()
            intent { postSideEffect(NotificationsSideEffect.NavigateBack)}
        }
    }
}

data class NotificationsState(
    val errorMessage: String? = null,
    val isMorningNotificationEnabled: Boolean = true,
    val isDayNotificationEnabled: Boolean = true,
    val isEveningNotificationEnabled: Boolean = true,
    val morningTime: Pair<Int, Int> = 7 to 0,
    val dayTime: Pair<Int, Int> = 12 to 0,
    val eveningTime: Pair<Int, Int> = 18 to 0,
    val timeOfDayConfig: TimeOfDayConfig = defaultTimeOfDayConfig()
)

sealed class NotificationsSideEffect {
    data object NavigateBack : NotificationsSideEffect()
}