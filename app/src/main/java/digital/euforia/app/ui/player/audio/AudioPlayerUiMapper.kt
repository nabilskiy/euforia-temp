package digital.euforia.app.ui.player.audio

import digital.euforia.app.data.db.entity.Resource
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.ui.util.widget.SoundEffectUi

internal fun Resource.toSoundEffectUi(): SoundEffectUi {
    return SoundEffectUi(
        id = id,
        title = name,
        imageUrl = previewUrl ?: entity?.imageUrl.orEmpty(),
        audioUrl = file?.url ?: entity?.musicUrl.orEmpty(),
        videoUrl = entity?.videoUrl,
    )
}

internal fun Resource.toAvatarUi(): AvatarUi {
    return AvatarUi(
        id = id,
        title = name,
        imageUrl = fileUrl.orEmpty(),
    )
}

internal fun AvatarUi.toSelectedVoiceAvatarStorageKey(): String = when {
    isCustom -> "custom:${storageId.orEmpty()}"
    else -> "default:$id"
}

internal fun resolveSelectedSoundEffectIndex(
    soundEffects: List<SoundEffectUi>,
    savedSoundId: Int?,
): Int {
    if (savedSoundId == null || savedSoundId == AppPreferences.SELECTED_VOICE_MUSIC_NONE) {
        return -1
    }
    return soundEffects.indexOfFirst { it.id == savedSoundId }.takeIf { it >= 0 } ?: -1
}

internal fun resolveSelectedVoiceAvatar(
    avatars: List<AvatarUi>,
    storedKey: String?,
): AvatarUi? {
    if (storedKey == null) return null
    return when {
        storedKey == AppPreferences.SELECTED_VOICE_AVATAR_NONE -> null
        storedKey.startsWith("default:") -> {
            val id = storedKey.removePrefix("default:").toIntOrNull()
            avatars.find { !it.isCustom && it.id == id }
        }
        storedKey.startsWith("custom:") -> {
            val storageId = storedKey.removePrefix("custom:")
            avatars.find { it.isCustom && it.storageId == storageId }
        }
        else -> null
    }
}