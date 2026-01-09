package digital.euforia.app.ui.sos.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.model.emergency.EmergencyContact
import digital.euforia.app.domain.usecase.emergency.GetEmergencyContactsUseCase
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getEmergencyContactsUseCase: GetEmergencyContactsUseCase,
    val analyticSender: AnalyticSender
) : ViewModel(),
    ContainerHost<ContactsState, ContactsSideEffect> {
    override val container = container<ContactsState, ContactsSideEffect>(
        initialState = ContactsState(),
        onCreate = {
            analyticSender.sosContactsShow()
            loadContacts()
        }
    )

    private fun loadContacts() {
        viewModelScope.launch {
            val contacts = getEmergencyContactsUseCase.invoke()

            intent {
                reduce {
                    state.copy(
                        contactsList = contacts
                    )
                }
            }
        }
    }
}

data class ContactsState(
    val errorMessage: String? = null,
    val contactsList: List<EmergencyContact> = emptyList(),
)

sealed class ContactsSideEffect {}