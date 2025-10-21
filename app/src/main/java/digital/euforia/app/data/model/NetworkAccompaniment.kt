package digital.euforia.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.db.entity.Accompaniment

// Кореневий тип відповіді: List<NetworkAccompaniment>
@JsonClass(generateAdapter = true)
data class NetworkAccompaniment(
    @field:Json(name = "class") val type: String,            // "accompaniment"
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "demo") val demo: Boolean,
    @field:Json(name = "name") val name: String,

    @field:Json(name = "morning_title") val morningTitle: String,
    @field:Json(name = "daytime_title") val daytimeTitle: String,
    @field:Json(name = "evening_title") val eveningTitle: String,
    @field:Json(name = "description") val description: String,

    @field:Json(name = "morning_image_url") val morningImageUrl: String?,
    @field:Json(name = "daytime_image_url") val daytimeImageUrl: String?,
    @field:Json(name = "evening_image_url") val eveningImageUrl: String?,

    @field:Json(name = "morning_music_url") val morningMusicUrl: String?,
    @field:Json(name = "daytime_music_url") val daytimeMusicUrl: String?,
    @field:Json(name = "evening_music_url") val eveningMusicUrl: String?,

    @field:Json(name = "morning_male_audio_url") val morningMaleAudioUrl: String?,
    @field:Json(name = "morning_female_audio_url") val morningFemaleAudioUrl: String?,
    @field:Json(name = "daytime_male_audio_url") val daytimeMaleAudioUrl: String?,
    @field:Json(name = "daytime_female_audio_url") val daytimeFemaleAudioUrl: String?,
    @field:Json(name = "evening_male_audio_url") val eveningMaleAudioUrl: String?,
    @field:Json(name = "evening_female_audio_url") val eveningFemaleAudioUrl: String?,

    @field:Json(name = "morning_content") val morningContent: String,
    @field:Json(name = "daytime_content") val daytimeContent: String,
    @field:Json(name = "evening_content") val eveningContent: String,

    @field:Json(name = "published_at") val publishedAt: Long, // epoch seconds

    // Вкладені файли (можуть бути null у відповіді)
    @field:Json(name = "morning_music") val morningMusic: NetworkFile?,
    @field:Json(name = "daytime_music") val daytimeMusic: NetworkFile?,
    @field:Json(name = "evening_music") val eveningMusic: NetworkFile?,

    @field:Json(name = "morning_male_audio") val morningMaleAudio: NetworkFile?,
    @field:Json(name = "morning_female_audio") val morningFemaleAudio: NetworkFile?,
    @field:Json(name = "daytime_male_audio") val daytimeMaleAudio: NetworkFile?,
    @field:Json(name = "daytime_female_audio") val daytimeFemaleAudio: NetworkFile?,
    @field:Json(name = "evening_male_audio") val eveningMaleAudio: NetworkFile?,
    @field:Json(name = "evening_female_audio") val eveningFemaleAudio: NetworkFile?,

    @field:Json(name = "phrases") val phrases: List<NetworkPhrase>
)


fun NetworkAccompaniment.toEntity(): Accompaniment {
    return Accompaniment(
        id = id,
        type = type,
        demo = demo,
        name = name,

        morningTitle = morningTitle,
        daytimeTitle = daytimeTitle,
        eveningTitle = eveningTitle,
        description = description,

        morningImageUrl = morningImageUrl,
        daytimeImageUrl = daytimeImageUrl,
        eveningImageUrl = eveningImageUrl,

        // Keep raw URL values exactly as received
        morningMusicUrl = morningMusicUrl,
        daytimeMusicUrl = daytimeMusicUrl,
        eveningMusicUrl = eveningMusicUrl,

        morningMaleAudioUrl = morningMaleAudioUrl,
        morningFemaleAudioUrl = morningFemaleAudioUrl,
        daytimeMaleAudioUrl = daytimeMaleAudioUrl,
        daytimeFemaleAudioUrl = daytimeFemaleAudioUrl,
        eveningMaleAudioUrl = eveningMaleAudioUrl,
        eveningFemaleAudioUrl = eveningFemaleAudioUrl,

        // Persist FileEntity by embedding details
        morningMusic = morningMusic?.toEntity(),
        daytimeMusic = daytimeMusic?.toEntity(),
        eveningMusic = eveningMusic?.toEntity(),

        morningMaleAudio = morningMaleAudio?.toEntity(),
        morningFemaleAudio = morningFemaleAudio?.toEntity(),
        daytimeMaleAudio = daytimeMaleAudio?.toEntity(),
        daytimeFemaleAudio = daytimeFemaleAudio?.toEntity(),
        eveningMaleAudio = eveningMaleAudio?.toEntity(),
        eveningFemaleAudio = eveningFemaleAudio?.toEntity(),

        morningContent = morningContent,
        daytimeContent = daytimeContent,
        eveningContent = eveningContent,

        // Map phrases from network to entity and persist via TypeConverter
        phrases = phrases.map { it.toEntity() },

        publishedAt = publishedAt,
    )
}
