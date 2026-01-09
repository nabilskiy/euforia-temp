package digital.euforia.app.ui.settings.support

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.domain.model.settings.FeedbackType
import digital.euforia.app.domain.usecase.settings.SendFeedbackUseCase
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val sendFeedbackUseCase: SendFeedbackUseCase
) : ViewModel(),
    ContainerHost<SupportState, SupportSideEffect> {
    override val container = container<SupportState, SupportSideEffect>(
        initialState = SupportState(),
        onCreate = {}
    )

    fun onEmailChanged(email: String) {
        intent {
            reduce { state.copy(email = email) }
        }
    }

    fun onMessageChanged(message: String) {
        intent {
            reduce { state.copy(message = message) }
        }
    }

    fun sendFeedback() {
        intent {
            sendFeedbackUseCase.invoke(
                type = FeedbackType.SUPPORT,
                message = state.message,
                userEmail = state.email
            )
            reduce {
                state.copy(
                    errorMessage = null,
                    email = null,
                    message = null,
                )
            }

        }
    }
}

data class SupportState(
    val errorMessage: String? = null,
    val email: String? = null,
    val message: String? = null,
)

sealed class SupportSideEffect {}