package digital.euforia.app.ui.splash

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.usecase.ParseDeepLinkUseCase
import digital.euforia.app.domain.usecase.UpdateRemoteConfigUseCase
import digital.euforia.app.domain.usecase.network.CheckInternetConnectionUseCase
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.util.postEffect
import kotlinx.coroutines.launch
import timber.log.Timber
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val updateRemoteConfigUseCase: UpdateRemoteConfigUseCase,
    private val appPreferences: AppPreferences,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val parseDeepLinkUseCase: ParseDeepLinkUseCase
) : ViewModel(),
    ContainerHost<SplashState, SplashSideEffect> {

    private val deepLinkUri: String? =
        savedStateHandle["deepLinkUri"]
    private val skipSplash: Boolean =
        savedStateHandle["skipSplash"] ?: false

    init {
        Timber.tag("NAVIGATION").d("SplashViewModel: init. skipSplash=$skipSplash, deepLinkUri=$deepLinkUri")
    }

    override val container = container<SplashState, SplashSideEffect>(
        initialState = SplashState(showContent = !skipSplash),
        onCreate = { updateRemoteConfig() }
    )

    private fun updateRemoteConfig() {
        Timber.tag("NAVIGATION").d("SplashViewModel: updateRemoteConfig. skipSplash=$skipSplash, deepLinkUri=$deepLinkUri")
        viewModelScope.launch {
            val isOnboardingCompleted = appPreferences.isOnboardingCompleted()
            val firstLaunchDate = appPreferences.getFirstLaunchDate()
            if (firstLaunchDate == null) {
                appPreferences.setFirstLaunchDate()
            }
            
            // Add a timeout for the whole initialization process
            var finished = false
            kotlinx.coroutines.withTimeoutOrNull(5000L) {
                val networkAvailable = checkInternetConnectionUseCase.invoke()

                if (networkAvailable) {
                    Timber.tag("NAVIGATION").d("SplashViewModel: Network available, updating remote config...")
                    updateRemoteConfigUseCase {
                        Timber.tag("NAVIGATION").d("SplashViewModel: Remote config updated. isOnboardingCompleted=$isOnboardingCompleted")
                        if (!finished) {
                            finished = true
                            navigateToNext(isOnboardingCompleted)
                        }
                    }
                } else {
                    Timber.tag("NAVIGATION").d("SplashViewModel: Network NOT available. isOnboardingCompleted=$isOnboardingCompleted")
                    if (!finished) {
                        finished = true
                        navigateToNext(isOnboardingCompleted)
                    }
                }
            } ?: run {
                Timber.tag("NAVIGATION").w("SplashViewModel: Initialization timed out! Forcing navigation.")
                if (!finished) {
                    finished = true
                    navigateToNext(isOnboardingCompleted)
                }
            }
        }
    }

    private fun navigateToNext(isOnboardingCompleted: Boolean) {
        Timber.tag("NAVIGATION").d("SplashViewModel: navigateToNext. isOnboardingCompleted=$isOnboardingCompleted")
        if (isOnboardingCompleted) {
            if (deepLinkUri != null) {
                Timber.tag("NAVIGATION").d("SplashViewModel: Posting NavigateDeepLink: $deepLinkUri")
                postEffect(SplashSideEffect.NavigateDeepLink(deepLinkUri))
            } else {
                Timber.tag("NAVIGATION").d("SplashViewModel: Posting NavigateHome")
                postEffect(SplashSideEffect.NavigateHome)
            }
        } else {
            Timber.tag("NAVIGATION").d("SplashViewModel: Posting NavigateOnboarding")
            postEffect(SplashSideEffect.NavigateOnboarding)
        }
    }
}

data class SplashState(val showContent: Boolean = true, val errorMessage: String? = null)

sealed class SplashSideEffect {
    object NavigateOnboarding : SplashSideEffect()
    object NavigateHome : SplashSideEffect()
    data class NavigateDeepLink(val deepLinkUri: String) : SplashSideEffect()
}