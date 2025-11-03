package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import digital.euforia.app.data.db.entity.AccompanimentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AccompanimentItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AccompanimentItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AccompanimentItem>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: AccompanimentItem)

    @Query("SELECT * FROM accompaniment_item WHERE id = :id")
    suspend fun getById(id: Int): AccompanimentItem?

    @Query("SELECT * FROM accompaniment_item")
    fun getAll(): Flow<List<AccompanimentItem>>

    @Query("DELETE FROM accompaniment_item")
    suspend fun clearAll()

    @Query("SELECT * FROM accompaniment_item WHERE accompaniment_id = :accompanimentId")
    fun getAllByAccompaniment(accompanimentId: Int): List<AccompanimentItem>

    @Query("SELECT * FROM accompaniment_item WHERE accompaniment_id = :accompanimentId")
    fun getAllByAccompanimentFlow(accompanimentId: Int): Flow<List<AccompanimentItem>>

    @Query("SELECT * FROM accompaniment_item WHERE accompaniment_id IN (:accompanimentIds)")
    fun getAllByAccompanimentIdsFlow(accompanimentIds: List<Int>): Flow<List<AccompanimentItem>>

    @Query("SELECT * FROM accompaniment_item WHERE accompaniment_id IN (:accompanimentIds)")
    fun getAllByAccompanimentIds(accompanimentIds: List<Int>): List<AccompanimentItem>
}
