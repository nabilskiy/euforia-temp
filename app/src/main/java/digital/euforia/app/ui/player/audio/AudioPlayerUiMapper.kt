package digital.euforia.app.ui.player.audio

import digital.euforia.app.data.db.entity.Resource

internal fun Resource.toSoundEffectUi(): SoundEffectUi {
    return SoundEffectUi(
        id = id,
        title = name,
        imageUrl = previewUrl.orEmpty(),
        audioUrl = file?.url.orEmpty()
    )
}

internal fun Resource.toAvatarUi(): AvatarUi {
    return AvatarUi(
        id = id,
        title = name,
        imageUrl = fileUrl.orEmpty(),
    )
}