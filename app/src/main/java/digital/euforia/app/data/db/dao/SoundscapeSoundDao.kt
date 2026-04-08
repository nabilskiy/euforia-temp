/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.SoundscapeSound
import digital.euforia.app.data.db.entity.SoundscapeSoundCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundscapeSoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSounds(items: List<SoundscapeSound>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(items: List<SoundscapeSoundCategory>)

    @Query("SELECT * FROM soundscape_sounds ORDER BY name COLLATE NOCASE ASC")
    fun getAllSoundsFlow(): Flow<List<SoundscapeSound>>

    @Query("SELECT * FROM soundscape_sounds ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllSounds(): List<SoundscapeSound>

    @Query("SELECT * FROM soundscape_sounds WHERE category_id = :categoryId ORDER BY name COLLATE NOCASE ASC")
    fun getSoundsByCategoryFlow(categoryId: Int): Flow<List<SoundscapeSound>>

    @Query("SELECT * FROM soundscape_sound_categories ORDER BY position ASC, name COLLATE NOCASE ASC")
    fun getAllCategoriesFlow(): Flow<List<SoundscapeSoundCategory>>

    @Query("DELETE FROM soundscape_sounds WHERE id NOT IN (:ids)")
    suspend fun deleteSoundsExcept(ids: List<Int>)

    @Query("DELETE FROM soundscape_sound_categories WHERE id NOT IN (:ids)")
    suspend fun deleteCategoriesExcept(ids: List<Int>)

    @Query("DELETE FROM soundscape_sounds")
    suspend fun clearSounds()

    @Query("DELETE FROM soundscape_sound_categories")
    suspend fun clearCategories()
}
