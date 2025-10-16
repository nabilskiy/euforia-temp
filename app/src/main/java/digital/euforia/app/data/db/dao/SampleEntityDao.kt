package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import digital.euforia.app.data.db.entity.SampleEntity

@Dao
interface SampleEntityDao {
    @Insert
    fun insert(sampleEntity: SampleEntity): Long
}