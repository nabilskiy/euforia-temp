package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Exercise)

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Exercise?>

    @Query("SELECT * FROM exercises WHERE main_package_id = :packageId")
    fun getByPackageIdFlow(packageId: Int): Flow<List<Exercise>>

    @Query("DELETE FROM exercises")
    suspend fun clearAll()
}