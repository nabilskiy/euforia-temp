package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.AccompanimentItem
import digital.euforia.app.data.db.entity.AccompanimentWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface AccompanimentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Accompaniment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Accompaniment)

    @Query("SELECT * FROM accompaniment WHERE id = :id")
    suspend fun getById(id: Int): Accompaniment?

    @Query("SELECT * FROM accompaniment")
    fun getAll(): Flow<List<Accompaniment>>

    @Query("SELECT * FROM accompaniment WHERE demo = :isDemo")
    fun getAllByDemo(isDemo: Boolean): Flow<List<Accompaniment>>

    @Query("DELETE FROM accompaniment")
    suspend fun clearAll()

    @Query("DELETE FROM accompaniment WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)

    @Transaction
    @Query("SELECT * FROM accompaniment WHERE id = :id")
    suspend fun getWithItems(id: Int): AccompanimentWithItems?

    @Transaction
    @RewriteQueriesToDropUnusedColumns()
//    @Query("SELECT a.* FROM accompaniment a LEFT JOIN accompaniment_item i ON i.accompaniment_id = a.id")
    @Query(" SELECT DISTINCT a.* FROM accompaniment a LEFT JOIN accompaniment_item i ON i.accompaniment_id = a.id")
    fun getAllWithItemsFlow(): Flow<List<AccompanimentWithItems>>
}