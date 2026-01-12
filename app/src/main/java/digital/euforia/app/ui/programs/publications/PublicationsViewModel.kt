package digital.euforia.app.ui.programs.publications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.domain.usecase.program.GetPublicationInfosUseCase
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class PublicationsViewModel @Inject constructor(
    private val getPublicationInfosUseCase: GetPublicationInfosUseCase
) : ViewModel(), ContainerHost<PublicationsState, PublicationsSideEffect> {
    override val container = container<PublicationsState, PublicationsSideEffect>(
        initialState = PublicationsState(),
        onCreate = {}
    )
}

data class PublicationsState(val errorMessage: String? = null)

sealed class PublicationsSideEffect {}