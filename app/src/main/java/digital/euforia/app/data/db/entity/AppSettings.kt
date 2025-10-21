package digital.euforia.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_settings")
data class AppSettings(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int = SINGLETON_ID,

    @ColumnInfo(name = "sound_min_repeat_delay") val soundMinRepeatDelay: Int,
    @ColumnInfo(name = "sound_max_repeat_delay") val soundMaxRepeatDelay: Int,
    @ColumnInfo(name = "phrases_limit") val phrasesLimit: Int,
    @ColumnInfo(name = "accompaniments_offset_before") val accompanimentsOffsetBefore: Int,
    @ColumnInfo(name = "accompaniments_offset_after") val accompanimentsOffsetAfter: Int,
    @ColumnInfo(name = "registration_bonus") val registrationBonus: Int,
) {
    companion object Companion {
        const val SINGLETON_ID = 1
    }
}
