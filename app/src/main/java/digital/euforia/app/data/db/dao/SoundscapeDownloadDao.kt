/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.SoundscapeDownloadItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundscapeDownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SoundscapeDownloadItem)

    @Query("SELECT * FROM soundscape_download_items ORDER BY updated_at DESC")
    fun getAllFlow(): Flow<List<SoundscapeDownloadItem>>

    @Query("SELECT * FROM soundscape_download_items ORDER BY updated_at DESC")
    suspend fun getAll(): List<SoundscapeDownloadItem>

    @Query("SELECT * FROM soundscape_download_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SoundscapeDownloadItem?

    @Query("DELETE FROM soundscape_download_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM soundscape_download_items")
    suspend fun deleteAll()
}

