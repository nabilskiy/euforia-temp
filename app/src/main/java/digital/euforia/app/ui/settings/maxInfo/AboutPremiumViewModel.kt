package digital.euforia.app.ui.settings.maxInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.model.subscription.MaxInfo
import digital.euforia.app.domain.usecase.subscription.GetMaxInfoUseCase
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AboutPremiumViewModel @Inject constructor(
    private val getMaxInfoUseCase: GetMaxInfoUseCase,
    private val analyticSender: AnalyticSender
) : ViewModel(),
    ContainerHost<MaxInfoState, MaxInfoSideEffect> {
    override val container = container<MaxInfoState, MaxInfoSideEffect>(
        initialState = MaxInfoState(),
        onCreate = {
            analyticSender.aboutPremiumItemClick()
            analyticSender.aboutPremiumShow()
            loadMaxInfo()
        }
    )

    private fun loadMaxInfo() {
        viewModelScope.launch {
            val info = getMaxInfoUseCase.invoke()
            intent { reduce { state.copy(maxInfo = info) } }
        }
    }
}

data class MaxInfoState(
    val errorMessage: String? = null,
    val maxInfo: MaxInfo? = null
)

sealed class MaxInfoSideEffect {}