/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Full snapshot for a user-saved soundscape ("Моя сцена"), keyed by the same id as [SoundscapePreset].
 * [payloadJson] mirrors editor state so catalog sync cannot overwrite saved assets.
 */
@Entity(tableName = "saved_soundscapes")
data class SavedSoundscape(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "source_catalog_scene_id") val sourceCatalogSceneId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "payload_json") val payloadJson: String,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int = 1,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
