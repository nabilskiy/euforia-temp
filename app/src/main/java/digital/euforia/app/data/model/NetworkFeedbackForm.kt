package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.FeedbackForm
import digital.euforia.app.data.db.entity.FeedbackOption
import digital.euforia.app.data.db.entity.FeedbackQuestion
import digital.euforia.app.data.db.entity.FeedbackQuestionType

@JsonClass(generateAdapter = true)
data class NetworkFeedbackForm(
    @Json(name = "class") val type: String, // "form"
    @Json(name = "id") val id: Int,
    @Json(name = "alias") val alias: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "buttonTitle") val buttonTitle: String?,
    @Json(name = "footerText") val footerText: String?,
    @Json(name = "refill") val refill: Boolean,
    // Could be null or a date string; keep as String? for flexibility
    @Json(name = "expiryDate") val expiryDate: String?,
    @Json(name = "items") val items: List<NetworkFeedbackQuestion>
)

@JsonClass(generateAdapter = true)
data class NetworkFeedbackQuestion(
    @Json(name = "class") val typeClass: String, // "question"
    @Json(name = "id") val id: Int,
    @Json(name = "type") val type: String, // multi_choice | single_choice | text
    @Json(name = "key") val key: String,
    @Json(name = "isRequired") val isRequired: Boolean,
    @Json(name = "title") val title: String?,
    @Json(name = "text") val text: String,
    @Json(name = "hint") val hint: String?,
    @Json(name = "items") val items: List<NetworkFeedbackOption>? = null,
)

@JsonClass(generateAdapter = true)
data class NetworkFeedbackOption(
    @Json(name = "class") val typeClass: String, // "question_option"
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "text") val text: String,
    @Json(name = "link") val link: String?,
)

fun NetworkFeedbackForm.toEntity(): FeedbackForm = FeedbackForm(
    id = id,
    type = type,
    alias = alias,
    title = title,
    description = description,
    buttonTitle = buttonTitle,
    footerText = footerText,
    refill = refill,
    expiryDate = expiryDate,
)

fun NetworkFeedbackQuestion.toEntity(formId: Int): FeedbackQuestion = FeedbackQuestion(
    id = id,
    formId = formId,
    typeClass = typeClass,
    type = FeedbackQuestionType.fromNetwork(type),
    key = key,
    isRequired = isRequired,
    title = title,
    text = text,
    hint = hint,
)

fun NetworkFeedbackOption.toEntity(questionId: Int): FeedbackOption = FeedbackOption(
    id = id,
    questionId = questionId,
    typeClass = typeClass,
    title = title,
    text = text,
    link = link,
)
