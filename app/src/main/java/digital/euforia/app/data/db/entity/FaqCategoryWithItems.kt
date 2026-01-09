package digital.euforia.app.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FaqCategoryWithItems(
    @Embedded val category: FaqCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val items: List<FaqItem>
)
