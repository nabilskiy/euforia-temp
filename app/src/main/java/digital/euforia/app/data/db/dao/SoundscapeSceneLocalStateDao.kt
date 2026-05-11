/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundscapeSceneLocalStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SoundscapeSceneLocalState)

    @Query("SELECT * FROM soundscape_scene_local_state WHERE scene_id = :sceneId LIMIT 1")
    suspend fun getBySceneId(sceneId: Int): SoundscapeSceneLocalState?

    @Query("DELETE FROM soundscape_scene_local_state WHERE scene_id = :sceneId")
    suspend fun deleteBySceneId(sceneId: Int)

    @Query("SELECT * FROM soundscape_scene_local_state")
    fun observeAll(): Flow<List<SoundscapeSceneLocalState>>

    @Query("SELECT * FROM soundscape_scene_local_state WHERE scene_id IN (:sceneIds)")
    suspend fun getBySceneIds(sceneIds: List<Int>): List<SoundscapeSceneLocalState>
}
