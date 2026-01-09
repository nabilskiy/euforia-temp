package digital.euforia.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.settings.SettingGroup
import digital.euforia.app.domain.usecase.subscription.GetSettingsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val profilePreferences: ProfilePreferences,
    val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {
    override val container = container<SettingsState, SettingsSideEffect>(
        initialState = SettingsState(),
        onCreate = {
            analyticSender.settingsShow()
            loadSettings()
            observePremium()
        }
    )

    private fun loadSettings() {
        viewModelScope.launch {
            val settingsList = getSettings.invoke()
            intent { reduce { state.copy(settingGroups = settingsList) } }

        }
    }

    private fun observePremium() {
        viewModelScope.launch {
            val isPremium = profilePreferences.getIsPremiumFlow()
            isPremium.collectLatest { premium ->
                intent {
                    reduce {
                        state.copy(isPremium = premium)
                    }
                }
            }
        }
    }
}

data class SettingsState(
    val errorMessage: String? = null,
    val settingGroups: List<SettingGroup> = emptyList(),
    val isPremium: Boolean = false
)

sealed class SettingsSideEffect {
    data object NavigateFAQ : SettingsSideEffect()
    data object NavigateFeedback : SettingsSideEffect()
}