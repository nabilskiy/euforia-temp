package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.Phrase
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Phrase>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Phrase)

    @Query("SELECT * FROM phrase WHERE id = :id")
    suspend fun getById(id: Int): Phrase?

    @Query("SELECT * FROM phrase")
    fun getAll(): Flow<List<Phrase>>

    @Query("DELETE FROM phrase")
    suspend fun clearAll()

    @Query("DELETE FROM phrase WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)
}
