package digital.euforia.app.ui.settings.name

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.store.ProfilePreferences
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NameViewModel @Inject constructor(
    private val profilePreferences: ProfilePreferences,
    private val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<NameState, NameSideEffect> {
    override val container = container<NameState, NameSideEffect>(
        initialState = NameState(),
        onCreate = {
            loadName()
        }
    )

    fun loadName() {
        intent {
            val name = profilePreferences.getName()
            reduce { state.copy(name = name) }
        }
    }

    fun onNameChanged(name: String) {
        intent {
            reduce { state.copy(name = name) }
            profilePreferences.setName(name)
            analyticSender.setName(name)
        }
    }
}

data class NameState(
    val errorMessage: String? = null,
    val name: String? = null
)

sealed class NameSideEffect {}