/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.Scene
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<Scene>)

    @Query("SELECT * FROM scenes ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Scene>>

    @Query("SELECT * FROM scenes WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Int): Flow<Scene?>

    @Query("SELECT * FROM scenes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Scene?

    @Query("SELECT * FROM scenes WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<Scene>

    @Query("DELETE FROM scenes WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)
}

