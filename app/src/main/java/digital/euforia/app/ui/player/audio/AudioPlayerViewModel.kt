package digital.euforia.app.ui.player.audio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.db.entity.Accompaniment
import javax.inject.Inject
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentUseCase
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getAccompanimentUseCase: GetAccompanimentUseCase
) : ViewModel(), ContainerHost<AudioPlayerState, AudioPlayerSideEffect> {

    private val accompanimentId: Int = requireNotNull(savedStateHandle.get<Int>("accompanimentId"))
    private val timeOfDay: TimeOfDay = requireNotNull(savedStateHandle.get<TimeOfDay>("timeOfDay"))

    override val container = container<AudioPlayerState, AudioPlayerSideEffect>(
        initialState = AudioPlayerState(),
        onCreate = {
            getAccompaniment()
        }
    )

    fun getMusicUrlForTimeOfDay(): String? {
        val acc = container.stateFlow.value.accompaniment ?: return null
        return when (timeOfDay) {
            TimeOfDay.MORNING -> acc.morningMusicUrl
            TimeOfDay.DAYTIME -> acc.daytimeMusicUrl
            TimeOfDay.EVENING -> acc.eveningMusicUrl
        }
    }

    private fun getAccompaniment() {
        viewModelScope.launch {
            val accompaniment = getAccompanimentUseCase.invoke(accompanimentId)
            accompaniment?.let {
                val title = when (timeOfDay) {
                    TimeOfDay.MORNING -> it.morningTitle
                    TimeOfDay.DAYTIME -> it.daytimeTitle
                    TimeOfDay.EVENING -> it.eveningTitle
                }
                val audioUrl = when (timeOfDay) {
                    TimeOfDay.MORNING -> it.morningMusicUrl
                    TimeOfDay.DAYTIME -> it.daytimeMusicUrl
                    TimeOfDay.EVENING -> it.eveningMusicUrl
                }

                reduceState {
                    copy(
                        accompaniment = accompaniment,
                        title = title,
                        audioUrl = audioUrl
                    )
                }
            }

        }
    }

    fun onPageSelected(index: Int) {
        intent {
            if (index < 0 || index >= state.pages.size) return@intent
            reduceState { copy(currentPageIndex = index) }
        }
    }

    fun onNavigateToAvatars() {
        reduceState { copy(currentPageIndex = 1) }
    }

    fun onNavigateToPlayer() {
        reduceState { copy(currentPageIndex = 0) }
    }
}

sealed class PlayerPage() {
    object Vibes : PlayerPage()
    object Avatars : PlayerPage()
}

data class AudioPlayerState(
    val accompaniment: Accompaniment? = null,
    val title: String? = null,
    val audioUrl: String? = null,
    val pages: List<PlayerPage> = listOf(PlayerPage.Vibes, PlayerPage.Avatars),
    val currentPageIndex: Int = 0,
)

sealed class AudioPlayerSideEffect {}