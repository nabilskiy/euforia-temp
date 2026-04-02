/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.SceneCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SceneCategory>)

    @Query("SELECT * FROM scene_categories ORDER BY position ASC, id ASC")
    fun getAllOrderedFlow(): Flow<List<SceneCategory>>

    @Query("DELETE FROM scene_categories WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)

    @Query("DELETE FROM scene_categories")
    suspend fun clear()
}
