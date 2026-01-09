package digital.euforia.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.billing.BillingViewModel
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.usecase.UpdateRemoteConfigUseCase
import digital.euforia.app.domain.usecase.network.CheckInternetConnectionUseCase
import digital.euforia.app.ui.util.postEffect
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val updateRemoteConfigUseCase: UpdateRemoteConfigUseCase,
    private val appPreferences: AppPreferences,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase
) : ViewModel(),
    ContainerHost<SplashState, SplashSideEffect> {
    override val container = container<SplashState, SplashSideEffect>(
        initialState = SplashState(),
        onCreate = { updateRemoteConfig() }
    )

    private fun updateRemoteConfig() {
        viewModelScope.launch() {
            val isOnboardingCompleted = appPreferences.isOnboardingCompleted()
            val firstLaunchDate = appPreferences.getFirstLaunchDate()
            if (firstLaunchDate == null) {
                appPreferences.setFirstLaunchDate()
            }
            val networkAvailable = checkInternetConnectionUseCase.invoke()

            if (networkAvailable) {
                updateRemoteConfigUseCase {
                    if (isOnboardingCompleted) {
                        postEffect(SplashSideEffect.NavigateHome)
                    } else {
                        postEffect(SplashSideEffect.NavigateOnboarding)
                    }
                }
            } else {
                if (isOnboardingCompleted) {
                    postEffect(SplashSideEffect.NavigateHome)
                } else {
                    postEffect(SplashSideEffect.NavigateOnboarding)
                }
            }
        }
    }
}

data class SplashState(val errorMessage: String? = null)

sealed class SplashSideEffect {
    object NavigateOnboarding : SplashSideEffect()
    object NavigateHome : SplashSideEffect()
}