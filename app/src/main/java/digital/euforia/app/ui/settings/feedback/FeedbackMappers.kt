package digital.euforia.app.ui.settings.feedback

import digital.euforia.app.data.db.entity.FeedbackFormWithQuestions
import digital.euforia.app.data.db.entity.FeedbackOption
import digital.euforia.app.data.db.entity.FeedbackQuestionWithOptions

/**
 * Extension mappers from DB entities to UI models for Feedback screen.
 */
fun FeedbackFormWithQuestions.toUiFeedbackForm(): UiFeedbackForm = UiFeedbackForm(
    id = form.id,
    alias = form.alias,
    title = form.title,
    description = form.description,
    buttonTitle = form.buttonTitle,
    footerText = form.footerText,
    refill = form.refill,
    expiryDate = form.expiryDate,
    questions = questions.map { it.toUiQuestion() }
)

fun FeedbackQuestionWithOptions.toUiQuestion(): UiQuestion = UiQuestion(
    formId = question.formId,
    id = question.id,
    type = question.type,
    key = question.key,
    isRequired = question.isRequired,
    title = question.title,
    text = question.text,
    hint = question.hint,
    options = options.map { it.toUiOption() },
    selectedOptionIds = emptySet()
)

fun FeedbackOption.toUiOption(): UiOption = UiOption(
    questionId = questionId,
    id = id,
    isSelected = false,
    text = text,
    title = title,
    link = link,
)
