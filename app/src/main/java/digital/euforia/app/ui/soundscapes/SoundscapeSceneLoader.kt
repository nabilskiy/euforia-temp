/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.service.soundscapes.SoundscapeLayerState

internal data class LoadedSceneData(
    val finalLayers: List<SoundscapeLayerState>,
    val finalButtons: List<SoundFloatingButtonUi>,
    val defaultSceneSoundIds: List<Int>,
    val resolvedMusicVolume: Float,
    val musicFactor: Float,
    val selectedMusicId: Int?,
    val selectedMusicUrl: String?,
    val selectedMusicTitle: String?,
)

internal suspend fun buildLoadedSceneData(
    repository: SoundscapesRepository,
    sceneId: Int,
    remoteScene: NetworkScene?,
    fallbackMusicVolume: Float,
    createFloatingButton: (sound: AvailableSoundUi, index: Int, total: Int, instanceKey: String) -> SoundFloatingButtonUi,
): LoadedSceneData {
    val sceneLayers = remoteScene
        ?.sceneSounds
        ?.mapIndexedNotNull { index, soundItem ->
            val soundId = soundItem.sound?.id ?: return@mapIndexedNotNull null
            val soundUrl = soundItem.soundFileUrl
                ?.takeIf { it.isNotBlank() }
                ?: soundItem.sound?.fileUrl?.takeIf { it.isNotBlank() }
                ?: soundItem.sound?.file?.url?.takeIf { it.isNotBlank() }
            SoundscapeLayerState(
                id = soundId,
                instanceKey = "$soundId:$index",
                title = soundItem.sound.name.orEmpty().ifBlank { "Layer ${index + 1}" },
                audioUrl = soundUrl,
                volume = ((soundItem.volume ?: 100).coerceIn(0, 100) / 100f),
                muted = false
            )
        }
        .orEmpty()
    val floatingButtons = remoteScene
        ?.sceneSounds
        ?.mapIndexedNotNull { index, soundItem ->
            val s = soundItem.sound ?: return@mapIndexedNotNull null
            val soundId = s.id ?: return@mapIndexedNotNull null
            val posXf = soundItem.posX?.let { it.coerceIn(0, 100) / 100f }
                ?: (0.12f + (index % 4) * 0.22f).coerceIn(0.08f, 0.92f)
            val posYf = soundItem.posY?.let { it.coerceIn(0, 100) / 100f }
                ?: (0.22f + index * 0.14f).coerceIn(0.15f, 0.85f)
            SoundFloatingButtonUi(
                id = soundId,
                instanceKey = "$soundId:$index",
                title = s.name.orEmpty().ifBlank { "Sound ${index + 1}" },
                imageUrl = s.imageUrl?.takeIf { it.isNotBlank() },
                posXFraction = posXf,
                posYFraction = posYf,
            )
        }
        .orEmpty()
    val remoteButtonsByLayerKey = floatingButtons.associateBy { it.instanceKey }
    val localState = repository.getLocalSceneState(sceneId)
    val localLayers = localState?.parseLayers().orEmpty()
    val localButtons = localState?.parseButtons().orEmpty().associateBy { it.instanceKey }
    val allSoundsById = repository.getAllSounds().associateBy { it.id }
    val finalLayers = if (localLayers.isNotEmpty()) {
        localLayers.map {
            val sceneLayerUrl = sceneLayers.firstOrNull { layer -> layer.id == it.id }?.audioUrl
            val catalogLayerUrl = allSoundsById[it.id]?.fileUrl?.takeIf { url -> url.isNotBlank() }
            SoundscapeLayerState(
                id = it.id,
                instanceKey = "${it.id}:local",
                title = it.title.ifBlank { allSoundsById[it.id]?.name ?: "Sound ${it.id}" },
                audioUrl = sceneLayerUrl ?: catalogLayerUrl,
                volume = it.volume,
                muted = false
            )
        }
    } else sceneLayers

    val floatingByKey = floatingButtons.associateBy { it.instanceKey }
    val finalButtons = finalLayers.mapIndexed { index, layer ->
        val fromLocal = localButtons[layer.instanceKey]
        if (fromLocal != null) {
            val catalog = allSoundsById[layer.id]
            SoundFloatingButtonUi(
                id = layer.id,
                instanceKey = layer.instanceKey,
                title = fromLocal.title.ifBlank { layer.title.ifBlank { catalog?.name.orEmpty() } },
                imageUrl = fromLocal.imageUrl
                    ?: remoteButtonsByLayerKey[layer.instanceKey]?.imageUrl
                    ?: catalog?.imageUrl?.takeIf { it.isNotBlank() },
                posXFraction = fromLocal.posXFraction,
                posYFraction = fromLocal.posYFraction,
            )
        } else {
            val catalog = allSoundsById[layer.id]
            floatingByKey[layer.instanceKey] ?: createFloatingButton(
                AvailableSoundUi(
                    id = layer.id,
                    categoryId = catalog?.categoryId ?: 0,
                    title = layer.title.ifBlank { catalog?.name ?: "Sound ${layer.id}" },
                    imageUrl = catalog?.imageUrl?.takeIf { it.isNotBlank() },
                    fileUrl = catalog?.fileUrl?.takeIf { it.isNotBlank() },
                ),
                index,
                finalLayers.size,
                layer.instanceKey,
            )
        }
    }
    val resolvedMusicVolume = localState?.musicVolume?.coerceIn(0f, 1f) ?: fallbackMusicVolume
    val musicFactor = remoteScene?.sceneMusics?.firstOrNull()?.volume
        ?.coerceIn(0, 100)?.div(100f) ?: 1f
    return LoadedSceneData(
        finalLayers = finalLayers,
        finalButtons = finalButtons,
        defaultSceneSoundIds = remoteScene?.sceneSounds?.mapNotNull { it.sound?.id }.orEmpty(),
        resolvedMusicVolume = resolvedMusicVolume,
        musicFactor = musicFactor,
        selectedMusicId = localState?.selectedMusicId,
        selectedMusicUrl = localState?.selectedMusicUrl,
        selectedMusicTitle = localState?.selectedMusicTitle,
    )
}
