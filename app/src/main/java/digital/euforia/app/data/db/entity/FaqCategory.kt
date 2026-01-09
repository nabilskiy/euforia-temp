package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faq_categories")
data class FaqCategory(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    // Network "class" field value, e.g. "faq_category"
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
)
