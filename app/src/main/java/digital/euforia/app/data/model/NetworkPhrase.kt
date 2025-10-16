package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.Phrase

@JsonClass(generateAdapter = true)
data class NetworkPhrase(
    @field:Json(name = "class") val type: String,            // "phrase"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "daytime_title") val daytimeTitle: String,
    @field:Json(name = "description") val description: String,
    @field:Json(name = "music_file_url") val musicFileUrl: String?,
    @field:Json(name = "male_audio_url") val maleAudioUrl: String?,
    @field:Json(name = "female_audio_url") val femaleAudioUrl: String?,
    @field:Json(name = "music") val music: NetworkFile?      // sometimes object instead of *_url
)

fun NetworkPhrase.toEntity(): Phrase = Phrase(
    id = id,
    type = type,
    name = name,
    daytimeTitle = daytimeTitle,
    description = description,
    musicFileUrl = musicFileUrl,
    maleAudioUrl = maleAudioUrl,
    femaleAudioUrl = femaleAudioUrl,
    music = music?.toEntity(),
)
