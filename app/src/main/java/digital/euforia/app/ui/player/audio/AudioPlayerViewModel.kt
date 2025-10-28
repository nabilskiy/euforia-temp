package digital.euforia.app.ui.player.audio

import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_AVATAR
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_MUSIC
import javax.inject.Inject
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentUseCase
import digital.euforia.app.domain.usecase.resources.GetResourcesUseCase
import digital.euforia.app.ui.util.reduceState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val configFetcher: EuforiaRemoteConfigFetcher,
    private val getAccompanimentUseCase: GetAccompanimentUseCase,
    private val getResourcesUseCase: GetResourcesUseCase
) : ViewModel(), ContainerHost<AudioPlayerState, AudioPlayerSideEffect> {

    private val accompanimentId: Int = requireNotNull(savedStateHandle.get<Int>("accompanimentId"))
    private val timeOfDay: TimeOfDay = requireNotNull(savedStateHandle.get<TimeOfDay>("timeOfDay"))
    private val entryPoint: AudioPlayerEntryPoint =
        requireNotNull(savedStateHandle.get<AudioPlayerEntryPoint>("entryPoint"))

    override val container = container<AudioPlayerState, AudioPlayerSideEffect>(
        initialState = AudioPlayerState(entryPoint = entryPoint),
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

    private suspend fun getAccompaniment() {
        viewModelScope.async {
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

                val soundEffects = getResourcesUseCase.invoke(CLASS_ALIAS_VOICE_MUSIC)
                    .map { resource -> resource.toSoundEffectUi() }
                val avatars = getResourcesUseCase.invoke(CLASS_ALIAS_VOICE_AVATAR)
                    .map { resource -> resource.toAvatarUi() }

                val avatarPreviewIds = configFetcher.getVoiceAvatarPreviewsIds()

                reduceState {
                    copy(
                        accompaniment = accompaniment,
                        title = title,
                        audioUrl = audioUrl,
                        avatarsList = avatars,
                        avatarPreviewIds = avatarPreviewIds,
                        soundEffectsList = soundEffects
                    )
                }
            }

        }.await()
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

    fun onSoundEffectSelected(index: Int) {
        intent {
            val soundEffect = state.soundEffectsList.getOrNull(index) ?: return@intent
            reduce { state.copy(selectedSoundEffectIndex = index) }
        }
    }

    fun onMuteClicked() {
        intent {
            reduce { state.copy(selectedSoundEffectIndex = -1) }
        }
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
    val avatarPreviewIds: List<Int> = emptyList(),
    val avatarPreviewUrl: String? = null,
    val selectedAvatar: AvatarUi? = null,
    val avatarsList: List<AvatarUi> = emptyList(),
    val soundEffectsList: List<SoundEffectUi> = emptyList(),
    val selectedSoundEffectIndex: Int = 0,
    val entryPoint: AudioPlayerEntryPoint = AudioPlayerEntryPoint.DAY
)

data class AvatarUi(
    val id: Int,
    val title: String,
    val imageUrl: String
)

data class SoundEffectUi(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val audioUrl: String,
    val maxVolume: Float = 0.5f
)

@Keep
enum class AudioPlayerEntryPoint { ONBOARDING, DAY }

sealed class AudioPlayerSideEffect {}