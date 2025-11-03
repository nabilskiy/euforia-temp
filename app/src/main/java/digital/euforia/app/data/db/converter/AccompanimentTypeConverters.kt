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

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(',')
            .filter { it.isNotEmpty() }
            .mapNotNull { part ->
                part.toIntOrNull()
            }
    }
}