package digital.euforia.app.data.db.converter

import androidx.room.TypeConverter
import digital.euforia.app.domain.model.TimeOfDay

class AccompanimentTypeConverters {
    @TypeConverter
    fun fromTimeOfDay(value: TimeOfDay): String = value.name

    @TypeConverter
    fun toTimeOfDay(value: String): TimeOfDay {
        return TimeOfDay.valueOf(value)
    }
}