package digital.euforia.app.data.db.converter

import androidx.room.TypeConverter
import digital.euforia.app.data.db.entity.FeedbackQuestionType

class FeedbackTypeConverters {

    @TypeConverter
    fun fromStringToQuestionType(value: String?): FeedbackQuestionType {
        return FeedbackQuestionType.fromNetwork(value)
    }

    @TypeConverter
    fun fromQuestionTypeToString(value: FeedbackQuestionType?): String? {
        return value?.wireValue
    }
}
