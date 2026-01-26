package digital.euforia.app.data.db.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class IntListConverter {
    private val moshi = Moshi.Builder().build()
    private val type = Types.newParameterizedType(List::class.java, Int::class.javaObjectType)
    private val adapter = moshi.adapter<List<Int>>(type)

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.let { adapter.fromJson(it) }
    }
}
