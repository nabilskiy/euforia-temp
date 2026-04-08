/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soundscape_scene_local_state")
data class SoundscapeSceneLocalState(
    @PrimaryKey
    @ColumnInfo(name = "scene_id")
    val sceneId: Int,
    @ColumnInfo(name = "music_volume")
    val musicVolume: Float,
    @ColumnInfo(name = "selected_music_id")
    val selectedMusicId: Int? = null,
    @ColumnInfo(name = "selected_music_url")
    val selectedMusicUrl: String? = null,
    @ColumnInfo(name = "selected_music_title")
    val selectedMusicTitle: String? = null,
    /** id:volume entries separated by "|" */
    @ColumnInfo(name = "layers_json")
    val layersJson: String,
    /** id:x:y:title:imageUrl entries separated by "|" */
    @ColumnInfo(name = "buttons_json")
    val buttonsJson: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
