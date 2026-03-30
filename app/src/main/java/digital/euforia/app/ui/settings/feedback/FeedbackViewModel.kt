package digital.euforia.app.ui.settings.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.FeedbackQuestionType
import digital.euforia.app.domain.usecase.feedback.GetFeedbackFormUseCase
import digital.euforia.app.domain.usecase.settings.SubmitFormUseCase
import digital.euforia.app.data.model.FormAnswerRequest
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val getFeedbackFormUseCase: GetFeedbackFormUseCase,
    private val submitFormUseCase: SubmitFormUseCase
) : ViewModel(),
    ContainerHost<FeedbackState, FeedbackSideEffect> {
    override val container = container<FeedbackState, FeedbackSideEffect>(
        initialState = FeedbackState(),
        onCreate = {
            loadForm()
        }
    )

    private fun Syntax<FeedbackState, FeedbackSideEffect>.loadForm() {
        viewModelScope.launch {
            val form = getFeedbackFormUseCase()
            reduce { state.copy(form = form?.toUiFeedbackForm()) }
        }
    }

    fun resetForm() {
        intent {
            val form = getFeedbackFormUseCase()
            reduce { FeedbackState(form = form?.toUiFeedbackForm()) }
        }
    }

    fun onOptionSelected(option: UiOption) {
        intent {
            val form = state.form ?: return@intent
            val questions = form.questions.map { question ->
                if (question.id == option.questionId) {
                    val selectedOptionIds = when (question.type) {
                        FeedbackQuestionType.SINGLE_CHOICE -> setOf(option.id)
                        FeedbackQuestionType.MULTI_CHOICE -> {
                            val currentSelections = question.selectedOptionIds.toMutableSet()
                            if (currentSelections.contains(option.id)) {
                                currentSelections.remove(option.id)
                            } else {
                                currentSelections.add(option.id)
                            }
                            currentSelections
                        }

                        else -> question.selectedOptionIds
                    }
                    question.copy(selectedOptionIds = selectedOptionIds)
                } else {
                    question
                }
            }
            val updatedForm = form.copy(questions = questions)
            reduce { state.copy(form = updatedForm) }
        }
    }

    fun onAnswerTextChanged(questionId: Int, answerText: String) {
        intent {
            val form = state.form ?: return@intent
            val questions = form.questions.map { question ->
                if (question.id == questionId) {
                    question.copy(answerText = answerText)
                } else {
                    question
                }
            }
            val updatedForm = form.copy(questions = questions)
            reduce { state.copy(form = updatedForm) }
        }
    }

    fun onCloseAttempt() {
        intent {
            val form = state.form ?: run {
                postSideEffect(FeedbackSideEffect.Close)
                return@intent
            }
            val hasAnswers = form.questions.any { question ->
                question.selectedOptionIds.isNotEmpty() || !question.answerText.isNullOrBlank()
            }

            if (hasAnswers) {
                reduce { state.copy(showCloseConfirmation = true) }
            } else {
                postSideEffect(FeedbackSideEffect.Close)
            }
        }
    }

    fun onConfirmClose() {
        intent {
            reduce { state.copy(showCloseConfirmation = false) }
            postSideEffect(FeedbackSideEffect.Close)
        }
    }

    fun onConfirmThanks() {
        intent {
            reduce { state.copy(showThanksDialog = false) }
            postSideEffect(FeedbackSideEffect.Close)
        }
    }

    fun onDismissCloseConfirmation() {
        intent {
            reduce { state.copy(showCloseConfirmation = false) }
        }
    }

    fun onSubmitForm() {
        intent {
            val form = state.form ?: return@intent
            val answers = mutableListOf<FormAnswerRequest>()
            var isValid = true

            form.questions.forEach { question ->
                val answer: Any? = when (question.type) {
                    FeedbackQuestionType.SINGLE_CHOICE -> question.selectedOptionIds.firstOrNull()
                    FeedbackQuestionType.MULTI_CHOICE -> question.selectedOptionIds.toList()
                    FeedbackQuestionType.TEXT -> question.answerText?.takeIf { it.isNotBlank() }
                    else -> null
                }

                if (question.isRequired && (answer == null || (answer is List<*> && answer.isEmpty()))) {
                    isValid = false
                }

                if (answer != null) {
                    answers.add(FormAnswerRequest(key = question.key, answer = answer))
                }
            }

            if (!isValid) {
                reduce { state.copy(errorResId = digital.euforia.app.R.string.feedback_form_validation_text) }
                return@intent
            }

            reduce { state.copy(isLoading = true, errorMessage = null, errorResId = null) }

            val result = submitFormUseCase(form.id.toString(), answers)
            when (result) {
                is ResultWrapper.Success -> {
                    reduce { state.copy(isLoading = false, isSuccess = true, showThanksDialog = true) }
                }

                is ResultWrapper.Failure -> {
                    reduce { state.copy(isLoading = false, errorMessage = result.throwable.message ?: "Something went wrong") }
                }
            }
        }
    }
}

data class FeedbackState(
    val errorMessage: String? = null,
    val errorResId: Int? = null,
    val form: UiFeedbackForm? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val showCloseConfirmation: Boolean = false,
    val showThanksDialog: Boolean = false,
)

data class UiOption(
    val questionId: Int,
    val id: Int,
    val isSelected: Boolean,
    val text: String,
    val title: String?,
    val link: String?,
)

data class UiQuestion(
    val formId: Int,
    val id: Int,
    val type: FeedbackQuestionType,
    val key: String,
    val isRequired: Boolean,
    val title: String?,
    val text: String,
    val hint: String?,
    val options: List<UiOption> = emptyList(),
    val selectedOptionIds: Set<Int> = emptySet(),
    val answerText: String? = null,
)

data class UiFeedbackForm(
    val id: Int,
    val alias: String,
    val title: String,
    val description: String?,
    val buttonTitle: String?,
    val footerText: String?,
    val refill: Boolean,
    val expiryDate: String?,
    val questions: List<UiQuestion> = emptyList(),

    )

sealed class FeedbackSideEffect {
    object Close : FeedbackSideEffect()
}