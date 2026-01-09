package digital.euforia.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.domain.usecase.app_settings.SyncAppSettingsUseCase
import digital.euforia.app.domain.usecase.subscription.SyncPurchaseUseCase
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val syncAppSettingsUseCase: SyncAppSettingsUseCase,
    private val syncPurchaseUseCase: SyncPurchaseUseCase,
) : ViewModel(), ContainerHost<MainState, MainSideEffect> {
    override val container = container<MainState, MainSideEffect>(
        initialState = MainState(),
        onCreate = {
            observeLanguageChanges()
        }
    )

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            appPreferences.getLanguageFlow().collectLatest { lang ->
                val localeList = LocaleListCompat.forLanguageTags(lang)
                AppCompatDelegate.setApplicationLocales(localeList)
                reduceState { copy(language = lang ?: "en") }
            }
        }
    }

     fun syncPurchase(purchaseJson: String) {
        viewModelScope.launch {

            syncPurchaseUseCase.invoke(purchaseJson)
        }
    }
}

data class MainState(val language: String = "en")

sealed class MainSideEffect {}