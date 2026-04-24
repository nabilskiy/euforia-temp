/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */
package digital.euforia.app.domain.usecase.soundscapes

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.db.entity.SoundscapePlaylist
import digital.euforia.app.data.repository.SoundscapesRepository
import javax.inject.Inject

class GetDefaultSoundscapeScenesUseCase @Inject constructor(
    private val repository: SoundscapesRepository,
    private val remoteConfigFetcher: EuforiaRemoteConfigFetcher,
) {
    suspend operator fun invoke(
        allScenes: List<Scene>,
        playlists: List<SoundscapePlaylist>,
    ): List<Scene> {
        if (allScenes.isEmpty()) return emptyList()
        val byId = allScenes.associateBy { it.id }
        val fromRemoteIds = remoteConfigFetcher.getScenesDefaultIds()
        if (fromRemoteIds.isNotEmpty()) {
            val scenesByIds = repository.getScenesByIds(fromRemoteIds).associateBy { it.id }
            val ordered = fromRemoteIds.distinct().mapNotNull { id -> scenesByIds[id] ?: byId[id] }
            if (ordered.isNotEmpty()) return ordered
        }

        // Fallback to the first server playlist if RC is empty/broken.
        val fallbackByPlaylist = playlists.firstOrNull()
            ?.sceneIds
            ?.mapNotNull(byId::get)
            .orEmpty()
        if (fallbackByPlaylist.isNotEmpty()) return fallbackByPlaylist

        return allScenes.take(10)
    }
}

