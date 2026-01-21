package digital.euforia.app.ui.player.audio

import digital.euforia.app.data.db.entity.Resource
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