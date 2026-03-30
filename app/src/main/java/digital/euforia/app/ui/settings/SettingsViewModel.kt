package digital.euforia.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
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
    private val appPreferences: AppPreferences,
    private val euforiaRemoteConfigFetcher: EuforiaRemoteConfigFetcher,
    val analyticSender: AnalyticSender,
) : ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {
    override val container = container<SettingsState, SettingsSideEffect>(
        initialState = SettingsState(),
        onCreate = {
            analyticSender.settingsShow()
            loadSettings()
            observePremium()
            observeNotificationPermission()
        }
    )

    private fun loadSettings() {
        viewModelScope.launch {
            val shareMessage = euforiaRemoteConfigFetcher.getShareMessage()
            val settingsList = getSettings.invoke()
            intent {
                reduce {
                    state.copy(
                        settingGroups = settingsList,
                        shareMessage = shareMessage
                    )
                }
            }

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

    private fun observeNotificationPermission() {
        viewModelScope.launch {
            appPreferences.isNotificationPermissionGrantedFlow().collectLatest { granted ->
                intent {
                    reduce {
                        state.copy(isNotificationPermissionGranted = granted)
                    }
                }
            }
        }
    }

    fun updateNotificationPermission(isGranted: Boolean) {
        intent {
            reduce {
                state.copy(isNotificationPermissionGranted = isGranted)
            }
        }
    }

    fun dismissNotificationPermissionItem() {
        intent {
            reduce {
                state.copy(showNotificationPermissionItem = false)
            }
        }
    }
}

data class SettingsState(
    val errorMessage: String? = null,
    val settingGroups: List<SettingGroup> = emptyList(),
    val isPremium: Boolean = false,
    val shareMessage: String? = null,
    val isNotificationPermissionGranted: Boolean = true,
    val showNotificationPermissionItem: Boolean = false,
)

sealed class SettingsSideEffect {
    data object NavigateFAQ : SettingsSideEffect()
    data object NavigateFeedback : SettingsSideEffect()
}