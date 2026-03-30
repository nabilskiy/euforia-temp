package digital.euforia.app.ui.splash

import android.net.Uri
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.BuildConfig
import digital.euforia.app.R
import digital.euforia.app.billing.BillingRepository
import digital.euforia.app.billing.localdb.Premium
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.config.CriticalUpdateConfig
import digital.euforia.app.domain.usecase.ParseDeepLinkUseCase
import digital.euforia.app.domain.usecase.UpdateRemoteConfigUseCase
import digital.euforia.app.domain.usecase.network.CheckInternetConnectionUseCase
import digital.euforia.app.domain.usecase.subscription.SyncPurchaseUseCase
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.util.postEffect
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val updateRemoteConfigUseCase: UpdateRemoteConfigUseCase,
    private val appPreferences: AppPreferences,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val parseDeepLinkUseCase: ParseDeepLinkUseCase,
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
    private val billingRepository: BillingRepository,
    private val profilePreferences: ProfilePreferences,
    private val analyticSender: AnalyticSender,
    private val syncPurchaseUseCase: SyncPurchaseUseCase
) : ViewModel(),
    ContainerHost<SplashState, SplashSideEffect> {

    private val deepLinkUri: String? =
        savedStateHandle.get<String>("deepLinkUri")

    override val container = container<SplashState, SplashSideEffect>(
        initialState = SplashState(),
        onCreate = { startSplashSteps() }
    )

    private val premiumObserver = Observer<Premium?> { premium ->
        viewModelScope.launch {
            try {
                if (premium != null && premium.entitled) {
                    analyticSender.premiumActive()
                    profilePreferences.setIsPremium(true)
                    val purchases: List<Purchase>? = billingRepository.currentPurchases
                    purchases?.firstOrNull()?.let { p ->
                        syncPurchaseUseCase.invoke(p.originalJson)
                    }
                } else {
                    analyticSender.premiumNotActive()
                    profilePreferences.setIsPremium(false)
                }
            } catch (e: Exception) {
                Timber.tag("SplashViewModel").e(e, "Error observing premium status")
            }
        }
    }

    private fun startSplashSteps() {
        viewModelScope.launch {
            executeStepPleaseWait()
            executeStepCheckingPurchases()
            executeStepUpdatingInformation()
        }
    }

    private suspend fun executeStepPleaseWait() {
        reduceState { copy(currentStep = SplashSteps.PLEASE_WAIT) }
        val time = measureTimeMillis {
            val isOnboardingCompleted = appPreferences.isOnboardingCompleted()
            if (appPreferences.getFirstLaunchDate() == null) {
                appPreferences.setFirstLaunchDate()
            }
            val networkAvailable = checkInternetConnectionUseCase.invoke()

            if (networkAvailable) {
                updateRemoteConfigUseCase {
                    val criticalUpdateConfig = remoteConfigFetcher.getCriticalUpdateConfig()
                    if (criticalUpdateConfig.needUpdate && BuildConfig.VERSION_CODE < criticalUpdateConfig.minVersionCode) {
                        reduceState {
                            copy(
                                criticalUpdateConfig = criticalUpdateConfig,
                                isOnboardingCompleted = isOnboardingCompleted
                            )
                        }
                        postEffect(SplashSideEffect.ShowCriticalUpdate(criticalUpdateConfig, isOnboardingCompleted))
                    }
                }
            }
        }
        val remaining = 1000L - time
        if (remaining > 0) delay(remaining)
    }

    private suspend fun executeStepCheckingPurchases() {
        reduceState { copy(currentStep = SplashSteps.CHECKING_PURCHASES) }
        val time = measureTimeMillis {
            billingRepository.startDataSourceConnections()
            billingRepository.premiumLiveData.observeForever(premiumObserver)
            // Wait a bit for billing to initialize or purchases to be fetched
            delay(500)
        }
        val remaining = 1000L - time
        if (remaining > 0) delay(remaining)
    }

    private suspend fun executeStepUpdatingInformation() {
        reduceState { copy(currentStep = SplashSteps.UPDATING_INFORMATION) }
        val name = profilePreferences.getName()
        if (!name.isNullOrEmpty()) {
            analyticSender.setName(name)
        }
        delay(1000)
        proceedNavigation(appPreferences.isOnboardingCompleted())
    }

    fun proceedNavigation(isOnboardingCompleted: Boolean) {
        viewModelScope.launch {
            if (isOnboardingCompleted) {
                if (deepLinkUri != null) {
                    val deepLinkDestination =
                        parseDeepLinkUseCase.invoke(Uri.parse(deepLinkUri))
                    // Here you can handle navigation to deep link destination if needed
                    Timber.tag("NAVIGATION")
                        .d("Deep link URI found, navigating to destination $deepLinkDestination")
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

    override fun onCleared() {
        super.onCleared()
        billingRepository.premiumLiveData.removeObserver(premiumObserver)
    }

    fun onDismissCriticalUpdate() {
        intent {
            reduce {
                state.copy(
                    criticalUpdateConfig = null,
                )
            }
        }
    }
}

data class SplashState(
    val errorMessage: String? = null,
    val criticalUpdateConfig: CriticalUpdateConfig? = null,
    val isOnboardingCompleted: Boolean = false,
    val currentStep: SplashSteps = SplashSteps.PLEASE_WAIT
)

enum class SplashSteps(val titleResId: Int) {
    PLEASE_WAIT(titleResId = R.string.splash_step_wait),
    CHECKING_PURCHASES(titleResId = R.string.splash_step_purchases),
    UPDATING_INFORMATION(titleResId = R.string.splash_step_updating),
}

sealed class SplashSideEffect {
    object NavigateOnboarding : SplashSideEffect()
    data class NavigateHome(val deepLinkUri: String?) : SplashSideEffect()
    data class NavigateDestination(val destination: HomeDestination) : SplashSideEffect()
//    data class NavigateDeepLink(val deepLinkDestination: HomeDestination) : SplashSideEffect()
    data class ShowCriticalUpdate(
        val config: digital.euforia.app.domain.model.config.CriticalUpdateConfig,
        val isOnboardingCompleted: Boolean
    ) : SplashSideEffect()
}