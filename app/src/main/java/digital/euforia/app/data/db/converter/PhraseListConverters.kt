package digital.euforia.app.data.db.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import digital.euforia.app.data.db.entity.Phrase

/**
 * Room TypeConverters to store a list of Phrase as JSON in a single column.
 * This is a minimal persistence approach to keep phrases bundled with an Accompaniment.
 */
class PhraseListConverters {
 private val moshi = Moshi.Builder().build()
    private val type = Types.newParameterizedType(List::class.java, Phrase::class.java)
    private val adapter = moshi.adapter<List<Phrase>>(type)

    @TypeConverter
    fun fromPhraseList(value: List<Phrase>?): String? {
        return value?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toPhraseList(value: String?): List<Phrase>? {
        return value?.let { adapter.fromJson(it) }
    }
}
