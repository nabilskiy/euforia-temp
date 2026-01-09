package digital.euforia.app.ui.settings.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.FeedbackQuestionType
import digital.euforia.app.domain.usecase.feedback.GetFeedbackFormUseCase
import digital.euforia.app.ui.settings.feedback.toUiFeedbackForm
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val getFeedbackFormUseCase: GetFeedbackFormUseCase
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

    fun onSubmitForm() {}
}

data class FeedbackState(
    val errorMessage: String? = null,
//    val form: FeedbackFormWithQuestions? = null
    val form: UiFeedbackForm? = null,
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

sealed class FeedbackSideEffect {}