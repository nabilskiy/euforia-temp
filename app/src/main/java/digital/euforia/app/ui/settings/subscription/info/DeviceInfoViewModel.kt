package digital.euforia.app.ui.settings.subscription.info

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.BuildConfig
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val profilePreferences: ProfilePreferences
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
            val deviceToken = appPreferences.getDeviceToken()
            val subsToken = profilePreferences.getSubsToken()
            val isSubscriptionValid = profilePreferences.isSubscriptionValid()
            val isDebug = BuildConfig.DEBUG
            val version = "Android " + Build.VERSION.SDK_INT + " " + Build.VERSION.INCREMENTAL
            val languageTag = appPreferences.getLanguage()
            val placeholder =
                "Device ID:\n%s\n\nSubscription ID:\n%s\n\nSubscription Valid:\n%s\n\nD-Mode:\n%s\n\nSystem:\n%s\n\nDevice:\n%s\n\nLocale:\n%s"

            val info = String.format(
                placeholder,
                deviceToken,
                subsToken,
                isSubscriptionValid,
                isDebug,
                version,
                (Build.BRAND + " " + Build.DEVICE + " " + Build.MODEL + " " + Build.ID),
                languageTag
            )

            intent { reduce { state.copy(info = info) } }
        }
    }
}

data class DeviceInfoState(val errorMessage: String? = null, val info: String = "")

sealed class DeviceInfoSideEffect {}