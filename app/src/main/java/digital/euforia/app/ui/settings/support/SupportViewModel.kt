package digital.euforia.app.ui.settings.support

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.R
import digital.euforia.app.domain.model.settings.FeedbackType
import digital.euforia.app.domain.usecase.onboarding.ValidateEmailUseCase
import digital.euforia.app.domain.usecase.settings.SendFeedbackUseCase
import digital.euforia.app.domain.usecase.settings.SendSupportMessageUseCase
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val sendFeedbackUseCase: SendFeedbackUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val sendSupportMessageUseCase: SendSupportMessageUseCase
) : ViewModel(),
    ContainerHost<SupportState, SupportSideEffect> {
    override val container = container<SupportState, SupportSideEffect>(
        initialState = SupportState(),
        onCreate = {}
    )

    fun onEmailChanged(email: String) {
        intent {
            val isEmailValid = validateEmailUseCase.invoke(email)
            reduce {
                state.copy(
                    email = email,
                    isEmailValid = isEmailValid,
                    emailError = null
                )
            }
        }
    }

    fun onMessageChanged(message: String) {
        intent {
            val isMessageValid = message.length > 5
            reduce {
                state.copy(
                    message = message,
                    isMessageValid = isMessageValid,
                    messageError = null
                )
            }
        }
    }

    fun sendFeedback() {
        intent {
            val isEmailValid = validateEmailUseCase.invoke(state.email.orEmpty())
            val isMessageValid = state.message?.trim().orEmpty().length > 7

            if (isEmailValid && isMessageValid) {
                sendSupportMessageUseCase.invoke(
                    message = state.message.orEmpty(),
                    email = state.email.orEmpty()
                ).onSuccess {
                    postSideEffect(SupportSideEffect.ShowToast(R.string.sent_success))
                    postSideEffect(SupportSideEffect.CloseSheet)
                    reduce {
                        state.copy(
                            errorMessage = null,
                            email = null,
                            message = null,
                            isEmailValid = false,
                            isMessageValid = false,
                            emailError = null,
                            messageError = null
                        )
                    }

                }.onFailure {
                    postSideEffect(SupportSideEffect.ShowToast(R.string.error))
                }.onFinish {

                }

//                sendFeedbackUseCase.invoke(
//                    type = FeedbackType.SUPPORT,
//                    message = state.message,
//                    userEmail = state.email
//                )
            } else {
                reduce {
                    state.copy(
                        emailError = if (isEmailValid) null else "Invalid email",
                        messageError = if (isMessageValid) null else "Message too short"
                    )
                }
            }
        }
    }
}

data class SupportState(
    val errorMessage: String? = null,
    val email: String? = null,
    val message: String? = null,
    val isEmailValid: Boolean = false,
    val isMessageValid: Boolean = false,
    val emailError: String? = null,
    val messageError: String? = null,
)

sealed class SupportSideEffect {
    data class ShowToast(val messageRes: Int) : SupportSideEffect()
    object CloseSheet : SupportSideEffect()
}