package digital.euforia.app.ui.howitworks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HowItWorksViewModel @Inject constructor(
    val configFetcher: EuforiaRemoteConfigFetcher,
) : ViewModel(),
    ContainerHost<HowItWorksState, HowItWorksSideEffect> {
    override val container = container<HowItWorksState, HowItWorksSideEffect>(
        initialState = HowItWorksState(),
        onCreate = {
            getVideoUrl()
        }
    )

    private fun getVideoUrl() {
        viewModelScope.launch {
            val videoUrl = configFetcher.getTodayIntroVideoUrl()
            intent { reduce { state.copy(videoUrl = videoUrl) } }
        }
    }
}

data class HowItWorksState(
    val errorMessage: String? = null,
    val videoUrl: String? = null
)

sealed class HowItWorksSideEffect {}