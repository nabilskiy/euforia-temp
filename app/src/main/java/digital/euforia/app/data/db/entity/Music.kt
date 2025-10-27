package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music")
data class Music(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    // Network "class" field
    @ColumnInfo(name = "type") val type: String,
    // Network "type" field (scene, etc.)
    @ColumnInfo(name = "content_type") val contentType: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "file_url") val fileUrl: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,

    @Embedded(prefix = "embedded_file_") val file: File?,
)