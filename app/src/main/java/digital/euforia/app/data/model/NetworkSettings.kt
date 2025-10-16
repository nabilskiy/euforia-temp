package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkSettings(
    @field:Json(name = "sound_min_repeat_delay") val soundMinRepeatDelay: Int,
    @field:Json(name = "sound_max_repeat_delay") val soundMaxRepeatDelay: Int,
    @field:Json(name = "phrases_limit") val phrasesLimit: Int,
    @field:Json(name = "accompaniments_offset_before") val accompanimentsOffsetBefore: Int,
    @field:Json(name = "accompaniments_offset_after") val accompanimentsOffsetAfter: Int,
    @field:Json(name = "registrationBonus") val registrationBonus: Int,
)

fun NetworkSettings.toEntity(): digital.euforia.app.data.db.entity.AppSettings {
    return digital.euforia.app.data.db.entity.AppSettings(
        soundMinRepeatDelay = soundMinRepeatDelay,
        soundMaxRepeatDelay = soundMaxRepeatDelay,
        phrasesLimit = phrasesLimit,
        accompanimentsOffsetBefore = accompanimentsOffsetBefore,
        accompanimentsOffsetAfter = accompanimentsOffsetAfter,
        registrationBonus = registrationBonus,
    )
}