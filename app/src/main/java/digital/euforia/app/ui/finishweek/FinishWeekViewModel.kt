package digital.euforia.app.ui.finishweek

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FinishWeekViewModel @Inject constructor() : ViewModel(), ContainerHost<FinishWeekState, FinishWeekSideEffect> {
    override val container = container<FinishWeekState, FinishWeekSideEffect>(
        initialState = FinishWeekState(),
        onCreate = {}
    )
}

data class FinishWeekState(val errorMessage: String? = null)

sealed class FinishWeekSideEffect {}