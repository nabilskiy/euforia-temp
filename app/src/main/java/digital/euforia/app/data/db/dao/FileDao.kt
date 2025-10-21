package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.File
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<File>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: File)

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getById(id: Int): File?

    @Query("SELECT * FROM files")
    fun getAll(): Flow<List<File>>

    @Query("DELETE FROM files")
    suspend fun clearAll()
}
