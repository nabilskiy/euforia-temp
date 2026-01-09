package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import digital.euforia.app.data.db.entity.FaqCategory
import digital.euforia.app.data.db.entity.FaqCategoryWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface FaqCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FaqCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FaqCategory)

    @Query("SELECT * FROM faq_categories ORDER BY id ASC")
    fun getAllFlow(): Flow<List<FaqCategory>>

    @Transaction
    @Query("SELECT * FROM faq_categories ORDER BY id ASC")
    fun getAllWithItemsFlow(): Flow<List<FaqCategoryWithItems>>

    @Transaction
    @Query("SELECT * FROM faq_categories ORDER BY id ASC")
    fun getAllWithItems(): List<FaqCategoryWithItems>

    @Query("DELETE FROM faq_categories")
    suspend fun clearAll()
}
