package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "phrase")
data class Phrase(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,

    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "daytime_title") val daytimeTitle: String,
    @ColumnInfo(name = "description") val description: String,

    // Keep simple URL fields
    @ColumnInfo(name = "music_file_url") val musicFileUrl: String?,
    @ColumnInfo(name = "male_audio_url") val maleAudioUrl: String?,
    @ColumnInfo(name = "female_audio_url") val femaleAudioUrl: String?,

    // Persist embedded file details if provided by API
    @Embedded(prefix = "music_file_object_") val music: File?,
)
