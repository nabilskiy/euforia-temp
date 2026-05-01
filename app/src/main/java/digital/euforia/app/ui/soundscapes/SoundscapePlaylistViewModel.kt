/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.Scene
import digital.euforia.app.data.repository.SoundscapesRepository
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.service.soundscapes.SoundscapePlaybackController
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SoundscapePlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SoundscapesRepository,
    private val playbackController: SoundscapePlaybackController,
    private val profilePreferences: ProfilePreferences,
) : ViewModel(), ContainerHost<SoundscapePlaylistState, SoundscapePlaylistSideEffect> {

    private val playlistId: Int = savedStateHandle["playlistId"] ?: 0

    override val container = container<SoundscapePlaylistState, SoundscapePlaylistSideEffect>(
        initialState = SoundscapePlaylistState(playlistId = playlistId),
        onCreate = {
            observePremium()
            observePlayback()
            loadPlaylist()
            observePlaylist()
        }
    )

    private fun observePremium() {
        viewModelScope.launch {
            profilePreferences.getIsPremiumFlow().collectLatest { isPremium ->
                reduceState { copy(isPremium = isPremium) }
            }
        }
    }



    private fun observePlayback() {
        viewModelScope.launch {
            playbackController.playback.collectLatest { playback ->
                reduceState {
                    copy(
                        activeSceneId = playback.sceneId,
                        isPlaybackActive = playback.isPlaying
                    )
                }
            }
        }
    }

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
        intent {
            if (playbackController.playback.value.sceneId != null &&
                playbackController.playback.value.sceneId != sceneId
            ) {
                playbackController.stop(fadeOut = true)
            }
            postSideEffect(SoundscapePlaylistSideEffect.OpenScene(sceneId, playlistId))
        }
    }
}

data class SoundscapePlaylistState(
    val playlistId: Int,
    val isPremium: Boolean = false,
    val title: String = "",
    val scenes: List<Scene> = emptyList(),
    val activeSceneId: Int? = null,
    val isPlaybackActive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class SoundscapePlaylistSideEffect {
    data class OpenScene(val sceneId: Int, val playlistId: Int) : SoundscapePlaylistSideEffect()
}

