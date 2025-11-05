package digital.euforia.app.ui.devoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.data.util.combine
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DevOptionsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
) : ViewModel(),
    ContainerHost<DevOptionsState, DevOptionsSideEffect> {
    override val container = container<DevOptionsState, DevOptionsSideEffect>(
        initialState = DevOptionsState(),
        onCreate = {
            observeStates()
        }
    )

    private fun observeStates() {
        viewModelScope.launch {
            intent {
                val isPremiumFlow = profilePreferences.getIsPremiumFlow()
                val isDemoFlow = profilePreferences.getIsDemoFlow()
                val isOnboardingCompletedFlow = appPreferences.isOnboardingCompletedFlow()
                combine(isPremiumFlow, isDemoFlow, isOnboardingCompletedFlow) { isPremium, isDemo, isOnboardingCompleted ->
                    state.copy(
                        isDemoUser = isDemo,
                        isPremium = isPremium,
                        isOnboardingCompleted = isOnboardingCompleted,
                    )
                }.collect { state ->
                    reduce { state }
                }
            }
        }
    }

    fun onIsDemoChanged(isDemo: Boolean) {
        viewModelScope.launch {
            profilePreferences.setIsDemo(isDemo)
        }
    }

    fun onIsPremiumChanged(isPremium: Boolean) {
        viewModelScope.launch {
            profilePreferences.setIsPremium(isPremium)
        }
    }

    fun onIsOnboardingCompletedChanged(isCompleted: Boolean) {
        viewModelScope.launch {
            appPreferences.setOnboardingCompleted(isCompleted)
        }
    }
}


data class DevOptionsState(
    val isDemoUser: Boolean = false,
    val isPremium: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val errorMessage: String? = null,
)

sealed class DevOptionsSideEffect {}