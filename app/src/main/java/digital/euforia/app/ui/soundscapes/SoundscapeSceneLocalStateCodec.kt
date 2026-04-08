/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import digital.euforia.app.data.db.entity.SoundscapeSceneLocalState
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.service.soundscapes.SoundscapePlaybackState
import kotlin.math.roundToInt

internal data class LocalLayer(
    val id: Int,
    val volume: Float,
    val title: String,
)

internal data class LocalButton(
    val id: Int,
    val posXFraction: Float,
    val posYFraction: Float,
    val title: String,
    val imageUrl: String?,
)

internal fun SoundscapeSceneLocalState.parseLayers(): List<LocalLayer> = layersJson
    .split("|")
    .mapNotNull { item ->
        val parts = item.split(":")
        if (parts.size < 3) return@mapNotNull null
        val id = parts[0].toIntOrNull() ?: return@mapNotNull null
        val volume = (parts[1].toIntOrNull() ?: 100).coerceIn(0, 100) / 100f
        val title = parts.subList(2, parts.size).joinToString(":").ifBlank { "Sound $id" }
        LocalLayer(id = id, volume = volume, title = title)
    }

internal fun SoundscapeSceneLocalState.parseButtons(): List<LocalButton> = buttonsJson
    .split("|")
    .mapNotNull { item ->
        val parts = item.split(":")
        if (parts.size < 5) return@mapNotNull null
        val id = parts[0].toIntOrNull() ?: return@mapNotNull null
        val x = (parts[1].toIntOrNull() ?: 50).coerceIn(0, 100) / 100f
        val y = (parts[2].toIntOrNull() ?: 50).coerceIn(0, 100) / 100f
        val title = parts[3].ifBlank { "Sound $id" }
        val image = parts.subList(4, parts.size).joinToString(":").replace("%7C", "|").ifBlank { null }
        LocalButton(id = id, posXFraction = x, posYFraction = y, title = title, imageUrl = image)
    }

internal suspend fun persistLocalSceneState(
    repository: SoundscapesRepository,
    sceneState: SoundscapeSceneState,
    playbackState: SoundscapePlaybackState,
) {
    val layersJson = playbackState.layers.joinToString(separator = "|") {
        val safeTitle = it.title.replace("|", " ").replace(":", " ")
        "${it.id}:${(it.volume.coerceIn(0f, 1f) * 100f).roundToInt()}:$safeTitle"
    }
    val buttonsJson = sceneState.soundFloatingButtons.joinToString(separator = "|") { btn ->
        val safeTitle = btn.title.replace("|", " ").replace(":", " ")
        val safeImage = btn.imageUrl.orEmpty().replace("|", "%7C")
        "${btn.id}:${(btn.posXFraction * 100f).roundToInt()}:${(btn.posYFraction * 100f).roundToInt()}:$safeTitle:$safeImage"
    }
    repository.upsertLocalSceneState(
        SoundscapeSceneLocalState(
            sceneId = sceneState.sceneId,
            musicVolume = playbackState.musicVolume.coerceIn(0f, 1f),
            selectedMusicId = sceneState.selectedMusicId,
            selectedMusicUrl = sceneState.sceneMusicUrl,
            selectedMusicTitle = sceneState.sceneMusicTitle,
            layersJson = layersJson,
            buttonsJson = buttonsJson,
        )
    )
}
