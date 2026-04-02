/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.SoundscapePreset
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundscapePresetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SoundscapePreset)

    @Query("SELECT * FROM soundscape_presets ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<SoundscapePreset>>

    @Query("DELETE FROM soundscape_presets WHERE id = :id")
    suspend fun deleteById(id: Int)
}

