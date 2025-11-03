package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Not an entity, just a DTO/POJO for reading. */
data class AccompanimentWithItems(
    @Embedded val accompaniment: Accompaniment,
    @Relation(
        parentColumn = "id",
        entityColumn = "accompaniment_id"
    )
    val items: List<AccompanimentItem>
)

@Entity(tableName = "accompaniment")
data class Accompaniment(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,

    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "demo") val demo: Boolean,
    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "morning_title") val morningTitle: String,
    @ColumnInfo(name = "daytime_title") val daytimeTitle: String,
    @ColumnInfo(name = "evening_title") val eveningTitle: String,
    @ColumnInfo(name = "description") val description: String,

    @ColumnInfo(name = "morning_image_url") val morningImageUrl: String?,
    @ColumnInfo(name = "daytime_image_url") val daytimeImageUrl: String?,
    @ColumnInfo(name = "evening_image_url") val eveningImageUrl: String?,

    // Keep simple URL fields
    @ColumnInfo(name = "morning_music_url") val morningMusicUrl: String?,
    @ColumnInfo(name = "daytime_music_url") val daytimeMusicUrl: String?,
    @ColumnInfo(name = "evening_music_url") val eveningMusicUrl: String?,

    @ColumnInfo(name = "morning_male_audio_url") val morningMaleAudioUrl: String?,
    @ColumnInfo(name = "morning_female_audio_url") val morningFemaleAudioUrl: String?,
    @ColumnInfo(name = "daytime_male_audio_url") val daytimeMaleAudioUrl: String?,
    @ColumnInfo(name = "daytime_female_audio_url") val daytimeFemaleAudioUrl: String?,
    @ColumnInfo(name = "evening_male_audio_url") val eveningMaleAudioUrl: String?,
    @ColumnInfo(name = "evening_female_audio_url") val eveningFemaleAudioUrl: String?,

    @Embedded(prefix = "morning_music_file_") val morningMusic: File?,
    @Embedded(prefix = "daytime_music_file_") val daytimeMusic: File?,
    @Embedded(prefix = "evening_music_file_") val eveningMusic: File?,

    @Embedded(prefix = "morning_male_audio_file_") val morningMaleAudio: File?,
    @Embedded(prefix = "morning_female_audio_file_") val morningFemaleAudio: File?,
    @Embedded(prefix = "daytime_male_audio_file_") val daytimeMaleAudio: File?,
    @Embedded(prefix = "daytime_female_audio_file_") val daytimeFemaleAudio: File?,
    @Embedded(prefix = "evening_male_audio_file_") val eveningMaleAudio: File?,
    @Embedded(prefix = "evening_female_audio_file_") val eveningFemaleAudio: File?,

    @ColumnInfo(name = "morning_content") val morningContent: String,
    @ColumnInfo(name = "daytime_content") val daytimeContent: String,
    @ColumnInfo(name = "evening_content") val eveningContent: String,

    // Store phrases list as JSON using TypeConverter
    @ColumnInfo(name = "phrases") val phrases: List<Phrase> = emptyList(),

    @ColumnInfo(name = "published_at") val publishedAt: Long,
)

fun Accompaniment.getCurrentPhrase(accompanimentItem: AccompanimentItem): Phrase? {
    val viewedPhraseIds = accompanimentItem.viewedPhraseId.toSet()
    return phrases.firstOrNull { phrase ->
        phrase.id !in viewedPhraseIds
    }
}

fun Accompaniment.getCompletedPhrases(accompanimentItem: AccompanimentItem): List<Phrase> {
    val viewedPhraseIds = accompanimentItem.viewedPhraseId.toSet()
    return phrases.filter { phrase ->
        phrase.id in viewedPhraseIds
    }
}