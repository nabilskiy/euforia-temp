package digital.euforia.app.data.db.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import digital.euforia.app.data.model.NetworkFile

/**
 * Room type converters for persisting NetworkFile as JSON string.
 * This lets us keep both the original *_url fields and the full file objects in the DB.
 */
class NetworkFileConverters {
    private val moshi: Moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(NetworkFile::class.java)

    @TypeConverter
    fun fromJson(value: String?): NetworkFile? {
        if (value.isNullOrBlank()) return null
        return runCatching { adapter.fromJson(value) }.getOrNull()
    }

    @TypeConverter
    fun toJson(file: NetworkFile?): String? {
        return file?.let { adapter.toJson(it) }
    }
}
