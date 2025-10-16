package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettings)

    @Query("SELECT * FROM network_settings WHERE id = :id LIMIT 1")
    fun observe(id: Int = AppSettings.SINGLETON_ID): Flow<AppSettings?>

    @Query("SELECT * FROM network_settings WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = AppSettings.SINGLETON_ID): AppSettings?

    @Query("DELETE FROM network_settings")
    suspend fun clear()
}
