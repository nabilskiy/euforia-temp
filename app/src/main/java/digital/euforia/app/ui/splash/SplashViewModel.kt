package digital.euforia.app.ui.splash

import android.net.Uri
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
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
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
        savedStateHandle.get<String>("deepLinkUri")

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
                        if (deepLinkUri != null) {
                            val deepLinkDestination =
                                parseDeepLinkUseCase.invoke(Uri.parse(deepLinkUri))
                            // Here you can handle navigation to deep link destination if needed
                            Timber.tag("NAVIGATION").d("Deep link URI found, navigating to destination $deepLinkDestination")
                            postEffect(SplashSideEffect.NavigateDestination(deepLinkDestination))
                        } else {
                            Timber.tag("NAVIGATION").d("No deep link URI found, navigating to home")
                            postEffect(SplashSideEffect.NavigateHome(deepLinkUri))
                        }
                    } else {
                        postEffect(SplashSideEffect.NavigateOnboarding)
                    }
                }
            } else {
                if (isOnboardingCompleted) {
                    if (deepLinkUri != null) {
                        val deepLinkDestination =
                            parseDeepLinkUseCase.invoke(Uri.parse(deepLinkUri))
                        // Here you can handle navigation to deep link destination if needed
                        Timber.tag("NAVIGATION").d("Deep link URI found, navigating to destination $deepLinkDestination")
                        postEffect(SplashSideEffect.NavigateDestination(deepLinkDestination))
                    } else {
                        Timber.tag("NAVIGATION").d("No deep link URI found, navigating to home")
                        postEffect(SplashSideEffect.NavigateHome(deepLinkUri))
                    }
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
    data class NavigateHome(val deepLinkUri: String?) : SplashSideEffect()
    data class NavigateDestination(val destination: HomeDestination) : SplashSideEffect()
//    data class NavigateDeepLink(val deepLinkDestination: HomeDestination) : SplashSideEffect()
}