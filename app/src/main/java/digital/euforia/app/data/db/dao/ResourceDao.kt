package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import digital.euforia.app.data.db.entity.Resource
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Resource>)

    @Upsert
    suspend fun upsertAll(items: List<Resource>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Resource)

    @Query("SELECT * FROM resources ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Resource>>

    @Query("SELECT * FROM resources WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Resource?>

    @Query("SELECT * FROM resources WHERE class_alias = :alias ORDER BY id DESC")
    suspend fun getByClassAlias(alias: String): List<Resource>

    @Query("SELECT * FROM resources WHERE category_id = :categoryId ORDER BY id DESC")
    fun getByCategoryIdFlow(categoryId: Int): Flow<List<Resource>>

    @Query("DELETE FROM resources")
    suspend fun clearAll()

    @Query("DELETE FROM resources WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)

}