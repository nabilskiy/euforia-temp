package digital.euforia.app.ui.settings.personaldata

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class PersonalDataViewModel @Inject constructor(
    val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<PersonalDataState, PersonalDataSideEffect> {
    override val container = container<PersonalDataState, PersonalDataSideEffect>(
        initialState = PersonalDataState(),
        onCreate = {}
    )
}

data class PersonalDataState(val errorMessage: String? = null)

sealed class PersonalDataSideEffect {}