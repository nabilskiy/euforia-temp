/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.repository.SoundscapesRepository
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SoundscapePlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SoundscapesRepository,
) : ViewModel(), ContainerHost<SoundscapePlaylistState, SoundscapePlaylistSideEffect> {

    private val playlistId: Int = savedStateHandle["playlistId"] ?: 0

    override val container = container<SoundscapePlaylistState, SoundscapePlaylistSideEffect>(
        initialState = SoundscapePlaylistState(playlistId = playlistId),
        onCreate = {
            loadPlaylist()
            observePlaylist()
        }
    )

    private fun loadPlaylist() {
        intent {
            reduce { state.copy(isLoading = true) }
            repository.getPlaylistDetails(playlistId)
                .onFailure { e -> reduce { state.copy(error = e.message ?: "Failed to load playlist") } }
                .onFinish { reduce { state.copy(isLoading = false) } }
        }
    }

    private fun observePlaylist() {
        intent {
            repository.getPlaylistFlow(playlistId).collectLatest { playlist ->
                if (playlist == null) return@collectLatest
                val scenesById = repository.getScenesByIds(playlist.sceneIds).associateBy { it.id }
                val orderedScenes = playlist.sceneIds.mapNotNull(scenesById::get)
                reduce {
                    state.copy(
                        title = playlist.name,
                        scenes = orderedScenes
                    )
                }
            }
        }
    }

    fun onSceneClick(sceneId: Int) {
        intent { postSideEffect(SoundscapePlaylistSideEffect.OpenScene(sceneId, playlistId)) }
    }
}

data class SoundscapePlaylistState(
    val playlistId: Int,
    val title: String = "",
    val scenes: List<Scene> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class SoundscapePlaylistSideEffect {
    data class OpenScene(val sceneId: Int, val playlistId: Int) : SoundscapePlaylistSideEffect()
}

