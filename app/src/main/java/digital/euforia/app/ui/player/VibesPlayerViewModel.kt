package digital.euforia.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.repository.SoundsRepository
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.vibe.PlayState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VibesPlayerViewModel @Inject constructor(
    private val soundsRepository: SoundsRepository,
    private val accompanimentRepository: AccompanimentRepository
) : ViewModel(),
    ContainerHost<VibesPlayerState, VibesPlayerSideEffect> {
    override val container = container<VibesPlayerState, VibesPlayerSideEffect>(
        initialState = VibesPlayerState(),
        onCreate = {
            viewModelScope.launch {
                val result = accompanimentRepository.getTodayAccompaniments(true)
                Timber.tag("VibesPlayerViewModel").d("Sounds: $result")
            }
            intent {
                delay(2000)
                reduceState { copy(playState = PlayState.LOADED) }
                delay(2000)
                reduceState { copy(playState = PlayState.READY) }
            }

        }
    )

    fun onPlay() {
        reduceState { copy(playState = PlayState.PLAYING) }
    }

    fun onPause() {
        reduceState { copy(playState= PlayState.PAUSED) }
    }

}


data class VibesPlayerState(val playState: PlayState = PlayState.LOADING)

sealed class VibesPlayerSideEffect {}