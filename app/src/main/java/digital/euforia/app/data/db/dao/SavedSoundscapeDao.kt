/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.SavedSoundscape
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSoundscapeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SavedSoundscape)

    @Query("SELECT * FROM saved_soundscapes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SavedSoundscape?

    @Query("SELECT * FROM saved_soundscapes ORDER BY updated_at DESC")
    fun observeAllFlow(): Flow<List<SavedSoundscape>>

    @Query("DELETE FROM saved_soundscapes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
