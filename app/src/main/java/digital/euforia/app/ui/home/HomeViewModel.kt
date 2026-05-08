package digital.euforia.app.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.home.NavBarItem
import digital.euforia.app.domain.model.home.defaultNavBarItems
import digital.euforia.app.domain.usecase.accompaniment.SyncAccompanimentsUseCase
import digital.euforia.app.domain.usecase.home.GetNavBarItemsFlowUseCase
import digital.euforia.app.domain.usecase.program.SyncPackagesUseCase
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.navigation.deeplink.DeepLinkCommand
import digital.euforia.app.service.soundscapes.SoundscapePlaybackController
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNavBarItemsFlowUseCase: GetNavBarItemsFlowUseCase,
    private val syncAccompanimentsUseCase: SyncAccompanimentsUseCase,
    private val syncTopProgramsUseCase: SyncPackagesUseCase,
    private val profilePreferences: ProfilePreferences,
    private val soundscapePlaybackController: SoundscapePlaybackController,
    val analyticSender: AnalyticSender
) : ViewModel(), ContainerHost<HomeState, HomeSideEffect> {
    override val container = container<HomeState, HomeSideEffect>(
        initialState = HomeState(),
        onCreate = {
            analyticSender.mainScreenShow()
            syncAccompaniments()
            observeNavBarItems()
            observeSoundscapeMiniPlayer()
        }
    )

    private fun observeNavBarItems() {
        viewModelScope.launch {
            getNavBarItemsFlowUseCase().collectLatest {
                reduceState { copy(navBarItems = it) }
            }
        }
    }

    private suspend fun syncAccompaniments() {
        viewModelScope.async {
//            val isDemo = profilePreferences.getIsDemo()
//            syncAccompanimentsUseCase(isDemo)
            syncTopProgramsUseCase()
        }.await()
    }

    fun onNavBarItemSelected(index: Int) {
        reduceState { copy(selectedItemIndex = index) }
    }

    private fun observeSoundscapeMiniPlayer() {
        viewModelScope.launch {
            soundscapePlaybackController.playback.collectLatest { playback ->
                reduceState {
                    copy(
                        soundscapeMiniPlayer = HomeSoundscapeMiniPlayer(
                            isVisible = playback.sceneId != null,
                            sceneId = playback.sceneId,
                            title = playback.sceneTitle,
                            isPlaying = playback.isPlaying,
                            imageUrl = playback.sceneImageUrl,
                            timerRemainingSeconds = playback.timerRemainingSeconds
                        )
                    )
                }
            }
        }
    }

    fun onSoundscapeMiniPlayerOpen() {
        val sceneId = container.stateFlow.value.soundscapeMiniPlayer.sceneId ?: return
        intent { postSideEffect(HomeSideEffect.OpenSoundscapeScene(sceneId)) }
    }

    fun onSoundscapeMiniPlayerTogglePlayPause() {
        soundscapePlaybackController.playPause()
    }

    fun onSoundscapeMiniPlayerClose() {
        soundscapePlaybackController.stop()
    }
}

data class HomeState(
    val navBarItems: List<NavBarItem> = defaultNavBarItems,
    val selectedItemIndex: Int = 0,
    val soundscapeMiniPlayer: HomeSoundscapeMiniPlayer = HomeSoundscapeMiniPlayer()
)

data class HomeSoundscapeMiniPlayer(
    val isVisible: Boolean = false,
    val sceneId: Int? = null,
    val title: String = "",
    val isPlaying: Boolean = false,
    val imageUrl: String? = null,
    val timerRemainingSeconds: Int? = null,
)

sealed class HomeSideEffect {
    data class OpenSoundscapeScene(val sceneId: Int) : HomeSideEffect()
}