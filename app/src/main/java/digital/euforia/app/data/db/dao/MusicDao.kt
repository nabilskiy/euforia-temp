package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.Music
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Music>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Music)

    @Query("SELECT * FROM music ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Music>>

    @Query("SELECT * FROM music WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Music?>

    @Query("SELECT * FROM music WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Music?

    @Query("SELECT * FROM music WHERE category_id = :categoryId ORDER BY id DESC")
    fun getByCategoryIdFlow(categoryId: Int): Flow<List<Music>>

    @Query("DELETE FROM music")
    suspend fun clearAll()
}