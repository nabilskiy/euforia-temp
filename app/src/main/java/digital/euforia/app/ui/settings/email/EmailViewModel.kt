package digital.euforia.app.ui.settings.email

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.ProfilePreferences
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class EmailViewModel @Inject constructor(
    private val profilePreferences: ProfilePreferences
) : ViewModel(), ContainerHost<EmailState, EmailSideEffect> {
    override val container = container<EmailState, EmailSideEffect>(
        initialState = EmailState(),
        onCreate = {
            loadEmail()
        }
    )

    fun loadEmail() {
        intent {
            val email = profilePreferences.getEmail()
            reduce { state.copy(email = email) }
        }
    }

    fun onEmailChanged(email: String) {
        intent {
            reduce { state.copy(email = email) }
            profilePreferences.setEmail(email)
        }
    }
}

data class EmailState(
    val errorMessage: String? = null,
    val email: String? = null
)

sealed class EmailSideEffect {}
