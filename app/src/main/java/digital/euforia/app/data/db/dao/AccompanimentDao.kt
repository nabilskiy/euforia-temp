package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import digital.euforia.app.data.db.entity.Accompaniment
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

    @Upsert
    suspend fun upsertAll(items: List<Accompaniment>)

    @Query("DELETE FROM accompaniment WHERE id NOT IN (:ids) AND (:keepDemo = 0 OR demo = 0)")
    suspend fun deleteAllExcept(ids: List<Int>, keepDemo: Boolean)

    @Transaction
    @Query("SELECT * FROM accompaniment WHERE id = :id")
    suspend fun getWithItems(id: Int): AccompanimentWithItems?

    @Transaction
    @RewriteQueriesToDropUnusedColumns()
//    @Query("SELECT a.* FROM accompaniment a LEFT JOIN accompaniment_item i ON i.accompaniment_id = a.id")
    @Query(" SELECT DISTINCT a.* FROM accompaniment a LEFT JOIN accompaniment_item i ON i.accompaniment_id = a.id WHERE a.demo = :isDemo")
    fun getAllWithItemsFlow(isDemo: Boolean): Flow<List<AccompanimentWithItems>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns()
//    @Query("SELECT a.* FROM accompaniment a LEFT JOIN accompaniment_item i ON i.accompaniment_id = a.id")
    @Query(" SELECT DISTINCT a.* FROM accompaniment a LEFT JOIN accompaniment_item i ON i.accompaniment_id = a.id WHERE a.demo = 1")
    fun getDemoWithItemsFlow(): Flow<List<AccompanimentWithItems>>

    // Counts accompaniments where all related items are completed
    @Query("SELECT COUNT(*) FROM accompaniment a WHERE NOT EXISTS ( SELECT 1 FROM accompaniment_item i WHERE i.accompaniment_id = a.id AND i.is_completed = 0 )")
    fun getCompletedAccompanimentsCountFlow(): Flow<Int>

    // Counts accompaniments where all related items are completed
    @Query("SELECT COUNT(*) FROM accompaniment a WHERE NOT EXISTS ( SELECT 1 FROM accompaniment_item i WHERE i.accompaniment_id = a.id AND i.is_completed = 0 )")
    fun getCompletedAccompanimentsCount(): Int
}