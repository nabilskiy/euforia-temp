package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faq_items")
data class FaqItem(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    // Network "class" field value, e.g. "faq"
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    // The API uses parameter name "type" to pass category alias; store it explicitly
    @ColumnInfo(name = "category_alias") val categoryAlias: String,
    @ColumnInfo(name = "question") val question: String,
    @ColumnInfo(name = "answer") val answer: String,
    @ColumnInfo(name = "date") val date: Long?,
)
