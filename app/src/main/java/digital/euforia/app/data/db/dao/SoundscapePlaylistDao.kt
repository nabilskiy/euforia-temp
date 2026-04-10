/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundscapePlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SoundscapePlaylist>)

    @Query("SELECT * FROM soundscape_playlists ORDER BY id DESC")
    fun getAllFlow(): Flow<List<SoundscapePlaylist>>

    @Query("SELECT * FROM soundscape_playlists WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Int): Flow<SoundscapePlaylist?>

    @Query("DELETE FROM soundscape_playlists WHERE id NOT IN (:ids)")
    suspend fun deleteAllExcept(ids: List<Int>)
}

