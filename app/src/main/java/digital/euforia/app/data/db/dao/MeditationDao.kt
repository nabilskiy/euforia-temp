package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import digital.euforia.app.data.db.entity.Meditation
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Meditation>)

    @Upsert
    suspend fun upsert(item: Meditation)

    @Upsert
    suspend fun upsertAll(items: List<Meditation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Meditation)

    @Query("SELECT * FROM meditations WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Meditation?>

    @Query("SELECT * FROM meditations WHERE id = :id")
    fun getById(id: Int): Meditation?

    @Query("SELECT * FROM meditations WHERE id IN (:ids)")
    fun getByIds(ids: List<Int>): List<Meditation>

    @Query("SELECT * FROM meditations WHERE main_category_id = :categoryId")
    fun getAllByMainCategoryId(categoryId: Int?): List<Meditation>

    @Query("SELECT * FROM meditations WHERE main_package_id = :packageId")
    fun getByPackageIdFlow(packageId: Int): Flow<List<Meditation>>

    @Query("SELECT * FROM meditations WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    fun getByRelatedPackageId(packageId: Int): List<Meditation>

    @Query("SELECT * FROM meditations WHERE related_package_ids LIKE '%,' || :packageId || ',%' OR related_package_ids LIKE '[' || :packageId || ',%' OR related_package_ids LIKE '%,' || :packageId || ']' OR related_package_ids LIKE '[' || :packageId || ']'")
    fun getByRelatedPackageIdFlow(packageId: Int): Flow<List<Meditation>>

    @Query("UPDATE meditations SET is_favourite = :isFavourite WHERE id = :id")
    suspend fun updateIsFavourite(id: Int, isFavourite: Boolean)

    @Query("DELETE FROM meditations")
    suspend fun clearAll()

    @Query("DELETE FROM meditations WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<Int>)
}