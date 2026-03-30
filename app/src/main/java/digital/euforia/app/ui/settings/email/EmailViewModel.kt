package digital.euforia.app.ui.settings.email

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.usecase.onboarding.ValidateEmailUseCase
import digital.euforia.app.domain.usecase.settings.UpdateEmailUseCase
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class EmailViewModel @Inject constructor(
    private val profilePreferences: ProfilePreferences,
    private val updateEmailUseCase: UpdateEmailUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
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
            val isValid = state.email?.let { email ->
                validateEmailUseCase.invoke(email)
            } ?: false

            reduce {
                state.copy(email = email, isValid = isValid)
            }
        }
    }

    fun onSaveClicked() {
        intent {
            if (!state.isValid || state.email.isNullOrBlank()) {
                return@intent
            } else {
                state.email?.let { email ->
                    updateEmailUseCase.invoke(email).onFinish {
                        postSideEffect(EmailSideEffect.NavigateBack)
                    }
                }
            }
        }
    }
}

data class EmailState(
    val errorMessage: String? = null,
    val email: String? = null,
    val isValid: Boolean = true,
    val isUpdating: Boolean = false
)

sealed class EmailSideEffect {
    data object NavigateBack : EmailSideEffect()
}
