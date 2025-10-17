package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import digital.euforia.app.data.db.entity.PackageEntity
import digital.euforia.app.data.db.entity.PackageWithChildren
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pkg: PackageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PackageEntity>)

    @Query("SELECT * FROM packages WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<PackageEntity?>

    @Transaction
    @Query("SELECT * FROM packages WHERE id = :id")
    fun getWithChildrenFlow(id: Int): Flow<PackageWithChildren?>

    @Query("DELETE FROM packages")
    suspend fun clearAll()

    @Query("DELETE FROM packages WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)
}