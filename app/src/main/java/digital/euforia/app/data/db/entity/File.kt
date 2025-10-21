package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Stores a single NetworkFile in local DB.
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "files")
data class File(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "ext") val ext: String,
    @ColumnInfo(name = "size") val size: Int,
    @ColumnInfo(name = "duration") val duration: Int,
)
