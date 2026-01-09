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
    suspend fun upsertAll(items: List<Meditation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Meditation)

    @Query("SELECT * FROM meditations WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Meditation?>

    @Query("SELECT * FROM meditations WHERE id = :id")
    fun getById(id: Int): Meditation?

    @Query("SELECT * FROM meditations WHERE main_package_id = :packageId")
    fun getByPackageIdFlow(packageId: Int): Flow<List<Meditation>>

    @Query("DELETE FROM meditations")
    suspend fun clearAll()
}