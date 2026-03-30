package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AccompanimentItemReview(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "accompaniment_item_id") val accompanimentId: Int,
    @ColumnInfo(name = "message") val viewCount: String? = null,
)