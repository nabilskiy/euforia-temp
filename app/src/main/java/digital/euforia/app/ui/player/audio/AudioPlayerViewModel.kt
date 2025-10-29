package digital.euforia.app.ui.player.audio

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_AVATAR
import digital.euforia.app.data.db.entity.Resource.Companion.CLASS_ALIAS_VOICE_MUSIC
import digital.euforia.app.data.store.AppPreferences
import javax.inject.Inject
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.onboarding.Gender
import digital.euforia.app.domain.usecase.accompaniment.GetAccompanimentUseCase
import digital.euforia.app.domain.usecase.resources.GetResourcesUseCase
import digital.euforia.app.ui.util.reduceState
import digital.euforia.app.ui.util.widget.vibe.PlayState
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
    private val getResourcesUseCase: GetResourcesUseCase,
    private val appPreferences: AppPreferences
) : ViewModel(), ContainerHost<AudioPlayerState, AudioPlayerSideEffect> {

    private val accompanimentId: Int = requireNotNull(savedStateHandle.get<Int>("accompanimentId"))
    private val timeOfDay: TimeOfDay = requireNotNull(savedStateHandle.get<TimeOfDay>("timeOfDay"))
    private val entryPoint: AudioPlayerEntryPoint =
        requireNotNull(savedStateHandle.get<AudioPlayerEntryPoint>("entryPoint"))

    override val container = container<AudioPlayerState, AudioPlayerSideEffect>(
        initialState = AudioPlayerState(entryPoint = entryPoint, timeOfDay = timeOfDay),
        onCreate = {
            getAccompaniment()
        }
    )

    fun getMusicUrlForTimeOfDay(): String? {
        val state = container.stateFlow.value
        val acc = state.accompaniment ?: return null
        val gender = state.gender
        return when (timeOfDay) {
            TimeOfDay.MORNING -> {
                if (gender == Gender.MALE) acc.morningFemaleAudioUrl else acc.morningMaleAudioUrl
            }

            TimeOfDay.DAYTIME -> acc.daytimeMusicUrl
            TimeOfDay.EVENING -> {
                if (gender == Gender.MALE) acc.eveningFemaleAudioUrl else acc.eveningMaleAudioUrl
            }
        }
    }

    private fun getAccompaniment() {
        viewModelScope.launch {
            val accompaniment = getAccompanimentUseCase(accompanimentId) ?: return@launch
            val title = when (timeOfDay) {
                TimeOfDay.MORNING -> accompaniment.morningTitle
                TimeOfDay.DAYTIME -> accompaniment.daytimeTitle
                TimeOfDay.EVENING -> accompaniment.eveningTitle
            }
            val audioUrl = when (timeOfDay) {
                TimeOfDay.MORNING -> accompaniment.morningMusicUrl
                TimeOfDay.DAYTIME -> accompaniment.daytimeMusicUrl
                TimeOfDay.EVENING -> accompaniment.eveningMusicUrl
            }
            val soundEffects =
                getResourcesUseCase(CLASS_ALIAS_VOICE_MUSIC).map { it.toSoundEffectUi() }
            val avatars = getResourcesUseCase(CLASS_ALIAS_VOICE_AVATAR).map { it.toAvatarUi() }
            val avatarPreviewIds = configFetcher.getVoiceAvatarPreviewsIds()
            val gender = appPreferences.getGender()
            reduceState {
                copy(
                    accompaniment = accompaniment,
                    title = title,
                    audioUrl = audioUrl,
                    avatarsList = avatars,
                    avatarPreviewIds = avatarPreviewIds,
                    soundEffectsList = soundEffects,
                    gender = gender
                )
            }
        }
    }

    fun onPageSelected(index: Int) {
        intent {
            if (index < 0 || index >= state.pages.size) return@intent
            reduce { state.copy(currentPageIndex = index) }
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
            if (state.selectedSoundEffectIndex == index) return@intent
            val soundEffect = state.soundEffectsList.getOrNull(index) ?: return@intent
            reduce { state.copy(selectedSoundEffectIndex = index) }
        }
    }

    fun onMuteClicked() {
        intent {
            reduce { state.copy(selectedSoundEffectIndex = -1) }
        }
    }

    fun onAvatarSelected(avatar: AvatarUi?) {
        reduceState { copy(selectedAvatar = avatar) }
    }

    fun onPlayStateChanged(playState: PlayState) {
        intent {
            reduce {
                state.copy(
                    playState = playState
                )
            }
        }
    }
}

sealed class PlayerPage() {
    object Vibes : PlayerPage()
    object Avatars : PlayerPage()
}

data class AudioPlayerState(
    val playState: PlayState = PlayState.LOADING,
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
    val entryPoint: AudioPlayerEntryPoint = AudioPlayerEntryPoint.DAY,
    val timeOfDay: TimeOfDay = TimeOfDay.DAYTIME,
    val gender: Gender = Gender.UNSPECIFIED
)

@Immutable
data class AvatarUi(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val isCustom: Boolean = false
)

@Immutable
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