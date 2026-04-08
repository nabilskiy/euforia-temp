/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soundscape_sounds")
data class SoundscapeSound(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "color") val color: String?,
    @ColumnInfo(name = "continuous") val continuous: Boolean,
    @ColumnInfo(name = "min_repeat_delay") val minRepeatDelay: Int?,
    @ColumnInfo(name = "max_repeat_delay") val maxRepeatDelay: Int?,
    @ColumnInfo(name = "file_url") val fileUrl: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @Embedded(prefix = "embedded_file_") val file: File?,
)

@Entity(tableName = "soundscape_sound_categories")
data class SoundscapeSoundCategory(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "alias") val alias: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "position") val position: Int,
)
