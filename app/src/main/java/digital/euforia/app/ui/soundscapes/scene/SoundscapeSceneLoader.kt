/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.scene

import digital.euforia.app.data.model.NetworkScene
import digital.euforia.app.data.db.entity.SoundscapePreset
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
    val backgroundImageUrl: String?,
    val backgroundVideoUrl: String?,
    val backgroundSource: String?,
)

internal suspend fun buildLoadedSceneData(
    repository: SoundscapesRepository,
    sceneId: Int,
    remoteScene: NetworkScene?,
    localStateSceneId: Int? = sceneId,
    preset: SoundscapePreset? = null,
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
                muted = false,
                isContinuous = soundItem.sound.continuous == true,
                minRepeatDelaySec = soundItem.sound.minRepeatDelay ?: 0,
                maxRepeatDelaySec = soundItem.sound.maxRepeatDelay ?: 300,
                repeatIntervalSec = (soundItem.interval ?: if (soundItem.sound.continuous == true) 0 else (soundItem.sound.minRepeatDelay ?: 30))
                    .coerceAtLeast(0),
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
    val localState = localStateSceneId?.let { repository.getLocalSceneState(it) }
    val localLayers = localState?.parseLayers().orEmpty()
    val localButtons = localState?.parseButtons().orEmpty()
    val localButtonsByKey = localButtons.associateBy { it.instanceKey }
    val localButtonsById = localButtons.groupBy { it.id }
    val allSoundsById = repository.getAllSounds().associateBy { it.id }
    val presetLayers = preset?.layersJson
        ?.split("|")
        ?.mapNotNull { item ->
            val parts = item.split(":")
            if (parts.size < 2) return@mapNotNull null
            val id = parts[0].toIntOrNull() ?: return@mapNotNull null
            val volume = parts[1].toFloatOrNull()?.coerceIn(0f, 1f) ?: return@mapNotNull null
            val catalog = allSoundsById[id]
            SoundscapeLayerState(
                id = id,
                instanceKey = "$id:local",
                title = catalog?.name ?: "Sound $id",
                audioUrl = catalog?.fileUrl?.takeIf { it.isNotBlank() },
                volume = volume,
                muted = false,
                isContinuous = catalog?.continuous == true,
                minRepeatDelaySec = catalog?.minRepeatDelay ?: 0,
                maxRepeatDelaySec = catalog?.maxRepeatDelay ?: 300,
                repeatIntervalSec = if (catalog?.continuous == true) 0 else (catalog?.minRepeatDelay ?: 30),
            )
        }
        .orEmpty()
    val finalLayers = when {
        localState != null -> {
        localLayers.map { localLayer ->
            val restoredButton = localButtonsById[localLayer.id]?.firstOrNull()
            val remoteLayer = sceneLayers.firstOrNull { layer -> layer.id == localLayer.id }
            val restoredInstanceKey = restoredButton?.instanceKey
                ?: remoteLayer?.instanceKey
                ?: "${localLayer.id}:local"
            val sceneLayerUrl = sceneLayers.firstOrNull { layer -> layer.id == localLayer.id }?.audioUrl
            val catalogLayerUrl = allSoundsById[localLayer.id]?.fileUrl?.takeIf { url -> url.isNotBlank() }
            SoundscapeLayerState(
                id = localLayer.id,
                instanceKey = restoredInstanceKey,
                title = localLayer.title.ifBlank { allSoundsById[localLayer.id]?.name ?: "Sound ${localLayer.id}" },
                audioUrl = sceneLayerUrl ?: catalogLayerUrl,
                volume = localLayer.volume,
                muted = false,
                isContinuous = allSoundsById[localLayer.id]?.continuous == true,
                minRepeatDelaySec = allSoundsById[localLayer.id]?.minRepeatDelay ?: 0,
                maxRepeatDelaySec = allSoundsById[localLayer.id]?.maxRepeatDelay ?: 300,
                repeatIntervalSec = if (allSoundsById[localLayer.id]?.continuous == true) 0 else (localLayer.repeatIntervalSec
                    ?: allSoundsById[localLayer.id]?.minRepeatDelay
                    ?: 30),
            )
        }
        }
        presetLayers.isNotEmpty() -> presetLayers
        else -> sceneLayers
    }

    val floatingByKey = floatingButtons.associateBy { it.instanceKey }
    val finalButtons = finalLayers.mapIndexed { index, layer ->
        val fromLocal = localButtonsByKey[layer.instanceKey]
            ?: localButtonsById[layer.id]?.firstOrNull()
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
                    isContinuous = catalog?.continuous == true,
                    minRepeatDelaySec = catalog?.minRepeatDelay ?: 0,
                    maxRepeatDelaySec = catalog?.maxRepeatDelay ?: 300,
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
        backgroundImageUrl = localState?.backgroundImageUrl,
        backgroundVideoUrl = localState?.backgroundVideoUrl,
        backgroundSource = localState?.backgroundSource,
    )
}
