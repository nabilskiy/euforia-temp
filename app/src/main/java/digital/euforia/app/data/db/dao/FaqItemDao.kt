package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.FaqItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FaqItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FaqItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FaqItem)

    @Query("SELECT * FROM faq_items ORDER BY id ASC")
    fun getAllFlow(): Flow<List<FaqItem>>

    @Query("SELECT * FROM faq_items WHERE category_id = :categoryId ORDER BY id ASC")
    fun getByCategoryIdFlow(categoryId: Int): Flow<List<FaqItem>>

    @Query("DELETE FROM faq_items")
    suspend fun clearAll()
}
