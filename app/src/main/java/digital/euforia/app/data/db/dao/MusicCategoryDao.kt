/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.MusicCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MusicCategory>)

    @Query("SELECT * FROM music_categories ORDER BY position ASC, name COLLATE NOCASE ASC")
    fun getAllFlow(): Flow<List<MusicCategory>>

    @Query("DELETE FROM music_categories WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)

    @Query("DELETE FROM music_categories")
    suspend fun clear()
}
