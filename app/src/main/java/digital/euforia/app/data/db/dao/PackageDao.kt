package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.db.entity.PackageWithChildren
import digital.euforia.app.data.db.entity.PackageWithMeditations
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pkg: Package)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Package>)

    @Query("SELECT * FROM packages WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Package?>

    @Transaction
    @Query("SELECT * FROM packages WHERE id = :id")
    fun getWithChildrenFlow(id: Int): Flow<PackageWithChildren?>

    @Transaction
    @Query("SELECT * FROM packages WHERE id = :id")
    fun getWithChildrenById(id: Int): PackageWithChildren?

    @Transaction
    @Query("SELECT * FROM packages")
    fun getAllWithChildren(): List<PackageWithChildren>

    @Transaction
    @Query("SELECT * FROM packages WHERE id IN (:ids)")
    fun getWithMeditationsByIdFlow(ids: List<String>): Flow<List<PackageWithMeditations>>

    @Transaction
    @Query("SELECT * FROM packages WHERE id = :id")
    fun getWithMeditationsByIdFlow(id: String): Flow<PackageWithMeditations?>

    @Query("DELETE FROM packages")
    suspend fun clearAll()

    @Query("DELETE FROM packages WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)

    @Query("DELETE FROM packages WHERE id IN (:ids)")
    suspend fun deleteIn(ids: List<Int>)

    @Query("SELECT * FROM packages WHERE id IN (:ids)")
    fun getPackagesByIdsFlow(ids: List<String>): Flow<List<Package>>
}