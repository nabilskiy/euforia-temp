package digital.euforia.app.ui.settings.subscription.info

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.BuildConfig
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.config.defaultTimeOfDayConfig
import digital.euforia.app.ui.util.getCurrentTimeOfDay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences,
    private val configFetcher: EuforiaRemoteConfigFetcher
) : ViewModel(),
    ContainerHost<DeviceInfoState, DeviceInfoSideEffect> {
    override val container = container<DeviceInfoState, DeviceInfoSideEffect>(
        initialState = DeviceInfoState(),
        onCreate = {
            initInfo()
        }
    )

    private fun initInfo() {
        viewModelScope.launch {
            val isDemo = profilePreferences.getIsDemo()
            val dateOffset = appPreferences.getCompletedDays()
            val firstLaunch = appPreferences.getFirstLaunchDate()
            val timeOfDay =
                getCurrentTimeOfDay(configFetcher.getTimeOfDayConfig() ?: defaultTimeOfDayConfig())
            val deviceToken = appPreferences.getDeviceToken()
            val subsToken = profilePreferences.getSubsToken()
            val isSubscriptionValid = profilePreferences.isSubscriptionValid()
            val isDebug = BuildConfig.DEBUG
            val version = "Android " + Build.VERSION.SDK_INT + " " + Build.VERSION.INCREMENTAL
            val languageTag = appPreferences.getLanguage()
            val placeholder =
                "Device ID:\n%s\n\nSubscription ID:\n%s\n\nSubscription Valid:\n%s\n\nDemo:\n%s\n\nTime Of Day:\n%s\n\nFirst Launch:\n%s\n\nDate Offset:\n%s\n\nD-Mode:\n%s\n\nSystem:\n%s\n\nDevice:\n%s\n\nLocale:\n%s"

            val info = String.format(
                placeholder,
                deviceToken,
                subsToken,
                isSubscriptionValid,
                isDemo,
                timeOfDay,
                firstLaunch,
                dateOffset,
                isDebug,
                version,
                (Build.BRAND + " " + Build.DEVICE + " " + Build.MODEL + " " + Build.ID),
                languageTag
            )

            intent {
                reduce {
                    state.copy(
                        info = info,
                        deviceToken = deviceToken,
                        subsToken = subsToken.toString(),
                        isSubscriptionValid = isSubscriptionValid,
                        isDebug = isDebug,
                        version = version,
                        languageTag = languageTag.toString(),
                        dateOffset = dateOffset.toString(),
                        isDemo = isDemo,
                        firstLaunch = firstLaunch.toString(),
                        timeOfDay = timeOfDay.name,
                        systemInfo = (Build.BRAND + " " + Build.DEVICE + " " + Build.MODEL + " " + Build.ID)
                    )
                }
            }
        }
    }

    fun copyToClipboard() {
        intent {
            postSideEffect(DeviceInfoSideEffect.ShowCopyNotification(state.info))
        }
    }

    fun sendSupportEmail() {
        intent {
            postSideEffect(DeviceInfoSideEffect.SendEmail(state.info))
        }
    }
}

data class DeviceInfoState(
    val errorMessage: String? = null,
    val info: String = "",
    val deviceToken: String = "",
    val subsToken: String = "",
    val isSubscriptionValid: Boolean = false,
    val isDebug: Boolean = false,
    val version: String = "",
    val languageTag: String = "",
    val dateOffset: String = "",
    val isDemo: Boolean = false,
    val firstLaunch: String = "",
    val timeOfDay: String = "",
    val systemInfo: String = "",
)

sealed class DeviceInfoSideEffect {
    data class ShowCopyNotification(val info: String) : DeviceInfoSideEffect()
    data class SendEmail(val info: String) : DeviceInfoSideEffect()
}