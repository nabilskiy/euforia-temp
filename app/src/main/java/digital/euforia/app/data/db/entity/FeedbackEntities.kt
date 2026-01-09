package digital.euforia.app.data.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "feedback_forms")
data class FeedbackForm(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String, // network "class" value, e.g. "form"
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "button_title") val buttonTitle: String?,
    @ColumnInfo(name = "footer_text") val footerText: String?,
    @ColumnInfo(name = "refill") val refill: Boolean,
    @ColumnInfo(name = "expiry_date") val expiryDate: String?,
)

@Entity(
    tableName = "feedback_questions",
    foreignKeys = [
        ForeignKey(
            entity = FeedbackForm::class,
            parentColumns = ["id"],
            childColumns = ["form_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        )
    ],
    indices = [Index(value = ["form_id"])],
)
data class FeedbackQuestion(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "form_id") val formId: Int,
    @ColumnInfo(name = "type_class") val typeClass: String, // network "class" value, e.g. "question"
    @ColumnInfo(name = "type") val type: FeedbackQuestionType, // multi_choice | single_choice | text
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "is_required") val isRequired: Boolean,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "hint") val hint: String?,
)

@Entity(
    tableName = "feedback_options",
    foreignKeys = [
        ForeignKey(
            entity = FeedbackQuestion::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        )
    ],
    indices = [Index(value = ["question_id"])],
)
data class FeedbackOption(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "question_id") val questionId: Int,
    @ColumnInfo(name = "type_class") val typeClass: String, // network "class" value, e.g. "question_option"
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "link") val link: String?,
)

data class FeedbackQuestionWithOptions(
    @Embedded val question: FeedbackQuestion,
    @Relation(
        parentColumn = "id",
        entityColumn = "question_id"
    )
    val options: List<FeedbackOption>
)

data class FeedbackFormWithQuestions(
    @Embedded val form: FeedbackForm,
    @Relation(
        parentColumn = "id",
        entityColumn = "form_id",
        entity = FeedbackQuestion::class
    )
    val questions: List<FeedbackQuestionWithOptions>
)

/**
 * Enum representing supported question types from the feedback form API.
 */
@Keep
enum class FeedbackQuestionType(val wireValue: String) {
    MULTI_CHOICE("multi_choice"),
    SINGLE_CHOICE("single_choice"),
    TEXT("text"),
    UNKNOWN("unknown");

    companion object {
        fun fromNetwork(value: String?): FeedbackQuestionType = when (value?.lowercase()) {
            MULTI_CHOICE.wireValue -> MULTI_CHOICE
            SINGLE_CHOICE.wireValue -> SINGLE_CHOICE
            TEXT.wireValue -> TEXT
            else -> UNKNOWN
        }
    }
}
