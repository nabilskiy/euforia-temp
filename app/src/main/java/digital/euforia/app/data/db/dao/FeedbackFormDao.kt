package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import digital.euforia.app.data.db.entity.FeedbackForm
import digital.euforia.app.data.db.entity.FeedbackFormWithQuestions
import digital.euforia.app.data.db.entity.FeedbackOption
import digital.euforia.app.data.db.entity.FeedbackQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackFormDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: FeedbackForm)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(items: List<FeedbackQuestion>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(items: List<FeedbackOption>)

    @Query("DELETE FROM feedback_forms WHERE id = :formId")
    suspend fun deleteForm(formId: Int)

    @Query("DELETE FROM feedback_questions WHERE form_id = :formId")
    suspend fun deleteQuestionsByFormId(formId: Int)

    @Query("DELETE FROM feedback_options WHERE question_id IN (SELECT id FROM feedback_questions WHERE form_id = :formId)")
    suspend fun deleteOptionsByFormId(formId: Int)

    @Transaction
    @Query("SELECT * FROM feedback_forms WHERE id = :id LIMIT 1")
    fun getFormWithQuestionsByIdFlow(id: Int): Flow<FeedbackFormWithQuestions?>

    @Transaction
    @Query("SELECT * FROM feedback_forms WHERE alias = :alias LIMIT 1")
    fun getFormWithQuestionsByAliasFlow(alias: String): Flow<FeedbackFormWithQuestions?>

    // Non-Flow getters for one-shot access
    @Transaction
    @Query("SELECT * FROM feedback_forms WHERE id = :id LIMIT 1")
    fun getFormWithQuestionsById(id: Int): FeedbackFormWithQuestions?

    @Transaction
    @Query("SELECT * FROM feedback_forms WHERE alias = :alias LIMIT 1")
    fun getFormWithQuestionsByAlias(alias: String): FeedbackFormWithQuestions?

    @Transaction
    suspend fun replaceForm(
        form: FeedbackForm,
        questions: List<FeedbackQuestion>,
        options: List<FeedbackOption>,
    ) {
        // Clear old data for this form
        deleteOptionsByFormId(form.id)
        deleteQuestionsByFormId(form.id)
        deleteForm(form.id)

        // Insert new data
        insertForm(form)
        if (questions.isNotEmpty()) insertQuestions(questions)
        if (options.isNotEmpty()) insertOptions(options)
    }

    @Query("DELETE FROM feedback_forms")
    suspend fun clearAllForms()

    @Query("DELETE FROM feedback_questions")
    suspend fun clearAllQuestions()

    @Query("DELETE FROM feedback_options")
    suspend fun clearAllOptions()

    @Transaction
    suspend fun clearAll() {
        clearAllOptions()
        clearAllQuestions()
        clearAllForms()
    }
}
