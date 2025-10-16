package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import digital.euforia.app.domain.model.TimeOfDay

/**
 * Tracks user interaction with accompaniment items.
 * Uses viewed_phrase_id as a nullable FK to Phrase.
 */
@Entity(
    tableName = "accompaniment_item",
    foreignKeys = [
        ForeignKey(
            entity = Phrase::class,
            parentColumns = ["id"],
            childColumns = ["viewed_phrase_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = Accompaniment::class,
            parentColumns = ["id"],
            childColumns = ["accompaniment_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("viewed_phrase_id"), Index("accompaniment_id")]
)
data class AccompanimentItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,

    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "time_of_day") val timeOfDay: TimeOfDay,

    @ColumnInfo(name = "accompaniment_id") val accompanimentId: Int,
//    @ColumnInfo(name = "title") val title: String,

    @ColumnInfo(name = "view_count") val viewCount: Int = 0,
    @ColumnInfo(name = "viewed_phrase_id") val viewedPhraseId: Int? = null,
    @ColumnInfo(name = "viewed_at") val viewedAt: Long? = null,

    @ColumnInfo(name = "is_rated") val isRated: Boolean = false,
    @ColumnInfo(name = "rating") val rating: Int? = null,

    @ColumnInfo(name = "playing_time") val playingTime: Long? = null,
    @ColumnInfo(name = "playing_duration") val playingDuration: Long? = null,
    @ColumnInfo(name = "total_playing_duration") val totalPlayingDuration: Long? = null,
)
